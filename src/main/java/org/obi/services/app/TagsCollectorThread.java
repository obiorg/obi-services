package org.obi.services.app;

import java.awt.TrayIcon;
import static java.lang.Thread.sleep;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.obi.services.core.moka7.IntByRef;
import org.obi.services.core.moka7.S7CpInfo;
import org.obi.services.core.moka7.S7CpuInfo;
import org.obi.services.core.moka7.S7OrderCode;
import org.obi.services.core.moka7.S7Szl;
import org.obi.services.entities.machines.Machines;
import org.obi.services.entities.persistence.Persistence;
import org.obi.services.entities.tags.Tags;
import org.obi.services.sessions.tags.TagsFacade;
import org.obi.services.util.DateUtil;
import org.obi.services.util.Ico;
import org.obi.services.util.Settings;
import org.obi.services.util.Util;
import org.obi.services.listener.thread.SystemThreadListener;
import org.obi.services.listener.thread.FetchThreadListener;
import org.obi.services.listener.machines.MachinesListener;

/**
 * Tags Collector Thread :
 *
 * The main purpose is to get tags value over a machine. First, connection need
 * to be open with a machine, Than, tags can be collected, At the en connection
 * need to be close with a machine.
 * <p>
 * In order to start the thread you should use normaly start
 * {@link TagsCollectorThread#start()} as all thread system.
 *
 * Note : main loop thread is depend on {@link  TagsCollectorThread#requestKill}
 * which is by default not kill. This loop will run until method
 * {@link TagsCollectorThread#kill()} is called. When finaly killed the
 * following even will be emitted
 * {@link SystemThreadListener#onProcessingStopThread(java.lang.Thread)}
 *
 *
 * @author r.hendrick
 */
public class TagsCollectorThread extends Thread implements MachinesListener, FetchThreadListener, SystemThreadListener {

    // Allow to display message on processing
    private TrayIcon trayIcon;
    private final String APP_ICO = "/img/obi/obi-signet-dark.png";

    // Allow to stop process run
    private Boolean requestStop = false;
    private boolean requestKill = false;
    private boolean running = false;

    //!< Machine on which we collecting data
    private Machines machine;

    /**
     * Defined available tags that needs to be collected
     */
    List<Tags> tags = new ArrayList<>();

    /**
     * Pending tags received from child to replace existing one
     */
    List<Tags> tagsPending = new ArrayList<>();

    /**
     * Specify available persistence to be archive
     */
    List<Persistence> persistences = new ArrayList<>();

    /**
     * Array list which contain all the SystemThreadListener listeners that
     * should receive event from client class
     */
    private ArrayList<SystemThreadListener> systemThreadListeners = new ArrayList<>();

    /**
     * Allow to add listener to the list of event listener
     *
     * @param _systemThreadListeners a class which will listen to service event
     */
    public void addClientListener(SystemThreadListener _systemThreadListeners) {
        this.systemThreadListeners.add(_systemThreadListeners);
    }

    /**
     * Allow to remove listener to the list of event listener
     *
     * @param _systemThreadListeners a class which will listen to service event
     */
    public void removeClientListener(SystemThreadListener _systemThreadListeners) {
        this.systemThreadListeners.remove(_systemThreadListeners);
    }

    /**
     * Creates new form
     *
     * @param machine defined machine
     */
    public TagsCollectorThread(Machines machine) {
        super("TagsCollector_" + machine.getName());
        pushFacadeThread = new PushFacadeThread("PushFaceThread_" + machine.getName());
        trayIcon = new TrayIcon(Ico.i16(APP_ICO, this).getImage());
        this.machine = machine;

    }

    public TagsCollectorThread(Machines machine, TrayIcon trayIcon) {
        super(machine.getName());
        pushFacadeThread = new PushFacadeThread("PushFaceThread_" + machine.getName());
        this.trayIcon = trayIcon;
        this.machine = machine;
    }

    /**
     * request stop main loop
     */
    public void doStop() {
        requestStop = true;
    }

    public void doRelease() {
        requestStop = false;
    }

    public void kill() {
        requestKill = true;
    }

    /**
     * Check if processus is running mean is processing but not yet kill
     *
     * @return
     */
    public boolean isRunning() {
        return running;
    }

    PushFacadeThread pushFacadeThread; //TagsFacadeThread.getInstance();

    /**
     * Main loop of the thread data collector
     */
    @Override
    public void run() {
        String methodName = getClass().getSimpleName() + " : run() >> ";

        // Check correct startup of Collection
        if (machine == null) {
            doStop();
            Util.out(methodName + " cannot start to run without any machine !\nPlease define machine before running.");
            return;
        }

        // Start parent thread
        super.run();//
        Util.out(Util.errLine() + methodName + "Thead(" + getName() + ") of machine(" + machine.getName() + ") started with review of connection each 5s on thread");

        // Prepare working elements
        TagsFacade tagsFacade = new TagsFacade();//TagsFacade.getInstance();
        MachineConnection mc = new MachineConnection(machine);
        boolean firstTimeInProcessing = true;   //< Inidcate run loop go back at first in main processing loop
        Integer gmtIndex = Integer.valueOf(Settings.read(Settings.CONFIG, Settings.GMT).toString());
        Integer companyId = Integer.valueOf(Settings.read(Settings.CONFIG, Settings.COMPANY).toString());

        /**
         * Create Sub process to write in database
         */
        pushFacadeThread.doRelease();
        pushFacadeThread.addClientListener(this);
        if (!pushFacadeThread.isAlive()) {
            pushFacadeThread.start();
        }
        Util.out(Util.errLine() + methodName + "Thead(" + getName() + ") of machine(" + machine.getName() + ") as pushFacadeThread (" + pushFacadeThread.getName() + ")");

        /**
         * Create Sub process to write in database
         */
        FetchFacadeThread fetchFacadeThread = new FetchFacadeThread(machine); //TagsFacadeThread.getInstance();
        fetchFacadeThread.doRelease();
        fetchFacadeThread.addClientFetchListener(this);
        if (!fetchFacadeThread.isAlive()) {
            fetchFacadeThread.start();
        }
        Util.out(Util.errLine() + methodName + "Thead(" + getName() + ") of machine(" + machine.getName() + ") as fetchFacadeThread (" + fetchFacadeThread.getName() + ")");

        /**
         * START MAIN THREAD LOOP will stop when requestKill is receive by
         * {@link TagsCollectorThread#kill()} or straight requestKill = true.
         */
        while (!requestKill) {
            /**
             * processCycleStamp allow to reduce processing analysis over olding
             * time waiting
             */
            long processCycleStamp = Instant.now().toEpochMilli(); // allow firstime play

            // Inform main thread only once it reach this point after execution of subproces
            if (firstTimeInProcessing) {
                for (int i = 0; i < systemThreadListeners.size(); i++) {
                    systemThreadListeners.get(i).onProcessingThread(this);
                    systemThreadListeners.get(i).onErrorCollection(this,
                            DateUtil.localDTFFZoneId(gmtIndex)
                            + " : " + machine.getName() + " Start processing...");
                    Util.out(Util.errLine() + methodName + " Machine " + machine.getName()
                            + " Start processing...");
                }
                firstTimeInProcessing = false;
            }

            // Check available tags for processing
            Boolean wait = false;
            tags = tagsFacade._findActiveByCompanyAndMachine(companyId, machine.getId());
            if (tags.isEmpty()) {
                wait = true;
                // Inform liteners about number off collection count and error
                for (int i = 0; i < systemThreadListeners.size(); i++) {
                    systemThreadListeners.get(i).onCollectionCount(this, 0);
                    systemThreadListeners.get(i).onErrorCollection(this,
                            DateUtil.localDTFFZoneId(gmtIndex)
                            + " : No tags to read, will run in \"wait\" mode !");
                }
            }

            // SUB PROCESS LOOP
            while (!requestStop & !requestKill & !wait) {
                /**
                 * subProcessCycleStamp allow to reduce processing analysis over
                 * connection mistakes and database access
                 */
                long subProcessCycleStamp = Instant.now().toEpochMilli(); // allow firstime play

                // Inform sub thread only once it reach this after exuction of subprocess 
                if (running == false) {
                    for (int i = 0; i < systemThreadListeners.size(); i++) {
                        systemThreadListeners.get(i).onProcessingSubThread(this);
                    }
                    running = true;
                }

                // release PushFacadeThread
                pushFacadeThread.doRelease();

                // release fetchFacadeThread
                fetchFacadeThread.doRelease();

                /**
                 * Try to connect to machine Inform that machine was not
                 * connected If connected than inform machine is connected
                 */
                if (!mc.getConnected()) {
                    // Specify no conncetion exist
                    for (int i = 0; i < systemThreadListeners.size(); i++) {
                        systemThreadListeners.get(i).onSubProcessActivityState(this, false);
                        systemThreadListeners.get(i).onErrorCollection(this,
                                DateUtil.localDTFFZoneId(gmtIndex)
                                + " : Not connected, start connection...");
                    }
                    // Try to connect
                    Long t_doConnectEpoch = Instant.now().toEpochMilli();
                    if (mc.doConnect()) {
                        for (int i = 0; i < systemThreadListeners.size(); i++) {
                            systemThreadListeners.get(i).onSubProcessActivityState(this, true);
                            systemThreadListeners.get(i).onErrorCollection(this,
                                    DateUtil.localDTFFZoneId(gmtIndex)
                                    + " : Connected !");
                            systemThreadListeners.get(i).onDuration(this, 1, Instant.now().toEpochMilli() - t_doConnectEpoch);
                        }
                    } else {// Inform why not able to connect
                        for (int i = 0; i < systemThreadListeners.size(); i++) {
                            systemThreadListeners.get(i).onSubProcessActivityState(this, false);
                            systemThreadListeners.get(i).onErrorCollection(this,
                                    DateUtil.localDTFFZoneId(gmtIndex)
                                    + " : Not connected !" + mc.getErrorText());
                            systemThreadListeners.get(i).onDuration(this, 1, Instant.now().toEpochMilli() - t_doConnectEpoch);
                        }
                    }
                }

                //2- create S7 connection to PLC
                Long processTagsCycle = 0l; // 
                while (mc.getConnected() & !requestStop & !requestKill) {
                    /**
                     * subProcessCycleStamp allow to reduce processing analysis
                     * over reading machine access this time in while connected
                     */
                    subProcessCycleStamp = Instant.now().toEpochMilli(); // allow firstime play

                    /**
                     * Cyclical update tags list
                     */
//                    if (subProcessCycleStamp - processTagsCycle >= 60000) {
//                        Long t_doConnectEpoch = Instant.now().toEpochMilli();
//                        tags = tagsFacade._findActiveByCompanyAndMachine(companyId, machine.getId());
//                        for (int i = 0; i < systemThreadListeners.size(); i++) {
//                            systemThreadListeners.get(i).onDuration(this, 2, Instant.now().toEpochMilli() - t_doConnectEpoch);
//                        }
//                        processTagsCycle = Instant.now().toEpochMilli();
//                    }
                    if (!tagsPending.isEmpty()) {
                        tags.clear();
                        tags.addAll(tagsPending);
                        tagsPending.clear();
                    }

                    // Inform liteners about number off collection count
                    for (int i = 0; i < systemThreadListeners.size(); i++) {
                        systemThreadListeners.get(i).onCollectionCount(this, Integer.valueOf(tags.size()));
                    }

                    /**
                     * Only process if there is some tags
                     */
                    if (!tags.isEmpty()) {

                        // Now process each tags
                        int badReadingRequestCounter = 0;
                        for (Tags tag : tags) {

                            // Collect only if cyle time is reached since last change
                            long cycleTime = tag.getCycle() * 1000; // msec
                            long savedEpoch = DateUtil.epochMilliOf(gmtIndex);
                            if (tag.getVStamp() != null) {
                                savedEpoch = DateUtil.epochMilliOf(gmtIndex, tag.getVStamp());//tag.getVStamp().toInstant().toEpochMilli();
                            } else {
                                savedEpoch = DateUtil.epochMilliOf(gmtIndex) - cycleTime - 1;
                            }
                            Long nowEpoch = DateUtil.epochMilliOf(gmtIndex);
                            Long deltaEpoch = (nowEpoch - savedEpoch);

                            // Collect only if elapse time as finished
                            if (deltaEpoch > cycleTime) {
                                // Init. default value
                                tag.setVFloat(0.0);
                                tag.setVInt(0);
                                tag.setVBool(false);
                                tag.setVStr("");
                                tag.setVDateTime(LocalDateTime.now(ZoneId.of(DateUtil.zoneIdOf(gmtIndex))));
                                tag.setVStamp(LocalDateTime.now(ZoneId.of(DateUtil.zoneIdOf(gmtIndex))));
                                tag.setError(false);
                                tag.setErrorMsg("");

                                if (tag.getType() != null) {
                                    Long t_doConnectEpoch = Instant.now().toEpochMilli();
                                    //
                                    // READING ON CPU
                                    //
                                    Object t = mc.readValue(tag);
                                    //
                                    //
                                    //

                                    for (int i = 0; i < systemThreadListeners.size(); i++) {
                                        systemThreadListeners.get(i).onDuration(this, 3, Instant.now().toEpochMilli() - t_doConnectEpoch);
                                    }

                                    if (t != null
                                            && tag.getVFloat() != null && tag.getVInt() != null
                                            && !Double.isNaN(tag.getVFloat())) {
                                        pushFacadeThread.addNewTag(tag); // in order to post-pose processing.
                                    } else {
                                        mc.close();
                                        for (int j = 0; j < systemThreadListeners.size(); j++) {
                                            systemThreadListeners.get(j).onErrorCollection(this, Util.errLine() + methodName + " read return null " + tag.getType() + " for tag " + tag + " close connection");
                                        }
                                        if (mc.getConnected()) {
                                            Util.out(Util.errLine() + methodName + " after closing connection is still on for machine " + getName() + " !");
                                        } else {
                                            Util.out(Util.errLine() + methodName + " after closing connection is correctly close for machine " + getName() + " !");
                                        }
                                        break;
                                    }
                                } else {
                                    // Inform liteners about number off collection count and error
                                    for (int j = 0; j < systemThreadListeners.size(); j++) {
                                        systemThreadListeners.get(j).onErrorCollection(this, Util.errLine() + methodName + " Unable to find type " + tag.getType() + " for tag " + tag);
                                    }
                                }
                            }
                        }
                    } else {
                        Util.out(Util.errLine() + methodName + " empty list tag found for machine = " + machine);
                        requestStop = true;

                    }

                    /**
                     * Manage reading time of data tags in the machine not less
                     * than 1s
                     */
                    long defaultDelay = 1000;
                    long delay = Instant.now().toEpochMilli() - subProcessCycleStamp;
                    // Inform on execution delay
                    for (int i = 0; i < systemThreadListeners.size(); i++) {
                        systemThreadListeners.get(i).onProcessingSubCycleTime(this, delay);
                    }
                    // Sleep remaining time
                    if (delay < defaultDelay) {
                        long d = defaultDelay - delay;
                        try {
                            sleep(d);
                        } catch (InterruptedException ex) {
                            Util.out(Util.errLine() + methodName + " >> \"Sub Processing Loop\" unable to proced minimum delay !\n" + ex.getLocalizedMessage());
                            Logger.getLogger(ManagerControllerThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }

                /**
                 * If Machine is still connected we close it by the mean of
                 * requestStop or kill! If request stop it will only appear once
                 */
                if (mc.getConnected() & requestKill) {
                    mc.close();
                    // Inform connection release
                    for (int i = 0; i < systemThreadListeners.size(); i++) {
                        systemThreadListeners.get(i).onSubProcessActivityState(this, false);
                        systemThreadListeners.get(i).onProcessingSubStopThread(this);
                    }
                }

                // sub process stop running
                running = false; //!< indicate end of processus running

                // stop PushFacadeThread
                pushFacadeThread.doStop();

                // stop fetchFacadeThread
                fetchFacadeThread.doStop();

                /**
                 * Manage initiation of new request reading Default preset time
                 * is 5s
                 */
                long processDelay = 5000;
                long delay = Instant.now().toEpochMilli() - processCycleStamp;
                // Inform on execution delay
                for (int i = 0; i < systemThreadListeners.size(); i++) {
                    systemThreadListeners.get(i).onProcessingCycleTime(this, delay);
                }
                // Sleep remaining delay
                if (delay < processDelay) {
                    long d = processDelay - delay;
                    try {
                        sleep(d);
                    } catch (InterruptedException ex) {
                        Util.out(Util.errLine() + methodName + " >> \"Processing Loop\" unable to proced minimum delay !\n" + ex.getLocalizedMessage());
                        Logger.getLogger(ManagerControllerThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            /**
             * Manage initiation of new request reading Default preset time is
             * 5s
             */
            long processDelay = 5000;
            long delay = Instant.now().toEpochMilli() - processCycleStamp;
            // Inform on execution delay
            for (int i = 0; i < systemThreadListeners.size(); i++) {
                systemThreadListeners.get(i).onProcessingCycleTime(this, delay);
            }
            // Sleep remaining delay
            if (delay < processDelay) {
                long d = processDelay - delay;
                try {
                    sleep(d);
                } catch (InterruptedException ex) {
                    Util.out(Util.errLine() + methodName + " >> \"Processing Loop\" unable to proced minimum delay !\n" + ex.getLocalizedMessage());
                    Logger.getLogger(ManagerControllerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        // Release PushFacadeThread
        while (pushFacadeThread.isAlive()) {
            pushFacadeThread.doStop();
            pushFacadeThread.kill();
            Util.out(Util.errLine() + methodName + " >> on Machine " + machine.getName() + " : ... wait pushFacadeThread to finish !");
            try {
                sleep(500);
            } catch (InterruptedException ex) {
                Util.out(Util.errLine() + methodName + " >> on Machine " + machine.getName() + " : Error will sleeping in order to kill thread pushFacadeThread !");
            }
        }
        Util.out(Util.errLine() + methodName + " >> on Machine " + machine.getName() + " : pushFacadeThread killed !");

        // Release fetchFacadeThread
        while (fetchFacadeThread.isAlive()) {
            fetchFacadeThread.doStop();
            fetchFacadeThread.kill();
            Util.out(Util.errLine() + methodName + " >> on Machine " + machine.getName() + " : ... wait fetchFacadeThread to finish !");
            try {
                sleep(500);
            } catch (InterruptedException ex) {
                Util.out(Util.errLine() + methodName + " >> on Machine " + machine.getName() + " : Error will sleeping in order to kill thread fetchFacadeThread !");
            }
        }
        Util.out(Util.errLine() + methodName + " >> on Machine " + machine.getName() + " : pushFacadeThread killed !");

        /**
         * Will kill tags collector controller : inform all client
         */
        for (int i = 0; i < systemThreadListeners.size(); i++) {
            systemThreadListeners.get(i).onProcessingStopThread(this);
        }
        Util.out(Util.errLine() + methodName + " on Machine " + machine.getName() + " : Terminate tag collector Controller Thread");
    }

    public Machines getMachine() {
        return machine;
    }

    public void setMachine(Machines machine) {
        this.machine = machine;
    }

    @Override
    public void onNewError(int errorCode, String err) {
        for (int i = 0; i < systemThreadListeners.size(); i++) {
            systemThreadListeners.get(i).onErrorCollection(this,
                    "[" + errorCode + "] - " + err);
        }
    }

    @Override
    public void onConnectionSucced(Machines machine, Integer errorCode, String err) {
        for (int i = 0; i < systemThreadListeners.size(); i++) {
            systemThreadListeners.get(i).onErrorCollection(this,
                    "[" + errorCode + "] - " + err + " for machine " + machine.getName() + " " + machine.getAddress());
        }
    }

    @Override
    public void onConnectionError(Machines machine, Integer errorCode, String err) {
        for (int i = 0; i < systemThreadListeners.size(); i++) {
            systemThreadListeners.get(i).onErrorCollection(this,
                    "[" + errorCode + "] - " + err + " for machine " + machine.getName() + " " + machine.getAddress());
        }
    }

    @Override
    public void onPDUUpdate(Integer PDUNegotiationByte) {
    }

    @Override
    public void onDateTimeResponse(Date plcDateTime) {
    }

    @Override
    public void isProcessing() {
    }

    @Override
    public void onOrderCodeResponse(S7OrderCode orderCode) {
    }

    @Override
    public void onPLCStatusResponse(IntByRef status) {
    }

    @Override
    public void onPLCInfoResponse(S7CpuInfo CpuInfo) {
    }

    @Override
    public void onCpInfoResponse(S7CpInfo CpInfo) {
    }

    @Override
    public void onReadSzlResponse(S7Szl SZL) {
    }

    @Override
    public void onNewTags(List<Tags> tagsActive) {
        /**
         * Cyclical update tags list
         */
        Long t_before = Instant.now().toEpochMilli();
        tagsPending.clear();
        tagsPending.addAll(tagsActive);
        for (int i = 0; i < systemThreadListeners.size(); i++) {
            systemThreadListeners.get(i).onDuration(this, 2, Instant.now().toEpochMilli() - t_before);
        }
    }

    @Override
    public void onNewPersistences(List<Persistence> newPersistences) {
        /**
         * Cyclical update tags list
         */
        Long t_doConnectEpoch = Instant.now().toEpochMilli();

        // Prepare persistence
        persistences.clear();
        persistences.addAll(newPersistences);

        // Check if persistence require a renew sequence
        pushFacadeThread.addNewPersistence(persistences);
//        Util.out(Util.errLine() + getClass().getName() + " T(" + getName() + ") >> onNewPersistence size = " + newPersistences.size());

        // Cycle manage
        for (int i = 0; i < systemThreadListeners.size(); i++) {
            systemThreadListeners.get(i).onDuration(this, 3, Instant.now().toEpochMilli() - t_doConnectEpoch);
        }
    }

    @Override
    public void onProcessingThread(Thread thread) {

    }

    @Override
    public void onProcessingSubThread(Thread thread) {

    }

    @Override
    public void onProcessingSubStopThread(Thread thread) {

    }

    @Override
    public void onProcessingStopThread(Thread thread) {

    }

    @Override
    public void onProcessingSubCycleTime(Thread thread, Long ms) {

    }

    @Override
    public void onProcessingCycleTime(Thread thread, Long ms) {

    }

    @Override
    public void onErrorCollection(Thread thread, String message) {

    }

    @Override
    public void onSubProcessActivityState(Thread thread, Boolean activity) {

    }

    @Override
    public void onCollectionCount(Thread thread, int count) {

    }

    @Override
    public void onDuration(Thread thread, int i, long toEpochMilli) {

    }

}

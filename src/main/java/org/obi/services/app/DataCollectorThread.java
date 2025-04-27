package org.obi.services.app;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.obi.services.entities.machines.Machines;
import org.obi.services.entities.tags.Tags;
import org.obi.services.listener.thread.SystemThreadListener;
import org.obi.services.util.DateUtil;
import org.obi.services.util.Settings;
import org.obi.services.util.Util;

/**
 * DataCollectorThread :
 *
 * The main purpose is to stored instant data in rxTags to keep it in memory
 * while processing writing in to file can be long. While writing is finish
 * processing writing in to database can operate.
 *
 *
 *
 * @author r.hendrick
 */
public class DataCollectorThread extends Thread {
// Allow to stop process run

    private Boolean requestStop = false;
    private boolean requestKill = false;
    private boolean running = false;

    private boolean NO_TAG_TO_UPDATE = false;
    private boolean NO_TAG_TO_PERSIST = false;

    private Machines machine;

    /**
     * Received Tags
     */
    private List<Tags> rxTags = new ArrayList<>();

    /**
     * Transmit Tags
     */
    private List<Tags> txTags = new ArrayList<>();
    private boolean NO_ERROR = true;

    /**
     * Add received tags to the list of received tags rxTags
     *
     * @param receivedTags newly received tags
     */
    public void addTags(List<Tags> receivedTags) {
        rxTags.addAll(receivedTags);
    }

    /**
     * Add received tag to the list of received tags rxTags
     *
     * @param receivedTag one tag newly received
     */
    public void addTag(Tags receivedTag) {
        rxTags.add(receivedTag);
    }

    /**
     * Array list which contain all the TagsFacadeThreadListener listeners that
     * should receive event from client class
     */
    private ArrayList<SystemThreadListener> systemThreadListeners = new ArrayList<>();

    /**
     * Allow to add listener to the list of event listener
     *
     * @param _tagsCollectorThreadListeners a class which will listen to service
     * event
     */
    public void addClientListener(SystemThreadListener systemThreadListener) {
        this.systemThreadListeners.add(systemThreadListener);
    }

    /**
     * Allow to remove listener to the list of event listener
     *
     * @param _tagsCollectorThreadListeners a class which will listen to service
     * event
     */
    public void removeClientListener(SystemThreadListener systemThreadListener) {
        this.systemThreadListeners.remove(systemThreadListener);
    }

    /**
     * Creates new form
     */
    public DataCollectorThread(String name) {
        super(name);
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

    /**
     * Main loop of the thread data collector
     */
    @Override
    public void run() {
        String methodName = getClass().getSimpleName() + " : run() >> ";

        // Start parent thread
        super.run();
        Util.out(Util.errLine() + methodName + "Thead DataCollector : started with review of connection each 1s");

        boolean firstTimeInProcessing = true;   //< Inidcate run loop go back at first in main processing loop
        Integer gmtIndex = Integer.valueOf(Settings.read(Settings.CONFIG, Settings.GMT).toString());
        Integer companyId = Integer.valueOf(Settings.read(Settings.CONFIG, Settings.COMPANY).toString());

        /**
         * START MAIN THREAD LOOP will stop when requestKill is receive by
         * {@link TagsCollectorThread#kill()} or straight requestKill = true.
         */
        LocalDateTime dtMachineChanged = LocalDateTime.now(ZoneId.of(DateUtil.zoneIdOf(gmtIndex)));
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
                            + " : Start processing TagsFacadeThread...");
                    Util.out(Util.errLine() + methodName + DateUtil.localDTFFZoneId(gmtIndex)
                            + " : Start processing TagsFacadeThread...");
                }
                firstTimeInProcessing = false;
            }

            // Check available tags pending for updating
            Boolean wait = false;

            // SUB PROCESS LOOP
            while (!requestStop & !requestKill & !wait & !rxTags.isEmpty()) {
                // Inform sub thread only once it reach this after exuction of subprocess 
                if (running == false) {
                    for (int i = 0; i < systemThreadListeners.size(); i++) {
                        systemThreadListeners.get(i).onProcessingSubThread(this);
                    }
                    running = true;
                }

                // Empty put rxTags in to rxTags and clear rxTags
                txTags.addAll(rxTags);
                rxTags.clear();

                try {
                    // write TxTags to file
                    writeAsPers_Standard();
                    NO_ERROR = true;
                } catch (IOException ex) {
                    if (NO_ERROR == true) {
                        Logger.getLogger(DataCollectorThread.class.getName()).log(Level.SEVERE, null, ex);
                        NO_ERROR = false;
                    }
                }
            }

            // sub process stop running
            running = false; //!< indicate end of processus running

            /**
             * Manage initiation of new request reading Default preset time is
             * 1s
             */
            long processDelay = 1000;
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
         * Will kill tags collector controller : inform all client
         */
        for (int i = 0; i < systemThreadListeners.size(); i++) {
            systemThreadListeners.get(i).onProcessingStopThread(this);
        }

        Util.out(Util.errLine() + methodName + " Terminate tag collector Controller Thread");
    }

    public void writeAsPers_Standard() throws IOException {
        FileWriter fw = new FileWriter("./DataCollector/ps_" + machine.getName() + ".csv", true);
        PrintWriter pw = new PrintWriter(fw);

        // Process each txTags
        txTags.forEach(tag -> {
//            Util.out(Util.errLine() + " writeAsPers >> " + tag.toStringFull()  );
            pw.printf(" %d, %d, %.6f, %d, %s, %s, %s, %s, %s, %s, %f, %f, %s, %s ;",
                    tag.getCompany().getId(), // companyId
                    tag.getId(), // id tag
                    tag.getVFloat(), // vFloat
                    tag.getVInt(), // vInt
                    tag.getVBool().toString(), // vBool
                    tag.getVStr(), // vStr
                    tag.getVDateTime().toString(), // vDateTime
                    tag.getVStamp().toString(), // vStamp
                    tag.getVStamp().toString(), // vStampStart
                    tag.getVStamp().toString(), // vStampEnd
                    0.0, // tbf
                    0.0, // ttr
                    (tag.getError() == null ? "false" : tag.getError().toString()), // error
                    (tag.getErrorMsg() == null ? "NULL" : (tag.getErrorMsg().isEmpty() ? "NULL" : tag.getErrorMsg())) // error Message
            );
        });

        // Fermeture du fichier
        pw.close();

        // Clear TxTags
        txTags.clear();

    }

    public Machines getMachine() {
        return machine;
    }

    public void setMachine(Machines machine) {
        this.machine = machine;
    }

}

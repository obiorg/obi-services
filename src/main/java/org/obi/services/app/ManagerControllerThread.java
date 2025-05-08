/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.obi.services.app;

import java.awt.TrayIcon;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.obi.services.Docking.ManagerControllerFrame;
import org.obi.services.entities.machines.Machines;
import org.obi.services.sessions.machines.MachinesFacade;
import org.obi.services.util.Ico;
import org.obi.services.util.Settings;
import org.obi.services.util.Util;
import org.obi.services.listener.thread.SystemThreadListener;
import org.obi.services.listener.machines.MachinesEvent;

/**
 * Manage list of connection over an existing list ????? and the database actual
 * defined machine. Then on the existing list it start by opening connection
 * than let the tag collector to process collection of datas. Finaly one stop or
 * finished request collection or connection dropped close communication to
 * machine
 *
 * @author r.hendrick
 */
public class ManagerControllerThread extends Thread implements SystemThreadListener {

    // Allow to display message on processing
    private TrayIcon trayIcon;
    private final String APP_ICO = "/img/obi/obi-signet-dark.png";

    // Allow to stop process run
    private Boolean requestStop = false;
    private boolean requestKill = false;
    private boolean running = false;

    // Process Saving
    KafkaToSQLServerThread kafkaToSQLServerThread;

    //!< List of machines already managed by Manager controller
    List<TagsCollectorThread> tagsCollectorManaged = new ArrayList<>();

    /**
     * Array list which contain all the SystemThreadListener listeners that
     * should receive event from client class
     */
    private ArrayList<SystemThreadListener> tagsCollectorThreadListeners = new ArrayList<>();

    /**
     * Allow to add listener to the list of event listener
     *
     * @param _tagsCollectorThreadListeners a class which will listen to service
     * event
     */
    public void addClientListener(SystemThreadListener _tagsCollectorThreadListeners) {
        this.tagsCollectorThreadListeners.add(_tagsCollectorThreadListeners);
    }

    /**
     * Allow to remove listener to the list of event listener
     *
     * @param _tagsCollectorThreadListeners a class which will listen to service
     * event
     */
    public void removeClientListener(SystemThreadListener _tagsCollectorThreadListeners) {
        this.tagsCollectorThreadListeners.remove(_tagsCollectorThreadListeners);
    }

    /**
     * Array List which contain all the Machines controller event that should
     * receive even from the server
     */
    private ArrayList<MachinesEvent> machinesControllerEvents = new ArrayList<>();

    /**
     * Allow to add event to the list of events
     *
     * @param _machinesControllerEvents a class which will listen to service
     * event
     */
    public void addMachinesEvent(MachinesEvent _machinesControllerEvents) {
        this.machinesControllerEvents.add(_machinesControllerEvents);
    }

    /**
     * Allow to remove listener to the list of event listener
     *
     * @param _machinesControllerEvents a class which will listen to service
     * event
     */
    public void removeMachinesEvent(MachinesEvent _machinesControllerEvents) {
        this.machinesControllerEvents.remove(_machinesControllerEvents);
    }

    /**
     * Creates new form
     *
     *
     *
     * @param threadName
     */
    public ManagerControllerThread(String threadName) {
        super(threadName);
        trayIcon = new TrayIcon(Ico.i16(APP_ICO, this).getImage());

    }

    public ManagerControllerThread(String threadName, TrayIcon trayIcon) {
        super(threadName);
        this.trayIcon = trayIcon;
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
     * Delay Method
     *
     * Delay allow to compute the delay between an old epoch compare to actual
     * epoch (now - oldEpoch) = delay
     *
     * @param oldEpoch an epoch value of previous time
     *
     * @return delay in ms from oldEpoch to now
     */
    public static Long delay(Long oldEpoch) {
        return (Instant.now().toEpochMilli() - oldEpoch);
    }

    /**
     * Main loop of the thread data collector
     */
    @Override
    public void run() {
        String methodName = getClass().getSimpleName() + " : run() >> ";

        // Récupération des facades de communication bdd
        MachinesFacade machinesFacade = MachinesFacade.getInstance();

        // Start parent thread
//        super.start();
        Util.out(Util.errLine() + methodName + " state of machine connection are review each 5s");

        // Thread tools
        boolean firstTimeInProcessing = true;   //< Inidcate run loop go back at first in main processing loop

        String kafka_name = "obi";
        String bootstrapServers = "localhost:7092"; // Adresse du broker Kafka
        String kafka_groupId = "grpPersStandard";
        String topic = "PersStandard";
        /**
         * Create Sub process to kafka Producer
         */
        kafkaToSQLServerThread = new KafkaToSQLServerThread(kafka_name,
                bootstrapServers, kafka_groupId, topic);
        kafkaToSQLServerThread.doRelease();
        kafkaToSQLServerThread.addClientListener(this);
        if (!kafkaToSQLServerThread.isAlive()) {
            //if (persMode == 1) {
                kafkaToSQLServerThread.start();
            //}
        }
        Util.out(Util.errLine() + methodName + "Thead(" + getName() + ") of  as kafkaToSQLServerThread (" + kafkaToSQLServerThread.getName() + ")");

        /**
         * Start running sub process
         */
        while (!requestKill) {
            /**
             * processCycleStamp allow to reduce processing analysis over olding
             * time waiting
             */
            long processCycleStamp = Instant.now().toEpochMilli(); // allow firstime play

            // Inform main thread only once it reach this point after execution of subproces
            if (firstTimeInProcessing) {
                for (SystemThreadListener tagCollectorThreadListener : tagsCollectorThreadListeners) {
                    tagCollectorThreadListener.onProcessingThread(this);
                }
                firstTimeInProcessing = false;
            }

            // SUB PROCESS LOOP
            while (!requestStop) {
                /**
                 * subProcessCycleStamp allow to reduce processing analysis over
                 * machines in database.
                 */
                long subProcessCycleStamp = Instant.now().toEpochMilli(); // allow firstime play

                // Inform sub thread only once it reach this after exuction of subprocess 
                if (running == false) {
                    for (SystemThreadListener tagCollectorThreadListener : tagsCollectorThreadListeners) {
                        tagCollectorThreadListener.onProcessingSubThread(this);
                    }
                    running = true;
                }

                // Refresh list of available machine in the company 
                String societeStr = Settings.read(Settings.CONFIG, Settings.COMPANY).toString();
                Integer societe = Integer.valueOf(societeStr);
                List<Machines> machines = machinesFacade.findByCompanyId(societe);
                for (MachinesEvent machinesControllerEvent : machinesControllerEvents) {
                    machinesControllerEvent.countEvent(machines.size());
                }

                kafkaToSQLServerThread.doRelease();

                // Actualize machine managed list
                // 1. Remove deleted connection from managed list
                // 2. Check for une connection to be initiated
                if (!machines.isEmpty() || !tagsCollectorManaged.isEmpty()) {

                    // 1. REMOVE DELETED CONNECTION IN DATABASE FROM MANAGED LIST
                    // 1.1. Recover machine to remove
                    for (TagsCollectorThread tagsCollector : tagsCollectorManaged) {
                        // > Check in database if still existing : if true do not remove otherwise remove
                        if (!machines.contains(tagsCollector.getMachine())) {
                            // Indicate to all events machines have been remove
                            machinesControllerEvents.stream().forEach((machinesControllerEvent) -> {
                                machinesControllerEvent.removeEvent(tagsCollector.getMachine());
                                Util.out(Util.errLine() + ManagerControllerThread.class.getSimpleName()
                                        + " : run >> machine to be kill " + tagsCollector.getMachine());
                            });
                            // 1.2. Request to kill this thread collector 
                            // @see void onProcessingStopThread(Machines m)
                            tagsCollector.kill();

                            // Wait system to be finished
                            long ltime = Instant.now().toEpochMilli(); // manage message
                            while (tagsCollector.isAlive()) {
                                if (ltime >= 5000) {
                                    Util.out(Util.errLine() + ManagerControllerThread.class.getSimpleName()
                                            + " : run >> wait machine to be killed " + tagsCollector.getMachine());
                                    ltime = Instant.now().toEpochMilli(); // update time
                                }
                            }
                        }
                    }

                    // 2 CHECK FOR NEW CONNECTION TO BE INITIATED
                    // 2.1. Recreate list of existing machine
                    List<Machines> machinesManaged = new ArrayList<>();
                    for (TagsCollectorThread tagsCollector : tagsCollectorManaged) {
                        machinesManaged.add(tagsCollector.getMachine());
                    }

                    // 2.2. Create new machine
                    for (Machines machine : machines) {
                        // Create not existing connection and start them
                        if (!machinesManaged.contains(machine)) {
                            // Create a new machine connection and start execution
                            TagsCollectorThread t = new TagsCollectorThread(machine);
                            t.doRelease();
                            if (!t.isAlive()) {
                                t.start();
                                Util.out(Util.errLine() + getClass().getSimpleName()
                                        + " : run >> Start thread " + machine.getName());
                            }
                            t.addClientListener(this);
                            // add the new collection to the tags collector manager
                            tagsCollectorManaged.add(t);

                            // Update list of machine to lister
                            for (MachinesEvent machinesControllerEvent : machinesControllerEvents) {
                                machinesControllerEvent.addEvent(machine);
                            }
                            for (SystemThreadListener tagsCollectorThreadListener : tagsCollectorThreadListeners) {
                                if (tagsCollectorThreadListener instanceof ManagerControllerFrame) {
                                    t.addClientListener(tagsCollectorThreadListener);
                                }
                            }
                        }
                    }
                }

                /**
                 * Manage testing time of new machine insert or remove. Default
                 * preset time is 5s
                 */
                long defaultDelay = 5000;
                long delay = delay(subProcessCycleStamp);
                // Inform on execution delay
                tagsCollectorThreadListeners.stream().forEach((tagsCollectorThreadListener) -> {
                    tagsCollectorThreadListener.onProcessingSubCycleTime(this, delay);
                });
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

            if (running) {
                // Inform register listeners that sub process is terminate in should indicate olding
                tagsCollectorThreadListeners.stream().forEach((tagsCollectorThreadListener) -> {
                    Util.out(Util.errLine() + methodName + " : run >> Manager Controller will go on old position !");

                    tagsCollectorManaged.stream().forEach((tagsCollectorThread) -> {
                        // Stop and kill the thread
                        tagsCollectorThread.doStop();
                        tagsCollectorThread.kill();
                        // Inform to all events machines have been remove
                        machinesControllerEvents.stream().forEach((machinesControllerEvent) -> {
                            machinesControllerEvent.removeEvent(tagsCollectorThread.getMachine());
                        });
                    });
                    tagsCollectorManaged.clear();
                    // Go on olding state
                    tagsCollectorThreadListener.onProcessingSubStopThread(this);
                    Util.out(Util.errLine() + methodName + " : run >> Manager Controller now on pause position !");
                });
            }

            // sub process stop running
            running = false; //!< indicate end of processus running

            kafkaToSQLServerThread.doStop();

            /**
             * Manage testing time of new machine insert or remove. Default
             * preset time is 5s
             */
            long processDelay = 5000;
            long delay = delay(processCycleStamp);
            // Inform on execution delay
            tagsCollectorThreadListeners.stream().forEach((tagsCollectorThreadListener) -> {
                tagsCollectorThreadListener.onProcessingCycleTime(this, delay);
            });
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
         * Will kill manager controller : inform all client
         */
        tagsCollectorThreadListeners.stream().forEach((tagCollectorThreadListener) -> {
            tagCollectorThreadListener.onProcessingStopThread(this);
        });
        Util.out(Util.errLine() + methodName + " Terminate Manager Controller Thread");
    }

    /**
     * On Processing Thread
     *
     * This will act when processing child action is received
     */
    @Override
    public void onProcessingThread(Thread thread) {
        String methodName = getClass().getSimpleName() + " : onProcessingThread() >> ";
//        Util.out(methodName + " not yet implemented !");
    }

    /**
     * On Olding Thread
     *
     * This will act when olding child action is received
     */
    @Override
    public void onProcessingSubStopThread(Thread thread) {
        String methodName = getClass().getSimpleName() + " : onOldingThread() >> ";
//        Util.out(methodName + " not yet implemented !");
    }

    /**
     * On Kill Process Thread
     *
     * This will act when a killing process of child is emit with specified
     * child
     *
     * @param t the thread to be kill that was killed
     */
    @Override
    public void onProcessingStopThread(Thread thread) {
        String methodName = getClass().getSimpleName() + " : onKilledProcessThread(Thread thread) >> ";
        TagsCollectorThread t = (TagsCollectorThread) thread;
        Util.out(methodName + "Machine connection " + t.getMachine().getAddress() + " remove done !");
        tagsCollectorManaged.remove(t);
//        tagsCollectorThreadListeners.remove(t);

        // 1 ! Remove listener !!!!
//        Util.out(methodName + " You didn't  remove listener !!!!!! ");
    }

    /**
     *
     * @param thread the value of thread
     * @param ms the value of ms
     */
    @Override
    public void onProcessingSubCycleTime(Thread thread, Long ms) {
    }

    @Override
    public void onProcessingSubThread(Thread thread) {
    }

    /**
     *
     * @param thread the value of thread
     * @param ms the value of ms
     */
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

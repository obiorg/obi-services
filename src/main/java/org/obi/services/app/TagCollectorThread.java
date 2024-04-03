/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.obi.services.app;

import org.obi.services.listener.TagsCollectorThreadListener;
import java.awt.TrayIcon;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.obi.services.entities.machines.Machines;
import org.obi.services.entities.tags.Tags;
import org.obi.services.entities.tags.TagsTypes;
import org.obi.services.sessions.machines.MachinesFacade;
import org.obi.services.sessions.tags.TagsFacade;
import org.obi.services.sessions.tags.TagsTypesFacade;
import org.obi.services.util.Ico;
import org.obi.services.util.Settings;
import org.obi.services.util.Util;

/**
 *
 * @author r.hendrick
 */
public class TagCollectorThread extends Thread implements TagsCollectorThreadListener {

    // Allow to display message on processing
    private TrayIcon trayIcon;
    private final String APP_ICO = "/img/obi/obi-signet-dark.png";

    // Allow to stop process run
    private Boolean requestStop = false;
    private boolean requestKill = false;
    private boolean running = false;

    /**
     * Array list which contain all the TagsCollectorThreadListener listeners
     * that should receive event from client class
     */
    private ArrayList<TagsCollectorThreadListener> tagsCollectorThreadListeners = new ArrayList<>();

    /**
     * Allow to add listener to the list of event listener
     *
     * @param _tagsCollectorThreadListeners a class which will listen to service
     * event
     */
    public void addClientListener(TagsCollectorThreadListener _tagsCollectorThreadListeners) {
        this.tagsCollectorThreadListeners.add(_tagsCollectorThreadListeners);
    }

    /**
     * Allow to remove listener to the list of event listener
     *
     * @param _tagsCollectorThreadListeners a class which will listen to service
     * event
     */
    public void removeClientListener(TagsCollectorThreadListener _tagsCollectorThreadListeners) {
        this.tagsCollectorThreadListeners.remove(_tagsCollectorThreadListeners);
    }

    /**
     * Creates new form
     */
    public TagCollectorThread() {
        trayIcon = new TrayIcon(Ico.i16(APP_ICO, this).getImage());

    }

    public TagCollectorThread(TrayIcon trayIcon) {
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
     * Main loop of the thread data collector
     */
    @Override
    public void run() {
        super.run(); //To change body of generated methods, choose Tools | Templates.
        String methodName = getClass().getSimpleName() + " : run() >> ";

        // Récupération des facades de communication bdd
        MachinesFacade machinesFacade = new MachinesFacade();
        TagsFacade tagsFacade = TagsFacade.getInstance();
        TagsTypesFacade tagsTypesFacade = new TagsTypesFacade();

        // Int Main Loop 
        Integer mainLoop = 0;
        boolean onceOnMain = false; // only display once
        boolean onceOnStop = false; // only display once
        while (!requestKill) {
            long requestEpoch = 0; // allow firstime play
            // Main loop
            while (!requestStop) {
                if (running == false) {
                    tagsCollectorThreadListeners.stream().forEach((tagCollectorThreadListener) -> {
                        tagCollectorThreadListener.onProcessingThread();
                    });
                }
                // Set processus in run mode
                running = true;
                onceOnMain = true;
                int requestEpochCnt = 0;
                // Minimum One second between request
                long now = Instant.now().toEpochMilli();
                long delay = (now - requestEpoch);
                if (delay < 1000) {
                    long d = 1000 - delay;
                    try {
                        sleep(d);
                    } catch (InterruptedException ex) {
                        Util.out(methodName + " > Unable to sleep for minimum dealy time !" + ex.getLocalizedMessage());
                        Logger.getLogger(TagCollectorThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                long now2 = Instant.now().toEpochMilli();

                // Process only if minimum time is respected
                if ((now2 - requestEpoch) >= 1000) {
                    onceOnStop = true;

                    // change epoch reference
                    requestEpoch = Instant.now().toEpochMilli();
                    requestEpochCnt = 0;

                    // Refresh list of available machine
                    List<Machines> machines = machinesFacade.findAll();

                    // For each machine 
                    // 1- Check if available tag to collect on it by recovering the associate list
                    // 2- If available data to collect, create s7 connection to PLC
                    // 3- For each tag
                    // 3.1- Detect type of data
                    // 3.2- Read data to it
                    // 3.3- Store data to corresponding line
                    // 4- Close connection to PLC
                    machines.stream().forEach((machine) -> {
                        List<Tags> tags = tagsFacade.findActiveByCompanyAndMachine(
                                (int) Settings.read(Settings.CONFIG,
                                        Settings.COMPANY),
                                machine.getId());

                        if (tags != null) {
                            if (tags.size() != 0) {
                                //2- create S7 connection to PLC
                                MachineConnection mc = new MachineConnection(machine);
                                if (mc.doConnect()) { // Check if connection is working
                                    tags.stream().forEach((tag) -> {
                                        // Collect only if cyle time is reached since last change
                                        Date date = tag.getVDateTime();
                                        long savedEpoch = date.toInstant().toEpochMilli();
                                        long cycleTime = tag.getCycle() * 1000; // sec
                                        long now3 = Instant.now().toEpochMilli();
                                        if ((now3 - savedEpoch) > cycleTime) {
                                            // Init. default value
                                            tag.setVBool(false);
                                            tag.setVFloat(0.0);
                                            tag.setVInt(0);
                                            tag.setVDateTime(Date.from(Instant.now()));

                                            TagsTypes tagsType = tagsTypesFacade.findById(tag.getType().getId());

                                            if (tagsType != null) {
                                                tag.setType(tagsType);
                                                mc.readValue(tag);
                                                tagsFacade.updateOnValue(tag);
                                            } else {
                                                Util.out(methodName + " Unable to find type " + tag.getType() + " for tag " + tag);
                                            }
                                        }
                                    });
                                } else {
                                    Util.out(methodName + " Unable to connect to client S7 machine = " + machine);
                                }
                                mc.close();
                            } else {
                                Util.out(methodName + " empty list tag found for machine = " + machine);
                            }
                        } else {
                            Util.out(methodName + " No tags found for machine = " + machine);
                        }
                    });
                } else {
                    requestEpochCnt++;
                    if (requestEpochCnt >= 2) {
                        Util.out(methodName + "Epoch not reach more than 2 times : " + requestEpochCnt
                                + " time : " + now + " - " + requestEpoch + " = " + (now - requestEpoch));
                    } else {
                        Util.out(methodName + "Epoch not reached ! "
                                + now + " - " + requestEpoch + " = " + (now - requestEpoch));
                    }
                }
                if (onceOnStop) {
                    onceOnStop = false;
                }
            }
            running = false; //!< indicate end of processus running
            if (onceOnMain) {
                Util.out("TagCollectorThread >> run >> back to mainLoop");
                onceOnMain = false;
                tagsCollectorThreadListeners.stream().forEach((tagCollectorThreadListener) -> {
                    tagCollectorThreadListener.onOldingThread();
                });
            }

            try {
                sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(TagCollectorThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void onProcessingThread() {

    }

    @Override
    public void onOldingThread() {

    }

    @Override
    public void onKillProcessThread(TagsCollectorThread m) {
        Util.out("TagsCollectorThread >> Not defined !!!");
        ///throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}

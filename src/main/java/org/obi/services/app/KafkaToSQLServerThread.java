/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.obi.services.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.obi.services.entities.persistence.PersStandard;
import org.obi.services.entities.tags.Tags;
import org.obi.services.sessions.persistence.PersStandardFacade;
import org.obi.services.util.Util; // Assurez-vous que cette classe est disponible

import org.obi.services.entities.persistence.PersStandard.Deserializer; // Import the Deserializer
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.obi.services.entities.machines.Machines;
import org.obi.services.gson.LocalDateTimeAdapter;
import org.obi.services.listener.thread.SystemThreadListener;
import org.obi.services.util.DateUtil;
import org.obi.services.util.Settings;

/**
 *
 * @author r.hendrick
 */
public class KafkaToSQLServerThread extends Thread {
    // Allow to stop process run

    private Boolean requestStop = false;
    private boolean requestKill = false;
    private boolean running = false;

    private boolean NO_TAG_TO_UPDATE = false;
    private boolean NO_TAG_TO_PERSIST = false;

    private Machines machine;

    private final String bootstrapServers;
    private final String groupId;
    private final String topic;
    private final PersStandardFacade persStandardFacade;

    // Formatter pour parser les timestamps
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS");

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

    public KafkaToSQLServerThread(String name, String bootstrapServers, String groupId, String topic) {
        super(name);
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
        this.topic = topic;
        this.persStandardFacade = PersStandardFacade.getInstance(); // Utilisation du Singleton
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

    @Override
    public void run() {
        final String methodName = getClass().getSimpleName() + " : run() >> ";
        Util.out(Util.errLine() + methodName + "Thread Kafka consumer started for topic: " + topic);

        boolean firstTimeInProcessing = true;        //< Inidcate run loop go back at first in main processing loop
        Integer gmtIndex = Integer.valueOf(Settings.read(Settings.CONFIG, Settings.GMT).toString());
        Integer companyId = Integer.valueOf(Settings.read(Settings.CONFIG, Settings.COMPANY).toString());

        // Configuration du consommateur Kafka
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Lire depuis le début

        Boolean processingDone = true;

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

            try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
                consumer.subscribe(Collections.singletonList(topic));

                // SUB PROCESS LOOP
                while (!requestStop & !requestKill & !wait) {
                    // Inform sub thread only once it reach this after exuction of subprocess 
                    if (running == false) {
                        for (int i = 0; i < systemThreadListeners.size(); i++) {
                            systemThreadListeners.get(i).onProcessingSubThread(this);
                        }
                        running = true;
                    }

                    try {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100)); // Délai de 100ms
                        Gson gson = new GsonBuilder()
                                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                                .registerTypeAdapter(PersStandard.class, new PersStandard.Deserializer())
                                .create();
                        if (!records.isEmpty()) {
                            List<PersStandard> persStandardList = new ArrayList<>();
                            for (org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record : records) {
                                // Désérialiser la valeur du message Kafka (qui est une chaîne au format de votre fichier)
                                
                                
                                Util.out(Util.errLine() + "> " + KafkaToSQLServerThread.class.getName()
                                        + " >> received record PersStandard : " + record.value());
                                PersStandard p = gson.fromJson(record.value(), PersStandard.class);
                            }

                            // Appeler la façade pour écrire dans SQL Server
                        if (!persStandardList.isEmpty()) {
                                boolean success = persStandardFacade.pushValue(persStandardList); // Utilisation de la liste
                                if (success) {
                                    Util.out(methodName + "Successfully wrote " + persStandardList.size() + " records to SQL Server.");
                                } else {
                                    Util.out(methodName + "Failed to write records to SQL Server.");
                                }
                            }
                        }
                    } catch (org.apache.kafka.common.errors.WakeupException e) {
                        if (!running) {
                            Util.out(methodName + "Consumer is shutting down...");
                        } else {
                            Logger.getLogger(KafkaToSQLServerThread.class.getName()).log(Level.SEVERE, "Unexpected WakeupException: ", e);
                        }
                    } catch (Exception e) {
                        Logger.getLogger(KafkaToSQLServerThread.class.getName()).log(Level.SEVERE, "Exception during polling", e);
                        // Gérer les autres exceptions (par exemple, problème de connexion à Kafka)
                    }
                }
            } finally {
                Util.out(methodName + "Consumer thread stopped.");
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

    private PersStandard createPersStandard(String[] values, int gmtIndex) throws DateTimeParseException, NumberFormatException, NullPointerException {
        PersStandard persStandard = new PersStandard();

        // companyId
        persStandard.getCompany().setId(Integer.parseInt(values[0].trim()));

        // tagId
        Tags tag = new Tags();
        tag.setId(Integer.parseInt(values[1].trim()));
        persStandard.setTag(tag);

        // vFloat
        persStandard.setVFloat(Double.parseDouble(values[2].trim()));

        // vInt
        persStandard.setVInt(Integer.parseInt(values[3].trim()));

        // vBool
        persStandard.setVBool(Boolean.parseBoolean(values[4].trim()));

        // vStr
        persStandard.setVStr(values[5].trim().replace("'", "")); // Supprimer les guillemets simples

        // vDateTime
        LocalDateTime vDateTime = LocalDateTime.parse(values[6].trim(), TIMESTAMP_FORMATTER);
        persStandard.setVDateTime(vDateTime);

        // vStamp
        LocalDateTime vStamp = LocalDateTime.parse(values[7].trim(), TIMESTAMP_FORMATTER);
        persStandard.setVStamp(vStamp);

        // stampStart
        LocalDateTime stampStart = LocalDateTime.parse(values[8].trim(), TIMESTAMP_FORMATTER);
        persStandard.setStampStart(stampStart);

        // stampEnd
        LocalDateTime stampEnd = LocalDateTime.parse(values[9].trim(), TIMESTAMP_FORMATTER);
        persStandard.setStampEnd(stampEnd);

        // tbf
        persStandard.setTbf(Double.parseDouble(values[10].trim()));

        // ttr
        persStandard.setTtr(Double.parseDouble(values[11].trim()));

        // error
        persStandard.setError(Boolean.parseBoolean(values[12].trim()));

        // errorMsg
        String errorMsg = values[13].trim();
        persStandard.setErrorMsg(!"NULL".equals(errorMsg) ? errorMsg : null); // Gérer les valeurs NULL

        return persStandard;
    }
}

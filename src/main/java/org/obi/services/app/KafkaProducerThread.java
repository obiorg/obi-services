/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.obi.services.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.obi.services.entities.machines.Machines;
import org.obi.services.entities.persistence.PersStandard;
import org.obi.services.entities.tags.Tags;
import org.obi.services.gson.LocalDateTimeAdapter;
import org.obi.services.listener.thread.SystemThreadListener;
import org.obi.services.util.DateUtil;
import org.obi.services.util.Settings;
import org.obi.services.util.Util;

/**
 * KafkaProducerThread :
 *
 * The main purpose is to stored instant data in rxTags to keep it in kafka
 * while processing writing in to file can be long. While writing is finish
 * processing writing in to database can operate.
 *
 *
 *
 * @author r.hendrick
 */
public class KafkaProducerThread extends Thread {
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

    private final String bootstrapServers;  // Ajout de l'attribut pour les serveurs Kafka
    private final String topic;           // Ajout de l'attribut pour le nom du topic

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
    public KafkaProducerThread(String name, String bootstrapServers, String topic) {
        super(name);
        this.bootstrapServers = bootstrapServers; // Initialisation des serveurs Kafka
        this.topic = topic;                   // Initialisation du nom du topic
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
        Util.out(Util.errLine() + methodName + "Thread Kafka Producer : started with review of connection each 1s");

        boolean firstTimeInProcessing = true;        //< Inidcate run loop go back at first in main processing loop
        Integer gmtIndex = Integer.valueOf(Settings.read(Settings.CONFIG, Settings.GMT).toString());
        Integer companyId = Integer.valueOf(Settings.read(Settings.CONFIG, Settings.COMPANY).toString());

        // Configuration du producteur Kafka
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(properties)) {

            /**
             * START MAIN THREAD LOOP will stop when requestKill is receive by
             * {@link TagsCollectorThread#kill()} or straight requestKill =
             * true.
             */
            LocalDateTime dtMachineChanged = LocalDateTime.now(ZoneId.of(DateUtil.zoneIdOf(gmtIndex)));
            while (!requestKill) {
                /**
                 * processCycleStamp allow to reduce processing analysis over
                 * olding time waiting
                 */
                long processCycleStamp = System.currentTimeMillis(); // allow firstime play

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
                        // Send TxTags to Kafka
                        sendToKafka(producer);
                        NO_ERROR = true;
                    } catch (IOException ex) {
                        if (NO_ERROR == true) {
                            Logger.getLogger(KafkaProducerThread.class.getName()).log(Level.SEVERE, null, ex);
                            NO_ERROR = false;
                        }
                    }
                }

                // sub process stop running
                running = false; //!< indicate end of processus running

                /**
                 * Manage initiation of new request reading Default preset time
                 * is 1s
                 */
                long processDelay = 1000;
                long delay = System.currentTimeMillis() - processCycleStamp;
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
                        Logger.getLogger(KafkaProducerThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            producer.flush();
        } finally {
            /**
             * Will kill tags collector controller : inform all client
             */
            for (int i = 0; i < systemThreadListeners.size(); i++) {
                systemThreadListeners.get(i).onProcessingStopThread(this);
            }

            Util.out(Util.errLine() + methodName + " Terminate tag collector Controller Thread");
        }
    }

    private void sendToKafka(KafkaProducer<String, String> producer) throws IOException {
        final String methodName = getClass().getSimpleName() + " : sendToKafka() >> ";
        // Process each txTags
        for (Tags tag : txTags) {
            // Construire la valeur du message Kafka au format spécifié
//            String messageValue = String.format("{\"company\":%d, \"tag\":%d, \"vFloat\": %.6f;%d;%s;'%s';%s;%s;%s;%s;%f;%f;%s;%s}",
//            String messageValue = String.format("%d;%d;%.6f;%d;%s;'%s';%s;%s;%s;%s;%f;%f;%s;%s",
//                    tag.getCompany().getId(),
//                    tag.getId(),
//                    tag.getVFloat(),
//                    tag.getVInt(),
//                    tag.getVBool().toString(),
//                    tag.getVStr(),
//                    tag.getVDateTime().toString(),
//                    tag.getVStamp().toString(),
//                    tag.getVStamp().toString(), // vStampStart
//                    tag.getVStamp().toString(), // vStampEnd
//                    0.0, // tbf
//                    0.0, // ttr
//                    (tag.getError() == null ? "false" : tag.getError().toString()),
//                    (tag.getErrorMsg() == null ? "NULL" : (tag.getErrorMsg().isEmpty() ? "NULL" : tag.getErrorMsg()))
//            );
            PersStandard p = new PersStandard();
            p.setTag(tag);
            p.setCompany(tag.getCompany());
            p.setVFloat(tag.getVFloat());
            p.setVInt(tag.getVInt());
            p.setVBool(tag.getVBool());
            p.setVStr(tag.getVStr());
            p.setVDateTime(tag.getVDateTime());
            p.setVStamp(tag.getVStamp());
            p.setStampStart(tag.getVStamp());
            p.setStampEnd(tag.getVStamp());
            p.setTbf(0.0);
            p.setTtr(0.0);
            p.setError(tag.getError());
            p.setErrorMsg(tag.getErrorMsg());

            //
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .registerTypeAdapter(PersStandard.class, new PersStandard.Serializer())
                    .create();
            
            String jsonString = gson.toJson(p);
//            System.out.println(jsonString);

            // Créer l'objet ProducerRecord
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, String.valueOf(tag.getId()), jsonString); // La clé est l'ID du tag

            // Envoyer le message de manière asynchrone
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
//                    Util.out(methodName + "Message envoyé au topic : " + metadata.topic()
//                            + ", partition : " + metadata.partition()
//                            + ", offset : " + metadata.offset() + " pour le tag : " + tag.getId());
                } else {
                    Util.out(methodName + "Erreur lors de l'envoi du message pour le tag : " + tag.getId() + " : " + exception.getMessage());
                    Logger.getLogger(KafkaProducerThread.class.getName()).log(Level.SEVERE, "Erreur lors de l'envoi du message pour le tag : " + tag.getId(), exception);
                }
            });
        }
        producer.flush();
        txTags.clear();
    }

    public Machines getMachine() {
        return machine;
    }

    public void setMachine(Machines machine) {
        this.machine = machine;
    }
}

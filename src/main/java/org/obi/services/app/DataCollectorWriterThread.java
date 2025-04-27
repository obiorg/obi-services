package org.obi.services.app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.obi.services.entities.business.Companies;
import org.obi.services.entities.machines.Machines;
import org.obi.services.entities.persistence.PersStandard;
import org.obi.services.entities.tags.Tags;
import org.obi.services.listener.thread.SystemThreadListener;
import org.obi.services.sessions.persistence.PersStandardFacade;
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
public class DataCollectorWriterThread extends Thread {
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
    public DataCollectorWriterThread() {
        super("DataCollectorWriterThread");
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
        Util.out(Util.errLine() + methodName + "Thead DataCollectorWriter : started with review of connection each 1s");

        boolean firstTimeInProcessing = true;   //< Inidcate run loop go back at first in main processing loop
        Integer gmtIndex = Integer.valueOf(Settings.read(Settings.CONFIG, Settings.GMT).toString());
        Integer companyId = Integer.valueOf(Settings.read(Settings.CONFIG, Settings.COMPANY).toString());

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

            PersStandardFacade pft = PersStandardFacade.getInstance();

            // SUB PROCESS LOOP
            while (!requestStop & !requestKill & !wait & !rxTags.isEmpty()) {
                // Inform sub thread only once it reach this after exuction of subprocess 
                if (running == false) {
                    for (int i = 0; i < systemThreadListeners.size(); i++) {
                        systemThreadListeners.get(i).onProcessingSubThread(this);
                    }
                    running = true;
                }

                // Create a directory if not exist
                String dirFrom = "./DataCollector";
                String dirTo = "./DataCollector/Processing";
                createDirectoryIfNotExist("./DataCollector/Processing");

                // Copy All file to directory 
                if (processingDone) {
                    moveFilesFromOneDirToAnother(dirFrom, dirTo);
                    processingDone = false;
                }
                // Load All file in to memory statement
                List<PersStandard> pers = new ArrayList<>();
                pers = loadFilesAsPersStandard(dirTo);

                // Parse to facade
                if (pers != null && !pers.isEmpty()) {
                    if (pft.pushValue(pers)) {
                        // successfuly push data
                        processingDone = true;
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

    /**
     * Create a directory defined by directory
     *
     * @param directory the path directory to check or create
     * @return true if file exist or created successfuly
     */
    private Boolean createDirectoryIfNotExist(String directory) {
        // Define the path for the directory you want to create
        // You can replace "my_new_directory" with your desired directory name
        // You can also provide a full path like "/home/user/documents/my_new_directory"
        String directoryPathString = directory;
        Path directoryPath = Paths.get(directoryPathString);

        // Check if the directory exists
        if (!Files.exists(directoryPath)) {
            try {
                // If the directory does not exist, create it
                Files.createDirectories(directoryPath); // Use createDirectories to create parent directories if needed
                System.out.println("Directory created successfully: " + directoryPath.toAbsolutePath());
            } catch (IOException e) {
                // Handle any potential IO errors during directory creation
                Util.out(Util.errLine() + "DataCollectorWriterThread : createDirectoryIfNotExist : "
                        + "Failed to create directory: " + directoryPath.toAbsolutePath());
                e.printStackTrace();
                return false;
            }

        } else {
            // If the directory already exists, inform the user
            //System.out.println("Directory already exists: " + directoryPath.toAbsolutePath());
        }
        return true;
    }

    /**
     * Processs move of files contain in "from" directory (like cut) and place
     * them in to "to" directory.
     * <p>
     * Note if files already exist in "to" directory they will be replaced.
     *
     * @param from directory where files should be taken
     * @param to directory wher files should be moved
     * @return true if full operation was done correctly
     */
    private Boolean moveFilesFromOneDirToAnother(String from, String to) {
        // Define the source and destination directory paths
        // Replace "source_directory" and "destination_directory" with your actual paths
        String sourceDirectoryPathString = from;
        String destinationDirectoryPathString = to;

        Path sourceDirectory = Paths.get(sourceDirectoryPathString);
        Path destinationDirectory = Paths.get(destinationDirectoryPathString);

        // Ensure the destination directory exists. Create it if it doesn't.
        createDirectoryIfNotExist(to);

        // Check if the source directory exists and is a directory
        if (Files.exists(sourceDirectory) && Files.isDirectory(sourceDirectory)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDirectory)) {
                // Iterate through all entries (files and subdirectories) in the source directory
                for (Path entry : stream) {
                    // Check if the current entry is a regular file (not a directory)
                    if (Files.isRegularFile(entry)) {
                        Path destinationFile = destinationDirectory.resolve(entry.getFileName());
                        try {
                            // Move the file to the destination directory
                            // StandardCopyOption.REPLACE_EXISTING will overwrite the file if it already exists in the destination
                            Files.move(entry, destinationFile, StandardCopyOption.REPLACE_EXISTING);
//                            System.out.println("Moved file: " + entry.getFileName() + " to " + destinationFile.toAbsolutePath());
                            return true;
                        } catch (IOException e) {
                            Util.out(Util.errLine() + "DataCollectorWriterThread : moveFilesFromOnDirToAnother >> "
                                    + "Failed to move file: " + entry.getFileName());
//                            System.err.println("Failed to move file: " + entry.getFileName());
                            e.printStackTrace();
                        }
                    }
                }
//                System.out.println("Finished attempting to move files from: " + sourceDirectory.toAbsolutePath());
            } catch (IOException e) {
                Util.out(Util.errLine() + "DataCollectorWriterThread : moveFilesFromOnDirToAnother >> "
                        + "Error listing files in source directory: " + sourceDirectory.toAbsolutePath());
//                System.err.println("Error listing files in source directory: " + sourceDirectory.toAbsolutePath());
                e.printStackTrace();
                return false;
            }
        } else {
            Util.out(Util.errLine() + "DataCollectorWriterThread : moveFilesFromOnDirToAnother >> "
                    + "Source directory does not exist or is not a directory: " + sourceDirectory.toAbsolutePath());
//            System.out.println("Source directory does not exist or is not a directory: " + sourceDirectory.toAbsolutePath());
            return false;
        }
        return true;

    }

    /**
     * Load All file contain in directory to process "dirProcesing" as
     * PersStandard in an list array of them.
     *
     * @param dirProcesing the directory path in which files to process should
     * be
     * @return null if any error occur and so nothing to be process otherwise
     * return list of persstandard
     */
    private List<PersStandard> loadFilesAsPersStandard(String dirProcesing) {
        // Define the source and destination directory paths
        String sourceDirectoryPathString = dirProcesing;
        List<PersStandard> pers = new ArrayList<>();

        Path sourceDirectory = Paths.get(sourceDirectoryPathString);

        // Check if the directory directory exists and is a directory
        if (Files.exists(sourceDirectory) && Files.isDirectory(sourceDirectory)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDirectory)) {
                // Iterate through all entries (files and subdirectories) in the directory
                for (Path entry : stream) {
                    // Check if the current entry is a regular file (not a directory)
                    if (Files.isRegularFile(entry)) {
                        Path filename = entry.getFileName();
                        List<PersStandard> readedPersStandards = filesToPersStandards(entry.getFileName());//                            System.err.println("Failed to move file: " + entry.getFileName());
                        if (readedPersStandards != null && !readedPersStandards.isEmpty()) {
                            pers.addAll(readedPersStandards);
                        }
                        return pers;
                    }
                }
//                System.out.println("Finished attempting to move files from: " + sourceDirectory.toAbsolutePath());
            } catch (IOException e) {
                Util.out(Util.errLine() + "DataCollectorWriterThread : loadFilesAsPersStandard >> "
                        + "Error listing files in source directory: " + sourceDirectory.toAbsolutePath());
//                System.err.println("Error listing files in source directory: " + sourceDirectory.toAbsolutePath());
                e.printStackTrace();
                return null;
            }
        } else {
            Util.out(Util.errLine() + "DataCollectorWriterThread : loadFilesAsPersStandard >> "
                    + "Source directory does not exist or is not a directory: " + sourceDirectory.toAbsolutePath());
//            System.out.println("Source directory does not exist or is not a directory: " + sourceDirectory.toAbsolutePath());
            return null;
        }
        return null;
    }

    /**
     * Read A file structured properly item separate by ";" and item object
     * separate by ","
     *
     * @param fileNamePath Path of the file to read
     * @return null if error file exist or reading file
     */
    private List<PersStandard> filesToPersStandards(Path fileNamePath) {
        // Define the path to the file you want to read
        // Replace "your_file.txt" with the actual name and path of your file
//        String filePathString = "your_file.txt";
//        Path filePath = Paths.get(filePathString);
        Path filePath = fileNamePath;

        List<PersStandard> persStandardList = new ArrayList<>();

        Integer gmtIndex = Integer.valueOf(Settings.read(Settings.CONFIG, Settings.GMT).toString());
        Integer companyId = Integer.valueOf(Settings.read(Settings.CONFIG, Settings.COMPANY).toString());

        // Check if the file exists
        if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
            try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
                String line;
                // Read the file line by line
                while ((line = br.readLine()) != null) {
                    // Remove leading/trailing whitespace from the line
                    line = line.trim();

                    // Skip empty lines
                    if (line.isEmpty()) {
                        continue;
                    }

                    // Split the line into individual items using ";" as the delimiter
                    String[] items = line.split(";");

                    for (String item : items) {
                        // Remove leading/trailing whitespace from each item
                        item = item.trim();

                        // Skip empty items that might result from splitting
                        if (item.isEmpty()) {
                            continue;
                        }

                        // Split each item into fields using "," as the delimiter
                        String[] fields = item.split(",");

                        // Check if the number of fields matches the expected structure (14 fields based on your printf)
                        if (fields.length == 14) {
                            try {
                                // Create a new PersStandard object
                                PersStandard persStandard = new PersStandard();

                                // Parse and set the fields based on the order in your printf
                                // Remember to trim whitespace from each field value
                                // 0: companyId (Integer)
                                persStandard.setCompany(new Companies(Integer.parseInt(fields[0].trim())));

                                // 1: id tag (Integer)
                                persStandard.setTag(new Tags(Integer.parseInt(fields[1].trim())));

                                // 2: vFloat (Double)
                                persStandard.setVFloat(Double.parseDouble(fields[2].trim()));

                                // 3: vInt (Integer)
                                persStandard.setVInt(Integer.parseInt(fields[3].trim()));

                                // 4: vBool (Boolean) - parsed from String
                                persStandard.setVBool(Boolean.parseBoolean(fields[4].trim()));

                                // 5: vStr (String)
                                persStandard.setVStr(fields[5].trim());

                                // 6: vDateTime (LocalDateTime) - parsed from String
                                try {
                                    persStandard.setVDateTime(LocalDateTime.parse(fields[6].trim()));
                                } catch (DateTimeParseException e) {
                                    System.err.println("Error parsing vDateTime: " + fields[6].trim() + " for item: " + item);
                                    persStandard.setVDateTime(null); // Set to null or handle as needed
                                }

                                // 7: vStamp (LocalDateTime) - parsed from String
                                try {
                                    persStandard.setVStamp(LocalDateTime.parse(fields[7].trim()));
                                } catch (DateTimeParseException e) {
                                    Util.out(Util.errLine() + "DataCollectorWriterThread : filesToPersStandards >> "
                                            + "Error parsing vStamp: " + fields[7].trim() + " for item: " + item);
                                    persStandard.setVStamp(null); // Set to null or handle as needed
                                }

                                // 8: vStampStart (LocalDateTime) - parsed from String
                                try {
                                    persStandard.setStampStart(LocalDateTime.parse(fields[8].trim()));
                                } catch (DateTimeParseException e) {
                                    Util.out(Util.errLine() + "DataCollectorWriterThread : filesToPersStandards >> "
                                            + "Error parsing stampStart: " + fields[8].trim() + " for item: " + item);
                                    persStandard.setStampStart(null); // Set to null or handle as needed
                                }

                                // 9: vStampEnd (LocalDateTime) - parsed from String
                                try {
                                    persStandard.setStampEnd(LocalDateTime.parse(fields[9].trim()));
                                } catch (DateTimeParseException e) {
                                    Util.out(Util.errLine() + "DataCollectorWriterThread : filesToPersStandards >> "
                                            + "Error parsing stampEnd: " + fields[9].trim() + " for item: " + item);
                                    persStandard.setStampEnd(null); // Set to null or handle as needed
                                }

                                // 10: tbf (Double)
                                persStandard.setTbf(Double.parseDouble(fields[10].trim()));

                                // 11: ttr (Double)
                                persStandard.setTtr(Double.parseDouble(fields[11].trim()));

                                // 12: error (Boolean) - parsed from String
                                persStandard.setError(Boolean.parseBoolean(fields[12].trim()));

                                // 13: errorMsg (String)
                                // Handle "NULL" string specifically if needed, otherwise just trim
                                String errorMsg = fields[13].trim();
                                persStandard.setErrorMsg(errorMsg.equals("NULL") ? null : errorMsg);

                                // Add the parsed object to the list
                                persStandardList.add(persStandard);

                            } catch (NumberFormatException e) {
                                Util.out(Util.errLine() + "DataCollectorWriterThread : filesToPersStandards >> "
                                        + "Error parsing number field in item: " + item);
                                e.printStackTrace();
                            } catch (Exception e) {
                                Util.out(Util.errLine() + "DataCollectorWriterThread : filesToPersStandards >> "
                                        + "An unexpected error occurred while processing item: " + item);
                                e.printStackTrace();
                            }
                        } else {
                            Util.out(Util.errLine() + "DataCollectorWriterThread : filesToPersStandards >> "
                                    + "Skipping item due to incorrect number of fields (" + fields.length + "): " + item);
                        }
                    }
                }

                System.out.println("Finished reading and parsing file: " + filePath.toAbsolutePath());
                System.out.println("Parsed " + persStandardList.size() + " PersStandard objects.");

                // You can now work with the list of PersStandard objects
                // For example, print them:
                // for (PersStandard ps : persStandardList) {
                //     System.out.println(ps);
                // }
            } catch (IOException e) {
                Util.out(Util.errLine() + "DataCollectorWriterThread : filesToPersStandards >> "
                        + "Error reading file: " + filePath.toAbsolutePath());
                e.printStackTrace();
                return null;
            }
        } else {
            Util.out(Util.errLine() + "DataCollectorWriterThread : filesToPersStandards >> "
                    + "File does not exist or is not a regular file: " + filePath.toAbsolutePath());
            return null;
        }

        return persStandardList;
    }

}

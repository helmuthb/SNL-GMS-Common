package gms.dataacquisition.stationreceiver.cd11.dataframeparser;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.dataacquisition.stationreceiver.cd11.common.GracefulThread;
import gms.dataacquisition.stationreceiver.cd11.dataframeparser.configuration.DataframeParserConfig;
import gms.dataacquisition.stationreceiver.osdgateway.SerializationUtility;
import gms.dataacquisition.stationreceiver.osdgateway.StationReceiverOsdGatewayInterface;
import gms.dataacquisition.stationreceiver.osdgateway.accesslibrary.StationReceiverOsdGatewayAccessLibrary;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.WaveformsJacksonMixins;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Application that listens to a directory specified in DataFrameParseConfig (/var/gms/dataframes/
 * by default) for new files (which should be dataframes), parses them and stores waveforms to
 * influx and postgres.
 */
public class DataframeParser extends GracefulThread {

    private static Logger logger = LoggerFactory.getLogger(DataframeParser.class);

    private final DataframeParserConfig config;
    private final StationReceiverOsdGatewayInterface osdGateway;
    private final WatchService watcher;
    private final Map<String, Instant> manifestFileToTime = new HashMap<>();
    private static final long READ_FREQUENCY_MS = 2000;
    public static final ObjectMapper objectMapper = new ObjectMapper();
    private final SystemControllerNotifier sysControllerNotifier;

    static {
        WaveformsJacksonMixins.register(objectMapper);
        objectMapper.findAndRegisterModules();
    }

    public DataframeParser(DataframeParserConfig config,
        SystemControllerNotifier sysControllerNotifier) throws IOException {
        this(config, sysControllerNotifier, new StationReceiverOsdGatewayAccessLibrary(
                config.osdGatewayHostname, config.osdGatewayPort));
    }

    public DataframeParser(DataframeParserConfig config,
        SystemControllerNotifier sysControllerNotifier,
        StationReceiverOsdGatewayInterface osdGateway)
            throws IOException {
        super(DataframeParser.class.getName(), true, false);

        // Initialize properties.
        this.config = Objects.requireNonNull(config);
        this.sysControllerNotifier = Objects.requireNonNull(sysControllerNotifier);

        // Create an OSD Gateway Access Library.
        this.osdGateway = osdGateway;

        this.watcher = FileSystems.getDefault().newWatchService();

        //Look at config to find out where data frames are stored, register the directory watchers
        walkAndRegisterDirectories(Paths.get(config.monitoredDirLocation));
    }

    /**
     * Register the given directory, and all its sub-directories, with the WatchService.
     *
     * @param start the starting directory to begin listing as directories to watch, relative to the
     * root of the project
     */
    private void walkAndRegisterDirectories(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                dir.register(watcher, ENTRY_CREATE);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Starts the Dataframe Parser.
     */
    @Override
    protected void onStart() {
        logger.info("Starting DataframeParser");
        try {
            while (this.keepThreadRunning()) {
                //Construct manifest file string
                String manifestFilePath = config.monitoredDirLocation + "manifest.inv";
                File manifestFile = new File(manifestFilePath);
                // read the manifest and update manifestFileToTime
                readManifest(manifestFilePath);
                // process each file in the manifest.  processFile will remove
                // any file it successfully processes from manifestFileToTime,
                // and silently do nothing if the file is not present.
                Set<String> filesToRemove = this.manifestFileToTime.keySet().stream()
                    .filter(this::processFile)  // processFile returns true if file was processed.
                    .collect(Collectors.toSet());
                filesToRemove.forEach(this.manifestFileToTime::remove);
                // this call will log manifest entries that have been known about for a while
                // and have never shown up.
                cullAndLogMissingManifestEntries();
                //delete manifest file
                manifestFile.delete();
                Thread.sleep(READ_FREQUENCY_MS);
            }
        } catch (Exception e) {
            logger.error(String.format(
                    "Unexpected exception thrown in thread %1$s, and thread must now close.",
                    this.getThreadName()), e);
        }
    }

    private boolean processFile(String fileName) {
        // Check if file exists.  If it doesn't, it is not this methods' job
        // to complain.  That occurs in cullAndLogMissingManifestEntries,
        // after the file has been missing for an amount of time.
        Path path = Paths.get(config.monitoredDirLocation + fileName);
        if (!Files.exists(path)) {
            logger.error("Path " + path.toString() + " does not exist");
            return false;
        }

        logger.info("Processing data file: " + fileName);
        //take those files and convert to RawStationDataFrame and ChannelSegment (waveform)
        //then write RawStationDataFrames and ChannelSegments to OSD gateway

        RawStationDataFrame frame;
        try {
            String contents = new String(Files.readAllBytes(path));
            frame = SerializationUtility.objectMapper.readValue(
                contents, RawStationDataFrame.class);
            Validate.notNull(frame);
            this.osdGateway.storeRawStationDataFrame(frame);
        } catch (Exception ex) {
            logger.error("Failed to store RawStationDataFrame: ", ex);
            deleteFile(path);
            return false;
        }

        try {
            //Switch parser based on acquisition protocol
            switch (frame.getAcquisitionProtocol()) {
                case CD11:
                    //Store channel segments and soh's
                    Pair<List<ChannelSegment>, List<AcquiredChannelSoh>> parsedData = Cd11RawStationDataFrameReader
                        .read(
                            frame, CreationInfo.DEFAULT, osdGateway::getChannelId);
                    String channelIds = parsedData.getLeft().stream()
                        .map(ChannelSegment::getProcessingChannelId)
                        .distinct()
                        .map(UUID::toString)
                        .collect(Collectors.joining(","));
                    try {
                        this.osdGateway.storeChannelSegments(parsedData.getLeft());
                        logger.info("Stored channel segments with ID's " + channelIds);
                    } catch (Exception ex) {
                        logger
                            .error("Failed to store channel segments with ID's " + channelIds, ex);
                    }

                    try {
                        this.osdGateway.storeChannelStatesOfHealth(parsedData.getRight());
                        logger.info("Stored channel states of health with ID's " + channelIds);
                    } catch (Exception ex) {
                        logger.error(
                            "Failed to store channel states of health with ID's " + channelIds, ex);
                    }
                    break;
                default:
                    logger.error(
                        "Unrecognized RawStationDataFrame acquisition protocol. Will not parse file.");
            }


        } catch (Exception e) {
            logger.error("Error processing file " + fileName, e);
            return false;
        } finally {
            deleteFile(path);
        }
        return true;
    }

    private static void deleteFile(Path p) {
        try {
            Files.delete(p);
        } catch(IOException ex) {
            logger.error("Could not delete file " + p);
        }
    }


    private void readManifest(String manifestFilePath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(manifestFilePath));
            Instant now = Instant.now();
            lines.stream().filter(s -> !s.isEmpty())
                .forEach(l -> this.manifestFileToTime.put(l.trim(), now));
        } catch (IOException e) {
        }
    }

    private void cullAndLogMissingManifestEntries() {
        List<String> missingFiles = new ArrayList<>();
        for (Iterator<Map.Entry<String, Instant>> it = this.manifestFileToTime.entrySet().iterator();
             it.hasNext();) {
            Map.Entry<String, Instant> entry = it.next();
            Instant threshold = Instant.now().minusMillis(this.config.manifestTimeThreshold);
            if (entry.getValue().isBefore(threshold)) {
                missingFiles.add(entry.getKey());
                it.remove();
            }
        }
        this.sysControllerNotifier.notifyMissingFiles(missingFiles);
    }
}

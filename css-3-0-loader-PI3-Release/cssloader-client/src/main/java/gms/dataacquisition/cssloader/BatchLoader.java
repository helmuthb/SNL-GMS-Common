package gms.dataacquisition.cssloader;

import gms.dataacquisition.cssloader.accesslibrary.CssLoaderOsdGatewayAccessLibrary;
import gms.dataacquisition.cssloader.configuration.CssLoaderConfiguration;
import gms.dataacquisition.cssloader.configuration.CssLoaderConfigurationLoader;
import gms.dataacquisition.cssloader.data.BatchLoaderCommandLineArgs;
import gms.dataacquisition.cssloader.data.SegmentAndSohBatch;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.DataExistsException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Command-line application to load data from CSS flat files.
 */
public class BatchLoader {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BatchLoader.class);

  private final CssLoaderOsdGatewayInterface cssLoaderAccessLibrary;

  private final BatchLoaderCommandLineArgs cmdLineArgs = new BatchLoaderCommandLineArgs();

  public static void main(String[] args) {
    int exit = new BatchLoader().execute(args);

    // BatchLoader.execute returns the number of rows loaded to facilitate unit testing, however
    // we want to return the standard "0" value representing no error.
    if (exit > 0) {
      exit = 0;
    }

    System.exit(exit);
  }

  /**
   * Creates a BatchLoader, using the default OSD gateway interface (Access Library).
   */
  public BatchLoader() {
    CssLoaderConfiguration config = CssLoaderConfigurationLoader.load();
    this.cssLoaderAccessLibrary = new CssLoaderOsdGatewayAccessLibrary(
        config.serviceHost, config.servicePort);
  }

  /**
   * Creates a BatchLoader, using the supplied OSD gateway interface (Access Library).
   */
  public BatchLoader(CssLoaderOsdGatewayAccessLibrary cssLoaderAccessLibrary) {
    this.cssLoaderAccessLibrary = cssLoaderAccessLibrary;
  }

  /**
   * Performs the load.  Implemented as an instance method since the fields that are annotated with
   * @Option can be instance also.
   *
   * @param args command-line args
   * @return exit code is number of waveforms loaded, and value less than 0 is error
   */
  public int execute(String[] args) {

    CmdLineParser parser = new CmdLineParser(cmdLineArgs);
    try {
      parser.parseArgument(args);

      boolean validated = validateArgs(parser, this.cmdLineArgs);
      if (!validated) {
        logger.error("Invalid command-line argument(s) received.");
        return -1;
      }

      // Get arguments and convert into proper formats.
      List<String> stationList = null;
      List<String> channelList = null;
      Instant time = null;
      Instant endtime = null;

      String stationsArg = cmdLineArgs.getStations();
      if (stationsArg != null && stationsArg.length() > 0) {
        stationList = Arrays.asList(stationsArg.trim().split(","));
      }
      String channelsArg = cmdLineArgs.getChannels();
      if (channelsArg != null && channelsArg.length() > 0) {
        channelList = Arrays.asList(channelsArg.trim().split(","));
      }
      long timeEpochArg = cmdLineArgs.getTimeEpoch();
      String timeDateArg = cmdLineArgs.getTimeDate();
      if (timeEpochArg > -1) {
        time = Instant.ofEpochSecond(timeEpochArg);
      } else if (timeDateArg != null && timeDateArg.length() > 0) {
        time = Instant.parse(timeDateArg);
      }
      long endtimeEpochArg = cmdLineArgs.getEndtimeEpoch();
      String endtimeDateArg = cmdLineArgs.getEndtimeDate();
      if (endtimeEpochArg > -1) {
        endtime = Instant.ofEpochSecond(endtimeEpochArg);
      } else if (endtimeDateArg != null && endtimeDateArg.length() > 0) {
        endtime = Instant.parse(endtimeDateArg);
      }

      // Load the WF Disc file.
      CssBatchReader cssBatchReader = new CssBatchReader(
          this.cssLoaderAccessLibrary, cmdLineArgs.getWfdiscFile(),
          cmdLineArgs.getBatchSize(), stationList, channelList,
          time, endtime);

      // Read through the WF Disc data in batches.
      int successfulStores = 0;
      while (cssBatchReader.nextBatchExists()) {
        SegmentAndSohBatch batch = cssBatchReader.readNextBatch();
        if (batch.segments.isEmpty() && batch.sohs.isEmpty()) {
          continue;
        }

        // Determine whether to display the data, or persist the data.
        if (cmdLineArgs.getPersistToOsd()) {
          // Transmit the CSS data to the OSD Gateway Service, in batches.
          boolean segmentStoreSuccess = true, sohStoreSuccess = true;
          if (batch.segments.size() > 0) {
            try {
              this.cssLoaderAccessLibrary.storeChannelSegments(batch.segments);
            } catch(DataExistsException ex) {
              logger.warn("Segment store rejected as already present: " + batch.segments);
            } catch(Exception ex) {
              logger.error("Could not store segments " + batch.segments, ex);
              segmentStoreSuccess = false;
            }
          }

          if (batch.sohs.size() > 0) {
            try {
              this.cssLoaderAccessLibrary.storeChannelStatesOfHealth(batch.sohs);
            } catch(DataExistsException ex) {
              logger.warn("soh store rejected as already present: " + batch.sohs);
            } catch(Exception ex) {
              logger.error("Could not store soh's " + batch.sohs, ex);
              sohStoreSuccess = false;
            }
          }
          if (segmentStoreSuccess && sohStoreSuccess) {
            successfulStores++;
            logger.info("Successful store of batch #" + successfulStores
                + "/" + cssBatchReader.size());
          }

          // Pause before sending next batch increment.
          if (cmdLineArgs.getBatchInterval() > 0) {
            Thread.sleep(cmdLineArgs.getBatchInterval());
          }
        }
      }

      int numWfDiscRecordsPresent = cssBatchReader.size();
      logger.info(String.format("Loaded %d wfdisc records out of %d present.",
          successfulStores, numWfDiscRecordsPresent));
      return numWfDiscRecordsPresent;
    } catch (CmdLineException e) {
      // If args4j throws an error then print out usage information.
      printUsage(parser, e.getLocalizedMessage());
      return -1;
    } catch (Exception e) {
      logger.error(BatchLoader.class.getSimpleName() + "failed to execute:", e);
      return -1;
    }
  }

  /**
   * Validate arguments, then print usage and exit if found any problems.
   *
   * @param parser the command-line arguments parser
   * @return true = arguments validated, false = there is an error.
   */
  private static boolean validateArgs(CmdLineParser parser,
      BatchLoaderCommandLineArgs cmdLineArgs) {
    if (cmdLineArgs.getBatchSize() < 1) {
      printUsage(parser, "The batchSize value must be greater than zero.");
      return false;
    }
    if ((cmdLineArgs.getTimeEpoch() > -1) &&
        (cmdLineArgs.getTimeDate() != null && cmdLineArgs.getTimeDate().length() > 0)) {
      printUsage(parser, "Cannot use both timeEpoch and timeDate in same call");
      return false;
    }
    if ((cmdLineArgs.getEndtimeEpoch() > -1) &&
        (cmdLineArgs.getEndtimeDate() != null && cmdLineArgs.getEndtimeDate().length() > 0)) {
      printUsage(parser, "Cannot use both endtimeEpoch and endtimeDate in same call");
      return false;
    }

    if ((cmdLineArgs.getTimeEpoch() > -1) && (cmdLineArgs.getEndtimeEpoch() > -1)) {
      if (cmdLineArgs.getTimeEpoch() >= cmdLineArgs.getEndtimeEpoch()) {
        printUsage(parser, "timeEpoch must be less than endtimeEpoch");
        return false;
      }
    }

    Instant timeDateInstant = null;
    if (cmdLineArgs.getTimeDate() != null && cmdLineArgs.getTimeDate().length() > 0) {
      try {
        timeDateInstant = Instant.parse(cmdLineArgs.getTimeDate());
      } catch (Exception e) {
        printUsage(parser, "Invalid format for timeDate: " + e.getLocalizedMessage());
        return false;
      }
    }

    Instant endtimeDateInstant = null;
    if (cmdLineArgs.getEndtimeDate() != null && cmdLineArgs.getEndtimeDate().length() > 0) {
      try {
        endtimeDateInstant = Instant.parse(cmdLineArgs.getEndtimeDate());
      } catch (Exception e) {
        printUsage(parser, "Invalid format for endtimeDate: " + e.getLocalizedMessage());
        return false;
      }
    }
    if ((timeDateInstant != null) && (endtimeDateInstant != null)) {
      if (timeDateInstant.compareTo(endtimeDateInstant) >= 0) {
        printUsage(parser, "timeDate must be less than endtimeDate");
        return false;
      }
    }
    String stations = cmdLineArgs.getStations();
    if (stations != null && stations.length() > 0) {
      if ((stations.length() > 6) && (stations.indexOf(",") <= 0)) {
        logger.warn("'stations' is unusually long without any commas, "
            + "but assuming user knows what they are doing and continuing anyaways.");
      }
    }
    String channels = cmdLineArgs.getChannels();
    if (channels != null) {
      if ((channels.length() > 8) && (channels.indexOf(",") <= 0)) {
        logger.warn("'channels' is unusually long without any commas, "
            + "but assuming user knows what they are doing and continuing anyways.");
      }
    }
    return true;
  }

  /**
   * Prints out usage information for this application.
   *
   * @param parser the command-line arguments parser
   * @param msg the error msg
   */
  private static void printUsage(CmdLineParser parser, String msg) {
    logger.error(msg);

    System.err.println(System.lineSeparator() +
        "Error - invalid argument: " + msg +
        System.lineSeparator());
    System.err.print("Usage: java " + BatchLoader.class + " ");
    parser.printSingleLineUsage(System.err);
    System.err.println();
    parser.printUsage(System.err);
  }
}

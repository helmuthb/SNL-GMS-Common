package gms.dataacquisition.cssloader.stationref;

import gms.dataacquisition.cssloader.CssLoaderOsdGatewayInterface;
import gms.dataacquisition.cssloader.accesslibrary.CssLoaderOsdGatewayAccessLibrary;
import gms.dataacquisition.cssloader.stationref.commandline.StationRefLoaderCommandLineArgs;
import gms.dataacquisition.cssloader.stationref.configuration.CssStationRefLoaderConfiguration;
import gms.dataacquisition.cssloader.stationref.configuration.CssStationRefLoaderConfigurationLoader;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.DataExistsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceCalibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetwork;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetworkMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSensor;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSite;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import org.hibernate.exception.DataException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.LoggerFactory;

/**
 * Command-line application to load data from CSS flat files.
 */
public class Application {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Application.class);

  private final CssLoaderOsdGatewayInterface cssLoaderAccessLibrary;

  private final StationRefLoaderCommandLineArgs cmdLineArgs
      = new StationRefLoaderCommandLineArgs();

  public static void main(String[] args) {
    boolean allStoresSuccessful = new Application().execute(args);
    System.exit(allStoresSuccessful ? 0 : -1);
  }

  /**
   * Creates a station reference loader, using the default OSD gateway interface (Access Library).
   */
  public Application() {
    CssStationRefLoaderConfiguration config = CssStationRefLoaderConfigurationLoader.load();
    this.cssLoaderAccessLibrary = new CssLoaderOsdGatewayAccessLibrary(
        config.serviceHost, config.servicePort);
  }

  /**
   * Creates a station reference loader, using the supplied OSD gateway interface (Access Library).
   *
   * @param cssLoaderAccessLibrary the access library
   */
  public Application(CssLoaderOsdGatewayAccessLibrary cssLoaderAccessLibrary) {
    this.cssLoaderAccessLibrary = cssLoaderAccessLibrary;
  }

  /**
   * Performs the load.
   *
   * @param args command-line args
   */
  public boolean execute(String[] args) {

    CmdLineParser parser = new CmdLineParser(cmdLineArgs);
    try {
      parser.parseArgument(args);
      CssReferenceReader refReader = new CssReferenceReader(
          cmdLineArgs.getAffiliationFile(),
          cmdLineArgs.getInstrumentFile(),
          cmdLineArgs.getNetworkFile(),
          cmdLineArgs.getSensorFile(),
          cmdLineArgs.getSiteFile(),
          cmdLineArgs.getSiteChanFile());

      // store networks
      boolean networkStoresSuccessful = applyStores(this.cssLoaderAccessLibrary::storeNetwork,
          refReader.getReferenceNetworks(), "network");
      // store stations
      boolean stationStoresSuccessful = applyStores(this.cssLoaderAccessLibrary::storeStation,
          refReader.getReferenceStations(), "station");
      // store sites
      boolean siteStoresSuccessful = applyStores(this.cssLoaderAccessLibrary::storeSite,
          refReader.getReferenceSites(), "site");
      // store channels
      boolean channelStoresSuccessful = applyStores(this.cssLoaderAccessLibrary::storeChannel,
          refReader.getReferenceChannels(), "channel");
      // store calibrations
      boolean calibrationStoresSuccessful = applyStores(this.cssLoaderAccessLibrary::storeCalibration,
          refReader.getCalibrations(), "calibration");
      // stores responses
      boolean responseStoresSuccessful = applyStores(this.cssLoaderAccessLibrary::storeResponse,
          refReader.getResponses(), "response");
      // store sensors
      boolean sensorStoresSuccessful = applyStores(this.cssLoaderAccessLibrary::storeSensor,
          refReader.getSensors(), "sensor");
      // store network memberships
      boolean netMemberStoresSuccessful = applyStore(this.cssLoaderAccessLibrary::storeNetworkMemberships,
          refReader.getReferenceNetworkMemberships());
      logger.info("Stored all network memberships successfully: " + netMemberStoresSuccessful);
      // store station memberships
      boolean staMemberStoresSuccessful = applyStore(this.cssLoaderAccessLibrary::storeStationMemberships,
          refReader.getReferenceStationMemberships());
      logger.info("Stored all station memberships successfully: " + staMemberStoresSuccessful);
      // store site memberships
      boolean siteMemberStoresSuccessful = applyStore(this.cssLoaderAccessLibrary::storeSiteMemberships,
          refReader.getReferenceSiteMemberships());
      logger.info("Stored all site memberships successfully: " + siteMemberStoresSuccessful);

      return networkStoresSuccessful && stationStoresSuccessful &&
          siteStoresSuccessful && channelStoresSuccessful &&
          calibrationStoresSuccessful && responseStoresSuccessful &&
          sensorStoresSuccessful && netMemberStoresSuccessful &&
          staMemberStoresSuccessful && siteMemberStoresSuccessful;
    } catch (Exception ex) {
      logger.error("Error in Application.execute", ex);
      return false;
    }
  }

  @FunctionalInterface
  private interface FunctionWithException<T> {
    void apply(T t) throws Exception;
  }

  private static <T> boolean applyStores(FunctionWithException<T> f, Collection<T> data, String dataName) {
    int successes = 0;
    for (T t : data) {
      if (applyStore(f, t)) {
        successes++;
      }
    }
    logger.info(String.format("Successfully stored %d %s's out of %d read",
        successes, dataName, data.size()));

    return successes == data.size();
  }

  private static <T> boolean applyStore(FunctionWithException<T> f, T data) {
    try {
      f.apply(data);
      return true;
    } catch(DataExistsException ex) {
      logger.warn("Not storing this data, was apparently already there: " + data);
      return true;
    } catch(Exception ex) {
      logger.error("Error storing data " + data, ex);
      return false;
    }
  }
}

package gms.dataacquisition.cssloader.stationref;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import gms.dataacquisition.cssreader.data.AffiliationRecord;
import gms.dataacquisition.cssreader.data.InstrumentRecord;
import gms.dataacquisition.cssreader.data.NetworkRecord;
import gms.dataacquisition.cssreader.data.SensorRecord;
import gms.dataacquisition.cssreader.data.SiteChannelRecord;
import gms.dataacquisition.cssreader.data.SiteRecord;
import gms.dataacquisition.cssreader.referencereaders.FlatFileAffiliationReader;
import gms.dataacquisition.cssreader.referencereaders.FlatFileInstrumentReader;
import gms.dataacquisition.cssreader.referencereaders.FlatFileNetworkReader;
import gms.dataacquisition.cssreader.referencereaders.FlatFileSensorReader;
import gms.dataacquisition.cssreader.referencereaders.FlatFileSiteChannelReader;
import gms.dataacquisition.cssreader.referencereaders.FlatFileSiteReader;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkOrganization;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkRegion;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceAlias;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceCalibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetwork;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetworkMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSensor;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSite;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSiteMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStationMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StatusType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.swing.text.html.CSS;
import org.apache.commons.lang.Validate;
import org.slf4j.LoggerFactory;


/**
 * Read flat files and convert their contents to Reference COI objects.
 */
public class CssReferenceReader {

  private static final org.slf4j.Logger logger = LoggerFactory
      .getLogger(CssReferenceReader.class);

  private List<ReferenceNetwork> referenceNetworks = new ArrayList<>();
  private List<ReferenceStation> referenceStations = new ArrayList<>();
  private List<ReferenceSite> referenceSites = new ArrayList<>();
  private List<ReferenceChannel> referenceChannels = new ArrayList<>();
  private List<ReferenceResponse> responses = new ArrayList<>();
  private List<ReferenceCalibration> calibrations = new ArrayList<>();
  private List<ReferenceSensor> sensors = new ArrayList<>();
  private List<AffiliationRecord> affiliationRecords = new ArrayList<>();

  // Lists that represent the relationships between entities.
  private Set<ReferenceNetworkMembership> referenceNetworkMemberships = new HashSet<>();
  private Set<ReferenceStationMembership> referenceStationMemberships = new HashSet<>();
  private Set<ReferenceSiteMembership> referenceSiteMemberships = new HashSet<>();

  // Maps that collect objects by name.  Duplicate names are allowed.
  private SetMultimap<String, ReferenceSite> station2Sites = HashMultimap.create();
  private SetMultimap<String, ReferenceStation> station2Station = HashMultimap.create();
  private SetMultimap<String, ReferenceNetwork> network2Network = HashMultimap.create();
  private SetMultimap<String, ReferenceSite> site2Site = HashMultimap.create();
  private ListMultimap<String, NetworkRecord> networkRecordMap = ArrayListMultimap.create();
  private ListMultimap<String, SiteRecord> siteRecordMap = ArrayListMultimap.create();
  private ListMultimap<UUID, ReferenceChannel> site2Channels = ArrayListMultimap.create();
  private ListMultimap<ReferenceChannel, ReferenceSensor> channel2Sensors = ArrayListMultimap.create();
  private ListMultimap<ReferenceChannel, ReferenceCalibration> channel2Calibrations = ArrayListMultimap.create();
  private ListMultimap<ReferenceChannel, ReferenceResponse> channel2Responses = ArrayListMultimap.create();

  private SetMultimap<Integer, ReferenceSensor> chan2Sensor = HashMultimap.create();
  private SetMultimap<Integer, ReferenceCalibration> chan2Calib = HashMultimap.create();
  private SetMultimap<Integer, ReferenceResponse> chan2Response = HashMultimap.create();

  private Map<ReferenceChannel, SiteChannelRecord> channel2Record = new HashMap<>();
  private Map<ReferenceSite, SiteRecord> site2SiteRecord = new HashMap<>();
  private Map<ReferenceSite, AffiliationRecord> site2AffiliationRecord = new HashMap<>();
  private Map<ReferenceStation, SiteRecord> station2Record = new HashMap<>();

  // Map by channel ID.
  private SetMultimap<Integer, ReferenceChannel> chanId2Channel = HashMultimap.create();
  // Map by instrument ID.
  private ListMultimap<Integer, InstrumentRecord> instrumentRecordMap = ArrayListMultimap.create();
  // MMap by channel ID.
  private ListMultimap<Integer, SensorRecord> sensorRecordMap = ArrayListMultimap.create();
  // Map by channel ID.
  private ListMultimap<Integer, SiteChannelRecord> siteChannelRecordMap = ArrayListMultimap
      .create();

  private final Set<String> missingResponseFiles = new HashSet<>();

  // In the CSS files this date (or close to it) is used to indicate no end date.
  private static final Instant MAX_DATE = Instant.parse("2200-01-01T00:00:00Z");

  private final String networkFilePath;
  private final String siteFilePath;
  private final String siteChannelFilePath;
  private final String affiliationFilePath;
  private final String sensorFilePath;
  private final String instrumentFilePath;

  /**
   * Pass in the text file paths which are parsed and converted into COI objects.
   *
   * @param affiliationFilePath path to the CSS .affiliation file
   * @param instrumentFilePath path to the CSS .instrument file
   * @param networkFilePath path to the CSS .network file
   * @param sensorFilePath path to the CSS .sensor file
   * @param siteFilePath path to the CSS .site file
   * @param siteChannelFilePath path to the CSS .sitechan file
   */
  public CssReferenceReader(
      String affiliationFilePath,
      String instrumentFilePath,
      String networkFilePath,
      String sensorFilePath,
      String siteFilePath,
      String siteChannelFilePath) throws Exception {

    referenceNetworks = new ArrayList<>();
    referenceStations = new ArrayList<>();
    referenceSites = new ArrayList<>();
    referenceChannels = new ArrayList<>();
    responses = new ArrayList<>();
    calibrations = new ArrayList<>();
    sensors = new ArrayList<>();
    affiliationRecords = new ArrayList<>();


    this.affiliationFilePath = affiliationFilePath;
    this.instrumentFilePath = instrumentFilePath;
    this.networkFilePath = networkFilePath;
    this.sensorFilePath = sensorFilePath;
    this.siteFilePath = siteFilePath;
    this.siteChannelFilePath = siteChannelFilePath;

    this.process();
  }

  /**
   * Read the input files and create the COI objects.
   *
   * @throws Exception If there are issues reading or parsing the files.
   */
  private void process() throws Exception {
    // Read each flat file.
    this.networkRecordMap = new FlatFileNetworkReader().read(this.networkFilePath);
    this.siteRecordMap = new FlatFileSiteReader().read(this.siteFilePath);
    this.affiliationRecords.addAll(new FlatFileAffiliationReader().read(this.affiliationFilePath));
    this.siteChannelRecordMap = new FlatFileSiteChannelReader().read(this.siteChannelFilePath);
    this.sensorRecordMap = new FlatFileSensorReader().read(this.sensorFilePath);

    // Read all the instrument records and index by their ID.
    FlatFileInstrumentReader instrumentReader = new FlatFileInstrumentReader();
    for (InstrumentRecord record : instrumentReader.read(this.instrumentFilePath)) {
      this.instrumentRecordMap.put(record.getInid(), record);
    }

    // Process the flat file data.  The order is important.
    processNetworkRecords();
    processSiteRecords();
    processAffiliationRecords();
    processSiteChannelRecords();
    processSensorRecords();
    addChannelMembership();

    if (!this.missingResponseFiles.isEmpty()) {
      logger.warn("Could not find response files: " + this.missingResponseFiles);
    }
  }



  /**
   * This will be removed when the output has been validated.
   *
   */
  public void outputResults() throws FileNotFoundException {
    Boolean includeMembers = false;
    List<ReferenceStationMembership> stationMembers = new ArrayList<>(referenceStationMemberships);
    stationMembers.sort(Comparator.comparing(ReferenceStationMembership::getActualChangeTime));
    List<ReferenceSiteMembership> siteMembers = new ArrayList<>(referenceSiteMemberships);
    siteMembers.sort(Comparator.comparing(ReferenceSiteMembership::getActualChangeTime));
    PrintWriter pw = new PrintWriter("/tmp/out1.txt");
    referenceStations.sort(Comparator.comparing(ReferenceStation::getName));
    for (ReferenceStation station : referenceStations) {
      pw.write("\r\n\r\n" + station);



      List<ReferenceSite> sites = new ArrayList<>(station2Sites.get(station.getName()));
      sites.sort(Comparator.comparing(ReferenceSite::getName));
      for (ReferenceSite site : sites) {
        if ( includeMembers ) {
          for (ReferenceStationMembership member : stationMembers) {
            if (member.getStationId().equals(station.getEntityId())
                && member.getSiteId().equals(site.getEntityId())
                //    ) {
                && member.getActualChangeTime().compareTo(site.getActualChangeTime()) == 0) {
              pw.write("\r\n    " + member);
            }
          }
        }
        pw.write("\r\n    " + site);
        List<ReferenceChannel> channels = site2Channels.get(site.getEntityId());
        channels.sort(Comparator.comparing(ReferenceChannel::getName));
        for (ReferenceChannel chan : channels) {
          SiteChannelRecord chanRecord = channel2Record.get(chan);
          if ( includeMembers ) {
            for (ReferenceSiteMembership member : siteMembers) {
              if (member.getSiteId().equals(site.getEntityId())
                  && member.getChannelId().equals(chan.getEntityId())
                  ) {
                //    && member.getActualChangeTime().compareTo(chan.getActualTime()) == 0) {
                if (member.getActualChangeTime().compareTo(chanRecord.getOndate()) >= 0
                    && member.getActualChangeTime().compareTo(chanRecord.getOffdate()) < 0) {
                  pw.write("\r\n       " + member);
                }
              }
            }
          }

          pw.write("\r\n       " + chan);
          for (ReferenceSensor sensor : channel2Sensors.get(chan)) {
            pw.write("\r\n           " + sensor);
          }
          for (ReferenceCalibration calib : channel2Calibrations.get(chan)) {
            pw.write("\r\n           " + calib);
          }
          for (ReferenceResponse response : channel2Responses.get(chan)) {
            pw.write("\r\n           " + "Response{chanId=" + response.getChannelId()
                + ", actualTime=" + response.getActualTime()
                + ", dataLen=" + response.getResponseData().length
                + "}"
            );
          }
        }
      }
    }
    pw.close();

    pw = new PrintWriter("/tmp/out0.txt");

    referenceSites.sort(Comparator.comparing(ReferenceSite::getName));
    for (ReferenceSite site : referenceSites) {

      pw.write("\r\n\r\n" + site);
      List<ReferenceChannel> channels = site2Channels.get(site.getEntityId());
      channels.sort(Comparator.comparing(ReferenceChannel::getName));
      for (ReferenceChannel chan : channels) {
        pw.write("\r\n    " + chan);
      }

    }
    pw.close();

    System.out.println("Networks:       " + referenceNetworks.size());
    System.out.println("Stations:       " + referenceStations.size());
    System.out.println("Sites:          " + referenceSites.size());
    System.out.println("Channels:       " + referenceChannels.size());
    System.out.println("Sensors:        " + sensors.size());
    System.out.println("Calibrations:   " + calibrations.size());
    System.out.println("Responses:      " + responses.size());
    System.out.println("NetworkMembers: " + referenceNetworkMemberships.size());
    System.out.println("StationMembers: " + referenceStationMemberships.size());
    System.out.println("SiteMembers:    " + referenceSiteMemberships.size());
  }

  //************  Filter by Station Helper Function ************/
  // TODO - remove this function and import
  // objectstoragedistribution.coi.stationreference.service.utility.FilterUtility.java
  // once it's made into library
  private static <T> List<T> filterByEndTime(List<T> elems, Instant endTime,
      Function<T, Instant> timeExtractor) {

    Validate.notNull(elems);
    Validate.notNull(timeExtractor);
    Validate.notNull(endTime);

    return elems.stream()
        .filter(t -> isBeforeOrEqual(timeExtractor.apply(t), endTime))
        .collect(Collectors.toList());
  }

  private static boolean isBeforeOrEqual(Instant time1, Instant time2) {
    return time1.isBefore(time2) || time1.equals(time2);
  }

  //************  Filter by Station  ************/
  // TODO make the params after Station optional (e.g., station, station site, station site channel)?
  //public void outputStationResults(String theStation, String theSite, String theChannel)
     //String stationName = theStation;
     //String siteName = theSite;
     //String channelName = theChannel;

  public void outputStationResults(String theStation)
      throws FileNotFoundException {

    List<ReferenceStationMembership> stationMembers = new ArrayList<>(referenceStationMemberships);
    stationMembers.sort(Comparator.comparing(ReferenceStationMembership::getActualChangeTime));

    List<ReferenceSiteMembership> siteMembers = new ArrayList<>(referenceSiteMemberships);
    //siteMembers.sort(Comparator.comparing(ReferenceSiteMembership::getActualChangeTime));
    siteMembers.sort(Comparator.comparing(ReferenceSiteMembership::getActualChangeTime));

    PrintWriter pw = new PrintWriter("/tmp/out.txt");

    int stnCount = 0;
    int siteCount = 0;
    int chanCount = 0;

    // Header info printed once at top of file
    pw.write("\r\n\r\n" + "Station_Name      " + " Description      " + " Type      " +
        " Latitude      " + " Longitude      " + " Elevation      " + " Actual_Change_Time ");

    // filter for station
    referenceStations.sort(Comparator.comparing(ReferenceStation::getName));
    for (ReferenceStation station : referenceStations) {
      if (station.getName().equalsIgnoreCase(theStation)) {
        stnCount++;

    Instant endTime = Instant.now();
    List<ReferenceStation> filteredStations =
        filterByEndTime(referenceStations, endTime,
            ReferenceStation::getActualChangeTime);

        //pw.write("\r\n\r\n" + station);  // prints all fields in the object
        pw.write("\r\n\r\n" + station.getName() + "   ");
        pw.write("  " + station.getDescription());
        pw.write("  " + station.getStationType());
        pw.write("  " + station.getLatitude());
        pw.write("  " + station.getLongitude());
        pw.write("  " + station.getElevation());
        pw.write("  " + station.getActualChangeTime());

      // sites associated to the station
      referenceSites.sort(Comparator.comparing(ReferenceSite::getName));
      for (ReferenceSite site : station2Sites.get(station.getName())) {
            siteCount++;
            if (site.getActualChangeTime().equals(station.getActualChangeTime())
                || site.getActualChangeTime().isBefore(station.getActualChangeTime())) {

            //pw.write("\r\n\r\n" + site);  // prints all fields in the object
            pw.write("\r\n" + "    " + site.getName());
            pw.write("   " + site.getDescription());
            pw.write("   " + site.getLatitude());
            pw.write("   " + site.getLongitude());
            pw.write("   " + site.getElevation());
            pw.write("   " + site.getActualChangeTime());

            // channels associated with sites
            List<ReferenceChannel> channels = site2Channels.get(site.getEntityId());
            channels.sort(Comparator.comparing(ReferenceChannel::getName));
            for (ReferenceChannel chan : channels) {
              chanCount++;
              //pw.write("\r\n\r\n" + chan);  // prints all fields in the object
              pw.write("\r\n" + "         " + chan.getName());
              pw.write("    " + chan.getType());
              pw.write("    " + chan.getLatitude());
              pw.write("    " + chan.getLongitude());
              pw.write("    " + chan.getElevation());
              pw.write("    " + chan.getActualTime());

              for (ReferenceSensor sensor : channel2Sensors.get(chan)) {
                //pw.write("\r\n\r\n" + sensor);  // prints all fields in the object
                pw.write("\r\n           sensor: " + sensor.getActualTime());
                pw.write("    " + sensor.getInstrumentManufacturer());
                pw.write("    " + sensor.getInstrumentModel());
              }
              for (ReferenceCalibration calib : channel2Calibrations.get(chan)) {
                //pw.write("\r\n\r\n" + calib);  // prints all fields in the object
                pw.write("\r\n           calib: " + calib.getActualTime());
                pw.write("    " + calib.getCalibrationFactor());
              }
              for (ReferenceResponse response : channel2Responses.get(chan)) {
                //pw.write("\r\n\r\n" + response);  // prints all fields in the object
                pw.write("\r\n           response: " + response.getActualTime());
                pw.write("    " + response.getResponseType());
              }
            }
          }
        }
      }
    }

    pw.close();
    System.out.println("*************** Requested Station Info ****************");
    System.out.println("Total Stations:     " + referenceStations.size());
    System.out.println(theStation + " Stations: " + stnCount);
    System.out.println("Total Sites:        " + referenceSites.size());
    //System.out.println(siteName + " Sites:    " + siteCount);
    System.out.println("Total Stations:     " + referenceStations.size());
    System.out.println(theStation + " Stations: " + stnCount);
    }

    /*
    System.out.println("Networks:       " + referenceNetworks.size());
    System.out.println("Stations:       " + referenceStations.size());
    System.out.println("Sites:          " + referenceSites.size());
    System.out.println("Channels:       " + referenceChannels.size());
    System.out.println("Sensors:        " + sensors.size());
    System.out.println("Calibrations:   " + calibrations.size());
    System.out.println("Responses:      " + responses.size());
    System.out.println("NetworkMembers: " + referenceNetworkMemberships.size());
    System.out.println("StationMembers: " + referenceStationMemberships.size());
    System.out.println("SiteMembers:    " + referenceSiteMemberships.size());
    */

  /**
   * Process all network records and convert into COI objects.
   */
  private void processNetworkRecords() throws Exception {

    NetworkRegion region = NetworkRegion.GLOBAL;
    NetworkOrganization org = NetworkOrganization.UNKNOWN;
    InformationSource source;

    for (NetworkRecord record : networkRecordMap.values()) {

      // TODO: this mapping is not correct.
      switch (record.getType().toUpperCase().trim()) {
        case "WW":
          region = NetworkRegion.GLOBAL;
          break;
        case "AR":
          region = NetworkRegion.REGIONAL;
          break;
        case "LO":
          region = NetworkRegion.LOCAL;
          break;
        default:
          logger.warn(
              "processNetworkRecordsUnknown() - network region detected: " + record.getType());
      }
      String filename = Paths.get(networkFilePath).getFileName().toString();
      source = InformationSource.create("External", record.getLddate(),
          "Loaded from file " + filename);

      UUID networkId = getIdFor(network2Network, record.getName(),
          ReferenceNetwork::getEntityId);
      ReferenceNetwork network = ReferenceNetwork.from(
          networkId, UUID.randomUUID(), record.getName(),
          record.getDesc(), org, region, source,
          "Loaded from network file: " + filename, Instant.EPOCH, Instant.EPOCH);
      referenceNetworks.add(network);
      network2Network.put(network.getName(), network);
    }
  }


  /**
   * Process all affiliation records and map the stations to networks.  This table also may indicate
   * a relationship between stations and sites.
   */
  private void processAffiliationRecords() throws Exception {
    if (network2Network.isEmpty() || station2Station.isEmpty()) {
      logger.error("Unable to process affiliation records before the network and station records.");
      return;
    }
    // Loop over all the records in the affiliation file.
    for (AffiliationRecord record : affiliationRecords) {
      String netName = record.getNet().trim().toUpperCase();
      String staName = record.getSta().trim().toUpperCase();

      // Get the start and end dates of when the relationship was valid.
      Instant start = record.getTime() == null ? Instant.EPOCH : record.getTime();
      Instant end = record.getEndtime();

      // If the netName and staName are equal, then this is a station to site relation.
      if (netName.equals(staName)) {

          for (ReferenceStation station : station2Station.get(staName)) {
            // Ignore this record unless the station is of a certain type.
            if (station.getStationType().equals(StationType.Seismic3Component)) {

              // Create a site for the station, using the station's attributes.
              ReferenceSite site = createStationsWithSingleSite(station);
              site2AffiliationRecord.put(site, record);

              ReferenceStationMembership member = ReferenceStationMembership.from(UUID.randomUUID(),
                  "Relationship for station "
                      + station.getName() + " and site " + site.getName(),
                  start, start,
                  station.getEntityId(),
                  site.getEntityId(),
                  StatusType.ACTIVE);
              if (!isStationMembershipDuplicate(member)) {
                referenceStationMemberships.add(member);
              }

              // Is there an end date for this relationship?  If so, add another
              // record with the status set to INACTIVE.
              if (end.isBefore(MAX_DATE)) {
                ReferenceStationMembership member2 = ReferenceStationMembership.from(UUID.randomUUID(),
                    "Relationship for station "
                        + station.getName() + " and site " + site.getName(),
                    end, end,
                    station.getEntityId(),
                    site.getEntityId(),
                    StatusType.INACTIVE);
                if (!isStationMembershipDuplicate(member2)) {
                  referenceStationMemberships.add(member2);
                }
              }
            }
          }
        continue;
      }

      // This is a station to site record.
      if (network2Network.get(netName).isEmpty()) {
        Instant stationEndDate;

        // Sort the stations in reverse order by creation date.
        List<ReferenceStation> stations = new ArrayList<>(station2Station.get(netName));
        stations.sort(Comparator.comparing(ReferenceStation::getActualChangeTime).reversed());
        for (ReferenceStation station : stations) {
          stationEndDate = station2Record.get(station).getOffdate();

          // Get all sites related to this station.
          Instant siteEndDate;
          List<ReferenceSite> sites = new ArrayList<>(station2Sites.get(netName));
          sites.sort(Comparator.comparing(ReferenceSite::getActualChangeTime).reversed());
          //Set<ReferenceSite> sites = station2Sites.get(netName);
          for (ReferenceSite site : sites) {
            siteEndDate = site2SiteRecord.get(site) != null
                ? site2SiteRecord.get(site).getOffdate() : site2AffiliationRecord.get(site).getEndtime();
            // Is this the site named in the affiliation record?
            if (site.getName().equals(staName)) {
              // If this site wasn't active when the relationship started, then skip.
              if (!isAssociatedByTime(station.getActualChangeTime(), stationEndDate,
                  site.getActualChangeTime(), siteEndDate)) {
                continue;
              }
              // Create a relationship between the station and site.
              ReferenceStationMembership member = ReferenceStationMembership.from(UUID.randomUUID(),
                  "Relationship for station "
                      + station.getName() + " and site " + site.getName(),
                  start, start,
                  station.getEntityId(),
                  site.getEntityId(),
                  StatusType.ACTIVE);
              // Is there already a matching relationship.
              if (!isStationMembershipDuplicate(member)) {
                referenceStationMemberships.add(member);
              }
              // If there is an end date on the relationship, create another
              // record to indicate that fact.
              if ( end.isBefore(MAX_DATE)) {
                ReferenceStationMembership member2 = ReferenceStationMembership.from(UUID.randomUUID(),
                    "Relationship for station "
                        + station.getName() + " and site " + site.getName(),
                    end, end,
                    station.getEntityId(),
                    site.getEntityId(),
                    StatusType.INACTIVE);
                // Is there already a matching relationship.
                if (!isStationMembershipDuplicate(member2)) {
                  referenceStationMemberships.add(member2);
                }
              }
              break;
            }
          }

        }
      }
      // This is a network to station record. Networks don't have a on or off date, so
      // we can't filter stations based on that relationship.
      else {
        //Instant netEndDate = Instant.MAX;
        // Sort the networks in reverse order by creation date.
        List<ReferenceNetwork> networks = new ArrayList<>(network2Network.get(netName));
        networks.sort(Comparator.comparing(ReferenceNetwork::getActualChangeTime).reversed());
        for (ReferenceNetwork network : networks) {
          List<ReferenceStation> stations = new ArrayList<>(station2Station.get(staName));
          stations.sort(Comparator.comparing(ReferenceStation::getActualChangeTime).reversed());

          for (ReferenceStation station : stations) {
            Instant actualDate = record.getTime() != null ? record.getTime() : Instant.EPOCH;
            ReferenceNetworkMembership member = ReferenceNetworkMembership.from(UUID.randomUUID(),
                "Relationship for network "
                    + network.getName() + " and station " + station.getName(),
                actualDate, actualDate,
                network.getEntityId(), station.getEntityId(), StatusType.ACTIVE);
            if (!isNetworkMembershipDuplicate(member)) {
              referenceNetworkMemberships.add(member);
            }

            // If there is an end date on this relationship, then add another relationship
            // record to indicate it ended on this date.
            if ( end.isBefore(MAX_DATE) ) {
              ReferenceNetworkMembership member2 = ReferenceNetworkMembership.from(UUID.randomUUID(),
                  "Relationship for network "
                      + network.getName() + " and station " + station.getName(),
                  end, end,
                  network.getEntityId(), station.getEntityId(), StatusType.INACTIVE);
              if (!isNetworkMembershipDuplicate(member2)) {
                referenceNetworkMemberships.add(member2);
              }
            }
          }
        }
      }

    } // end for each affiliation record

  }

  /**
   * Process all site-channel records and convert into COI objects.
   */
  private void processSiteChannelRecords() throws Exception {
    RelativePosition position = RelativePosition.create(0, 0, 0);
    ChannelDataType dataType = ChannelDataType.UNKNOWN;

    // Loop over all the SiteChannelRecords read from the flat file.
    for (SiteChannelRecord record : siteChannelRecordMap.values()) {
      List<ReferenceAlias> channelAliases = new ArrayList<>();
      // Get the station or site name.  Could be either!
      String entityName = record.getSta().trim().toUpperCase();
      Instant onDate = record.getOndate();
      Instant offDate = record.getOffdate();

      Optional<ReferenceSite> associatedSite = site2Site.get(entityName).stream()
          .filter(s -> s.getActualChangeTime().isBefore(onDate) ||
              s.getActualChangeTime().equals(onDate))
          .max(Comparator.comparing(ReferenceSite::getActualChangeTime));
      if (!associatedSite.isPresent()) {
        logger.warn("Could not find site associated to channel record: " + record);
        continue;
      }
      ReferenceSite site = associatedSite.get();

      // Create a information source object.
      String filename = Paths.get(siteChannelFilePath).getFileName().toString();
      InformationSource source = InformationSource.create("External",
          record.getOndate(), "OffDate: " + offDate + ", Loaded from file " + filename);

      // Get some details from other records.
      double sampleRate = 0;  // default value
      Optional<SensorRecord> sensorRecord = getFirst(sensorRecordMap, record.getChanid());
      Optional<InstrumentRecord> instrumentRecord = sensorRecord.isPresent() ?
          getFirst(instrumentRecordMap, sensorRecord.get().getInid()) : Optional.empty();
      if (instrumentRecord.isPresent()) {
        sampleRate = instrumentRecord.get().getSamprate();
      }

      UUID chanId = getIdFor(chanId2Channel, record.getChanid(),
          ReferenceChannel::getEntityId);
      ReferenceChannel channel = ReferenceChannel.from(
          chanId, UUID.randomUUID(), record.getChan(),
          ChannelTypeConverter.getChannelType(record.getChan()),
          dataType, 0, site.getLatitude(),
          site.getLongitude(), site.getElevation(),
          record.getEdepth(), record.getVang(), record.getHang(),
          sampleRate, onDate, onDate,
          source, "Channel is associated with site " + entityName,
          position, channelAliases);

      if (!isChannelDuplicate(site, channel)) {
        chanId2Channel.put(record.getChanid(), channel);
        referenceChannels.add(channel);
        site2Channels.put(site.getEntityId(), channel);
        channel2Record.put(channel, record);
      } else {
        logger.warn("processSiteChannelRecords() - found duplicate channel: " + channel);
      }
    } // end for loop
  }

  /**
   * Now scan all the sites and add membership records for each channel.
   */
  private void addChannelMembership() {
    // Look at all the sites and their channels.
    for (UUID siteId : site2Channels.keys()) {
      // Get all the channels associated with this site.
      List<ReferenceChannel> channels = site2Channels.get(siteId);

      // Sort the channels into groups by channel type (name).
      // There may be multiple channels of a particular type.
      ListMultimap<String, ReferenceChannel> channelGroups = ArrayListMultimap.create();
      for (ReferenceChannel channel : channels) {
        channelGroups.put(channel.getName(), channel);
      }
      // Process each type of channel for this site.
      for (String name : channelGroups.keys()) {
        List<ReferenceChannel> channelList = channelGroups.get(name);
        channelList.sort(Comparator.comparing(ReferenceChannel::getActualTime));
        // Loop over all the versions of this type of channel.
        for (int i = 0; i < channelList.size(); i++) {
          ReferenceChannel channel = channelList.get(i);
          SiteChannelRecord rec = channel2Record.get(channel);
          Instant onDate = rec.getOndate();
          Instant offDate = rec.getOffdate();

          // create ACTIVE membership for this entry.  Each sitechan entry results in an active membership.
          ReferenceSiteMembership activeMember = ReferenceSiteMembership.from(UUID.randomUUID(),
              "Channel " + channel.getName() + " is associated with site " + rec.getSta(),
              onDate, onDate, siteId,
              channel.getEntityId(), StatusType.ACTIVE);
          if (!isSiteMembershipDuplicate(activeMember)) {
            referenceSiteMemberships.add(activeMember);
          }

          // check if this is the last sitechan record, and offdate is the special value.
          boolean recordIsLastAndHasOffDate = (i == channelList.size() - 1) && offDate.isBefore(MAX_DATE);
          // check if the next record exists and if so, whether there's a gap between the offdate
          // of this record and the ondate of the next record.
          boolean nextRecordExistsAndTimeGap = false;
          if ((i + 1) < channelList.size()) {
            ReferenceChannel channel2 = channelList.get(i + 1);
            SiteChannelRecord rec2 = channel2Record.get(channel2);
            Instant nextOnDate = rec2.getOndate();
            nextRecordExistsAndTimeGap = !onDate.equals(nextOnDate);
          }
          // if either of those situations exists, an INACTIVE record is called for.
          if (recordIsLastAndHasOffDate || nextRecordExistsAndTimeGap) {
            ReferenceSiteMembership inactiveMember = ReferenceSiteMembership.from(UUID.randomUUID(),
                "Channel " + channel.getName() + " is un-associated with site " + rec.getSta(),
                offDate, offDate, siteId,
                channel.getEntityId(), StatusType.INACTIVE);
            if (!isSiteMembershipDuplicate(inactiveMember)) {
              referenceSiteMemberships.add(inactiveMember);
            }
          }
        } // end for each channel of some type
      } // end for type of channel
    } // end for site

  }

  /**
   * Process all site records and convert into COI objects.
   */
  private void processSiteRecords() throws Exception {

    for (SiteRecord record : siteRecordMap.values()) {
      List<ReferenceAlias> stationAliases = new ArrayList<>();
      List<ReferenceAlias> siteAliases = new ArrayList<>();
      String filename = Paths.get(siteFilePath).getFileName().toString();
      InformationSource source = InformationSource.create("External",
          record.getLddate(),
          "OffDate: " + record.getOffdate() + ", Loaded from file " + filename);

      // TODO: this mapping may not be correct.
      StationType type = StationType.UNKNOWN;
      switch (record.getStatype().toUpperCase().trim()) {
        case "SS":
          type = StationType.Seismic3Component;
          break;
        case "AR":
          type = StationType.SeismicArray;
          break;
        default:
          logger.warn("processSiteRecords() - Unknown site type detected: "
              + record.getStatype() + " for record: " + record);
      }

      String name = record.getSta().trim().toUpperCase();

      // If the name equals the refsta name, then this is a station.
      if (name.equals(record.getRefsta().trim().toUpperCase())) {
        UUID staId = getIdFor(station2Station, record.getSta(),
            ReferenceStation::getEntityId);
        ReferenceStation station = ReferenceStation.from(
            staId, UUID.randomUUID(), name, record.getStaname(),
            type, source, "Loaded from site file.", record.getLat(),
            record.getLon(), record.getElev(), record.getOndate(), record.getOndate(),
            stationAliases);
        if (!isStationDuplicate(station)) {
          referenceStations.add(station);
          station2Station.put(name, station);
          station2Record.put(station, record);
        }
        else {
          logger.warn("processSiteRecords() - found duplicate station: " + station);
        }

      }
      // Otherwise this defines a site.
      else {
        UUID siteId = getIdFor(site2Site, record.getSta(),
            ReferenceSite::getEntityId);
        RelativePosition relativePosition = RelativePosition
            .create(record.getDnorth(), record.getDeast(), 0);
        ReferenceSite site = ReferenceSite.from(
            siteId, UUID.randomUUID(), record.getSta(),
            record.getStaname(), source,
            "Site is associated with station " + record.getRefsta().trim().toUpperCase(),
            record.getLat(), record.getLon(), record.getElev(),
            record.getOndate(), record.getOndate(), relativePosition, siteAliases);

        // Save the site in various structures for later processing.  But first make sure this
        // isn't a duplicate entry in the input file.
        if (!isSiteDuplicate(site)) {
          referenceSites.add(site);
          site2Site.put(name, site);
          station2Sites.put(record.getRefsta().trim().toUpperCase(), site);
          site2SiteRecord.put(site, record);
        }
        else {
          logger.warn("processSiteRecords() - found duplicate site: " + site);
        }
      }
    } // end for loop

  }

  /**
   * Some stations have only a single site, so use the station's attributes to create the site.
   */
  private ReferenceSite createStationsWithSingleSite(ReferenceStation station) {

    logger.debug(
        "createStationsWithSingleSite() - Adding single site for station: " + station.getName());

    UUID siteId = getIdFor(site2Site, station.getName(), ReferenceSite::getEntityId);
    ReferenceSite site = ReferenceSite.from(
        siteId, UUID.randomUUID(), station.getName(),
        station.getDescription(), station.getSource(),
        "Associated with station " + station.getName(),
        station.getLatitude(), station.getLongitude(), station.getElevation(),
        station.getActualChangeTime(), station.getSystemChangeTime(),
        RelativePosition.create(0, 0, 0),
        new ArrayList<>());

    // Save the site in various structures for later processing.
    if (!isSiteDuplicate(site)) {
      referenceSites.add(site);
      site2Site.put(site.getName(), site);
      station2Sites.put(station.getName(), site);

    }

    return site;
  }

  /**
   * Process all sensor records and convert into COI objects.
   */
  private void processSensorRecords() {

    for (SensorRecord record : sensorRecordMap.values()) {
      Instant onDate = record.getTime();
      Instant offDate = record.getEndTime();
      String filename = Paths.get(sensorFilePath).getFileName().toString();
      for (InstrumentRecord instr : instrumentRecordMap.get(record.getInid())) {
        InformationSource source = InformationSource.create("External",
            record.getLddate(),
            "OffDate: " + offDate + ", Loaded from file " + filename );
        int chanId = record.getChanid();

        // Get the sites with the indicated site name.
        List<ReferenceSite> sites = new ArrayList<>(site2Site.get(record.getSta()));
        sites.sort(Comparator.comparing(ReferenceSite::getActualChangeTime).reversed());

        Instant siteEndDate;
        for (ReferenceSite site : sites) {

          siteEndDate = site2SiteRecord.get(site) != null
              ? site2SiteRecord.get(site).getOffdate() : site2AffiliationRecord.get(site).getEndtime();
          // Is this site valid for the given sensor record?
          if (!isAssociatedByTime(onDate, offDate, site.getActualChangeTime(), siteEndDate)) {
            continue;
          }

          // Get the channels associated with this site and sort by creation date with
          // newest first.
          List<ReferenceChannel> channels = site2Channels.get(site.getEntityId());
          channels.sort(Comparator.comparing(ReferenceChannel::getActualTime).reversed());

          Instant channelEndDate;
          for (ReferenceChannel channel : channels) {
            channelEndDate = channel2Record.get(channel).getOffdate();
            // Is this channel valid for the given sensor record?
            //if (!compareSite2Station(channel.getActualTime(), channelEndDate, onDate, offDate)) {
            if (!isAssociatedByTime(onDate, offDate, channel.getActualTime(), channelEndDate)) {
              continue;
            }
            UUID channelId = channel.getEntityId();
            ReferenceSensor sensor = ReferenceSensor.from(UUID.randomUUID(), channelId,
                instr.getInsname(), instr.getInstype(), "SNxxxx",
                1, 1, 0, 0,
                record.getTime(), record.getTime(),
                source, "Sensor is associated with channel " + channel.getName());
            if (!isSensorDuplicate(sensor, channel)) {
              this.sensors.add(sensor);
              this.chan2Sensor.put(chanId, sensor);
              this.channel2Sensors.put(channel, sensor);
            }

            ReferenceCalibration calib = ReferenceCalibration.from(
                UUID.randomUUID(), channelId, 0,
                instr.getNcalib(),
                0,
                record.getCalper(),
                record.getTshift(),
                record.getTime(), record.getTime(),  // not known, pass special value
                source,
                "Calibration is associated with channel " + channel.getName());
            if (!isCalibrationDuplicate(calib, channel)) {
              this.calibrations.add(calib);
              this.chan2Calib.put(chanId, calib);
              this.channel2Calibrations.put(channel, calib);
            }

            String dir = instr.getDir();
            if (!dir.endsWith(File.separator)) {
              dir = dir + File.separator;
            }
            String responsePath = dir + instr.getDfile();

            try {
              byte[] responseData = Files.readAllBytes(Paths.get(responsePath));
              ReferenceResponse response = ReferenceResponse.from(UUID.randomUUID(),
                  channelId, instr.getRsptype(), responseData, "UNKNOWN",
                  record.getTime(), record.getTime(), source,
                  "Response associated with channel "
                      + channel.getName());
              if (!isResponseDuplicate(response, channel)) {
                this.responses.add(response);
                this.chan2Response.put(chanId, response);
                this.channel2Responses.put(channel, response);
              }
            } catch (IOException e) {
              this.missingResponseFiles.add(responsePath);
            }

          }
        }
      } // end for loop
    }
  }



  // Compare one entities on and off dates to another.
  private boolean isAssociatedByTime(Instant on1, Instant off1, Instant on2, Instant off2) {

    // If entity one's end date is before entity two was created, then they are not
    // associated.
    if (off1.compareTo(on2) <= 0) {
      return false;
    }

    // If entity one was created after the end date for entity two, then they are
    // not associated.
    if (on1.compareTo(off2) >= 0) {
      return false;
    }
    return true;
  }


  /**
   * Get the list of ReferenceNetwork objects created.
   *
   * @return A list of objects, it may be empty.
   */
  public List<ReferenceNetwork> getReferenceNetworks() {
    return referenceNetworks;
  }

  /**
   * Get the list of ReferenceStation objects created.
   *
   * @return A list of objects, it may be empty.
   */
  public List<ReferenceStation> getReferenceStations() {
    return referenceStations;
  }

  /**
   * Get the list of ReferenceSite objects created.
   *
   * @return A list of objects, it may be empty.
   */
  public List<ReferenceSite> getReferenceSites() {
    return referenceSites;
  }

  /**
   * Get the list of ReferenceChannel objects created.
   *
   * @return A list of objects, it may be empty.
   */
  public List<ReferenceChannel> getReferenceChannels() {
    return referenceChannels;
  }

  /**
   * Get the list of ReferenceResponse objects created.
   *
   * @return A list of objects, it may be empty.
   */
  public List<ReferenceResponse> getResponses() {
    return responses;
  }

  /**
   * Get the list of ReferenceCalibration objects created.
   *
   * @return A list of objects, it may be empty.
   */
  public List<ReferenceCalibration> getCalibrations() {
    return calibrations;
  }

  /**
   * Get the list of ReferenceSensor objects created.
   *
   * @return A list of objects, it may be empty.
   */
  public List<ReferenceSensor> getSensors() {
    return sensors;
  }

  /**
   * Get the set of ReferenceNetworkMembership objects.
   *
   * @return A set of objects, it may be empty.
   */
  public Set<ReferenceNetworkMembership> getReferenceNetworkMemberships() {
    return referenceNetworkMemberships;
  }

  /**
   * Get the set of ReferenceStationMembership objects.
   *
   * @return A set of objects, it may be empty.
   */
  public Set<ReferenceStationMembership> getReferenceStationMemberships() {
    return referenceStationMemberships;
  }

  /**
   * Get the set of ReferenceSiteMembership objects.
   *
   * @return A set of objects, it may be empty.
   */
  public Set<ReferenceSiteMembership> getReferenceSiteMemberships() {
    return referenceSiteMemberships;
  }

  private static <K, T> UUID getIdFor(Multimap<K, T> m, K key, Function<T, UUID> idExtractor) {
    Collection<T> existingEntries = m.get(key);
    if (existingEntries != null && !existingEntries.isEmpty()) {
      return idExtractor.apply(existingEntries.iterator().next());
    } else {
      return UUID.randomUUID();
    }
  }

  private static <K, T> Optional<T> getFirst(Multimap<K, T> m, K key) {
    if (!m.containsKey(key)) {
      return Optional.empty();
    }
    Collection<T> l = m.get(key);
    return l.isEmpty() ? Optional.empty() : Optional.of(l.iterator().next());
  }

  private boolean isNetworkMembershipDuplicate(ReferenceNetworkMembership member) {
    boolean match = false;
    for (ReferenceNetworkMembership item : referenceNetworkMemberships) {
      if (member.getStationId().equals(item.getStationId())
          && member.getNetworkId().equals(item.getNetworkId())
          && member.getActualChangeTime().equals(item.getActualChangeTime())
          && member.getSystemChangeTime().equals(item.getSystemChangeTime())
          && member.getStatus().equals(item.getStatus())
          ) {
        match = true;
        break;
      }
    }
    return match;
  }

  private boolean isStationMembershipDuplicate(ReferenceStationMembership member) {
    boolean match = false;
    for (ReferenceStationMembership item : referenceStationMemberships) {
      if (member.getStationId().equals(item.getStationId())
          && member.getSiteId().equals(item.getSiteId())
          && member.getActualChangeTime().equals(item.getActualChangeTime())
          && member.getSystemChangeTime().equals(item.getSystemChangeTime())
          && member.getStatus().equals(item.getStatus())
          ) {
        match = true;
        break;
      }
    }
    return match;
  }

  private boolean isSiteMembershipDuplicate(ReferenceSiteMembership member) {
    boolean match = false;
    for (ReferenceSiteMembership item : referenceSiteMemberships) {
      if (member.getChannelId().equals(item.getChannelId())
          && member.getSiteId().equals(item.getSiteId())
          && member.getActualChangeTime().equals(item.getActualChangeTime())
          && member.getSystemChangeTime().equals(item.getSystemChangeTime())
          && member.getStatus().equals(item.getStatus())
          ) {
        match = true;
        break;
      }
    }
    return match;
  }

  private boolean isSiteDuplicate(ReferenceSite site) {
    boolean match = false;
    for ( ReferenceSite item : referenceSites ) {
      if ( site.getName().equals(item.getName())
          && site.getActualChangeTime().equals(item.getActualChangeTime())
          && site.getElevation() == item.getElevation()
          && site.getLatitude() == item.getLatitude()
          && site.getLongitude() == item.getLongitude()
          ) {
        match = true;
        break;
      }

    }
    return match;
  }


  private boolean isStationDuplicate(ReferenceStation station) {
    boolean match = false;
    for ( ReferenceStation item : referenceStations ) {
      if ( station.getName().equals(item.getName())
          && station.getActualChangeTime().equals(item.getActualChangeTime())
          && station.getElevation() == item.getElevation()
          && station.getLatitude() == item.getLatitude()
          && station.getLongitude() == item.getLongitude()
          && station.getStationType().equals(item.getStationType())
          ) {
        match = true;
        break;
      }

    }
    return match;
  }

  private boolean isChannelDuplicate(ReferenceSite site, ReferenceChannel channel) {
    boolean match = false;
    for ( ReferenceChannel item : site2Channels.get(site.getEntityId()) ) {
      if ( channel.getName().equals(item.getName())
          && channel.getActualTime().equals(item.getActualTime())
          && channel.getElevation() == item.getElevation()
          && channel.getLatitude() == item.getLatitude()
          && channel.getLongitude() == item.getLongitude()
          && channel.getDepth() == item.getDepth()
          && channel.getVerticalAngle() == item.getVerticalAngle()
          && channel.getHorizontalAngle() == item.getHorizontalAngle()
          && channel.getNominalSampleRate() == item.getNominalSampleRate()
          && channel.getType().equals(item.getType())
          && channel.getDataType().equals(item.getDataType())
          && channel.getComment().equals(item.getComment())
          ) {
        match = true;

        break;
      }

    }
    return match;
  }

  private boolean isSensorDuplicate(ReferenceSensor sensor, ReferenceChannel channel) {
    boolean match = false;
    for ( ReferenceSensor item : channel2Sensors.get(channel) ) {
      if ( channel.getEntityId().equals(item.getChannelId())
          && sensor.getActualTime().equals(item.getActualTime())
          && sensor.getSerialNumber().equals(item.getSerialNumber())
          && sensor.getInstrumentManufacturer().equals(item.getInstrumentManufacturer())
          && sensor.getInstrumentModel().equals(item.getInstrumentModel())
          && sensor.getComment().equals(item.getComment())
          && sensor.getNumberOfComponents() == item.getNumberOfComponents()
          && sensor.getCornerPeriod() == item.getCornerPeriod()
          && sensor.getHighPassband() == item.getHighPassband()
          && sensor.getLowPassband() == item.getLowPassband()
          && sensor.getHighPassband() == item.getHighPassband()
          ) {
        match = true;
        break;
      }
    }
    return match;
  }


  private boolean isCalibrationDuplicate(ReferenceCalibration calib, ReferenceChannel channel) {
    boolean match = false;
    for ( ReferenceCalibration item : channel2Calibrations.get(channel) ) {
      if ( channel.getEntityId().equals(item.getChannelId())
          && calib.getActualTime().equals(item.getActualTime())
          && calib.getComment().equals(item.getComment())
          && calib.getCalibrationFactor() == item.getCalibrationFactor()
          && calib.getCalibrationPeriod() == item.getCalibrationPeriod()
          && calib.getCalibrationInterval() == item.getCalibrationInterval()
          && calib.getCalibrationFactorError() == item.getCalibrationFactorError()
          && calib.getTimeShift() == item.getTimeShift()
          ) {
        match = true;
        break;
      }
    }
    return match;
  }

  private boolean isResponseDuplicate(ReferenceResponse response, ReferenceChannel channel) {
    boolean match = false;
    for ( ReferenceResponse item : channel2Responses.get(channel) ) {
      if ( channel.getEntityId().equals(item.getChannelId())
          && response.getActualTime().equals(item.getActualTime())
          && response.getComment().equals(item.getComment())
          && response.getUnits().equals(item.getUnits())
          && response.getResponseType().equals(item.getResponseType())
          && response.getResponseData().length == item.getResponseData().length
          ) {
        match = true;
        break;
      }
    }
    return match;
  }
}

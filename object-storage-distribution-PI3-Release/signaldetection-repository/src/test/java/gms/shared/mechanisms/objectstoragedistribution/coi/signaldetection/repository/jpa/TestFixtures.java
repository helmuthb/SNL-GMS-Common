package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa;


import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.*;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class TestFixtures {

  static final UUID
      UNKNOWN_UUID = UUID.fromString("515bcbe0-2c0d-48ec-83f5-9f11cfe30318"),
      CALIBRATION_ID = UUID.fromString("ce7c377a-b6a4-478f-b3bd-5c934ee6b7ef"),
      CHANNEL_ID = UUID.fromString("46947cc2-8c86-4fa1-a764-c9b9944614b7"),
      CHANNEL_VERSION_ID = UUID.fromString("27f2b7af-6e16-48e1-98f5-8d935fe02d10"),
      DIGITIZER_ID = UUID.fromString("0be27c41-3c14-479a-8f87-66a05e8b3936"),
      NETWORK_ID = UUID.fromString("407c377a-b6a4-478f-b3cd-5c934ee6b876"),
      NETWORK_VERSION_ID = UUID.fromString("a773aa04-2ed6-40a1-a1e8-0fa8fcd0945b"),
      RESPONSE_ID = UUID.fromString("ce7c377a-b6a4-478f-b3bd-5c934ee6b7ea"),
      SITE_ID = UUID.fromString("36c1ba53-b124-4286-9c2c-72647e749e32"),
      SITE_VERSION_ID = UUID.fromString("494a75ff-109b-4b39-9e13-a96e35726668"),
      STATION_ID = UUID.fromString("ab7c377a-b6a4-478f-b3cd-5c934ee6b879"),
      STATION_VERSION_ID = UUID.fromString("190ef60d-3550-44a8-aac5-249f58d4b08d");

  private static final String comment = "This is a comment.";
  private static final String description = "This is a description.";

  private static final Instant actualTime = Instant.parse("1980-01-02T03:04:05.123Z");
  private static final Instant systemTime = Instant.parse("2010-11-07T06:05:04.321Z");

  private static final InformationSource source = InformationSource.create("Internet",
      actualTime, comment);

  private static final StatusType STATUS = StatusType.ACTIVE;

  // calibrations
  private static final double calibrationFactor = 2.5;
  private static final double calibrationFactorError = 0.9876;
  private static final double calibrationPeriod = 1.0;
  private static final double timeShift = 0.0;
  private static final double calibrationInterval = 1.0;

  static final ReferenceCalibration refCalibration = ReferenceCalibration.from(
      CALIBRATION_ID, CHANNEL_ID, calibrationInterval, calibrationFactor,
      calibrationFactorError, calibrationPeriod, timeShift, actualTime, systemTime,
      source, "comment");
  static final Calibration calibration = Calibration.from(
      CALIBRATION_ID, calibrationFactor,
      calibrationPeriod, calibrationFactorError, timeShift);
  ////////////////////////////////////////////////////////////////////////////////////

  // responses
  private static final byte[] RESPONSE_DATA = new byte[]{
      (byte)1, (byte)2, (byte)3, (byte)4, (byte)5 };
  public static final ReferenceResponse refResponse = ReferenceResponse.from(RESPONSE_ID,
      CHANNEL_ID, "response type", RESPONSE_DATA, "units",
      actualTime, systemTime, source, "comment");
  static final Response response = Response.from(RESPONSE_ID, RESPONSE_DATA);
  ////////////////////////////////////////////////////////////////////////////////////

  // channels and digitizers
  static final String channelName = "CHAN01";
  private static final ChannelType chanType = ChannelType.BROADBAND_HIGH_GAIN_EAST_WEST;
  private static final ChannelDataType dataType = ChannelDataType.HYDROACOUSTIC_ARRAY;
  private static final double
      lat = 12.34, lon = 56.78, elev = 89.90,
      depth = 4321.0, vertAngle = 125.1, horizAngle = 216.2, sampleRate = 40,
      displacementNorth = 2.01, displacementEast = 2.95, displacementVert = 0.56;

  private static final RelativePosition position = RelativePosition.create(
      displacementNorth, displacementEast, displacementVert);

  public  static final ReferenceSiteMembership siteMembership
      = ReferenceSiteMembership.create("", actualTime, SITE_ID, CHANNEL_ID, STATUS);

  static final ReferenceDigitizer refDigitizer = ReferenceDigitizer.create(
      "digitizer name", DigitizerManufacturers.TRIMBLE,
      DigitizerModels.REFTEK, "12345", actualTime,
      source, "comment", "description");

  static final Channel channel = Channel.from(
      CHANNEL_VERSION_ID, channelName, chanType,
      dataType, lat, lon, elev, depth, vertAngle,
      horizAngle, sampleRate, response, calibration);

  static final ReferenceChannel refChannel = ReferenceChannel.from(
      CHANNEL_ID, CHANNEL_VERSION_ID, channelName, chanType, dataType, 0,
      lat, lon, elev, depth, vertAngle,
      horizAngle, sampleRate, actualTime, systemTime, source, comment, position, List.of());

  ////////////////////////////////////////////////////////////////////////////////////

  // sites
  static final String siteName = "SITE33";

  static final Site
      slimSite = Site.from(SITE_VERSION_ID, siteName, lat, lon, elev, List.of()),
      fatSite = Site.from(SITE_VERSION_ID, siteName, lat, lon, elev, List.of(channel));

  public static final ReferenceStationMembership stationMembership
      = ReferenceStationMembership.create("", actualTime, STATION_ID, SITE_ID, STATUS);

  static final ReferenceSite refSite = ReferenceSite.from(
      SITE_ID, SITE_VERSION_ID, siteName,
      description, source, comment, lat, lon, elev, actualTime,
      systemTime, position, List.of());
  ////////////////////////////////////////////////////////////////////////////////////

  // stations
  static final String stationName = "STA01";
  static final Station
      slimStation = Station.from(STATION_VERSION_ID, stationName, lat, lon, elev, List.of()),
      fatStation  = Station.from(STATION_VERSION_ID, stationName, lat, lon, elev, List.of(fatSite));

  public static final ReferenceNetworkMembership networkMembership =
      ReferenceNetworkMembership.create("", actualTime, NETWORK_ID, STATION_ID, STATUS);

  static final ReferenceStation refStation = ReferenceStation.from(
      STATION_ID, STATION_VERSION_ID, stationName, description,
      StationType.Hydroacoustic, source, comment, lat, lon, elev,
      actualTime, systemTime, List.of());
  ////////////////////////////////////////////////////////////////////////////////////

  // networks
  static final String networkName = "NET01";
  private static final NetworkOrganization org = NetworkOrganization.CTBTO;
  private static final NetworkRegion region = NetworkRegion.GLOBAL;

  static final Network
      slimNetwork = Network.from(NETWORK_VERSION_ID, networkName, org, region, List.of()),
      fatNetwork  = Network.from(NETWORK_VERSION_ID, networkName, org, region, List.of(fatStation));

  static final ReferenceNetwork refNetwork = ReferenceNetwork.from(
      NETWORK_ID, NETWORK_VERSION_ID, networkName, description, org, region, source, comment,
      actualTime, systemTime);
  ////////////////////////////////////////////////////////////////////////////////////
}

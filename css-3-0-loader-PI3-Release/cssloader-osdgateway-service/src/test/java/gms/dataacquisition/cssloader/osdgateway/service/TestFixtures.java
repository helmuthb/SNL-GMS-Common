package gms.dataacquisition.cssloader.osdgateway.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkOrganization;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkRegion;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceAlias;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceCalibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceDigitizer;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceDigitizerMembership;
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
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Contains test data used by the unit tests.
 */
public class TestFixtures {

  public static final double SAMPLE_RATE = 2.0;
  public static final int SAMPLE_COUNT = 5;
  private static final long segmentLengthMillis = 2000;

  public static final UUID
      CHANNEL_SEGMENT_ID = UUID.fromString("57015315-f7b2-4487-b3e7-8780fbcfb413"),
      PROCESSING_CHANNEL_ID = UUID.fromString("46947cc2-8c86-4fa1-a764-c9b9944614b7");

  public static final UUID TEST_UUID = UUID.fromString("e922a49a-f410-4162-8628-2ef8e1926298");

  public static final String segmentStartDateString = "1970-01-02T03:04:05.123456Z";

  public static final Instant SEGMENT_START = Instant.parse(segmentStartDateString);

  public static final Instant SEGMENT_END = SEGMENT_START.plusMillis(segmentLengthMillis);

  public static final double[] WAVEFORM_POINTS = new double[]{1.1, 2.2, 3.3, 4.4, 5.5};

  public static final SoftwareComponentInfo softwareComponentInfo = new SoftwareComponentInfo(
      "unit test component name",
      "unit test component version");

  public static final CreationInfo creationInfo = new CreationInfo(
      "unit test creator name",
      softwareComponentInfo);

  public static final Waveform waveform1 = Waveform.create(
      SEGMENT_START, SEGMENT_END, SAMPLE_RATE, SAMPLE_COUNT, WAVEFORM_POINTS);

  public static final SortedSet<Waveform> waveforms = new TreeSet<>(Set.of(waveform1));

  public static final ChannelSegment channelSegment = ChannelSegment.from(
      CHANNEL_SEGMENT_ID, PROCESSING_CHANNEL_ID, "segmentName",
      ChannelSegment.ChannelSegmentType.RAW,
      SEGMENT_START, SEGMENT_END, waveforms, creationInfo);

  public static final AcquiredChannelSohBoolean acquiredChannelSohBoolean = AcquiredChannelSohBoolean
      .from(
          TEST_UUID, PROCESSING_CHANNEL_ID,
          AcquiredChannelSoh.AcquiredChannelSohType.DIGITIZING_EQUIPMENT_OPEN,
          SEGMENT_START, SEGMENT_END, true, creationInfo);


  public static final String channelName = "CHAN01"; // when stored it should be uppercase
  public static final String comment = "This is a comment.";
  public static final String description = "This is a description.";
  public static final Instant actualTime = Instant.parse("1980-01-02T03:04:05.123Z");
  public static final Instant systemTime = Instant.parse("2010-01-02T03:04:05.123Z");

  public static final ReferenceAlias stationAlias;
  public static final ReferenceAlias siteAlias;
  public static final ReferenceAlias channelAlias;

  public static final List<ReferenceAlias> stationAliases = new ArrayList<>();
  public static final List<ReferenceAlias> siteAliases = new ArrayList<>();
  public static final List<ReferenceAlias> channelAliases = new ArrayList<>();


  public static final InformationSource source = InformationSource.create("Internet",
      actualTime, comment);

  public static final String aliasName1 = "StationAlias";
  public static final String aliasName2 = "SiteAlias";
  public static final String aliasName3 = "ChannelAlias";

  public static final double latitude = -13.56789;
  public static final double longitude = 89.04123;
  public static final double elevation = 376.43;

  public static final double displacementNorth = 2.01;
  public static final double displacementEast = 2.95;
  public static final double displacementVertical = 0.56;

  public static final RelativePosition position = RelativePosition.create(displacementNorth,
      displacementEast, displacementVertical);


  public static Set<ReferenceNetworkMembership> networkMemberships = new HashSet<ReferenceNetworkMembership>();
  public static Set<ReferenceStationMembership> stationMemberships = new HashSet<ReferenceStationMembership>();
  public static Set<ReferenceSiteMembership> siteMemberships = new HashSet<ReferenceSiteMembership>();
  public static Set<ReferenceDigitizerMembership> digitizers2Channels = new HashSet<ReferenceDigitizerMembership>();

  // Create a ReferenceNetwork
  public static final String networkName = "NET01"; // when stored it should be uppercase
  public static final NetworkOrganization networkOrg = NetworkOrganization.USGS;
  public static final NetworkRegion networkRegion = NetworkRegion.REGIONAL;
  public static final ReferenceNetwork network = ReferenceNetwork.create(networkName, description,
      networkOrg, networkRegion, source, comment, actualTime);

  // Create a ReferenceStation
  public static final String stationName = "STATION01"; // when stored it should be uppercase
  public static final StationType stationType = StationType.Hydroacoustic;
  public static final ReferenceStation station = ReferenceStation.create(stationName,
      description, stationType, source, comment, latitude, longitude, elevation, actualTime,
      stationAliases);

  // Create a ReferenceSite
  public static final String siteName = "SITE01"; // when stored it should be uppercase
  public static final ReferenceSite site = ReferenceSite.create(siteName, description,
      source, comment, latitude, longitude, elevation, actualTime, position,
      siteAliases);

  // Create a ReferenceChannel
  public static final ChannelType channelType = ChannelType.BROADBAND_HIGH_GAIN_EAST_WEST;
  public static final ChannelDataType channelDataType = ChannelDataType.SEISMIC_3_COMPONENT;
  public static final int locationCode = 23;
  public static final double depth = 12.943;
  public static final double verticalAngle = 1.005;
  public static final double horizontalAngle = 3.66;
  public static final double nominalSampleRate = 40.0;
  public static final List<ReferenceCalibration> calibrations = new ArrayList<>();
  public static final List<ReferenceResponse> responses = new ArrayList<>();
  public static final List<ReferenceSensor> sensors = new ArrayList<>();
  public static final List<ReferenceDigitizerMembership> digitizers = new ArrayList<>();
  public static final List<ReferenceAlias> aliases = new ArrayList<>();

  public static final ReferenceChannel channel = ReferenceChannel.create(channelName,
      channelType,
      channelDataType,
      locationCode,
      latitude,
      longitude,
      elevation,
      depth,
      verticalAngle,
      horizontalAngle,
      nominalSampleRate,
      actualTime,
      source,
      comment,
      position,
      aliases);

  // Create a ReferenceSensor
  public static final String instrumentManufacturer = "Geotech_Instruments_LLC";
  public static final String instrumentModel = "GS_13";
  public static final String serialNumber = "S1234-00";
  public static final int numberOfComponents = 2;
  public static final double cornerPeriod = 3.0;
  public static final double lowPassband = 1.0;
  public static final double highPassband = 5.0;
  public static final ReferenceSensor sensor = ReferenceSensor.create(
      channel.getEntityId(), instrumentManufacturer, instrumentModel, serialNumber,
      numberOfComponents, cornerPeriod, lowPassband, highPassband, actualTime, source, comment);


  // Create a ReferenceResponse
  public static final String responseType = "PAZFIR";
  public static final byte[] responseData = "kt0naPqwrtoij2541akAx".getBytes();
  public static final String responseUnits = "millimeters";
  public static final ReferenceResponse response = ReferenceResponse.create(
      channel.getEntityId(), responseType, responseData, responseUnits, actualTime, source, comment);

  // Create a ReferenceCalibration
  public static final double calibrationInterval = 3.0;
  public static final double calibrationFactor = 2.5;
  public static final double calibrationFactorError = 0.9876;
  public static final double calibrationPeriod = 1.0;
  public static final double calibrationTimeShift = 0.0;
  public static final ReferenceCalibration calibration = ReferenceCalibration.create(
      channel.getEntityId(), calibrationInterval, calibrationFactor, calibrationFactorError,
      calibrationPeriod, calibrationTimeShift, actualTime, source, comment);

  // Create a ReferenceDigitizer
  public static final String digitName = "DIGITIZER-063";
  public static final String digitManufacturer = "TRIMBLE";
  public static final String digitModel = "REFTEK";
  public static final String digitSerial = "124590B";
  public static final String digitComment = "Digitizer comment";

  public static final ReferenceNetworkMembership netMember = ReferenceNetworkMembership.from(
      UUID.randomUUID(), "Testing",
      actualTime, systemTime, network.getEntityId(), station.getEntityId(), StatusType.ACTIVE);

  public static final ReferenceStationMembership stationMember = ReferenceStationMembership.from(
      UUID.randomUUID(), "Testing",
      actualTime, systemTime, station.getEntityId(), site.getEntityId(), StatusType.ACTIVE);


  public static final ReferenceSiteMembership siteMember = ReferenceSiteMembership.from(
      UUID.randomUUID(), "Testing",
      actualTime, systemTime, site.getEntityId(), PROCESSING_CHANNEL_ID, StatusType.ACTIVE);

  public static final ReferenceDigitizerMembership digitizerMember = ReferenceDigitizerMembership.from(
      UUID.randomUUID(), "Testing",
      actualTime, systemTime, PROCESSING_CHANNEL_ID, channel.getEntityId(), StatusType.ACTIVE);

  static {
    networkMemberships.add(netMember);
    stationMemberships.add(stationMember);
    siteMemberships.add(siteMember);
    digitizers2Channels.add(digitizerMember);
    calibrations.add(calibration);
    responses.add(response);
    sensors.add(sensor);

    stationAlias = ReferenceAlias.create(aliasName1,
        StatusType.ACTIVE, comment, actualTime, systemTime);

    siteAlias = ReferenceAlias.create(aliasName2,
        StatusType.ACTIVE, comment, actualTime, systemTime);

    channelAlias = ReferenceAlias.create(aliasName3,
        StatusType.ACTIVE, comment, actualTime, systemTime);

    stationAliases.add(stationAlias);
    siteAliases.add(siteAlias);
    channelAliases.add(channelAlias);
  }
}

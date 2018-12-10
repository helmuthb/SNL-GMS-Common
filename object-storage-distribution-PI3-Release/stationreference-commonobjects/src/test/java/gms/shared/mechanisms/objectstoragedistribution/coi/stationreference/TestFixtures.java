package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects.ProvenanceJacksonMixins;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.DigitizerManufacturers;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.DigitizerModels;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.InstrumentManufacturers;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.InstrumentModels;
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
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ResponseTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StatusType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects.ReferenceJacksonMixins;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestFixtures {

  public static final String comment = "This is a comment.";
  public static final String description = "This is a description.";
  public static final Instant actualTime = Instant.parse("1980-01-02T03:04:05.123Z");
  public static final Instant systemTime = Instant.parse("2010-11-07T06:05:04.321Z");
  public static final StatusType status = StatusType.ACTIVE;

  public static final UUID networkId = UUID.fromString("0332f98e-ff83-4f3d-a832-a82a04022144");
  public static final UUID networkVersionId = UUID.fromString("9c00c874-650c-479f-90be-cfd3995c3e6d");
  public static final UUID stationId = UUID.fromString("4712f988-cc83-4f3d-a832-a82a04022100");
  public static final UUID siteId = UUID.fromString("0152f98e-ff83-4f3d-a832-a82a04022978");
  public static final UUID digitizerId = UUID.fromString("3712f9de-ff83-4f3d-a832-a82a04067001");
  public static final UUID channelId = UUID.fromString("f890198e-ff83-4f3d-a832-a82a04022002");
  public static final UUID calibrationId = UUID.fromString("aaa0198e-ff83-4f3d-a832-a82a04022000");
  public static final UUID responseId = UUID.fromString("fdf0198e-ff83-4f3d-a832-a82a04022aba");
  public static final UUID sensorId = UUID.fromString("1230198e-ff83-4f3d-a832-a82a04022321");

  public static final ReferenceAlias stationAlias = ReferenceAlias.create(
      "StationAlias", StatusType.ACTIVE, comment, actualTime, systemTime);
  public static final ReferenceAlias siteAlias = ReferenceAlias.create(
      "SiteAlias", StatusType.ACTIVE, comment, actualTime, systemTime);
  public static final ReferenceAlias channelAlias = ReferenceAlias.create(
      "ChannelAlias", StatusType.ACTIVE, comment, actualTime, systemTime);
  public static final List<ReferenceAlias> stationAliases = List.of(stationAlias);
  public static final List<ReferenceAlias> siteAliases = List.of(siteAlias);
  public static final List<ReferenceAlias> channelAliases = List.of(channelAlias);

  public static final InformationSource source = InformationSource.create("Internet",
      actualTime, comment);

  public static final double latitude = -13.56789;
  public static final double longitude = 89.04123;
  public static final double elevation = 376.43;

  public static final double precision = 0.00001;

  //////////////////////////////////////////////////////////

  // Create a ReferenceNetwork
  public static final String networkName = "NET01"; // when stored it should be uppercase
  public static final NetworkOrganization networkOrg = NetworkOrganization.ORG;
  public static final NetworkRegion networkRegion = NetworkRegion.REGIONAL;
  public static final ReferenceNetwork network = ReferenceNetwork.create(networkName, description,
      networkOrg, networkRegion, source, comment, actualTime);

  // Create a ReferenceStation
  public static final String stationName = "STATION01"; // when stored it should be uppercase
  public static final StationType stationType = StationType.Hydroacoustic;
  public static final ReferenceStation station = ReferenceStation.create(stationName,
      description, stationType, source, comment, latitude, longitude, elevation, actualTime,
      stationAliases);

  // Create a RelativePosition
  public static final double displacementNorth = 2.01;
  public static final double displacementEast = 2.95;
  public static final double displacementVertical = 0.56;
  public static final RelativePosition position = RelativePosition.create(displacementNorth,
      displacementEast, displacementVertical);

  // Create a ReferenceSite
  public static final String siteName = "SITE01"; // when stored it should be uppercase
  public static final ReferenceSite site = ReferenceSite.create(siteName, description,
      source, comment, latitude, longitude, elevation, actualTime, position,
      siteAliases);

  // Create a ReferenceDigitizer
  public static final String digitName = "digitizer name";
  public static final String digitManufacturer = DigitizerManufacturers.TRIMBLE;
  public static final String digitModel = DigitizerModels.REFTEK;
  public static final String digitSerial = "124590B";
  public static final String digitComment = "Digitizer comment";
  public static final ReferenceDigitizer digitizer = ReferenceDigitizer.create(
      digitName, digitManufacturer, digitModel, digitSerial, actualTime, source,
      description, digitComment);

  // Create a ReferenceChannel
  public static final String channelName = "CHAN01"; // when stored it should be uppercase
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

  // Create a Channel ReferenceSensor
  public static final String instrumentManufacturer = InstrumentManufacturers.Geotech_Instruments_LLC;
  public static final String instrumentModel = InstrumentModels.GS_13;
  public static final String serialNumber = "S1234-00";
  public static final int numberOfComponents = 2;
  public static final double cornerPeriod = 3.0;
  public static final double lowPassband = 1.0;
  public static final double highPassband = 5.0;
  public static final ReferenceSensor sensor = ReferenceSensor.create(channel.getEntityId(),
      instrumentManufacturer, instrumentModel, serialNumber, numberOfComponents,
      cornerPeriod, lowPassband, highPassband, actualTime, source, comment);

  // Create a Channel ReferenceResponse
  public static final String responseType = ResponseTypes.PAZFIR;
  public static final byte[] responseData = "kt0naPqwrtoij2541akAx".getBytes();
  public static final String responseUnits = "millimeters";
  public static final ReferenceResponse response = ReferenceResponse.create(
      channel.getEntityId(), responseType, responseData, responseUnits, actualTime, source, comment);

  // Create a Channel ReferenceCalibration
  public static final double calibrationInterval = 3.0;
  public static final double calibrationFactor = 2.5;
  public static final double calibrationFactorError = 0.9876;
  public static final double calibrationPeriod = 1.0;
  public static final double calibrationTimeShift = 0.0;
  public static final ReferenceCalibration calibration = ReferenceCalibration.create(
      channel.getEntityId(), calibrationInterval, calibrationFactor, calibrationFactorError,
      calibrationPeriod, calibrationTimeShift, actualTime, source, comment);

  public static final ReferenceNetworkMembership netMember = ReferenceNetworkMembership.create("Testing",
      actualTime, network.getEntityId(), station.getEntityId(), status);

  public static final ReferenceStationMembership stationMember = ReferenceStationMembership.create("Testing",
      actualTime, station.getEntityId(), site.getEntityId(), status);


  public static final ReferenceSiteMembership siteMember = ReferenceSiteMembership.create("Testing",
      actualTime, site.getEntityId(), channel.getEntityId(), status);

  public static final ReferenceDigitizerMembership digitizerMember = ReferenceDigitizerMembership.create("Testing",
      actualTime, digitizerId, channel.getEntityId(), status);

  public static final ObjectMapper objMapper = new ObjectMapper();

  static {
    ReferenceJacksonMixins.register(objMapper);
    ProvenanceJacksonMixins.register(objMapper);
    objMapper.findAndRegisterModules();
    objMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    calibrations.add(calibration);
    responses.add(response);
    sensors.add(sensor);
  }
}

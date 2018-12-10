package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.testUtilities;

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
import java.util.List;

public class TestFixtures {

  private static final InformationSource source = InformationSource.create(
      "originating org", Instant.EPOCH, "reference");

  public static final String comment = "This is a comment.";
  public static final String description = "This is a description.";
  public static final Instant actualTime = Instant.parse("1980-01-02T03:04:05.123Z");
  public static final Instant actualTime2 = actualTime.plusSeconds(60);
  public static final Instant actualTime3 = actualTime2.plusSeconds(60);
  public static final Instant actualTime4 = actualTime3.plusSeconds(60);
  public static final Instant systemTime = Instant.parse("2010-11-07T06:05:04.321Z");
  public static final StatusType status = StatusType.ACTIVE;

  public static final ReferenceAlias stationAlias = ReferenceAlias.create(
      "StationAlias", StatusType.ACTIVE, comment, actualTime, systemTime);
  public static final ReferenceAlias siteAlias = ReferenceAlias.create(
      "SiteAlias", StatusType.ACTIVE, comment, actualTime, systemTime);
  public static final ReferenceAlias channelAlias = ReferenceAlias.create(
      "ChannelAlias", StatusType.ACTIVE, comment, actualTime, systemTime);

  public static final List<ReferenceAlias> stationAliases = List.of(stationAlias);
  public static final List<ReferenceAlias> siteAliases = List.of(siteAlias);
  public static final List<ReferenceAlias> channelAliases = List.of(channelAlias);

  public static final double latitude = -13.56789;
  public static final double longitude = 89.04123;
  public static final double elevation = 376.43;

  //////////////////////////////////////////////////////////

  // Create a ReferenceNetwork
  public static final String networkName = "NET01", networkName2 = "NET02";
  public static final NetworkOrganization networkOrg = NetworkOrganization.ORG;
  public static final NetworkRegion networkRegion = NetworkRegion.REGIONAL;
  public static final ReferenceNetwork network = ReferenceNetwork.create(networkName, description,
      networkOrg, networkRegion, source, comment, actualTime);
  public static final ReferenceNetwork network_v2 = ReferenceNetwork.createNewVersion(
      network.getEntityId(), networkName, description, networkOrg,
      networkRegion, source, comment, actualTime3);
  public static final ReferenceNetwork network2 = ReferenceNetwork.create(networkName2, description,
      networkOrg, networkRegion, source, comment, actualTime2);
  public static final ReferenceNetwork network2_v2 = ReferenceNetwork.createNewVersion(
      network2.getEntityId(), networkName2, description, networkOrg,
      networkRegion, source, comment, actualTime4);
  public static final List<ReferenceNetwork> allNetworks = List.of(
      network, network2, network_v2, network2_v2);

  // Create a ReferenceStation
  public static final String stationName = "STATION01", stationName2 = "STATION02";
  public static final StationType stationType = StationType.Hydroacoustic;
  public static final ReferenceStation station = ReferenceStation.create(stationName,
      description, stationType, source, comment, latitude, longitude, elevation, actualTime,
      stationAliases);
  public static final ReferenceStation station2 = ReferenceStation.create(stationName2,
      description, stationType, source, comment, latitude + 1, longitude + 1,
      elevation, actualTime2, stationAliases);
  public static final List<ReferenceStation> allStations = List.of(station, station2);

  // Create a RelativePosition
  public static final double displacementNorth = 2.01;
  public static final double displacementEast = 2.95;
  public static final double displacementVertical = 0.56;
  public static final RelativePosition position = RelativePosition.create(displacementNorth,
      displacementEast, displacementVertical);

  // Create a ReferenceSite
  public static final String siteName = "SITE01", siteName2 = "SITE02";
  public static final ReferenceSite site = ReferenceSite.create(siteName, description,
      source, comment, latitude, longitude, elevation, actualTime, position, siteAliases);
  public static final ReferenceSite site2 = ReferenceSite.create(siteName2, description,
      source, comment, latitude + 1, longitude + 1, elevation + 1,
      actualTime2, position, siteAliases);
  public static final List<ReferenceSite> allSites = List.of(site, site2);

  // Create a ReferenceDigitizer
  public static final String digitName = "digitizer", digitName2 = "digitizer2";
  public static final String digitManufacturer = DigitizerManufacturers.TRIMBLE;
  public static final String digitModel = DigitizerModels.REFTEK;
  public static final String digitSerial = "124590B";
  public static final String digitComment = "Digitizer comment";
  public static final ReferenceDigitizer digitizer = ReferenceDigitizer.create(
      digitName, digitManufacturer, digitModel, digitSerial, actualTime, source,
      description, digitComment);
  public static final ReferenceDigitizer digitizer2 = ReferenceDigitizer.create(
      digitName2, digitManufacturer, digitModel, digitSerial, actualTime2, source,
      description, digitComment);
  public static final List<ReferenceDigitizer> allDigitizers = List.of(digitizer, digitizer2);

 // Create ReferenceChannels
  public static final String channelName = "CHAN01", channelName2 = "CHAN02";
  public static final ChannelType channelType = ChannelType.BROADBAND_HIGH_GAIN_EAST_WEST;
  public static final ChannelDataType channelDataType = ChannelDataType.SEISMIC_3_COMPONENT;
  public static final int locationCode = 23;
  public static final double depth = 12.943;
  public static final double verticalAngle = 1.005;
  public static final double horizontalAngle = 3.66;
  public static final double nominalSampleRate = 40.0;

  public static final ReferenceChannel channel = ReferenceChannel.create(channelName,
      channelType, channelDataType, locationCode, latitude, longitude, elevation,
      depth, verticalAngle, horizontalAngle, nominalSampleRate, actualTime,
      source, comment, position, channelAliases);
  public static final ReferenceChannel channel2 = ReferenceChannel.create(channelName2,
      channelType, channelDataType, locationCode, latitude+1, longitude+1,
      elevation, depth, verticalAngle, horizontalAngle, nominalSampleRate, actualTime2,
      source, comment, position, channelAliases);
  public static final List<ReferenceChannel> allChannels = List.of(channel, channel2);

  // Create a Channel ReferenceSensor
  public static final String instrumentManufacturer = InstrumentManufacturers.Geotech_Instruments_LLC;
  public static final String instrumentModel = InstrumentModels.GS_13;
  public static final String serialNumber = "S1234-00";
  public static final int numberOfComponents = 2;
  public static final double cornerPeriod = 3.0;
  public static final double lowPassband = 1.0;
  public static final double highPassband = 5.0;
  public static final ReferenceSensor sensor_chan1_v1 = ReferenceSensor.create(
      channel.getEntityId(), instrumentManufacturer,
      instrumentModel, serialNumber, numberOfComponents, cornerPeriod, lowPassband, highPassband,
      actualTime, source, comment  );
  public static final ReferenceSensor sensor_chan1_v2 = ReferenceSensor.create(
      channel.getEntityId(), instrumentManufacturer,
      instrumentModel, serialNumber, numberOfComponents, cornerPeriod, lowPassband, highPassband,
      actualTime2, source, comment  );
  public static final ReferenceSensor sensor_chan2_v1 = ReferenceSensor.create(
      channel2.getEntityId(), instrumentManufacturer,
      instrumentModel, serialNumber, numberOfComponents, cornerPeriod, lowPassband, highPassband,
      actualTime3, source, comment  );
  public static final ReferenceSensor sensor_chan2_v2 = ReferenceSensor.create(
      channel2.getEntityId(), instrumentManufacturer,
      instrumentModel, serialNumber, numberOfComponents, cornerPeriod, lowPassband, highPassband,
      actualTime4, source, comment  );
  public static final List<ReferenceSensor> chan1_sensors = List.of(sensor_chan1_v1, sensor_chan1_v2);
  public static final List<ReferenceSensor> chan2_sensors = List.of(sensor_chan2_v1, sensor_chan2_v2);
  public static final List<ReferenceSensor> allSensors = List.of(
      sensor_chan1_v1, sensor_chan1_v2, sensor_chan2_v1, sensor_chan2_v2);

  // Create a Channel ReferenceResponse
  public static final String responseType = ResponseTypes.PAZFIR;
  public static final byte[] responseData = "kt0naPqwrtoij2541akAx".getBytes();
  public static final String responseUnits = "millimeters";
  public static final ReferenceResponse response_chan1_v1 = ReferenceResponse.create(
      channel.getEntityId(), responseType,
      responseData, responseUnits, actualTime, source, comment);
  public static final ReferenceResponse response_chan1_v2 = ReferenceResponse.create(
      channel.getEntityId(), responseType,
      responseData, responseUnits, actualTime2, source, comment);
  public static final ReferenceResponse response_chan2_v1 = ReferenceResponse.create(
      channel2.getEntityId(), responseType,
      responseData, responseUnits, actualTime3, source, comment);
  public static final ReferenceResponse response_chan2_v2 = ReferenceResponse.create(
      channel2.getEntityId(), responseType,
      responseData, responseUnits, actualTime4, source, comment);
  public static final List<ReferenceResponse> chan1_responses = List.of(
      response_chan1_v1, response_chan1_v2);
  public static final List<ReferenceResponse> chan2_responses = List.of(
      response_chan2_v1, response_chan2_v2);
  public static final List<ReferenceResponse> allResponses = List.of(
      response_chan1_v1, response_chan1_v2, response_chan2_v1, response_chan2_v2);

  // Create a Channel ReferenceCalibration
  public static final double calibrationInterval = 3.0;
  public static final double calibrationFactor = 2.5;
  public static final double calibrationFactorError = 0.9876;
  public static final double calibrationPeriod = 1.0;
  public static final double calibrationTimeShift = 0.0;
  public static final ReferenceCalibration calibration_chan1_v1 = ReferenceCalibration.create(
      channel.getEntityId(), calibrationInterval,
      calibrationFactor, calibrationFactorError, calibrationPeriod, calibrationTimeShift,
      actualTime, source, comment);
  public static final ReferenceCalibration calibration_chan1_v2 = ReferenceCalibration.create(
      channel.getEntityId(), calibrationInterval,
      calibrationFactor, calibrationFactorError, calibrationPeriod, calibrationTimeShift,
      actualTime2, source, comment);
  public static final ReferenceCalibration calibration_chan2_v1 = ReferenceCalibration.create(
      channel2.getEntityId(), calibrationInterval,
      calibrationFactor, calibrationFactorError, calibrationPeriod, calibrationTimeShift,
      actualTime3, source, comment);
  public static final ReferenceCalibration calibration_chan2_v2 = ReferenceCalibration.create(
      channel2.getEntityId(), calibrationInterval,
      calibrationFactor, calibrationFactorError, calibrationPeriod, calibrationTimeShift,
      actualTime4, source, comment);
  public static final List<ReferenceCalibration> chan1_calibrations = List.of(
      calibration_chan1_v1, calibration_chan1_v2);
  public static final List<ReferenceCalibration> chan2_calibrations = List.of(
      calibration_chan2_v1, calibration_chan2_v2);
  public static final List<ReferenceCalibration> allCalibrations = List.of(calibration_chan1_v1,
      calibration_chan1_v2, calibration_chan2_v1, calibration_chan2_v2);

  public static final ReferenceNetworkMembership netMember =
      ReferenceNetworkMembership.create(comment, actualTime, network.getEntityId(),
          station.getEntityId(), status);
  public static final ReferenceNetworkMembership netMember2 =
      ReferenceNetworkMembership.create(comment, actualTime2, network2.getEntityId(),
          station2.getEntityId(), status);
  public static final List<ReferenceNetworkMembership> allNetworkMemberships
      = List.of(netMember, netMember2);

  public static final ReferenceStationMembership stationMember =
      ReferenceStationMembership.create(comment, actualTime, station.getEntityId(),
          site.getEntityId(), status);
  public static final ReferenceStationMembership stationMember2 =
      ReferenceStationMembership.create(comment, actualTime2, station2.getEntityId(),
          site2.getEntityId(), status);
  public static final List<ReferenceStationMembership> allStationMemberships
      = List.of(stationMember, stationMember2);

  public static final ReferenceSiteMembership siteMember
      = ReferenceSiteMembership.create(comment, actualTime, site.getEntityId(),
      channel.getEntityId(), status);
  public static final ReferenceSiteMembership siteMember2
      = ReferenceSiteMembership.create(comment, actualTime2, site2.getEntityId(),
      channel2.getEntityId(), status);
  public static final List<ReferenceSiteMembership> allSiteMemberships
      = List.of(siteMember, siteMember2);

  public static final ReferenceDigitizerMembership digitizerMember =
      ReferenceDigitizerMembership.create(comment, actualTime, digitizer.getEntityId(),
          channel.getEntityId(), status);
  public static final ReferenceDigitizerMembership digitizerMember2 =
      ReferenceDigitizerMembership.create(comment, actualTime2, digitizer2.getEntityId(),
          channel2.getEntityId(), status);
  public static final List<ReferenceDigitizerMembership> allDigitizerMemberships
      = List.of(digitizerMember, digitizerMember2);

  ///////////////////////////////////////////////////////////////
  public static final ObjectMapper objectMapper = new ObjectMapper();

  static {
    ReferenceJacksonMixins.register(objectMapper);
    ProvenanceJacksonMixins.register(objectMapper);
    objectMapper.findAndRegisterModules();
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

}

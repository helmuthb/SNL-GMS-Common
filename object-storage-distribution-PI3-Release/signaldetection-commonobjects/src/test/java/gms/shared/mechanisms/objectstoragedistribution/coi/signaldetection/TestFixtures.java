package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.*;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.SignalDetectionJacksonMixins;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkOrganization;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkRegion;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


/**
 * Defines objects used in testing
 */
public class TestFixtures {

  public static final ObjectMapper objMapper = new ObjectMapper();

  static {
    SignalDetectionJacksonMixins.register(objMapper);
    objMapper.findAndRegisterModules();
    objMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  // QcMask Related Test Fixtures

  public static final QcMask qcMask;
  public static final QcMaskVersion qcMaskVersion;
  public static final QcMaskVersionReference qcMaskVersionReference;

  static {
    qcMask = QcMask.create(
        UUID.randomUUID(),
        List.of(
            QcMaskVersionReference.from(UUID.randomUUID(), 3),
            QcMaskVersionReference.from(UUID.randomUUID(), 1)),
        List.of(UUID.randomUUID(), UUID.randomUUID()),
        QcMaskCategory.WAVEFORM_QUALITY,
        QcMaskType.LONG_GAP,
        "Rationale",
        Instant.now(),
        Instant.now().plusSeconds(2),
        UUID.randomUUID());

    qcMask.addQcMaskVersion(
        List.of(UUID.randomUUID(), UUID.randomUUID()),
        QcMaskCategory.WAVEFORM_QUALITY,
        QcMaskType.SPIKE,
        "Rationale SPIKE",
        Instant.now().plusSeconds(3),
        Instant.now().plusSeconds(4),
        UUID.randomUUID());

    qcMaskVersion = qcMask.getCurrentQcMaskVersion();

    qcMaskVersionReference = qcMaskVersion.getParentQcMasks().iterator().next();
  }

  // Processing Station Reference Test Fixtures

  public static final double lat = 67.00459;
  public static final double lon = -103.00459;
  public static final double elev = 13.05;
  public static final double depth = 6.899;
  public static final double verticalAngle = 3.4;
  public static final double horizontalAngle = 5.7;
  public static final double sampleRate = 60;

  // Create a Response
  public static final UUID responseID = UUID.fromString("cccaa77a-b6a4-478f-b3cd-5c934ee6b999");
  public static final byte[] responseData = "2M64390-amYq45pi5qag".getBytes();
  public static final Response response = Response.from(responseID, responseData);

  // Create a Calibration
  public static final UUID calibID = UUID.fromString("5432a77a-b6a4-478f-b3cd-5c934ee6b000");
  public static final double factor = 1.2;
  public static final double factorError = 0.112;
  public static final double period = 14.5;
  public static final double timeShift = 2.24;
  public static final Calibration calibration = Calibration.from(calibID, factor, period,
      factorError, timeShift);


  // Create a Channel
  public static final UUID channelID = UUID.fromString("d07aa77a-b6a4-478f-b3cd-5c934ee6b812");
  public static final String channelName = "CHAN01";
  public static final ChannelType channelType = ChannelType.BROADBAND_HIGH_GAIN_EAST_WEST;
  public static final ChannelDataType channelDataType = ChannelDataType.SEISMIC_3_COMPONENT;
  public static final Channel channel = Channel.from(channelID, channelName, channelType,
      channelDataType, lat, lon, elev, depth, verticalAngle, horizontalAngle, sampleRate,
      response, calibration);

  // Create a Site
  public static final UUID siteID = UUID.fromString("ab7c377a-b6a4-478f-b3cd-5c934ee6b879");
  public static final String siteName = "SITE01";
  public static final  List<Channel> channels = new ArrayList<Channel>();
  public static final Site site = Site.from(siteID, siteName, lat, lon, elev,
      channels);


  // Create Network
  public static final UUID networkID = UUID.fromString("407c377a-b6a4-478f-b3cd-5c934ee6b876");
  public static final String networkName = "Net01";
  public static final  List<Station> stations = new ArrayList<Station>();
  public static final Network network = Network.from(networkID, networkName,
      NetworkOrganization.CTBTO, NetworkRegion.GLOBAL, stations);


  // Create a Station
  public static final UUID stationID = UUID.fromString("ab7c377a-b6a4-478f-b3cd-5c934ee6b879");
  public static final String stationName = "STATION01";
  public static final  List<Site> sites = new ArrayList<Site>();

  public static final Station station = Station.from(stationID, stationName, lat, lon, elev,
      sites);

  static {
    sites.add(site);
    channels.add(channel);
    stations.add(station);
  }


  // Everything below will be removed later.

  // ProcessingCalibration
  public static final UUID PROCESSING_CALIBRATION_1_ID = UUID
      .fromString("ce7c377a-b6a4-478f-b3bd-5c934ee6b7ef");

  public static final Calibration processingCalibration1 =
      Calibration.from(PROCESSING_CALIBRATION_1_ID, 1.4, 1.0, 1.1, 1.1);

  public static final UUID PROCESSING_CALIBRATION_2_ID = UUID
      .fromString("cb3bffe0-553e-4398-b2a7-4026699ae9f2");

  public static final Calibration processingCalibration2 =
      Calibration.from(PROCESSING_CALIBRATION_2_ID, 2.4, 2.0, 2.1, 2.1);


  // ProcessingChannel
  public static final UUID PROCESSING_CHANNEL_1_ID = UUID
      .fromString("46947cc2-8c86-4fa1-a764-c9b9944614b7");

  public static final Channel processingChannel01 = Channel.from(
      PROCESSING_CHANNEL_1_ID, "CHAN01", ChannelType.BROADBAND_HIGH_GAIN_VERTICAL,
      ChannelDataType.HYDROACOUSTIC_ARRAY, 12.34, 45.67, 1001.0,
      123.0, 180.0, 90.0, 40.0,
      response, processingCalibration1);

  public static final UUID PROCESSING_CHANNEL_2_ID = UUID
      .fromString("2bc8381f-8443-443a-83c8-cbbbe29ed796");

  public static final Channel processingChannel02 = Channel.from(
      PROCESSING_CHANNEL_2_ID, "CHAN02", ChannelType.BROADBAND_HIGH_GAIN_VERTICAL,
      ChannelDataType.HYDROACOUSTIC_ARRAY, 12.34, 45.67, 1001.0,
      123.0, 180.0, 90.0, 40.0,
      response, processingCalibration2);

  // ProcessingSite
  public static final List<Channel> processingChannels
      = List.of(processingChannel01, processingChannel02);

  public static final UUID PROCESSING_SITE_ID = UUID
      .fromString("36c1ba53-b124-4286-9c2c-72647e749e32");

  public static final Site processingSite = Site.from(PROCESSING_SITE_ID,
      "SITE33", 12.34, 56.78, 78.90,
      processingChannels);

}

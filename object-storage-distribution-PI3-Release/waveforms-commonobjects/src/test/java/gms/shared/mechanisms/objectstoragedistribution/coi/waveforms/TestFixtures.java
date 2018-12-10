package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects.ProvenanceJacksonMixins;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.*;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.WaveformsJacksonMixins;

import java.time.Instant;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Defines static objects used in unit tests
 */
public class TestFixtures {

  public static final Instant SEGMENT_START = Instant.parse("1970-01-02T03:04:05.123Z");
  public static final Instant SEGMENT_END = SEGMENT_START.plusMillis(2000);

  // AcquiredChannelSohBoolean
  public static final UUID SOH_BOOLEAN_ID = UUID.fromString("5f1a3629-ffaf-4190-b59d-5ca6f0646fd6");
  public static final UUID PROCESSING_CHANNEL_1_ID = UUID
      .fromString("46947cc2-8c86-4fa1-a764-c9b9944614b7");
  public static final AcquiredChannelSohBoolean channelSohBoolean = AcquiredChannelSohBoolean.from(
      SOH_BOOLEAN_ID, PROCESSING_CHANNEL_1_ID,
      AcquiredChannelSoh.AcquiredChannelSohType.DEAD_SENSOR_CHANNEL, SEGMENT_START, SEGMENT_END,
      true, CreationInfo.DEFAULT);

  // AcquiredChannelSohAnalog
  public static final UUID SOH_ANALOG_ID = UUID.fromString("b12c0b3a-4681-4ee3-82fc-4fcc292aa59f");
  public static final UUID PROCESSING_CHANNEL_2_ID = UUID
      .fromString("2bc8381f-8443-443a-83c8-cbbbe29ed796");
  public static final AcquiredChannelSohAnalog channelSohAnalog = AcquiredChannelSohAnalog.from(
      SOH_ANALOG_ID, PROCESSING_CHANNEL_2_ID,
      AcquiredChannelSoh.AcquiredChannelSohType.STATION_POWER_VOLTAGE, SEGMENT_START, SEGMENT_END,
      1.5, CreationInfo.DEFAULT);

  // Waveform
  public static final double SAMPLE_RATE = 2.0;
  public static final int SAMPLE_COUNT = 5;
  public static final double[] WAVEFORM_POINTS = new double[]{1.1, 2.2, 3.3, 4.4, 5.5};
  public static final Waveform waveform1 = Waveform.create(SEGMENT_START, SEGMENT_END, SAMPLE_RATE,
      SAMPLE_COUNT, WAVEFORM_POINTS);

  // ChannelSegment
  public static final SortedSet<Waveform> waveforms = new TreeSet<>(Collections.singleton(waveform1));
  public static final UUID CHANNEL_SEGMENT_ID = UUID.fromString("57015315-f7b2-4487-b3e7-8780fbcfb413");
  public static final ChannelSegment channelSegment = ChannelSegment.from(CHANNEL_SEGMENT_ID,
      PROCESSING_CHANNEL_1_ID, "segmentName", ChannelSegment.ChannelSegmentType.RAW, SEGMENT_START,
      SEGMENT_END, waveforms, CreationInfo.DEFAULT);

  // RawStationDataFrame
  public static final UUID FRAME_ID = UUID.fromString("12347cc2-8c86-4fa1-a764-c9b9944614b7"),
        STATION_ID = UUID.fromString("46947cc2-8c86-4fa1-a764-c9b9944614b7");
  public static final RawStationDataFrame rawStationDataFrame = RawStationDataFrame.from(
        FRAME_ID, STATION_ID, AcquisitionProtocol.CD11,
      "staName", SEGMENT_START, SEGMENT_END,
        SEGMENT_END.plusSeconds(10), new byte[50],
        AuthenticationStatus.AUTHENTICATION_SUCCEEDED, CreationInfo.DEFAULT
  );

  public static final ObjectMapper objMapper = new ObjectMapper();

  static {
    WaveformsJacksonMixins.register(objMapper);
    // Register mix-ins for CreationInfo
    ProvenanceJacksonMixins.register(objMapper);
    objMapper.findAndRegisterModules();
    objMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }
}

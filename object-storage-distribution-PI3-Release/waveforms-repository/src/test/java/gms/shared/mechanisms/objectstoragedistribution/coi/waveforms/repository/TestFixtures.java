package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

public class TestFixtures {

  public static final double SAMPLE_RATE = 2.0;
  public static final double SAMPLE_RATE2 = 5.0;
  public static final int SAMPLE_COUNT = 5;
  public static final long segmentLengthMillis = 2000;
  public static final long segmentLengthMillis2 = 800;

  public static final UUID SOH_BOOLEAN_ID = UUID.fromString("5f1a3629-ffaf-4190-b59d-5ca6f0646fd6");
  public static final UUID SOH_ANALOG_ID = UUID.fromString("b12c0b3a-4681-4ee3-82fc-4fcc292aa59f");
  public static final UUID CHANNEL_SEGMENT_ID = UUID
      .fromString("57015315-f7b2-4487-b3e7-8780fbcfb413");
  public static final UUID CHANNEL_SEGMENT_2_ID = UUID
      .fromString("67015315-f7b2-4487-b3e7-8780fbcfb413");
  public static final UUID PROCESSING_CHANNEL_ID = UUID
      .fromString("46947cc2-8c86-4fa1-a764-c9b9944614b7");
  public static final UUID FRAME_1_ID = UUID.fromString(
      "12347cc2-8c86-4fa1-a764-c9b9944614b7");
  public static final UUID FRAME_2_ID = UUID.fromString(
      "23447cc2-8c86-4fa1-a764-c9b9944614b7");
  public static final UUID STATION_ID = UUID.fromString(
      "8deaf835-c74f-49a6-9b7a-254dfb90b47f");

  public static final String segmentStartDateString = "1970-01-02T03:04:05.123Z";
  public static final String segmentStartDateString2 = "1970-01-02T03:04:08.123Z";

  public static final Instant SEGMENT_START = Instant.parse(segmentStartDateString);
  public static final Instant SEGMENT_END = SEGMENT_START.plusMillis(segmentLengthMillis);

  public static final Instant SEGMENT_START2 = Instant.parse(segmentStartDateString2);
  public static final Instant SEGMENT_END2 = SEGMENT_START2.plusMillis(segmentLengthMillis2);

  public static final double[] WAVEFORM_POINTS = new double[]{1.1, 2.2, 3.3, 4.4, 5.5};
  public static final double[] WAVEFORM_POINTS2 = new double[]{6, 7, 8, 9, 10};

  public static final Waveform waveform1 = Waveform.create(
      SEGMENT_START, SEGMENT_END, SAMPLE_RATE, SAMPLE_COUNT, WAVEFORM_POINTS);

  public static final Waveform waveform2 = Waveform.create(
      SEGMENT_START2, SEGMENT_END2, SAMPLE_RATE2, SAMPLE_COUNT, WAVEFORM_POINTS2);

  public static final SortedSet<Waveform> waveforms = new TreeSet<>(Set.of(waveform1));

  public static final SortedSet<Waveform> waveforms2 = new TreeSet<>(Set.of(waveform1, waveform2));

  public static final ChannelSegment channelSegment = ChannelSegment.from(
      CHANNEL_SEGMENT_ID, PROCESSING_CHANNEL_ID, "segmentName",
      ChannelSegment.ChannelSegmentType.RAW,
      SEGMENT_START, SEGMENT_END, waveforms, CreationInfo.DEFAULT);

  public static final ChannelSegment channelSegment2 = ChannelSegment.from(
      CHANNEL_SEGMENT_2_ID, PROCESSING_CHANNEL_ID, "segmentName",
      ChannelSegment.ChannelSegmentType.RAW,
      SEGMENT_START2, SEGMENT_END2, waveforms2, CreationInfo.DEFAULT);

  public static final AcquiredChannelSohBoolean channelSohBool = AcquiredChannelSohBoolean.from(
      SOH_BOOLEAN_ID, PROCESSING_CHANNEL_ID,
      AcquiredChannelSoh.AcquiredChannelSohType.DEAD_SENSOR_CHANNEL,
      SEGMENT_START, SEGMENT_END,
      true, CreationInfo.DEFAULT);

  public static final AcquiredChannelSohAnalog channelSohAnalog = AcquiredChannelSohAnalog.from(
      SOH_ANALOG_ID, PROCESSING_CHANNEL_ID,
      AcquiredChannelSoh.AcquiredChannelSohType.STATION_POWER_VOLTAGE,
      SEGMENT_START, SEGMENT_END,
      1.5, CreationInfo.DEFAULT);

  public static final RawStationDataFrame frame1 = RawStationDataFrame.from(
      FRAME_1_ID, UUID.randomUUID(), AcquisitionProtocol.CD11,
      "staName", SEGMENT_START, SEGMENT_END,
      SEGMENT_END.plusSeconds(10), new byte[50],
      AuthenticationStatus.AUTHENTICATION_SUCCEEDED, CreationInfo.DEFAULT
  );

  public static final RawStationDataFrame frame2 = RawStationDataFrame.from(
      FRAME_2_ID, UUID.randomUUID(), AcquisitionProtocol.CD11,
      "staName2", SEGMENT_START2, SEGMENT_END2,
      SEGMENT_END2.plusSeconds(10), new byte[50],
      AuthenticationStatus.AUTHENTICATION_FAILED, CreationInfo.DEFAULT
  );

  public static final List<RawStationDataFrame> allFrames = List.of(frame1, frame2);
}

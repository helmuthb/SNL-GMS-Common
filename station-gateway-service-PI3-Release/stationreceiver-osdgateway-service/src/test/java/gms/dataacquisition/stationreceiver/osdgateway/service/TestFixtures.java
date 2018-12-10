package gms.dataacquisition.stationreceiver.osdgateway.service;


import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;


public class TestFixtures {

  public static final UUID
      FRAME_ID = UUID.fromString("12347cc2-8c86-4fa1-a764-c9b9944614b7"),
      PROCESSING_CHANNEL_ID = UUID.fromString("46947cc2-8c86-4fa1-a764-c9b9944614b7"),
      STATION_ID = UUID.fromString("0406366b-9e7d-45e9-ba64-0c50de509820");

  public static final Instant SEGMENT_START = LocalDateTime.of(1970, 1, 2, 3, 4, 5, 678000000)
      .toInstant(ZoneOffset.UTC);

  public static final Instant SEGMENT_MIDDLE = LocalDateTime.of(1970, 1, 2, 3, 5, 5, 778000000)
      .toInstant(ZoneOffset.UTC);

  public static final Instant SEGMENT_END = LocalDateTime.of(1970, 1, 2, 3, 6, 5, 878000000)
      .toInstant(ZoneOffset.UTC);

  public static final double[] WAVEFORM1_POINTS = new double[]{1.1, 2.2, 3.3, 4.4, 5.5};

  public static final double[] WAVEFORM2_POINTS = new double[]{6.6, 7.7, 8.8, 9.9, 10.10};

  public static final Waveform waveform1 = Waveform
      .create(SEGMENT_START, SEGMENT_MIDDLE, 60.0, 3600, WAVEFORM1_POINTS);

  public static final Waveform waveform2 = Waveform
      .create(SEGMENT_MIDDLE, SEGMENT_END, 60.0, 3600, WAVEFORM2_POINTS);

  public static final SortedSet<Waveform> waveforms = new TreeSet<>(Set.of(waveform1, waveform2));

  public static final ChannelSegment channelSegment = ChannelSegment.from(
      UUID.randomUUID(), PROCESSING_CHANNEL_ID, "segmentName",
      ChannelSegment.ChannelSegmentType.RAW,
      SEGMENT_START, SEGMENT_END, waveforms, CreationInfo.DEFAULT);

  public static final AcquiredChannelSohBoolean channelSohBool =
      AcquiredChannelSohBoolean.from(UUID.randomUUID(), PROCESSING_CHANNEL_ID,
          AcquiredChannelSoh.AcquiredChannelSohType.DEAD_SENSOR_CHANNEL,
          SEGMENT_START, SEGMENT_END, true, CreationInfo.DEFAULT);

  public static final AcquiredChannelSohAnalog channelSohAnalog =
      AcquiredChannelSohAnalog.from(UUID.randomUUID(), PROCESSING_CHANNEL_ID,
          AcquiredChannelSoh.AcquiredChannelSohType.STATION_POWER_VOLTAGE,
          SEGMENT_START, SEGMENT_END, 1.5, CreationInfo.DEFAULT);

  public static final RawStationDataFrame frame1 = RawStationDataFrame.from(
      FRAME_ID, STATION_ID, AcquisitionProtocol.CD11, "staName",
      SEGMENT_START, SEGMENT_END,
      SEGMENT_END.plusSeconds(10), new byte[50],
      AuthenticationStatus.AUTHENTICATION_SUCCEEDED, CreationInfo.DEFAULT
  );

  public static final String STATION_NAME = "someSta";
  public static final Station station = Station.from(UUID.randomUUID(), STATION_NAME,
      12.34, 45.67, 78.90, List.of());

  public static final String CHAN_NAME = "someChan";
  public static final String SITE_NAME = "someSite";
}

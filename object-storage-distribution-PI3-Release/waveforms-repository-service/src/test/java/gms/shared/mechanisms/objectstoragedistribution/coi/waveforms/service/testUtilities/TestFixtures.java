package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.testUtilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.WaveformsJacksonMixins;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import org.msgpack.jackson.dataformat.MessagePackFactory;

public class TestFixtures {

  public static ObjectMapper objectMapper;
  public static ObjectMapper msgPackMapper;
  private static final String segmentStartDateString = "1970-01-02T03:04:05.123Z";
  private static final double SAMPLE_RATE = 2.0;
  private static final int SAMPLE_COUNT = 5;
  private static final long segmentLengthMillis = 2000;
  private static final double[] WAVEFORM_POINTS = new double[]{1.1, 2.2, 3.3, 4.4, 5.5};
  public static final Instant SEGMENT_START = Instant.parse(segmentStartDateString);
  public static final Instant SEGMENT_END = SEGMENT_START.plusMillis(segmentLengthMillis);
  public static final String stationName = "A1e5769fe-29e1-4e4c-8219-2f8f581038a1BC";
  public static final Instant startTime = Instant.parse("2010-05-20T00:59:59.108Z");
  public static final Instant endTime = Instant.parse("2010-05-20T01:00:01.991Z");
  private static final String sohIdString = "b38ae749-2833-4197-a8cb-4609ddd4342f";
  public static UUID acquiredChannelSohId = UUID.fromString(sohIdString);

  public static final UUID
      CHANNEL_SEGMENT_ID = UUID.fromString("627adaf7-417e-4ecd-9c5f-767a23d06bbc"),
      PROCESSING_CHANNEL_1_ID = UUID.fromString("cd412982-2996-46d8-812e-e30b5dc7eb62");

  public static final UUID PROCESSING_CALIBRATION_1_ID = UUID
      .fromString("ce7c377a-b6a4-478f-b3bd-5c934ee6b7ef");

  public static final Waveform waveform1 = Waveform.create(SEGMENT_START, SEGMENT_END, SAMPLE_RATE,
      SAMPLE_COUNT, WAVEFORM_POINTS);

  public static final SortedSet<Waveform> waveforms = new TreeSet<Waveform>() {{
    add(waveform1);
  }};

  public static final RawStationDataFrame frame1 = RawStationDataFrame.from(
      UUID.randomUUID(), UUID.randomUUID(), AcquisitionProtocol.CD11,
      "staName", SEGMENT_START, SEGMENT_END.plusSeconds(10),
      Instant.parse("2016-05-06T07:08:09Z"), new byte[50],
      AuthenticationStatus.AUTHENTICATION_SUCCEEDED, CreationInfo.DEFAULT
  );

  public static final RawStationDataFrame frame2 = RawStationDataFrame.from(
      UUID.randomUUID(), UUID.randomUUID(), AcquisitionProtocol.CD11,
      "staName2", frame1.getPayloadDataEndTime().plusSeconds(1),
      frame1.getPayloadDataEndTime().plusSeconds(11), Instant.parse("2015-01-01T12:34:56Z"),
      new byte[50], AuthenticationStatus.AUTHENTICATION_FAILED, CreationInfo.DEFAULT
  );

  public static final List<RawStationDataFrame> allFrames = List.of(frame1, frame2);

  public static final SoftwareComponentInfo softwareComponentInfo = new SoftwareComponentInfo(
      "unit test component name",
      "unit test component version");

  public static final CreationInfo creationInfo = new CreationInfo(
      "unit test creator name",
      softwareComponentInfo);

  public static ChannelSegment channelSegment1;
  public static ChannelSegment channelSegment2;
  public static AcquiredChannelSohAnalog channelSohAnalog;
  public static AcquiredChannelSohBoolean channelSohBoolean;
  public static String channelSegmentAsJson;
  public static byte[] channelSegmentAsMsgPack;
  public static String channelSohBooleanAsJson;
  public static String channelSohBooleanListAsJson;
  public static String channelSohAnalogAsJson;
  public static String channelSohAnalogListAsJson;
  public static List<AcquiredChannelSohBoolean> channelSohBooleanListAsList;
  public static List<AcquiredChannelSohAnalog> channelSohAnalogListAsList;

  static {
    channelSegment1 = ChannelSegment.from(
        CHANNEL_SEGMENT_ID, PROCESSING_CHANNEL_1_ID, "ChannelName",
        ChannelSegment.ChannelSegmentType.RAW,
        SEGMENT_START, SEGMENT_END, waveforms, CreationInfo.DEFAULT);

    channelSegment2 = ChannelSegment.from(
        CHANNEL_SEGMENT_ID, PROCESSING_CHANNEL_1_ID, "segmentName2",
        ChannelSegment.ChannelSegmentType.RAW,
        SEGMENT_START, SEGMENT_END, waveforms, CreationInfo.DEFAULT);

    channelSohAnalog = AcquiredChannelSohAnalog.from(acquiredChannelSohId,
        UUID.randomUUID(), AcquiredChannelSoh.AcquiredChannelSohType.STATION_POWER_VOLTAGE,
        TestFixtures.SEGMENT_START, TestFixtures.SEGMENT_END, 1.5, CreationInfo.DEFAULT);

    channelSohBoolean = AcquiredChannelSohBoolean.from(acquiredChannelSohId,
        UUID.randomUUID(), AcquiredChannelSoh.AcquiredChannelSohType.DEAD_SENSOR_CHANNEL,
        SEGMENT_START, SEGMENT_END, true, CreationInfo.DEFAULT);

    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    WaveformsJacksonMixins.register(objectMapper);

    msgPackMapper = new ObjectMapper(new MessagePackFactory());
    msgPackMapper.findAndRegisterModules();
    WaveformsJacksonMixins.register(msgPackMapper);

    channelSegmentAsJson = toJson(channelSegment1);
    channelSegmentAsMsgPack = toMsgPack(channelSegment1);

    channelSohBooleanAsJson = toJson(channelSohBoolean);
    channelSohBooleanListAsList = generateMockBooleanSoh(
        UUID.fromString("b38ae749-2833-4197-a8cb-4609ddd4342f"),
        Instant.parse("2017-11-29T10:30:00.000Z"), Instant.parse("2017-11-29T10:35:00.000Z"));
    channelSohBooleanListAsJson = toJson(channelSohBooleanListAsList);

    channelSohAnalogAsJson = toJson(channelSohAnalog);
    channelSohAnalogListAsList = generateMockAnalogSoh(
        UUID.fromString("b38ae749-2833-4197-a8cb-4609ddd4342f"),
        Instant.parse("2017-11-29T10:30:00.000Z"), Instant.parse("2017-11-29T10:35:00.000Z"));
    channelSohAnalogListAsJson = toJson(channelSohAnalogListAsList);

  }

  private static String toJson(Object object) {
    String json = "{Initialization error}";
    try {
      json = objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    return json;
  }

  private static byte[] toMsgPack(Object object) {
    byte[] byteMessage = null;
    try {
      byteMessage = msgPackMapper.writeValueAsBytes(object);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    return byteMessage;
  }

  private static List<AcquiredChannelSohBoolean> generateMockBooleanSoh(UUID processingChannelId,
      Instant startTime, Instant endTime) {

    CreationInfo creationInfo = new CreationInfo("unitTest", Instant.now(),
        new SoftwareComponentInfo("CoiWaveformHttpControllerTests", "1.0.0"));

    List<AcquiredChannelSohBoolean> mockResult = new ArrayList<>();
    Duration step = Duration.between(startTime, endTime).dividedBy(10);

    for (Instant time = startTime; endTime.isAfter(time); time = time.plus(step)) {
      mockResult.add(AcquiredChannelSohBoolean.create(processingChannelId,
          AcquiredChannelSohType.AUTHENTICATION_SEAL_BROKEN, time, time.plus(step), false,
          creationInfo));

      mockResult.add(
          AcquiredChannelSohBoolean
              .create(processingChannelId, AcquiredChannelSohType.CLIPPED, time,
                  time.plus(step), true, creationInfo));
    }

    return mockResult;
  }

  private static List<AcquiredChannelSohAnalog> generateMockAnalogSoh(UUID processingChannelId,
      Instant startTime, Instant endTime) {

    CreationInfo creationInfo = new CreationInfo("unitTest", Instant.now(),
        new SoftwareComponentInfo("CoiWaveformHttpControllerTests", "1.0.0"));

    List<AcquiredChannelSohAnalog> mockResult = new ArrayList<>();
    Duration step = Duration.between(startTime, endTime).dividedBy(10);

    for (Instant time = startTime; endTime.isAfter(time); time = time.plus(step)) {
      mockResult.add(AcquiredChannelSohAnalog.create(processingChannelId,
          AcquiredChannelSohType.STATION_POWER_VOLTAGE, time, time.plus(step), Math.random(),
          creationInfo));
    }

    return mockResult;
  }


}

package gms.core.waveformqc.waveformsignalqc.algorithm;


import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.ChannelSegmentType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.DoubleStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WaveformRepeatedAmplitudeInterpreterTests {

  private static final Instant start = Instant.EPOCH;
  private static final int durationSecs = 10;
  private static final Instant end = Instant.EPOCH.plusSeconds(durationSecs);

  private static final UUID channelId = UUID.randomUUID();

  private static final int sampleRate = 2;
  private static final double nanosPerSample = 1.0e9 / sampleRate;
  private static final int sampleCount = 21;

  private final ChannelSegment emptyChannelSegment = createChannelSegment(List.of());

  private final ChannelSegment oneToZeroChannelSegment = createChannelSegment(List.of(
      createWaveform(createOneToZeroValues(), start, durationSecs)));

  private final ChannelSegment multipleRepeatsChannelSegment = createChannelSegment(
      List.of(createWaveform(createMultipleRepeatsValues(), start, durationSecs)));

  private final ChannelSegment multipleChannelSegments = createChannelSegment(
      List.of(createWaveform(createMultipleRepeatsValues(), start, durationSecs),
          createWaveform(createMultipleRepeatsValues(), start.plusMillis(10500), durationSecs)));

  private static ChannelSegment createChannelSegment(List<Waveform> waveforms) {
    Instant startTime = start;
    Instant endTime = end;

    if (waveforms.size() > 0) {
      startTime = waveforms.get(0).getStartTime();
      endTime = waveforms.get(waveforms.size() - 1).getEndTime();
    }

    return ChannelSegment.create(channelId, "test", ChannelSegmentType.RAW, startTime, endTime,
        new TreeSet<>(waveforms), CreationInfo.DEFAULT);
  }

  private static Waveform createWaveform(double[] values, Instant start, long durationSecs) {
    return Waveform.create(start, start.plusSeconds(durationSecs), sampleRate, sampleCount, values);
  }

  private static double[] createOneToZeroValues() {
    final double step = 1 / 25.0;
    return DoubleStream.iterate(step, d -> d + step).limit(21).toArray();
  }

  private static double[] createMultipleRepeatsValues() {
    return new double[]{
        0.0, 0.0, 0.0, 10, 20, 30, 40, 50, 60, 70,
        80.0, 80.0, 80.0, 0, 1, 2, 3, 4, 5, 6,
        7
    };
  }

  private WaveformRepeatedAmplitudeInterpreter waveformRepeatedAmplitudeInterpreter;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    waveformRepeatedAmplitudeInterpreter = new WaveformRepeatedAmplitudeInterpreter();
  }

  @Test
  public void testCreateWaveformRepeatedAmplitudeQcMasksEntireWaveformRepeats() throws Exception {
    final int minRepeatedSamples = 3;
    final double maxDeltaFromStart = 1.0;

    List<WaveformRepeatedAmplitudeQcMask> repeats = waveformRepeatedAmplitudeInterpreter
        .createWaveformRepeatedAmplitudeQcMasks(oneToZeroChannelSegment, minRepeatedSamples,
            maxDeltaFromStart);

    assertEquals(1, repeats.size());

    WaveformRepeatedAmplitudeQcMask mask = repeats.get(0);
    assertEquals(channelId, mask.getChannelId());
    assertEquals(oneToZeroChannelSegment.getId(), mask.getChannelSegmentId());
    assertEquals(start, mask.getStartTime());
    assertEquals(end, mask.getEndTime());
  }

  @Test
  public void testCreateWaveformRepeatedAmplitudeQcMasksMultipleRepeats() throws Exception {
    final int minRepeatedSamples = 3;
    final double maxDeltaFromStart = 0.5;

    List<WaveformRepeatedAmplitudeQcMask> repeats = waveformRepeatedAmplitudeInterpreter
        .createWaveformRepeatedAmplitudeQcMasks(multipleRepeatsChannelSegment, minRepeatedSamples,
            maxDeltaFromStart);

    assertEquals(2, repeats.size());

    assertTrue(repeats.stream().map(WaveformRepeatedAmplitudeQcMask::getChannelSegmentId)
        .allMatch(multipleRepeatsChannelSegment.getId()::equals));

    assertTrue(repeats.stream().map(WaveformRepeatedAmplitudeQcMask::getChannelId)
        .allMatch(channelId::equals));

    repeats.sort(Comparator.comparing(WaveformRepeatedAmplitudeQcMask::getStartTime));

    WaveformRepeatedAmplitudeQcMask mask = repeats.get(0);
    assertEquals(start, mask.getStartTime());
    assertEquals(start.plusNanos((long) (2 * nanosPerSample)), mask.getEndTime());

    mask = repeats.get(1);
    assertEquals(start.plusNanos((long) (10 * nanosPerSample)), mask.getStartTime());
    assertEquals(start.plusNanos((long) (12 * nanosPerSample)), mask.getEndTime());
  }

  @Test
  public void testCreateWaveformRepeatedAmplitudeQcMasksMultipleChannelSegments() throws Exception {
    final int minRepeatedSamples = 2;
    final double maxDeltaFromStart = 0.0000001;

    List<WaveformRepeatedAmplitudeQcMask> repeats = waveformRepeatedAmplitudeInterpreter
        .createWaveformRepeatedAmplitudeQcMasks(multipleChannelSegments, minRepeatedSamples,
            maxDeltaFromStart);

    assertEquals(4, repeats.size());

    assertTrue(repeats.stream().map(WaveformRepeatedAmplitudeQcMask::getChannelSegmentId)
        .allMatch(multipleChannelSegments.getId()::equals));

    assertTrue(repeats.stream().map(WaveformRepeatedAmplitudeQcMask::getChannelId)
        .allMatch(channelId::equals));

    repeats.sort(Comparator.comparing(WaveformRepeatedAmplitudeQcMask::getStartTime));

    WaveformRepeatedAmplitudeQcMask mask = repeats.get(0);
    assertEquals(start, mask.getStartTime());
    assertEquals(start.plusNanos((long) (2 * nanosPerSample)), mask.getEndTime());

    mask = repeats.get(1);
    assertEquals(start.plusNanos((long) (10 * nanosPerSample)), mask.getStartTime());
    assertEquals(start.plusNanos((long) (12 * nanosPerSample)), mask.getEndTime());

    mask = repeats.get(2);
    assertEquals(start.plusNanos((long) (21 * nanosPerSample)), mask.getStartTime());
    assertEquals(start.plusNanos((long) (23 * nanosPerSample)), mask.getEndTime());

    mask = repeats.get(3);
    assertEquals(start.plusNanos((long) (31 * nanosPerSample)), mask.getStartTime());
    assertEquals(start.plusNanos((long) (33 * nanosPerSample)), mask.getEndTime());
  }

  @Test
  public void testCreateNoRepeatedAmplitudesExpectNoMasks() throws Exception {
    // Use repeats outside configured range

    final int minRepeatedSamples = 3;
    final double maxDeltaFromStart = 1.0 / 1000;

    List<WaveformRepeatedAmplitudeQcMask> repeats = waveformRepeatedAmplitudeInterpreter
        .createWaveformRepeatedAmplitudeQcMasks(oneToZeroChannelSegment, minRepeatedSamples,
            maxDeltaFromStart);

    // Samples change by 1/25 which is > maxDeltaFromStart
    assertEquals(0, repeats.size());
  }

  @Test
  public void testCreateNotEnoughRepeatedAmplitudesExpectNoMasks() throws Exception {
    final int minRepeatedSamples = 4;
    final double maxDeltaFromStart = 0.5;

    List<WaveformRepeatedAmplitudeQcMask> repeats = waveformRepeatedAmplitudeInterpreter
        .createWaveformRepeatedAmplitudeQcMasks(multipleRepeatsChannelSegment, minRepeatedSamples,
            maxDeltaFromStart);

    // Repeats are only 3 samples long so expect no repeated amplitude masks
    assertEquals(0, repeats.size());
  }

  @Test
  public void testCreateMinRepeatedSamplesTooLowExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeInterpreter.createWaveformRepeatedAmplitudeQcMasks requires minRepeatedSamples > 1");
    waveformRepeatedAmplitudeInterpreter
        .createWaveformRepeatedAmplitudeQcMasks(emptyChannelSegment, 1, 1.0);
  }

  @Test
  public void testCreateDeviationNotPositiveExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeInterpreter.createWaveformRepeatedAmplitudeQcMasks requires maxDeltaFromStartAmplitude >= 0.0");
    waveformRepeatedAmplitudeInterpreter
        .createWaveformRepeatedAmplitudeQcMasks(emptyChannelSegment, 2, 0.0 - Double.MIN_NORMAL);
  }

  @Test
  public void testCreateNullChannelSegmentExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeInterpreter.createWaveformRepeatedAmplitudeQcMasks requires non-null channelSegment");
    waveformRepeatedAmplitudeInterpreter.createWaveformRepeatedAmplitudeQcMasks(null, 2, 1.0);
  }
}

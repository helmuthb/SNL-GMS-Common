package gms.core.waveformqc.waveformsignalqc.algorithm;

import static gms.core.waveformqc.waveformsignalqc.algorithm.TestUtility.createChannelSegment;
import static gms.core.waveformqc.waveformsignalqc.algorithm.TestUtility.createWaveform;
import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WaveformGapInterpreterTest {

  private static Instant shortGapStart;
  private static Instant shortGapEnd;
  private static Instant longGapStart;
  private static Instant longGapEnd;
  private static Instant shortGap2Start;
  private static Instant shortGap2End;

  private static ChannelSegment noGap;
  private static ChannelSegment oneShortGap;
  private static ChannelSegment oneLongGap;
  private static ChannelSegment oneLongOneShortGap;

  private static int minLongGapLengthInSamples = 2;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @BeforeClass
  public static void setUp() throws Exception {

    final double samplesPerSec = 40.0;
    final double samplePeriodSec = 1.0 / 40.0;

    final Instant startTime = Instant.EPOCH;
    final Waveform first = createWaveform(startTime, startTime.plusSeconds(10), samplesPerSec);

    Instant startTime2 = first.getEndTime().plusMillis((long) (samplePeriodSec * 1000));
    final Waveform secondNoGap = createWaveform(startTime2, startTime2.plusSeconds(10),
        samplesPerSec);
    noGap = createChannelSegment(List.of(first, secondNoGap));

    shortGapStart = first.getEndTime();
    shortGapEnd = first.getEndTime().plusMillis((long) (samplePeriodSec * 1000 * 2));
    final Waveform secondShortGap = createWaveform(shortGapEnd, shortGapEnd.plusSeconds(10),
        samplesPerSec);
    oneShortGap = createChannelSegment(List.of(first, secondShortGap));

    longGapStart = first.getEndTime();
    longGapEnd = first.getEndTime().plusMillis((long) (samplePeriodSec * 1000 * 3));
    final Waveform secondLongGap = createWaveform(longGapEnd, longGapEnd.plusSeconds(10),
        samplesPerSec);
    oneLongGap = createChannelSegment(List.of(first, secondLongGap));

    shortGap2Start = secondLongGap.getEndTime();
    shortGap2End = secondLongGap.getEndTime().plusMillis((long) (samplePeriodSec * 1000 * 2));
    final Waveform thirdShortGap = createWaveform(shortGap2End, shortGap2End.plusSeconds(10),
        samplesPerSec);
    oneLongOneShortGap = createChannelSegment(List.of(first, secondLongGap, thirdShortGap));
  }

  @Test
  public void testCreateWaveformGapQcMasksNoGap() {
    List<WaveformGapQcMask> masks = WaveformGapInterpreter
        .createWaveformGapQcMasks(noGap, minLongGapLengthInSamples);
    assertEquals(0, masks.size());
  }

  @Test
  public void testCreateWaveformGapQcMasksOneRepairableGap() {
    List<WaveformGapQcMask> masks = WaveformGapInterpreter
        .createWaveformGapQcMasks(oneShortGap, minLongGapLengthInSamples);
    assertEquals(1, masks.size());
    assertEquals(shortGapStart, masks.get(0).getStartTime());
    assertEquals(shortGapEnd, masks.get(0).getEndTime());
    assertEquals(QcMaskType.REPAIRABLE_GAP, masks.get(0).getQcMaskType());
  }

  @Test
  public void testCreateWaveformGapQcMasksOneLongGap() {
    List<WaveformGapQcMask> masks = WaveformGapInterpreter
        .createWaveformGapQcMasks(oneLongGap, minLongGapLengthInSamples);
    assertEquals(1, masks.size());
    assertEquals(longGapStart, masks.get(0).getStartTime());
    assertEquals(longGapEnd, masks.get(0).getEndTime());
    assertEquals(QcMaskType.LONG_GAP, masks.get(0).getQcMaskType());
  }

  @Test
  public void testCreateWaveformGapQcMasksOneLongOneRepairableGap() {
    List<WaveformGapQcMask> masks = WaveformGapInterpreter
        .createWaveformGapQcMasks(oneLongOneShortGap, minLongGapLengthInSamples);
    assertEquals(2, masks.size());

    assertEquals(longGapStart, masks.get(0).getStartTime());
    assertEquals(longGapEnd, masks.get(0).getEndTime());
    assertEquals(QcMaskType.LONG_GAP, masks.get(0).getQcMaskType());

    assertEquals(shortGap2Start, masks.get(1).getStartTime());
    assertEquals(shortGap2End, masks.get(1).getEndTime());
    assertEquals(QcMaskType.REPAIRABLE_GAP, masks.get(1).getQcMaskType());
  }

  @Test
  public void testCreateWaveformGapQcMasksTwoRepairableGap() {
    List<WaveformGapQcMask> masks = WaveformGapInterpreter
        .createWaveformGapQcMasks(oneLongOneShortGap, 3);
    assertEquals(2, masks.size());

    assertEquals(longGapStart, masks.get(0).getStartTime());
    assertEquals(longGapEnd, masks.get(0).getEndTime());
    assertEquals(QcMaskType.REPAIRABLE_GAP, masks.get(0).getQcMaskType());

    assertEquals(shortGap2Start, masks.get(1).getStartTime());
    assertEquals(shortGap2End, masks.get(1).getEndTime());
    assertEquals(QcMaskType.REPAIRABLE_GAP, masks.get(1).getQcMaskType());
  }

  @Test
  public void testCreateNullChannelSegmentExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception
        .expectMessage("WaveformGapInterpreter.updateQcMasks requires non-null channelSegment");
    WaveformGapInterpreter.createWaveformGapQcMasks(null, 1);
  }

  @Test
  public void testCreateNonPositiveThresholdExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception
        .expectMessage("WaveformGapInterpreter.updateQcMasks requires minLongGapLengthInSamples > 0");
    WaveformGapInterpreter.createWaveformGapQcMasks(oneLongOneShortGap, 0);
  }
}
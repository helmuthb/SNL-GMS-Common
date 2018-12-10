package gms.core.signaldetection.staltapowerdetector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import gms.core.signaldetection.staltapowerdetector.Algorithm.AlgorithmType;
import gms.core.signaldetection.staltapowerdetector.Algorithm.WaveformTransformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class StaLtaPowerDetectorPluginTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  final AlgorithmType type = AlgorithmType.STANDARD;
  final WaveformTransformation transformation = WaveformTransformation.RECTIFIED;
  final int staLead = 2;
  final int staLength = 4;
  final int ltaLead = 4;
  final int ltaLength = 4;
  final double triggerThreshold = 12.0;
  final double detriggerThreshold = 10.0;
  final double interpolateGapsSampleRateTolerance = 1.2;
  final double mergeSampleRateTolerance = 1.3;
  final int length = 100;
  final double sampleRate = 40.0;
  final long sampleLengthMillis = (long) ((1.0 / sampleRate) * 1000);
  final Duration mergeMinLength = Duration.ofMillis((long) (sampleLengthMillis * 0.5));

  final StaLtaParameters staLtaParameters = StaLtaParameters.create(
      type, transformation, Duration.ofMillis(sampleLengthMillis).multipliedBy(staLead),
      Duration.ofMillis(sampleLengthMillis).multipliedBy(staLength),
      Duration.ofMillis(sampleLengthMillis).multipliedBy(ltaLead),
      Duration.ofMillis(sampleLengthMillis).multipliedBy(ltaLength), triggerThreshold,
      detriggerThreshold, interpolateGapsSampleRateTolerance, mergeSampleRateTolerance,
      mergeMinLength);

  @Test
  public void testCreate() throws Exception {
    StaLtaPowerDetectorPlugin plugin = StaLtaPowerDetectorPlugin
        .create(mock(Algorithm.class), mock(StaLtaConfiguration.class));
    assertNotNull(plugin);
  }

  @Test
  public void testCreateNullAlgorithmExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("STA/LTA algorithm cannot be null");
    StaLtaPowerDetectorPlugin.create(null, mock(StaLtaConfiguration.class));
  }

  @Test
  public void testCreateNullConfigurationExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("STA/LTA configuration cannot be null");
    StaLtaPowerDetectorPlugin.create(new Algorithm(), null);
  }

  @Test
  public void testCreateConditionsChannelSegment() throws Exception {
    Algorithm mockAlgorithm = mock(Algorithm.class);
    StaLtaConfiguration mockConfiguration = mock(StaLtaConfiguration.class);
    StaLtaPowerDetectorPlugin plugin = StaLtaPowerDetectorPlugin.create(mockAlgorithm,
        mockConfiguration);

    given(mockConfiguration.createParameters(any(UUID.class))).willReturn(staLtaParameters);

    // Mock wf1
    final double[] samples1 = new double[length];
    final Waveform wf1 = Waveform.withInferredEndTime(Instant.EPOCH, sampleRate, length, samples1);

    // Mock wf2 - begins less than LTA from the end of wf1
    final double sampleRate2 = sampleRate - (sampleRate * 0.0001);
    final double[] samples2 = new double[200];
    final Waveform wf2 = Waveform
        .withInferredEndTime(
            wf1.getEndTime()
                .plus(Duration.ofMillis((long) ((ltaLength - 2) * 1000.0 / sampleRate))),
            sampleRate2, 200, samples2);

    final ChannelSegment channelSegment = channelSegmentFromWaveforms(List.of(wf1, wf2));

    // Condition channelSegment
    // Interpolates because the waveforms are closer than the LTA window length
    // Merges after interpolation because the sample rates are within tolerance
    // Can't mock ChannelSegment so compute the conditioned ChannelSegments here
    final OptionalDouble nominalSampleRate = channelSegment.getWaveforms().stream()
        .mapToDouble(Waveform::getSampleRate).findFirst();

    final ChannelSegment conditioned = channelSegment
        .interpolateWaveformGap(interpolateGapsSampleRateTolerance, ltaLength)
        .mergeWaveforms(mergeSampleRateTolerance,
            fractionalSamplesFromDuration(nominalSampleRate.orElse(0.0), mergeMinLength));

    // Make sure conditioning actually occurred.  The rest of the test requires this.
    assertEquals(1, conditioned.getWaveforms().size());

    // Mock results of processing conditioned samples
    final Set<Integer> triggerIndices = Set.of(1, 5, 10);
    final double[] conditionedSamples = conditioned.getWaveforms().first().getValues();
    given(mockAlgorithm
        .staLta(type, transformation, staLead, staLength, ltaLead, ltaLength, triggerThreshold,
            detriggerThreshold, conditionedSamples)).willReturn(triggerIndices);

    plugin.detect(channelSegment);

    // Make sure the algorithm is only called once and only with the expected parameters
    verify(mockAlgorithm)
        .staLta(type, transformation, staLead, staLength, ltaLead, ltaLength, triggerThreshold,
            detriggerThreshold, conditionedSamples);

    verifyNoMoreInteractions(mockAlgorithm);
  }

  @Test
  public void testCreateConditionsChannelSegmentWithLtaLength() throws Exception {
    Algorithm mockAlgorithm = mock(Algorithm.class);
    StaLtaConfiguration mockConfiguration = mock(StaLtaConfiguration.class);
    StaLtaPowerDetectorPlugin plugin = StaLtaPowerDetectorPlugin.create(mockAlgorithm,
        mockConfiguration);

    given(mockConfiguration.createParameters(any(UUID.class))).willReturn(staLtaParameters);

    // Mock wf1
    final double[] samples1 = new double[length];
    final Waveform wf1 = Waveform.withInferredEndTime(Instant.EPOCH, sampleRate, length, samples1);

    // Mock wf2 - begins more than LTA from the end of wf1
    final double sampleRate2 = sampleRate - (sampleRate * 0.0001);
    final double[] samples2 = new double[200];
    final Waveform wf2 = Waveform
        .withInferredEndTime(
            wf1.getEndTime().plus(Duration.ofSeconds((long) (ltaLength * sampleRate))),
            sampleRate2, 200, samples2);


    // Current ChannelSegment can't be mocked so have to check the conditioning here.
    final ChannelSegment channelSegment = channelSegmentFromWaveforms(List.of(wf1, wf2));

    // Conditioned ChannelSegment should still have two Waveforms.  These mock's won't work
    // correctly if conditioning changes the ChannelSegment and the test will fail.
    // Can't mock ChannelSegment so assume conditioning occurs based on other unit tests.

    // Mock results of processing conditioned samples
    final Set<Integer> triggerIndices1 = Set.of(1, 5, 10);
    given(mockAlgorithm
        .staLta(type, transformation, staLead, staLength, ltaLead, ltaLength, triggerThreshold,
            detriggerThreshold, samples1)).willReturn(triggerIndices1);

    final Set<Integer> triggerIndices2 = Set.of(2, 6, 11);
    given(mockAlgorithm
        .staLta(type, transformation, staLead, staLength, ltaLead, ltaLength, triggerThreshold,
            detriggerThreshold, samples2)).willReturn(triggerIndices2);

    plugin.detect(channelSegment);

    // Make sure the algorithm is only called with the two waveforms expected parameters
    verify(mockAlgorithm)
        .staLta(type, transformation, staLead, staLength, ltaLead, ltaLength, triggerThreshold,
            detriggerThreshold, samples1);

    verify(mockAlgorithm)
        .staLta(type, transformation, staLead, staLength, ltaLead, ltaLength, triggerThreshold,
            detriggerThreshold, samples2);

    verifyNoMoreInteractions(mockAlgorithm);
  }

  @Test
  public void testDetect() throws Exception {

    Algorithm mockAlgorithm = mock(Algorithm.class);
    StaLtaConfiguration mockConfiguration = mock(StaLtaConfiguration.class);

    StaLtaPowerDetectorPlugin plugin = StaLtaPowerDetectorPlugin
        .create(mockAlgorithm, mockConfiguration);

    // Mock Algorithm results
    final double[] samples = new double[length];
    final Waveform wf = Waveform.withInferredEndTime(Instant.now(), sampleRate, length, samples);

    final Set<Integer> triggerIndices = Set.of(1, 5, 10);
    final Set<Instant> expectedTriggerTimes = triggerIndices.stream().map(wf::timeForSample)
        .collect(Collectors.toSet());

    given(mockConfiguration.createParameters(any(UUID.class))).willReturn(staLtaParameters);

    given(mockAlgorithm
        .staLta(type, transformation, staLead, staLength, ltaLead, ltaLength, triggerThreshold,
            detriggerThreshold, samples)).willReturn(triggerIndices);

    final ChannelSegment channelSegment = channelSegmentFromWaveforms(List.of(wf));

    final Set<Instant> actualTriggerTimes = plugin.detect(channelSegment);

    assertEquals(expectedTriggerTimes.size(), actualTriggerTimes.size());
    assertTrue(expectedTriggerTimes.containsAll(actualTriggerTimes));
    assertTrue(actualTriggerTimes.containsAll(expectedTriggerTimes));
  }

  @Test
  public void testDetectTwoWaveforms() throws Exception {
    Algorithm mockAlgorithm = mock(Algorithm.class);
    StaLtaConfiguration mockConfiguration = mock(StaLtaConfiguration.class);

    StaLtaPowerDetectorPlugin plugin = StaLtaPowerDetectorPlugin.create(mockAlgorithm,
        mockConfiguration);

    given(mockConfiguration.createParameters(any(UUID.class))).willReturn(staLtaParameters);

    // Mock Algorithm results on wf1
    final double[] samples1 = new double[length];
    final Waveform wf1 = Waveform.withInferredEndTime(Instant.EPOCH, sampleRate, length, samples1);

    final Set<Integer> triggerIndices1 = Set.of(1, 5, 10);
    given(mockAlgorithm
        .staLta(type, transformation, staLead, staLength, ltaLead, ltaLength, triggerThreshold,
            detriggerThreshold, samples1)).willReturn(triggerIndices1);

    // Mock Algorithm results on wf2
    final double[] samples2 = new double[200];
    final Waveform wf2 = Waveform
        .withInferredEndTime(Instant.EPOCH.plus(Duration.ofDays(5000)), sampleRate, 200, samples2);

    final Set<Integer> triggerIndices2 = Set.of(99, 17);
    given(mockAlgorithm
        .staLta(type, transformation, staLead, staLength, ltaLead, ltaLength, triggerThreshold,
            detriggerThreshold, samples2)).willReturn(triggerIndices2);

    // Get expected trigger times
    final Set<Instant> expectedTriggerTimes = Stream.concat(
        triggerIndices1.stream().map(wf1::timeForSample),
        triggerIndices2.stream().map(wf2::timeForSample))
        .collect(Collectors.toSet());

    final ChannelSegment channelSegment = channelSegmentFromWaveforms(List.of(wf1, wf2));
    final Set<Instant> actualTriggerTimes = plugin.detect(channelSegment);

    assertEquals(expectedTriggerTimes.size(), actualTriggerTimes.size());
    assertTrue(expectedTriggerTimes.containsAll(actualTriggerTimes));
    assertTrue(actualTriggerTimes.containsAll(expectedTriggerTimes));
  }

  private static double fractionalSamplesFromDuration(double samplesPerSec, Duration duration) {
    return (samplesPerSec * duration.getSeconds()) + (samplesPerSec * duration.getNano() / 1.0e9);
  }

  private static ChannelSegment channelSegmentFromWaveforms(List<Waveform> waveforms) {
    return ChannelSegment.from(UUID.randomUUID(), UUID.randomUUID(), "ChannelName",
        ChannelSegment.ChannelSegmentType.RAW, waveforms.get(0).getStartTime(),
        waveforms.get(waveforms.size() - 1).getEndTime(),
        new TreeSet<>(waveforms), CreationInfo.DEFAULT);
  }

  @Test
  public void testDetectNullChannelSegmentExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("STA/LTA plugin cannot process null channelSegment");
    StaLtaPowerDetectorPlugin.create(mock(Algorithm.class), mock(StaLtaConfiguration.class))
        .detect(null);
  }
}

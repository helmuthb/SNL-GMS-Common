package gms.core.signaldetection.staltapowerdetector;

import static org.junit.Assert.assertEquals;

import gms.core.signaldetection.staltapowerdetector.Algorithm.AlgorithmType;
import gms.core.signaldetection.staltapowerdetector.Algorithm.WaveformTransformation;
import java.time.Duration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class StaLtaParametersTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  final AlgorithmType algorithmType = AlgorithmType.STANDARD;
  final WaveformTransformation waveformTransformation = WaveformTransformation.RECTIFIED;
  final Duration staLead = Duration.ofSeconds(5);
  final Duration staLength = Duration.ofSeconds(4);
  final Duration ltaLead = Duration.ofSeconds(10);
  final Duration ltaLength = Duration.ofSeconds(7);
  final double triggerThreshold = 12.0;
  final double detriggerThreshold = 10.0;
  final double interpolateGapsSampleRateTolerance = 1.2;
  final double mergeSampleRateTolerance = 1.3;
  final Duration mergeMinimumGapLength = Duration.ofMillis(25);

  @Test
  public void testCreate() throws Exception {
    StaLtaParameters parameters = StaLtaParameters
        .create(algorithmType, waveformTransformation, staLead, staLength, ltaLead, ltaLength,
            triggerThreshold, detriggerThreshold, interpolateGapsSampleRateTolerance,
            mergeSampleRateTolerance, mergeMinimumGapLength);

    assertEquals(algorithmType, parameters.getAlgorithmType());
    assertEquals(waveformTransformation, parameters.getWaveformTransformation());
    assertEquals(staLead, parameters.getStaLead());
    assertEquals(staLength, parameters.getStaLength());
    assertEquals(ltaLead, parameters.getLtaLead());
    assertEquals(ltaLength, parameters.getLtaLength());
    assertEquals(triggerThreshold, parameters.getTriggerThreshold(), 0);
    assertEquals(detriggerThreshold, parameters.getDetriggerThreshold(), 0);
    assertEquals(interpolateGapsSampleRateTolerance,
        parameters.getInterpolateGapsSampleRateTolerance(), 0);
    assertEquals(mergeSampleRateTolerance, parameters.getMergeWaveformsSampleRateTolerance(), 0);
    assertEquals(mergeMinimumGapLength, parameters.getMergeWaveformsMinLength());
  }

  @Test
  public void testCreateNullAlgorithmTypeExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("StaLtaParameters cannot have a null algorithmType");
    StaLtaParameters.create(null, waveformTransformation, staLead, staLength, ltaLead, ltaLength,
        triggerThreshold, detriggerThreshold, interpolateGapsSampleRateTolerance,
        mergeSampleRateTolerance, mergeMinimumGapLength);
  }

  @Test
  public void testCreateNullWaveformTransformationExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("StaLtaParameters cannot have a null waveformTransformation");
    StaLtaParameters
        .create(algorithmType, null, staLead, staLength, ltaLead, ltaLength, triggerThreshold,
            detriggerThreshold, interpolateGapsSampleRateTolerance, mergeSampleRateTolerance,
            mergeMinimumGapLength);
  }

  @Test
  public void testCreateNullStaLeadExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("StaLtaParameters cannot have a null staLead");
    StaLtaParameters
        .create(algorithmType, waveformTransformation, null, staLength, ltaLead, ltaLength,
            triggerThreshold, detriggerThreshold, interpolateGapsSampleRateTolerance,
            mergeSampleRateTolerance, mergeMinimumGapLength);
  }

  @Test
  public void testCreateNullStaLengthExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("StaLtaParameters cannot have a null staLength");
    StaLtaParameters
        .create(algorithmType, waveformTransformation, staLead, null, ltaLead, ltaLength,
            triggerThreshold, detriggerThreshold, interpolateGapsSampleRateTolerance,
            mergeSampleRateTolerance, mergeMinimumGapLength);
  }

  @Test
  public void testCreateNullLtaLeadExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("StaLtaParameters cannot have a null ltaLead");
    StaLtaParameters
        .create(algorithmType, waveformTransformation, staLead, staLength, null, ltaLength,
            triggerThreshold, detriggerThreshold, interpolateGapsSampleRateTolerance,
            mergeSampleRateTolerance, mergeMinimumGapLength);
  }

  @Test
  public void testCreateNullLtaLengthExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("StaLtaParameters cannot have a null ltaLength");
    StaLtaParameters
        .create(algorithmType, waveformTransformation, staLead, staLength, ltaLead, null,
            triggerThreshold, detriggerThreshold, interpolateGapsSampleRateTolerance,
            mergeSampleRateTolerance, mergeMinimumGapLength);
  }

  @Test
  public void testCreateNullMergeWaveformsMinLengthExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("StaLtaParameters cannot have a null mergeWaveformsMinLength");
    StaLtaParameters
        .create(algorithmType, waveformTransformation, staLead, staLength, ltaLead, ltaLength,
            triggerThreshold, detriggerThreshold, interpolateGapsSampleRateTolerance,
            mergeSampleRateTolerance, null);
  }

  @Test
  public void testNegativeStaLengthExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("STA window must have positive length");
    StaLtaParameters
        .create(algorithmType, waveformTransformation, staLead, Duration.ofMillis(-10), ltaLead,
            ltaLength, triggerThreshold, detriggerThreshold, interpolateGapsSampleRateTolerance,
            mergeSampleRateTolerance, mergeMinimumGapLength);
  }

  @Test
  public void testNegativeLtaLengthExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("LTA window must have positive length");
    StaLtaParameters.create(algorithmType, waveformTransformation, staLead, staLength, ltaLead,
        Duration.ofMillis(-10), triggerThreshold, detriggerThreshold,
        interpolateGapsSampleRateTolerance, mergeSampleRateTolerance, mergeMinimumGapLength);
  }

  @Test
  public void testNegativeTriggerThresholdExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("STA/LTA trigger threshold must be positive");
    StaLtaParameters
        .create(algorithmType, waveformTransformation, staLead, staLength, ltaLead, ltaLength, -.1,
            detriggerThreshold, interpolateGapsSampleRateTolerance, mergeSampleRateTolerance,
            mergeMinimumGapLength);
  }

  @Test
  public void testNegativeDetriggerThresholdExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("STA/LTA detrigger threshold must be positive");
    StaLtaParameters
        .create(algorithmType, waveformTransformation, staLead, staLength, ltaLead, ltaLength,
            triggerThreshold, -71, interpolateGapsSampleRateTolerance, mergeSampleRateTolerance,
            mergeMinimumGapLength);
  }
}

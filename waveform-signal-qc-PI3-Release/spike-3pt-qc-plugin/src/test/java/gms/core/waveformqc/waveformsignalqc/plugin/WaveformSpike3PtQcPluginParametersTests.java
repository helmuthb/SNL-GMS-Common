package gms.core.waveformqc.waveformsignalqc.plugin;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WaveformSpike3PtQcPluginParametersTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testCreate() throws Exception {
    WaveformSpike3PtQcPluginParameters params =
        WaveformSpike3PtQcPluginParameters.create(.9,
            2, 3, 4 );
    assertEquals(0.9, params.getMinConsecutiveSampleDifferenceSpikeThreshold(), 0);
    assertEquals(2., params.getRmsAmplitudeRatioThreshold(), 0);
    assertEquals(3, params.getRmsLeadSampleDifferences(), 0);
    assertEquals(4, params.getRmsLagSampleDifferences(), 0);

    params =
        WaveformSpike3PtQcPluginParameters.create(0.9,
            3.0, 4, 5 );
    assertEquals(0.9, params.getMinConsecutiveSampleDifferenceSpikeThreshold(), 0);
    assertEquals(3., params.getRmsAmplitudeRatioThreshold(), 0);
    assertEquals(4, params.getRmsLeadSampleDifferences(), 0);
    assertEquals(5, params.getRmsLagSampleDifferences(), 0);
  }

  @Test
  public void testCreateNotPositiveConsecutiveSampleDifferenceExceedsLowerLimitExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginParameters requires minConsecutiveSampleDifferenceSpikeThreshold > 0.0 and < 1.0");
    WaveformSpike3PtQcPluginParameters.create(0,
        2.0, 3, 4);
  }

  @Test
  public void testCreateNotPositiveConsecutiveSampleDifferenceExceedsUpperLimitExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginParameters requires minConsecutiveSampleDifferenceSpikeThreshold > 0.0 and < 1.0");
    WaveformSpike3PtQcPluginParameters.create(1,
        2.0, 3, 4);
  }

  @Test
  public void testCreateNotPositiveRmsAmplitudeRatioThresholdExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginParameters requires rmsAmplitudeRatioThreshold > 1.0");
    WaveformSpike3PtQcPluginParameters.create(0.9,
        0.0, 3, 4);
  }

  @Test
  public void testCreateNotPositiveRmsLeadSampleDifferenceExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginParameters requires rmsLeadSampleDifferences >= 0");
    WaveformSpike3PtQcPluginParameters.create(0.9,
        2.0, -1, 4);
  }

  @Test
  public void testCreateNotPositiveRmsLagSampleDifferenceExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginParameters requires rmsLagSampleDifferences >= 0");
    WaveformSpike3PtQcPluginParameters.create(0.9,
        2.0, 3, -1);
  }

  @Test
  public void testCreateNotPositiveRmsLeadLagSumExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginParameters requires (rmsLeadSampleDifferences + rmsLagSampleDifferences) >= 2");
    WaveformSpike3PtQcPluginParameters.create(0.9,
        2.0, 1, 0);
  }

}

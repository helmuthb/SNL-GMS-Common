package gms.core.waveformqc.waveformsignalqc.plugin;

import static org.junit.Assert.assertEquals;

import gms.core.waveformqc.waveformsignalqc.plugin.WaveformRepeatedAmplitudeQcPluginParameters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WaveformRepeatedAmplitudeQcPluginParametersTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testCreate() throws Exception {
    WaveformRepeatedAmplitudeQcPluginParameters params = WaveformRepeatedAmplitudeQcPluginParameters
        .create(2, 0.25, 1.2);
    assertEquals(2, params.getMinSeriesLengthInSamples());
    assertEquals(0.25, params.getMaxDeltaFromStartAmplitude(), Double.MIN_NORMAL);
    assertEquals(1.2, params.getMaskMergeThresholdSeconds(), Double.MIN_NORMAL);

    params = WaveformRepeatedAmplitudeQcPluginParameters.create(100, 0.0001, 0.0001);
    assertEquals(100, params.getMinSeriesLengthInSamples());
    assertEquals(0.0001, params.getMaxDeltaFromStartAmplitude(), Double.MIN_NORMAL);
    assertEquals(0.0001, params.getMaskMergeThresholdSeconds(), Double.MIN_NORMAL);
  }

  @Test
  public void testCreateSeriesLengthTooShortExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPluginParameters requires minSeriesLengthInSamples >= 2");
    WaveformRepeatedAmplitudeQcPluginParameters.create(1, 0.25, 1.0);
  }

  @Test
  public void testCreateAmplitudeDeltaNegativeExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPluginParameters requires maxDeltaFromStartAmplitude >= 0.0");
    WaveformRepeatedAmplitudeQcPluginParameters.create(2, 0.0 - Double.MIN_NORMAL, 1.0);
  }

  @Test
  public void testCreateMergeThresholdNegativeExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPluginParameters requires maskMergeThresholdSeconds >= 0.0");
    WaveformRepeatedAmplitudeQcPluginParameters.create(2, 1.0, 0.0 - Double.MIN_NORMAL);
  }
}

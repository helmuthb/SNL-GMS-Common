package gms.core.waveformqc.waveformsignalqc.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import gms.core.waveformqc.waveformqccontrol.plugin.PluginConfiguration;
import gms.core.waveformqc.waveformsignalqc.plugin.WaveformRepeatedAmplitudeQcPluginConfiguration;
import gms.core.waveformqc.waveformsignalqc.plugin.WaveformRepeatedAmplitudeQcPluginParameters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WaveformRepeatedAmplitudeQcPluginConfigurationTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testFromPluginConfiguration() throws Exception {
    PluginConfiguration pluginConfig = PluginConfiguration.builder()
        .add("minSeriesLengthInSamples", 5)
        .add("maxDeltaFromStartAmplitude", .555)
        .add("maskMergeThresholdSeconds", 2.0)
        .build();

    WaveformRepeatedAmplitudeQcPluginConfiguration config = WaveformRepeatedAmplitudeQcPluginConfiguration
        .from(pluginConfig);
    assertNotNull(config);

    WaveformRepeatedAmplitudeQcPluginParameters params = config.createParameters();
    assertEquals(5, params.getMinSeriesLengthInSamples());
    assertEquals(.555, params.getMaxDeltaFromStartAmplitude(), Double.MIN_NORMAL);
    assertEquals(2.0, params.getMaskMergeThresholdSeconds(), Double.MIN_NORMAL);
  }

  @Test
  public void testFromPluginConfigExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPluginConfiguration.from requires non-null PluginConfiguration");
    WaveformRepeatedAmplitudeQcPluginConfiguration.from(null);
  }

  @Test
  public void testFromPluginConfigNoSeriesLengthExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPluginConfiguration.from requires PluginConfiguration with a configuration for minSeriesLengthInSamples");
    WaveformRepeatedAmplitudeQcPluginConfiguration
        .from(PluginConfiguration.builder().add("maxDeltaFromStartAmplitude", .555)
            .add("maskMergeThresholdSeconds", 2.0).build());
  }

  @Test
  public void testFromPluginConfigSeriesLengthTooShortExpectIllegalArgumentException()
      throws Exception {

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPluginConfiguration.from requires minSeriesLengthInSamples >= 2");
    WaveformRepeatedAmplitudeQcPluginConfiguration
        .from(PluginConfiguration.builder()
            .add("minSeriesLengthInSamples", 1)
            .add("maxDeltaFromStartAmplitude", .555)
            .add("maskMergeThresholdSeconds", 2.0).build());
  }

  @Test
  public void testFromPluginConfigSeriesLengthNotIntExpectIllegalArgumentException()
      throws Exception {

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPluginConfiguration.from requires integer value for minSeriesLengthInSamples");
    WaveformRepeatedAmplitudeQcPluginConfiguration
        .from(PluginConfiguration.builder()
            .add("minSeriesLengthInSamples", "string")
            .add("maxDeltaFromStartAmplitude", .555)
            .add("maskMergeThresholdSeconds", 2.0).build());
  }

  @Test
  public void testFromPluginConfigNoStartAmplitudeDeltaExpectIllegalArgumentException()
      throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPluginConfiguration.from requires PluginConfiguration with a configuration for maxDeltaFromStartAmplitude");
    WaveformRepeatedAmplitudeQcPluginConfiguration
        .from(PluginConfiguration.builder()
            .add("minSeriesLengthInSamples", 5)
            .add("maskMergeThresholdSeconds", 2.0).build());
  }

  @Test
  public void testFromPluginConfigNegativeStartAmplitudeDeltaExpectIllegalArgumentException()
      throws Exception {

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPluginConfiguration.from requires maxDeltaFromStartAmplitude >= 0.0");
    WaveformRepeatedAmplitudeQcPluginConfiguration.from(
        PluginConfiguration.builder()
            .add("maxDeltaFromStartAmplitude", 0 - Double.MIN_NORMAL)
            .add("minSeriesLengthInSamples", 5)
            .add("maskMergeThresholdSeconds", 2.0).build());
  }

  @Test
  public void testFromPluginConfigStartAmplitudeDeltaNotDoubleExpectIllegalArgumentException()
      throws Exception {

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPluginConfiguration.from requires double value for maxDeltaFromStartAmplitude");
    WaveformRepeatedAmplitudeQcPluginConfiguration
        .from(PluginConfiguration.builder()
            .add("maxDeltaFromStartAmplitude", "string")
            .add("minSeriesLengthInSamples", 5)
            .add("maskMergeThresholdSeconds", 2.0).build());
  }

  @Test
  public void testFromPluginConfigNoMergeThresholdExpectIllegalArgumentException()
      throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPluginConfiguration.from requires PluginConfiguration with a configuration for maskMergeThresholdSeconds");
    WaveformRepeatedAmplitudeQcPluginConfiguration.from(
        PluginConfiguration.builder()
            .add("minSeriesLengthInSamples", 5)
            .add("maxDeltaFromStartAmplitude", .555).build());
  }

  @Test
  public void testFromPluginConfigNegativeMergeThresholdExpectIllegalArgumentException()
      throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPluginConfiguration.from requires maskMergeThresholdSeconds >= 0.0");
    WaveformRepeatedAmplitudeQcPluginConfiguration.from(
        PluginConfiguration.builder()
            .add("minSeriesLengthInSamples", 5)
            .add("maxDeltaFromStartAmplitude", .555)
            .add("maskMergeThresholdSeconds", 0.0 - Double.MIN_NORMAL)
            .build());
  }

  @Test
  public void testFromPluginConfigMergeThresholdNotDoubleExpectIllegalArgumentException()
      throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPluginConfiguration.from requires double value for maskMergeThresholdSeconds");
    WaveformRepeatedAmplitudeQcPluginConfiguration.from(
        PluginConfiguration.builder()
            .add("minSeriesLengthInSamples", 5)
            .add("maxDeltaFromStartAmplitude", .555)
            .add("maskMergeThresholdSeconds", "2.5")
            .build());
  }
}

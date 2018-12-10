package gms.core.waveformqc.waveformsignalqc.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import gms.core.waveformqc.waveformqccontrol.plugin.PluginConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WaveformSpike3PtQcPluginConfigurationTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testFromPluginConfiguration() throws Exception {
    PluginConfiguration pluginConfig = PluginConfiguration.builder()
        .add("minConsecutiveSampleDifferenceSpikeThreshold", 0.8)
        .add("rmsAmplitudeRatioThreshold", 3.0)
        .add("rmsLeadSampleDifferences", 3)
        .add("rmsLagSampleDifferences", 4)
        .build();

    WaveformSpike3PtQcPluginConfiguration config = WaveformSpike3PtQcPluginConfiguration
        .from(pluginConfig);
    assertNotNull(config);

    WaveformSpike3PtQcPluginParameters params = config.createParameters();
    assertEquals(0.8, params.getMinConsecutiveSampleDifferenceSpikeThreshold(),0);
    assertEquals(3.0, params.getRmsAmplitudeRatioThreshold(),0);
    assertEquals(3, params.getRmsLeadSampleDifferences(),0);
    assertEquals(4, params.getRmsLagSampleDifferences(),0);
  }

  @Test
  public void testFromPluginConfigExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginConfiguration.from requires non-null PluginConfiguration");
    WaveformSpike3PtQcPluginConfiguration.from(null);
  }

  @Test
  public void testFromPluginConfigNoConsecutiveSampleThresholdExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginConfiguration.from requires PluginConfiguration with a configuration for (double) minConsecutiveSampleDifferenceSpikeThreshold");
    WaveformSpike3PtQcPluginConfiguration
        .from(PluginConfiguration.builder().add("dummy", 0.8).
            add("rmsAmplitudeRatioThreshold", 3.0).
            add( "rmsLeadSampleDifferences", 3).
            add("rmsLagSampleDifferences", 4).build());
  }

  @Test
  public void testFromPluginConfigNoRmsAmplitudeRatioThresholdExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginConfiguration.from requires PluginConfiguration with a configuration for (double) rmsAmplitudeRatioThreshold");
    WaveformSpike3PtQcPluginConfiguration
        .from(PluginConfiguration.builder().add("minConsecutiveSampleDifferenceSpikeThreshold", 0.8).
            add("dummy", 3.0).
            add( "rmsLeadSampleDifferences", 3).
            add("rmsLagSampleDifferences", 4).build());
  }

  @Test
  public void testFromPluginConfigNoRmsLeadSampleDifferencesExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginConfiguration.from requires PluginConfiguration with a configuration for (int) rmsLeadSampleDifferences");
    WaveformSpike3PtQcPluginConfiguration
        .from(PluginConfiguration.builder().add("minConsecutiveSampleDifferenceSpikeThreshold", 0.8).
            add("rmsAmplitudeRatioThreshold", 3.0).
            add( "dummy", 3).
            add("rmsLagSampleDifferences", 4).build());
  }

  @Test
  public void testFromPluginConfigNoRmsLagSampleDifferencesExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginConfiguration.from requires PluginConfiguration with a configuration for (int) rmsLagSampleDifferences");
    WaveformSpike3PtQcPluginConfiguration
        .from(PluginConfiguration.builder().add("minConsecutiveSampleDifferenceSpikeThreshold", 0.8).
            add("rmsAmplitudeRatioThreshold", 3.0).
            add( "rmsLeadSampleDifferences", 3).
            add("dummy", 4).build());
  }

  @Test
  public void testFromPluginConfigNegativeConsecutiveSampleThresholdExpectIllegalArgumentException()
      throws Exception {

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginParameters requires minConsecutiveSampleDifferenceSpikeThreshold > 0.0 and < 1.0");
    WaveformSpike3PtQcPluginConfiguration
        .from(PluginConfiguration.builder()
            .add("minConsecutiveSampleDifferenceSpikeThreshold", 0.0).
            add("rmsAmplitudeRatioThreshold", 3.0).
                add( "rmsLeadSampleDifferences", 3).
                add("rmsLagSampleDifferences", 4).build());
  }

  @Test
  public void testFromPluginConfigExcessivePositiveConsecutiveSampleThresholdExpectIllegalArgumentException()
      throws Exception {

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginParameters requires minConsecutiveSampleDifferenceSpikeThreshold > 0.0 and < 1.0");
    WaveformSpike3PtQcPluginConfiguration
        .from(PluginConfiguration.builder()
            .add("minConsecutiveSampleDifferenceSpikeThreshold", 1.0).
                add("rmsAmplitudeRatioThreshold", 3.0).
                add( "rmsLeadSampleDifferences", 3).
                add("rmsLagSampleDifferences", 4).build());
  }

  @Test
  public void testFromPluginConfigNegativeRmsAmplitudeRatioThresholdExpectIllegalArgumentException()
      throws Exception {

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginConfiguration.from requires rmsAmplitudeRatioThreshold > 1.0");
    WaveformSpike3PtQcPluginConfiguration
        .from(PluginConfiguration.builder()
            .add("minConsecutiveSampleDifferenceSpikeThreshold", 0.8).
                add("rmsAmplitudeRatioThreshold", 0.0).
                add( "rmsLeadSampleDifferences", 3).
                add("rmsLagSampleDifferences", 4).build());
  }

  @Test
  public void testFromPluginConfigNegativeRmsLeadSampleDifferencesExpectIllegalArgumentException()
      throws Exception {

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginConfiguration.from requires rmsLeadSampleDifferences >= 0");
    WaveformSpike3PtQcPluginConfiguration
        .from(PluginConfiguration.builder()
            .add("minConsecutiveSampleDifferenceSpikeThreshold", 0.8).
                add("rmsAmplitudeRatioThreshold", 3.0).
                add( "rmsLeadSampleDifferences", -1).
                add("rmsLagSampleDifferences", 4).build());
  }

  @Test
  public void testFromPluginConfigNegativeRmsLagSampleDifferencesExpectIllegalArgumentException()
      throws Exception {

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginConfiguration.from requires rmsLagSampleDifferences >= 0");
    WaveformSpike3PtQcPluginConfiguration
        .from(PluginConfiguration.builder()
            .add("minConsecutiveSampleDifferenceSpikeThreshold", 0.8).
                add("rmsAmplitudeRatioThreshold", 3.0).
                add( "rmsLeadSampleDifferences", 3).
                add("rmsLagSampleDifferences", -1).build());
  }

  @Test
  public void testFromPluginConfigInvalidRMSLeadLagSumExpectIllegalArgumentException()
      throws Exception {

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginConfiguration.from requires (rmsLeadSampleDifferences + rmsLagSampleDifferences) >= 2");
    WaveformSpike3PtQcPluginConfiguration
        .from(PluginConfiguration.builder()
            .add("minConsecutiveSampleDifferenceSpikeThreshold", 0.8).
                add("rmsAmplitudeRatioThreshold", 3.0).
                add( "rmsLeadSampleDifferences", 0).
                add("rmsLagSampleDifferences", 1).build());
  }

  @Test
  public void testFromPluginConfigNonDoubleConsecutiveSampleThresholdExpectIllegalArgumentException()
          throws Exception {

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
            "WaveformSpike3PtQcPluginConfiguration.from requires (double) minConsecutiveSampleDifferenceSpikeThreshold");
    WaveformSpike3PtQcPluginConfiguration
        .from(PluginConfiguration.builder()
            .add("minConsecutiveSampleDifferenceSpikeThreshold", "x").
                add("rmsAmplitudeRatioThreshold", 3.0).
                add("rmsLeadSampleDifferences", 3).
                add("rmsLagSampleDifferences", 4).build());

  }

  @Test
  public void testFromPluginConfigIntegerRmsAmplitudeRatioThresholdExpectIllegalArgumentException()
      throws Exception {

    WaveformSpike3PtQcPluginConfiguration config = WaveformSpike3PtQcPluginConfiguration
        .from(PluginConfiguration.builder()
            .add("minConsecutiveSampleDifferenceSpikeThreshold", 0.8).
                add("rmsAmplitudeRatioThreshold", 9).
                add("rmsLeadSampleDifferences", 3).
                add("rmsLagSampleDifferences", 4).build());

    assertNotNull(config);

    WaveformSpike3PtQcPluginParameters params = config.createParameters();
    assertEquals(9.0, params.getRmsAmplitudeRatioThreshold(),0);
  }

  @Test
  public void testFromPluginConfigNonDoubleRmsAmplitudeRatioThresholdExpectIllegalArgumentException()
      throws Exception {

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginConfiguration.from requires (double) rmsAmplitudeRatioThreshold");
    WaveformSpike3PtQcPluginConfiguration
        .from(PluginConfiguration.builder()
            .add("minConsecutiveSampleDifferenceSpikeThreshold", 0.8).
                add("rmsAmplitudeRatioThreshold", "x").
                add("rmsLeadSampleDifferences", 3).
                add("rmsLagSampleDifferences", 4).build());
  }

  @Test
  public void testFromPluginConfigNonIntegerRmsLeadSampleDifferencesExpectIllegalArgumentException()
      throws Exception {

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginConfiguration.from requires (int) rmsLeadSampleDifferences");
    WaveformSpike3PtQcPluginConfiguration
        .from(PluginConfiguration.builder()
            .add("minConsecutiveSampleDifferenceSpikeThreshold", 0.8).
                add("rmsAmplitudeRatioThreshold", 3.0).
                add("rmsLeadSampleDifferences", "x").
                add("rmsLagSampleDifferences", 4).build());
  }

  @Test
  public void testFromPluginConfigNonIntegerRmsLagSampleDifferencesExpectIllegalArgumentException()
      throws Exception {

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginConfiguration.from requires (int) rmsLagSampleDifferences");
    WaveformSpike3PtQcPluginConfiguration
        .from(PluginConfiguration.builder()
            .add("minConsecutiveSampleDifferenceSpikeThreshold", 0.8).
                add("rmsAmplitudeRatioThreshold", 3.0).
                add("rmsLeadSampleDifferences", 3).
                add("rmsLagSampleDifferences", "x").build());
  }

}

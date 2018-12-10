package gms.core.waveformqc.waveformsignalqc.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import gms.core.waveformqc.waveformqccontrol.plugin.PluginConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WaveformGapQcPluginConfigurationTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testFromPluginConfiguration() throws Exception {
    PluginConfiguration pluginConfig = PluginConfiguration.builder()
        .add("minLongGapLengthInSamples", 1).build();

    WaveformGapQcPluginConfiguration config = WaveformGapQcPluginConfiguration.from(pluginConfig);
    assertNotNull(config);

    WaveformGapQcPluginParameters params = config.createParameters();
    assertEquals(1, params.getMinLongGapLengthInSamples());
  }

  @Test
  public void testFromPluginConfigExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformGapQcPluginConfiguration.from requires non-null PluginConfiguration");
    WaveformGapQcPluginConfiguration.from(null);
  }

  @Test
  public void testFromPluginConfigNoLongGapLengthExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformGapQcPluginConfiguration.from requires PluginConfiguration with a configuration for minLongGapLengthInSamples");
    WaveformGapQcPluginConfiguration.from(PluginConfiguration.builder().build());
  }

  @Test
  public void testFromPluginConfigNotPositiveLongGapLengthExpectIllegalArgumentException()
      throws Exception {

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformGapQcPluginConfiguration.from requires positive minLongGapLengthInSamples");
    WaveformGapQcPluginConfiguration
        .from(PluginConfiguration.builder().add("minLongGapLengthInSamples", 0).build());
  }

  @Test
  public void testFromPluginConfigLongGapLengthNotIntExpectIllegalArgumentException()
      throws Exception {

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformGapQcPluginConfiguration.from requires integer minLongGapLengthInSamples");
    WaveformGapQcPluginConfiguration
        .from(PluginConfiguration.builder().add("minLongGapLengthInSamples", "string").build());
  }
}

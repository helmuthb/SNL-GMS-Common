package gms.core.waveformqc.waveformsignalqc.plugin;

import static org.junit.Assert.assertEquals;

import gms.core.waveformqc.waveformqccontrol.objects.PluginVersion;
import gms.core.waveformqc.waveformqccontrol.plugin.PluginConfiguration;
import java.util.List;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WaveformGapQcPluginComponentTests {

  // TODO: testing initialize and generate left to component or integration testing

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testGetName() throws Exception {
    assertEquals("waveformGapQcPlugin", new WaveformGapQcPluginComponent().getName());
  }

  @Test
  public void testGetVersion() throws Exception {
    assertEquals(PluginVersion.from(1, 0, 0), new WaveformGapQcPluginComponent().getVersion());
  }

  @Test
  public void testInitializeNullParameterExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformGapQcPluginComponent cannot be initialized with null PluginConfiguration");
    new WaveformGapQcPluginComponent().initialize(null);
  }

  @Test
  public void testGenerateNullChannelSegmentsExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformGapQcPluginComponent cannot generateQcMasks with null channelSegments");
    createInitializedPluginComponent()
        .generateQcMasks(null, List.of(), List.of(), UUID.randomUUID());
  }

  @Test
  public void testGenerateNullSohStatusExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformGapQcPluginComponent cannot generateQcMasks with null waveformQcChannelSohStatuses");
    createInitializedPluginComponent()
        .generateQcMasks(List.of(), null, List.of(), UUID.randomUUID());
  }

  @Test
  public void testGenerateNullExistingQcMasksExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformGapQcPluginComponent cannot generateQcMasks with null existingQcMasks");
    createInitializedPluginComponent()
        .generateQcMasks(List.of(), List.of(), null, UUID.randomUUID());
  }

  @Test
  public void testGenerateNullCreationInfoIdExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformGapQcPluginComponent cannot generateQcMasks with null creationInfoId");
    createInitializedPluginComponent().generateQcMasks(List.of(), List.of(), List.of(), null);
  }

  private static WaveformGapQcPluginComponent createInitializedPluginComponent() {
    WaveformGapQcPluginComponent component = new WaveformGapQcPluginComponent();
    component.initialize(pluginConfiguration());

    return component;
  }

  @Test
  public void testInitializeTwiceExpectIllegalStateException() throws Exception {
    WaveformGapQcPluginComponent component = createInitializedPluginComponent();

    exception.expect(IllegalStateException.class);
    exception.expectMessage("WaveformGapQcPluginComponent cannot be initialized twice");
    component.initialize(pluginConfiguration());
  }

  private static PluginConfiguration pluginConfiguration() {
    return PluginConfiguration.builder().add("minLongGapLengthInSamples", 10).build();
  }

  @Test
  public void testGenerateWithoutInitializeExpectIllegalStateExdception() throws Exception {
    exception.expect(IllegalStateException.class);
    exception.expectMessage("WaveformGapQcPluginComponent cannot be used before it is initialized");
    new WaveformGapQcPluginComponent()
        .generateQcMasks(List.of(), List.of(), List.of(), UUID.randomUUID());
  }

}

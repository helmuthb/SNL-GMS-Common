package gms.core.waveformqc.waveformsignalqc.plugin;

import static org.junit.Assert.assertEquals;

import gms.core.waveformqc.waveformqccontrol.objects.PluginVersion;
import gms.core.waveformqc.waveformqccontrol.plugin.PluginConfiguration;
import java.util.List;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WaveformSpike3PtQcPluginComponentTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private static WaveformSpike3PtQcPluginComponent createInitializedPluginComponent() {
    WaveformSpike3PtQcPluginComponent component = new WaveformSpike3PtQcPluginComponent();
    component.initialize(pluginConfiguration());

    return component;
  }

  private static PluginConfiguration pluginConfiguration() {
    return PluginConfiguration.builder()
        .add("minConsecutiveSampleDifferenceSpikeThreshold", 0.8)
        .add("rmsAmplitudeRatioThreshold", 3.0)
        .add("rmsLeadSampleDifferences", 3)
        .add("rmsLagSampleDifferences", 4)
        .build();
  }

  @Test
  public void testGetName() throws Exception {
    assertEquals("waveformSpike3PtQcPlugin",
        new WaveformSpike3PtQcPluginComponent().getName());
  }

  @Test
  public void testGetVersion() throws Exception {
    assertEquals(PluginVersion.from(1, 0, 0),
        new WaveformSpike3PtQcPluginComponent().getVersion());
  }

  @Test
  public void testInitializeNullParameterExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginComponent cannot be initialized with null PluginConfiguration");
    new WaveformSpike3PtQcPluginComponent().initialize(null);
  }

  @Test
  public void testInitializeTwiceExpectIllegalStateException() throws Exception {
    WaveformSpike3PtQcPluginComponent component = createInitializedPluginComponent();

    exception.expect(IllegalStateException.class);
    exception
        .expectMessage("WaveformSpike3PtQcPluginComponent cannot be initialized twice");
    component.initialize(pluginConfiguration());
  }

  @Test
  public void testGenerateWithoutInitializeExpectIllegalStateExdception() throws Exception {
    exception.expect(IllegalStateException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginComponent cannot be used before it is initialized");
    new WaveformSpike3PtQcPluginComponent()
        .generateQcMasks(List.of(), List.of(), List.of(), UUID.randomUUID());
  }

  @Test
  public void testGenerateNullChannelSegmentsExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginComponent cannot generateQcMasks with null channelSegments");
    createInitializedPluginComponent()
        .generateQcMasks(null, List.of(), List.of(), UUID.randomUUID());
  }

  @Test
  public void testGenerateNullSohStatusExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginComponent cannot generateQcMasks with null waveformQcChannelSohStatuses");
    createInitializedPluginComponent()
        .generateQcMasks(List.of(), null, List.of(), UUID.randomUUID());
  }

  @Test
  public void testGenerateNullExistingQcMasksExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginComponent cannot generateQcMasks with null existingQcMasks");
    createInitializedPluginComponent()
        .generateQcMasks(List.of(), List.of(), null, UUID.randomUUID());
  }

  @Test
  public void testGenerateNullCreationInfoIdExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformSpike3PtQcPluginComponent cannot generateQcMasks with null creationInfoId");
    createInitializedPluginComponent().generateQcMasks(List.of(), List.of(), List.of(), null);
  }

}

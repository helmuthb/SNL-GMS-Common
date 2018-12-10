package gms.core.waveformqc.waveformsignalqc.plugin;

import static org.junit.Assert.assertEquals;

import gms.core.waveformqc.waveformqccontrol.objects.PluginVersion;
import gms.core.waveformqc.waveformqccontrol.plugin.PluginConfiguration;
import gms.core.waveformqc.waveformsignalqc.plugin.WaveformRepeatedAmplitudeQcPluginComponent;
import java.util.List;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WaveformRepeatedAmplitudeQcPluginComponentTests {

  // TODO: testing initialize and generate left to component or integration testing

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private static WaveformRepeatedAmplitudeQcPluginComponent createInitializedPluginComponent() {
    WaveformRepeatedAmplitudeQcPluginComponent component = new WaveformRepeatedAmplitudeQcPluginComponent();
    component.initialize(pluginConfiguration());

    return component;
  }

  private static PluginConfiguration pluginConfiguration() {
    return PluginConfiguration.builder()
        .add("minSeriesLengthInSamples", 2)
        .add("maxDeltaFromStartAmplitude", 2.5)
        .add("maskMergeThresholdSeconds", 0.001)
        .build();
  }

  @Test
  public void testGetName() throws Exception {
    assertEquals("waveformRepeatedAmplitudeQcPlugin",
        new WaveformRepeatedAmplitudeQcPluginComponent().getName());
  }

  @Test
  public void testGetVersion() throws Exception {
    assertEquals(PluginVersion.from(1, 0, 0),
        new WaveformRepeatedAmplitudeQcPluginComponent().getVersion());
  }

  @Test
  public void testInitializeNullParameterExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPluginComponent cannot be initialized with null PluginConfiguration");
    new WaveformRepeatedAmplitudeQcPluginComponent().initialize(null);
  }

  @Test
  public void testInitializeTwiceExpectIllegalStateException() throws Exception {
    WaveformRepeatedAmplitudeQcPluginComponent component = createInitializedPluginComponent();

    exception.expect(IllegalStateException.class);
    exception
        .expectMessage("WaveformRepeatedAmplitudeQcPluginComponent cannot be initialized twice");
    component.initialize(pluginConfiguration());
  }

  @Test
  public void testGenerateWithoutInitializeExpectIllegalStateExdception() throws Exception {
    exception.expect(IllegalStateException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPluginComponent cannot be used before it is initialized");
    new WaveformRepeatedAmplitudeQcPluginComponent()
        .generateQcMasks(List.of(), List.of(), List.of(), UUID.randomUUID());
  }

  @Test
  public void testGenerateNullChannelSegmentsExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPluginComponent cannot generateQcMasks with null channelSegments");
    createInitializedPluginComponent()
        .generateQcMasks(null, List.of(), List.of(), UUID.randomUUID());
  }

  @Test
  public void testGenerateNullSohStatusExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPluginComponent cannot generateQcMasks with null waveformQcChannelSohStatuses");
    createInitializedPluginComponent()
        .generateQcMasks(List.of(), null, List.of(), UUID.randomUUID());
  }

  @Test
  public void testGenerateNullExistingQcMasksExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPluginComponent cannot generateQcMasks with null existingQcMasks");
    createInitializedPluginComponent()
        .generateQcMasks(List.of(), List.of(), null, UUID.randomUUID());
  }

  @Test
  public void testGenerateNullCreationInfoIdExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformRepeatedAmplitudeQcPluginComponent cannot generateQcMasks with null creationInfoId");
    createInitializedPluginComponent().generateQcMasks(List.of(), List.of(), List.of(), null);
  }
}

package gms.core.waveformqc.channelsohqc.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import gms.core.waveformqc.waveformqccontrol.objects.PluginVersion;
import gms.core.waveformqc.waveformqccontrol.objects.RegistrationInfo;
import gms.core.waveformqc.waveformqccontrol.plugin.PluginConfiguration;
import gms.core.waveformqc.waveformqccontrol.plugin.WaveformQcPlugin;
import gms.core.waveformqc.waveformqccontrol.plugin.WaveformQcPluginRegistry;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ChannelSohQcPluginComponentTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private TestData testData;
  private WaveformQcPluginRegistry registry;
  private ChannelSohQcPluginComponent pluginComponent;

  @Before
  public void setUp() throws Exception {
    testData = new TestData();
    registry = new WaveformQcPluginRegistry();
    pluginComponent = new ChannelSohQcPluginComponent();

    // TODO: need to implement initialize for real
    pluginComponent.initialize(getDefaultPluginConfiguration());
  }

  private PluginConfiguration getDefaultPluginConfiguration() {
    final Map<String, Object> defaults = new HashMap<>();
    defaults.put("mergeThreshold", "PT0.037S");
    defaults.put("shouldCreateQcMasks", true);

    return PluginConfiguration.builder().add("defaults", defaults).build();
  }

  @Test
  public void testRegistration() throws Exception {
    assertEquals("channelSohQcPlugin", pluginComponent.getName());
    assertEquals(PluginVersion.from(1, 0, 0), pluginComponent.getVersion());

    registry.register(pluginComponent);

    Optional<WaveformQcPlugin> registeredPlugin = registry
        .lookup(RegistrationInfo.from(pluginComponent.getName(), pluginComponent.getVersion()));
    assertTrue(registeredPlugin.isPresent());
    assertEquals(pluginComponent, registeredPlugin.get());
  }

  @Test
  public void testQcMaskGeneration() {
    final Collection<QcMask> existingMasks = Arrays
        .asList(testData.qcMaskChan1AfterGap, testData.qcMaskChan1, testData.qcMaskChan1NoOverlap);

    List<QcMask> qcMasks = pluginComponent
        .generateQcMasks(Collections.emptyList(), testData.overlapWaveformQcChannelSohStatus,
            existingMasks, UUID.randomUUID()).collect(Collectors.toList());

    testData.verifyPluginMergeQcMasks(qcMasks);
  }

  @Test
  public void testGenerateQcMasksNullWaveformsExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception
        .expectMessage("ChannelSohQcPluginComponent generateQcMasks cannot accept null waveforms");
    pluginComponent.generateQcMasks(null, Collections.emptyList(), Collections.emptyList(),
        UUID.randomUUID());
  }

  @Test
  public void testGenerateQcMasksNullSohExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "ChannelSohQcPluginComponent generateQcMasks cannot accept null acquiredChannelSohs");
    pluginComponent.generateQcMasks(Collections.emptyList(), null, Collections.emptyList(),
        UUID.randomUUID());
  }

  @Test
  public void testGenerateQcMasksNullQcMasksExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "ChannelSohQcPluginComponent generateQcMasks cannot accept null existing QcMasks");
    pluginComponent.generateQcMasks(Collections.emptyList(), Collections.emptyList(), null,
        UUID.randomUUID());
  }

  @Test
  public void testGenerateCreationInfoIdNullCreationInfoIdExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
            "ChannelSohQcPluginComponent generateQcMasks cannot accept null existing creationInfoId");
    pluginComponent.generateQcMasks(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);
  }

  @Test
  public void testInitializeNullConfigurationExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "ChannelSohQcPluginComponent initialize cannot accept null pluginConfiguration");
    new ChannelSohQcPluginComponent().initialize(null);
  }

  @Test
  public void testInitializeTwiceExpectIllegalStateException() throws Exception {
    exception.expect(IllegalStateException.class);
    exception.expectMessage("ChannelSohQcPluginComponent cannot be initialized twice");
    pluginComponent.initialize(PluginConfiguration.builder().build());
  }

  @Test
  public void testGenerateWithoutInitializeExpectIllegalStateExdception() throws Exception {
    exception.expect(IllegalStateException.class);
    exception.expectMessage("ChannelSohQcPluginComponent cannot be used before it is initialized");
    new ChannelSohQcPluginComponent()
        .generateQcMasks(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
            UUID.randomUUID());
  }

  @Test
  public void testInitializeParsesDefaultCreateMasksConfiguration() throws Exception {
    final Map<String, Object> defaults = new HashMap<>();
    defaults.put("mergeThreshold", "PT0.001S");
    defaults.put("shouldCreateQcMasks", false);

    ChannelSohQcPluginComponent plugin = new ChannelSohQcPluginComponent();
    plugin.initialize(PluginConfiguration.builder().add("defaults", defaults).build());

    // Verify no masks created from the default false shouldCreateQcMasks
    List<QcMask> qcMasks = plugin
        .generateQcMasks(Collections.emptyList(), testData.overlapWaveformQcChannelSohStatus,
            Collections.emptyList(), UUID.randomUUID()).collect(Collectors.toList());

    assertTrue(qcMasks.isEmpty());
  }

  @Test
  public void testInitializeParsesDefaultMergeThresholdConfiguration() throws Exception {
    final Map<String, Object> defaults = new HashMap<>();
    defaults.put("mergeThreshold", "PT0.001S");
    defaults.put("shouldCreateQcMasks", true);

    ChannelSohQcPluginComponent plugin = new ChannelSohQcPluginComponent();
    plugin.initialize(PluginConfiguration.builder().add("defaults", defaults).build());

    // Verify no masks merged with short default mergeThreshold
    final Collection<QcMask> existingMasks = Arrays
        .asList(testData.qcMaskChan1AfterGap, testData.qcMaskChan1, testData.qcMaskChan1NoOverlap);

    List<QcMask> qcMasks = plugin
        .generateQcMasks(Collections.emptyList(), testData.overlapWaveformQcChannelSohStatus,
            existingMasks, UUID.randomUUID()).collect(Collectors.toList());

    TestData.verifySingleMask(qcMasks, testData.qcMaskNewOverlap, existingMasks);
  }

  @Test
  public void testInitializeParsesConfigurationOverrides() throws Exception {
    final Map<String, Object> defaults = new HashMap<>();
    defaults.put("mergeThreshold", "PT0.0017S");
    defaults.put("shouldCreateQcMasks", false);

    final Map<String, Object> override = new HashMap<>();
    override.put("type", AcquiredChannelSohType.CALIBRATION_UNDERWAY.toString());
    override.put("mergeThreshold", "PT0.037S");
    override.put("shouldCreateQcMasks", true);
    final List<Map<String, Object>> overrides = Collections.singletonList(override);

    ChannelSohQcPluginComponent plugin = new ChannelSohQcPluginComponent();
    plugin.initialize(
        PluginConfiguration.builder()
            .add("defaults", defaults)
            .add("overrides", overrides)
            .build());

    final Collection<QcMask> existingMasks = Arrays
        .asList(testData.qcMaskChan1AfterGap, testData.qcMaskChan1, testData.qcMaskChan1NoOverlap);

    List<QcMask> qcMasks = plugin
        .generateQcMasks(Collections.emptyList(), testData.overlapWaveformQcChannelSohStatus,
            existingMasks, UUID.randomUUID()).collect(Collectors.toList());

    testData.verifyPluginMergeQcMasks(qcMasks);
  }
}

package gms.core.signaldetection.staltapowerdetector;

import static org.junit.Assert.assertEquals;

import gms.core.signaldetection.signaldetectorcontrol.objects.PluginVersion;
import gms.core.signaldetection.signaldetectorcontrol.plugin.PluginConfiguration;
import java.io.InputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.yaml.snakeyaml.Yaml;

public class StaLtaPowerDetectorComponentTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private StaLtaPowerDetectorComponent component = new StaLtaPowerDetectorComponent();

  @Test
  public void testGetName() throws Exception {
    assertEquals("staLtaPowerDetectorPlugin", new StaLtaPowerDetectorComponent().getName());
  }

  @Test
  public void testGetVersion() throws Exception {
    assertEquals(PluginVersion.from(1, 0, 0), new StaLtaPowerDetectorComponent().getVersion());
  }

  @Test
  public void testInitializeTwiceExpectIllegalStateException() throws Exception {
    final InputStream pluginConfigInputStream = getClass().getClassLoader()
        .getResourceAsStream("configuration.yaml");
    final PluginConfiguration pluginConfiguration = PluginConfiguration
        .from(new Yaml().load(pluginConfigInputStream));

    StaLtaPowerDetectorComponent component = new StaLtaPowerDetectorComponent();
    component.initialize(pluginConfiguration);

    exception.expect(IllegalStateException.class);
    exception.expectMessage("StaLtaPowerDetectorComponent cannot be initialized twice");
    component.initialize(pluginConfiguration);
  }

  @Test
  public void testDetectWithoutInitializeExpectIllegalStateException() throws Exception {
    exception.expect(IllegalStateException.class);
    exception.expectMessage("StaLtaPowerDetectorComponent cannot be used before it is initialized");
    new StaLtaPowerDetectorComponent().detectSignals(TestFixtures.randomChannelSegment());
  }

  @Test
  public void testInitializeNullParameterExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "StaLtaPowerDetectorComponent cannot be initialized with null PluginConfiguration");
    new StaLtaPowerDetectorComponent().initialize(null);
  }
}

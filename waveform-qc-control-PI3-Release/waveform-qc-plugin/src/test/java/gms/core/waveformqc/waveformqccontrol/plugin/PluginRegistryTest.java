package gms.core.waveformqc.waveformqccontrol.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import gms.core.waveformqc.waveformqccontrol.objects.PluginVersion;
import gms.core.waveformqc.waveformqccontrol.objects.RegistrationInfo;
import gms.core.waveformqc.waveformqccontrol.plugin.mock.MockWaveformQcPlugin;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the plugin registry
 */
public class PluginRegistryTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testPluginRegistryNullThrowsException() throws Exception {
    WaveformQcPluginRegistry registry = new WaveformQcPluginRegistry();

    exception.expect(IllegalArgumentException.class);
    exception
        .expectMessage("Error retrieving plugin: null is an invalid RegistrationInformation key");
    registry.lookup(null);
  }

  @Test
  public void testPluginRegistryUnregistered() throws Exception {
    WaveformQcPluginRegistry registry = new WaveformQcPluginRegistry();

    Optional<WaveformQcPlugin> plugin = registry
        .lookup(RegistrationInfo.from("test", PluginVersion.from(0, 0, 0)));
    assertFalse(plugin.isPresent());
  }

  @Test
  public void testPluginRegistry() throws Exception {
    WaveformQcPluginRegistry registry = new WaveformQcPluginRegistry();
    WaveformQcPlugin plugin = MockWaveformQcPlugin.from("mock",
        PluginVersion.from(1, 0, 0));

    RegistrationInfo registrationInfo = registry.register(plugin);
    Optional<WaveformQcPlugin> registeredPlugin = registry.lookup(registrationInfo);

    assertTrue(registeredPlugin.isPresent());
    assertEquals(plugin, registeredPlugin.get());
  }

  @Test
  public void testPluginRegistrySameValuesDifferentObjects() throws Exception {
    WaveformQcPluginRegistry registry = new WaveformQcPluginRegistry();
    WaveformQcPlugin plugin = MockWaveformQcPlugin.from("mock",
        PluginVersion.from(1, 0, 0));

    registry.register(plugin);

    RegistrationInfo differentRegistration = RegistrationInfo.from("mock",
        PluginVersion.from(1, 0, 0));

    Optional<WaveformQcPlugin> registeredPlugin = registry.lookup(differentRegistration);

    assertTrue(registeredPlugin.isPresent());
    assertEquals(plugin, registeredPlugin.get());
  }

  @Test
  public void testPluginRegistryDifferentValues() throws Exception {

    WaveformQcPluginRegistry registry = new WaveformQcPluginRegistry();
    WaveformQcPlugin plugin = MockWaveformQcPlugin.from("mock",
        PluginVersion.from(1, 0, 0));

    registry.register(plugin);

    RegistrationInfo differentRegistration = RegistrationInfo.from("not-mock",
        PluginVersion.from(1, 0, 0));

    Optional<WaveformQcPlugin> registeredPlugin = registry.lookup(differentRegistration);
    assertFalse(registeredPlugin.isPresent());

    differentRegistration = RegistrationInfo.from("mock",
        PluginVersion.from(2, 0, 0));

    registeredPlugin = registry.lookup(differentRegistration);
    assertFalse(registeredPlugin.isPresent());

    differentRegistration = RegistrationInfo.from("mock",
        PluginVersion.from(1, 1, 0));

    registeredPlugin = registry.lookup(differentRegistration);
    assertFalse(registeredPlugin.isPresent());

    differentRegistration = RegistrationInfo.from("mock",
        PluginVersion.from(1, 0, 1));

    registeredPlugin = registry.lookup(differentRegistration);
    assertFalse(registeredPlugin.isPresent());
  }

  @Test
  public void testPluginRegistryMultiplePlugins() throws Exception {
    WaveformQcPluginRegistry registry = new WaveformQcPluginRegistry();

    WaveformQcPlugin plugin1 = MockWaveformQcPlugin.from("mock1",
        PluginVersion.from(1, 0, 0));

    WaveformQcPlugin plugin2 = MockWaveformQcPlugin.from("mock2",
        PluginVersion.from(1, 0, 0));

    WaveformQcPlugin plugin3 = MockWaveformQcPlugin.from("mock1",
        PluginVersion.from(2, 0, 0));

    WaveformQcPlugin plugin4 = MockWaveformQcPlugin.from("mock1",
        PluginVersion.from(1, 1, 0));

    WaveformQcPlugin plugin5 = MockWaveformQcPlugin.from("mock1",
        PluginVersion.from(1, 0, 1));

    RegistrationInfo registrationInfo1 = registry.register(plugin1);
    RegistrationInfo registrationInfo2 = registry.register(plugin2);
    RegistrationInfo registrationInfo3 = registry.register(plugin3);
    RegistrationInfo registrationInfo4 = registry.register(plugin4);
    RegistrationInfo registrationInfo5 = registry.register(plugin5);

    Optional<WaveformQcPlugin> registeredPlugin1 = registry.lookup(registrationInfo1);
    Optional<WaveformQcPlugin> registeredPlugin2 = registry.lookup(registrationInfo2);
    Optional<WaveformQcPlugin> registeredPlugin3 = registry.lookup(registrationInfo3);
    Optional<WaveformQcPlugin> registeredPlugin4 = registry.lookup(registrationInfo4);
    Optional<WaveformQcPlugin> registeredPlugin5 = registry.lookup(registrationInfo5);

    assertTrue(registeredPlugin1.isPresent());
    assertTrue(registeredPlugin2.isPresent());
    assertTrue(registeredPlugin3.isPresent());
    assertTrue(registeredPlugin4.isPresent());
    assertTrue(registeredPlugin5.isPresent());

    assertEquals(plugin1, registeredPlugin1.get());
    assertEquals(plugin2, registeredPlugin2.get());
    assertEquals(plugin3, registeredPlugin3.get());
    assertEquals(plugin4, registeredPlugin4.get());
    assertEquals(plugin5, registeredPlugin5.get());

  }
}

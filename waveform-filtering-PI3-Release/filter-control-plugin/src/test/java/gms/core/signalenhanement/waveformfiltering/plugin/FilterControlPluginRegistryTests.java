package gms.core.signalenhanement.waveformfiltering.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gms.core.signalenhancement.waveformfiltering.objects.PluginVersion;
import gms.core.signalenhancement.waveformfiltering.objects.RegistrationInfo;
import gms.core.signalenhancement.waveformfiltering.plugin.FilterControlPluginRegistry;
import gms.core.signalenhancement.waveformfiltering.plugin.FilterPlugin;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the filter control plugin registry
 */
public class FilterControlPluginRegistryTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testPluginRegistryNullThrowsException() {
    FilterControlPluginRegistry registry = new FilterControlPluginRegistry();

    exception.expect(NullPointerException.class);
    exception
        .expectMessage("Error retrieving plugin: null is an invalid RegistrationInformation key");
    registry.lookup(null);
  }

  @Test
  public void testPluginRegistryUnregistered() {
    FilterControlPluginRegistry registry = new FilterControlPluginRegistry();

    Optional<FilterPlugin> plugin = registry
        .lookup(RegistrationInfo.from("test", PluginVersion.from(0, 0, 0)));
    assertFalse(plugin.isPresent());
  }

  @Test
  public void testPluginRegistry() {
    FilterControlPluginRegistry registry = new FilterControlPluginRegistry();

    PluginVersion pluginVersion = PluginVersion.from(1, 0, 0);
    FilterPlugin plugin = mock(FilterPlugin.class);
    when(plugin.getVersion()).thenReturn(pluginVersion);
    when(plugin.getName()).thenReturn("AFilterPlugin");

    RegistrationInfo registrationInfo = registry.register(plugin);
    Optional<FilterPlugin> registeredPlugin = registry.lookup(registrationInfo);

    assertTrue(registeredPlugin.isPresent());
    assertEquals(plugin, registeredPlugin.get());

    assertEquals(plugin.getVersion(), registeredPlugin.get().getVersion());
    assertEquals(plugin.getName(), registeredPlugin.get().getName());

  }

  @Test
  public void testPluginRegistryMultipleDifferentRegistrations() {
    FilterControlPluginRegistry registry = new FilterControlPluginRegistry();

    // create 3 plugins to be added to registry
    PluginVersion pluginVersion1 = PluginVersion.from(1, 0, 0);
    FilterPlugin plugin1 = mock(FilterPlugin.class);
    when(plugin1.getVersion()).thenReturn(pluginVersion1);
    when(plugin1.getName()).thenReturn("AFilterPluginVersion1");

    PluginVersion pluginVersion2 = PluginVersion.from(2, 0, 0);
    FilterPlugin plugin2 = mock(FilterPlugin.class);
    when(plugin2.getVersion()).thenReturn(pluginVersion2);
    when(plugin2.getName()).thenReturn("AFilterPluginVersion2");

    PluginVersion pluginVersion3 = PluginVersion.from(3, 0, 0);
    FilterPlugin plugin3 = mock(FilterPlugin.class);
    when(plugin3.getVersion()).thenReturn(pluginVersion3);
    when(plugin3.getName()).thenReturn("AFilterPluginVersion3");

    // create 2 registrationInfo objects that will not be added
    RegistrationInfo differentRegistration1 = RegistrationInfo.from("DifferentFilter1",
        PluginVersion.from(1, 1, 1));

    RegistrationInfo differentRegistration2 = RegistrationInfo.from("DifferentFilter2",
        PluginVersion.from(2, 2, 2));

    // get 3 added plugin registration info objects and the two that were not added
    RegistrationInfo registrationInfo1 = registry.register(plugin1);
    Optional<FilterPlugin> registeredPlugin1 = registry.lookup(registrationInfo1);

    RegistrationInfo registrationInfo2 = registry.register(plugin2);
    Optional<FilterPlugin> registeredPlugin2 = registry.lookup(registrationInfo2);

    RegistrationInfo registrationInfo3 = registry.register(plugin3);
    Optional<FilterPlugin> registeredPlugin3 = registry.lookup(registrationInfo3);

    Optional<FilterPlugin> differentPlugin1 = registry.lookup(differentRegistration1);
    Optional<FilterPlugin> differentPlugin2 = registry.lookup(differentRegistration2);

    // check both plugin1, 2 and 3 are registered, and differentRegistration1 and 2 were not
    assertTrue(registeredPlugin1.isPresent());
    assertTrue(registeredPlugin2.isPresent());
    assertTrue(registeredPlugin3.isPresent());
    assertFalse(differentPlugin1.isPresent());
    assertFalse(differentPlugin2.isPresent());

    // validate names for registered plugins
    assertEquals(plugin1.getName(), registeredPlugin1.get().getName());
    assertEquals(plugin2.getName(), registeredPlugin2.get().getName());
    assertEquals(plugin3.getName(), registeredPlugin3.get().getName());
  }
}
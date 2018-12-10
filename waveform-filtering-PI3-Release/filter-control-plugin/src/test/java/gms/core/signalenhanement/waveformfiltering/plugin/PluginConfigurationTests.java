package gms.core.signalenhanement.waveformfiltering.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import gms.core.signalenhancement.waveformfiltering.plugin.PluginConfiguration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PluginConfigurationTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testFromNullConfigurationParametersExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error creating PluginConfiguration, cannot create from null configuration parameters.");
    PluginConfiguration.from(null);
  }

  @Test
  public void testGetParameterExistingKey() {
    Map<String, Object> map = new HashMap<>();
    map.put("configKey1", "configValue1");
    PluginConfiguration configuration = PluginConfiguration.from(map);
    Optional<Object> parameter = configuration.getParameter("configKey1");
    assertTrue(parameter.isPresent());
    assertEquals("configValue1", parameter.get());
  }

  @Test
  public void testGetParameterNonExistentKey() {
    PluginConfiguration configuration = PluginConfiguration.from(new HashMap<>());
    assertFalse(configuration.getParameter("configKey1").isPresent());
  }
}

package gms.core.waveformqc.waveformqccontrol.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import gms.core.waveformqc.waveformqccontrol.configuration.ServiceConfiguration;
import gms.core.waveformqc.waveformqccontrol.configuration.ConfigurationManagement;
import gms.core.waveformqc.waveformqccontrol.configuration.OsdGatewayClientConfiguration;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Suite for testing capabilities of configuration management. NOTE: ServiceConfiguration Management
 * can only be initialized once during a full run of the unit tests, otherwise a runtime exception
 * is thrown.  Because of this, we initialize configuration only in this unit test, and as such all
 * tests relating to configuration should be placed here.
 */
public class ServiceConfigurationComponentTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @BeforeClass
  public static void initialize() {
    ConfigurationManagement.initialize();
  }

  @Test
  public void testCreateConfiguration() {
    ServiceConfiguration config = ServiceConfiguration.create();

    assertNotNull(config);
    assertEquals("/waveform-qc/waveform-qc-control", config.getBaseUri());
    assertEquals(8080, config.getPort());
    assertEquals(2, config.getMinThreads());
    assertEquals(10, config.getMaxThreads());
    assertEquals(10000, config.getIdleTimeOutMillis());
  }

  @Test
  public void testCreateGatewayConfiguration() {
    OsdGatewayClientConfiguration config = OsdGatewayClientConfiguration.create();

    assertNotNull(config);
    assertEquals("localhost", config.getHost());
    assertEquals(8081, config.getPort());
    assertEquals("/waveform-qc/waveform-qc-control/osd-gateway", config.getBaseUri());
  }

}

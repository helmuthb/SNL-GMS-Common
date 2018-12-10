package gms.core.waveformqc.waveformqccontrol.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import gms.core.waveformqc.waveformqccontrol.osdgateway.configuration.ConfigurationManagement;
import gms.core.waveformqc.waveformqccontrol.osdgateway.configuration.OsdGatewayConfiguration;
import gms.core.waveformqc.waveformqccontrol.osdgateway.configuration.ServiceConfiguration;
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
  public void testCreateServiceConfiguration() {
    ServiceConfiguration config = ServiceConfiguration.create();

    assertNotNull(config);
    assertEquals("/waveform-qc/waveform-qc-control", config.getBaseUri());
    assertEquals(8081, config.getPort());
    assertEquals(2, config.getMinThreads());
    assertEquals(10, config.getMaxThreads());
    assertEquals(10000, config.getIdleTimeOutMillis());
  }

  @Test
  public void testCreateOsdGatewayConfiguration() {
    OsdGatewayConfiguration config = OsdGatewayConfiguration.create();

    assertNotNull(config);
    assertEquals("jdbc:postgresql://localhost:5432/xmp_metadata",
        config.getWaveformPersistenceUrl());
    assertEquals("jdbc:postgresql://localhost:5432/xmp_metadata",
        config.getStationSohPersistenceUrl());
    assertEquals("jdbc:postgresql://localhost:5432/xmp_metadata",
        config.getQcMaskPersistenceUrl());
    assertEquals("jdbc:postgresql://localhost:5432/xmp_metadata",
        config.getProvenancePersistenceUrl());
  }

}

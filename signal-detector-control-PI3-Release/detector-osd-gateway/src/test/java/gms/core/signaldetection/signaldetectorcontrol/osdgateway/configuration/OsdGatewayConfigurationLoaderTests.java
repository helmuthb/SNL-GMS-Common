package gms.core.signaldetection.signaldetectorcontrol.osdgateway.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class OsdGatewayConfigurationLoaderTests {

  @Test
  public void testLoadConfiguration() {
    OsdGatewayConfiguration config = OsdGatewayConfigurationLoader.load();

    assertNotNull(config);
    assertEquals("jdbc:postgresql://localhost:5432/xmp_metadata",
        config.getWaveformPersistenceUrl());
    assertEquals("jdbc:postgresql://localhost:5432/xmp_metadata",
        config.getProvenancePersistenceUrl());
    assertEquals("jdbc:postgresql://localhost:5432/xmp_metadata",
        config.getSignalDetectionUrl());
  }
}

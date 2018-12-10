package gms.core.signaldetection.signaldetectorcontrol.osdgateway.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class OsdGatewayConfigurationTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testBuilder() {
    OsdGatewayConfiguration.Builder builder = OsdGatewayConfiguration
        .builder();

    OsdGatewayConfiguration config = builder.build();

    assertNotNull(config.getWaveformPersistenceUrl());
    assertNotNull(config.getProvenancePersistenceUrl());
    assertNotNull(config.getSignalDetectionUrl());
  }

  @Test
  public void testBuilderOverrides() {
    OsdGatewayConfiguration.Builder builder = OsdGatewayConfiguration.builder();

    builder.setWaveformPersistenceUrl("url1");
    builder.setProvenancePersistenceUrl("url2");
    builder.setSignalDetectionUrl("url3");

    OsdGatewayConfiguration config = builder.build();

    assertEquals("url1", config.getWaveformPersistenceUrl());
    assertEquals("url2", config.getProvenancePersistenceUrl());
    assertEquals("url3", config.getSignalDetectionUrl());
  }

  @Test
  public void testBuilderNullChecks() throws Exception {
    OsdGatewayConfiguration.Builder builder = OsdGatewayConfiguration.builder();
    TestUtilities.checkMethodValidatesNullArguments(builder, "setWaveformPersistenceUrl", "url1");
    TestUtilities.checkMethodValidatesNullArguments(builder, "setProvenancePersistenceUrl", "url2");
    TestUtilities.checkMethodValidatesNullArguments(builder, "setSignalDetectionUrl", "url3");
  }

}

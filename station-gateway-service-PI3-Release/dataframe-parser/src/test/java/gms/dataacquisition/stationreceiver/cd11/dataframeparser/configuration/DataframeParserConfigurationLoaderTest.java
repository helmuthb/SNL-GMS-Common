package gms.dataacquisition.stationreceiver.cd11.dataframeparser.configuration;

import static junit.framework.TestCase.*;

import org.junit.Test;


public class DataframeParserConfigurationLoaderTest {

  @Test
  public void testLoader() {
    DataframeParserConfig config = DataframeParserConfigurationLoader.load();

    assertNotNull(config);
    assertNotNull(config.osdGatewayHostname);
    assertNotNull(config.monitoredDirLocation);

    assertTrue(config.osdGatewayHostname.equals("254.253.252.251"));
    assertTrue(config.osdGatewayPort == 64999);
    assertTrue(config.monitoredDirLocation.equals("shared-volume/dataframes/"));
  }
}

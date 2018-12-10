package gms.core.signalenhancement.waveformfiltering.osdgateway.client;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HttpClientConfigurationLoaderTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private URL propertiesUrl;

  @Before
  public void setUp() {
    final String filePath = "gms/core/signalenhancement/waveformfiltering/control/osdgateway/client/osdGatewayClient.properties";
    propertiesUrl = Thread.currentThread().getContextClassLoader().getResource(filePath);
    assertNotNull("Test could not load properties file " + filePath, propertiesUrl);
  }

  private static URL urlForPath(String path) {
    URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    assertNotNull("Test could not load properties file " + path, url);
    return url;
  }

  @Test
  public void testLoad() {
    HttpClientConfiguration config = HttpClientConfigurationLoader
        .load("osdGateway_", propertiesUrl);
    assertNotNull(config);
    assertEquals("waveform-filtering-osd-gateway", config.getHost());
    assertEquals(8080, config.getPort());
    assertEquals("/signal-enhancement/waveform-filtering-test-base/osd-gateway",
        config.getBasePath());
  }

  @Test
  public void testLoadWrongTypePropertyExpectIllegalStateException() {
    URL wrongTypeFile = urlForPath(
        "gms/core/signalenhancement/waveformfiltering/control/osdgateway/client/osdGatewayClient_useStringForInt.properties");

    exception.expect(IllegalStateException.class);
    exception.expectMessage(
        "HttpClientConfigurationLoader could not parse property osdGateway_port as an integer");
    HttpClientConfigurationLoader.load("osdGateway_", wrongTypeFile);
  }

  @Test
  public void testLoadMissingStringPropertyExpectIllegalStateException() {
    URL missingPropertyFile = urlForPath(
        "gms/core/signalenhancement/waveformfiltering/control/osdgateway/client/osdGatewayClient_missingStringProperty.properties");

    exception.expect(IllegalStateException.class);
    exception.expectMessage(
        "HttpClientConfigurationLoader requires property osdGateway_host to be defined as a string");
    HttpClientConfigurationLoader.load("osdGateway_", missingPropertyFile);
  }

  @Test
  public void testLoadMissingIntPropertyExpectIllegalStateException() {
    URL missingPropertyFile = urlForPath(
        "gms/core/signalenhancement/waveformfiltering/control/osdgateway/client/osdGatewayClient_missingIntProperty.properties");

    exception.expect(IllegalStateException.class);
    exception.expectMessage(
        "HttpClientConfigurationLoader requires property osdGateway_port to be defined as an integer");
    HttpClientConfigurationLoader.load("osdGateway_", missingPropertyFile);
  }

  @Test
  public void testLoadNullPrefixExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("HttpClientConfigurationLoader requires non-null configurationPrefix");
    HttpClientConfigurationLoader.load(null, propertiesUrl);
  }

  @Test
  public void testLoadNullFileExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("HttpClientConfigurationLoader requires non-null propertiesFileUrl");
    HttpClientConfigurationLoader.load("prefix_", null);
  }
}

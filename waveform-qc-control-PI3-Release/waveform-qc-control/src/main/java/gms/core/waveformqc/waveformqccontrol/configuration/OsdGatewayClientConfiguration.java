package gms.core.waveformqc.waveformqccontrol.configuration;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicStringProperty;
import com.netflix.config.validation.ValidationException;
import gms.core.waveformqc.waveformqccontrol.osdgateway.client.OsdGatewayClient;

/**
 * ServiceConfiguration used by {@link OsdGatewayClient}
 */
public class OsdGatewayClientConfiguration {

  private final DynamicStringProperty host;
  private final DynamicIntProperty port;
  private final DynamicStringProperty baseUri;

  private OsdGatewayClientConfiguration(DynamicStringProperty host,
      DynamicIntProperty port, DynamicStringProperty baseUri) {
    this.host = host;
    this.port = port;
    this.baseUri = baseUri;
  }

  /**
   * Construct a {@link OsdGatewayClientConfiguration}
   *
   * @return OsdGatewayClientConfiguration, not null
   */
  public static OsdGatewayClientConfiguration create() {

    DynamicStringProperty host = new DynamicStringProperty("gateway_host",
        Defaults.HOST);

    DynamicIntProperty port = new DynamicIntProperty("gateway_port",
        Defaults.PORT) {
      @Override
      protected void validate(String newValue) {
        if (Integer.parseInt(newValue) < 0 || Integer.parseInt(newValue) > 65535) {
          throw new ValidationException("Gateway Port must be between 0 and 65535");
        }
      }
    };

    DynamicStringProperty baseUri = new DynamicStringProperty("gateway_baseUri",
        Defaults.BASE_URI);

    return new OsdGatewayClientConfiguration(host, port, baseUri);
  }

  public String getHost() {
    return host.get();
  }

  public int getPort() {
    return port.get();
  }

  public String getBaseUri() {
    return baseUri.get();
  }

  private static class Defaults {

    private static final String HOST = "waveform-qc-osd-gateway";
    private static final int PORT = 8081;
    private static final String BASE_URI = "/waveform-qc/waveform-qc-control/osd-gateway";
  }
}




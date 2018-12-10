package gms.core.waveformqc.waveformqccontrol.osdgateway.configuration;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicProperty;
import com.netflix.config.DynamicStringProperty;

/**
 * ServiceConfiguration values for {@link gms.core.waveformqc.waveformqccontrol.osdgateway.Application}.
 * Uses a builder to create immutable instances.
 */
public class ServiceConfiguration {

  private final DynamicStringProperty baseUri;
  private final DynamicIntProperty port;
  private final DynamicIntProperty minThreads;
  private final DynamicIntProperty maxThreads;
  private final DynamicIntProperty idleTimeOutMillis;

  private ServiceConfiguration(DynamicStringProperty baseUri, DynamicIntProperty port,
      DynamicIntProperty minThreads, DynamicIntProperty maxThreads,
      DynamicIntProperty idleTimeOutMillis) {
    this.baseUri = baseUri;
    this.port = port;
    this.minThreads = minThreads;
    this.maxThreads = maxThreads;
    this.idleTimeOutMillis = idleTimeOutMillis;
  }

  public static ServiceConfiguration create() {
    DynamicStringProperty baseUri = new DynamicStringProperty("service_baseUri",
        Defaults.BASE_URI);

    DynamicIntProperty port = new DynamicIntProperty("service_port",
        Defaults.PORT) {
      @Override
      protected void validate(String newValue) {
        ConfigurationValidator.validatePort(Integer.parseInt(newValue));
      }
    };

    DynamicIntProperty minThreads = new DynamicIntProperty("service_minThreads",
        Defaults.MIN_THREADS) {
      @Override
      protected void validate(String newValue) {
        ConfigurationValidator.validateMinThreads(Integer.parseInt(newValue),
            DynamicProperty.getInstance("service_maxThreads").getInteger());
      }
    };

    DynamicIntProperty maxThreads = new DynamicIntProperty("service_maxThreads",
        Defaults.MAX_THREADS) {
      @Override
      protected void validate(String newValue) {
        ConfigurationValidator.validateMaxThreads(Integer.parseInt(newValue),
            DynamicProperty.getInstance("service_minThreads").getInteger());
      }
    };

    DynamicIntProperty idleTimeoutMillis = new DynamicIntProperty("service_idleTimeoutMillis",
        Defaults.IDLE_TIMEOUT_MILLIS) {
      @Override
      protected void validate(String newValue) {
        ConfigurationValidator.validateIdleTimeout(Integer.parseInt(newValue));
      }
    };

    return new ServiceConfiguration(baseUri, port, minThreads, maxThreads, idleTimeoutMillis);
  }

  public String getBaseUri() {
    return baseUri.get();
  }

  public int getPort() {
    return port.get();
  }

  public int getMinThreads() {
    return minThreads.get();
  }

  public int getMaxThreads() {
    return maxThreads.get();
  }

  public int getIdleTimeOutMillis() {
    return idleTimeOutMillis.get();
  }

  private static class Defaults {

    private static final int PORT = 8080;
    private static final int MIN_THREADS = 1;
    private static final int MAX_THREADS = 5;
    private static final int IDLE_TIMEOUT_MILLIS = 30000;
    private static final String BASE_URI = "/waveform-qc/waveform-qc-control/osd-gateway";
  }
}

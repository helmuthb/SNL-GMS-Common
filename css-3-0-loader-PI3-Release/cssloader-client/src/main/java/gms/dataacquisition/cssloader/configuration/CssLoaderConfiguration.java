package gms.dataacquisition.cssloader.configuration;

import java.util.Objects;
import org.apache.commons.lang3.Validate;

public class CssLoaderConfiguration {

  public static final short DEFAULT_SERVICE_PORT = 8080;
  public static final String DEFAULT_SERVICE_HOST = "localhost";

  public final String serviceHost;
  public final short servicePort;

  private CssLoaderConfiguration(String serviceHost, short servicePort) {
    this.serviceHost = serviceHost;
    this.servicePort = servicePort;
  }

  /**
   * Obtains an instance of {@link Builder}
   *
   * @return a Builder, not null
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Constructs instances of {@link CssLoaderConfiguration} following the builder pattern
   */
  public static class Builder {

    private String serviceHost;
    private short servicePort;

    private Builder() {
      this.serviceHost = DEFAULT_SERVICE_HOST;
      this.servicePort = DEFAULT_SERVICE_PORT;
    }

    /**
     * Construct the {@link CssLoaderConfiguration}
     *
     * @return Configuration built from this {@link Builder}, not null
     * @throws IllegalArgumentException if minThreads, maxThreads, idleTimeOutMillis, or port are
     * negative; if minThreads is greater than maxThreads; if port is beyond the valid range
     */
    public CssLoaderConfiguration build() {
      Validate.notEmpty(serviceHost, "serviceHost cannot be null or empty");
      Validate.isTrue(servicePort > 0, "servicePort cannot be negative");

      return new CssLoaderConfiguration(serviceHost, servicePort);
    }

    /**
     * Set the service's base URL
     *
     * @param serviceHost host name of the service, not null or empty
     * @return this {@link Builder}
     * @throws NullPointerException if the host parameter is null or empty
     */
    public Builder setHost(String serviceHost) {
      this.serviceHost = Objects.requireNonNull(serviceHost,
          "Configuration cannot have null serviceHost");
      return this;
    }

    /**
     * Set the service's port
     *
     * @param servicePort service's port
     * @return this {@link Builder}
     */
    public Builder setPort(short servicePort) {
      this.servicePort = servicePort;
      return this;
    }
  }
}

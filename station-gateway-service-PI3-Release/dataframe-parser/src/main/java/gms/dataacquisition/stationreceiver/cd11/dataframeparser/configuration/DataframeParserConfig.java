package gms.dataacquisition.stationreceiver.cd11.dataframeparser.configuration;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11Validator;
import org.apache.commons.lang3.Validate;

public class DataframeParserConfig {

  public static final String DEFAULT_OSD_GATEWAY_HOSTNAME = "localhost";
  public static final int DEFAULT_OSD_GATEWAY_PORT = 8080;
  public static final String DEFAULT_MONITORED_DIR_LOCATION = "./shared-volume/dataframes/";
  public static final int DEFAULT_PARSER_THREADS = 10;
  public static final int DEFAULT_MANIFEST_TIME_THRESHOLD = 30;

  public final String osdGatewayHostname;
  public final int osdGatewayPort;
  public final String monitoredDirLocation;
  public final int parserThreads;
  public final int manifestTimeThreshold;

  private DataframeParserConfig(String osdGatewayHostname, int osdGatewayPort, String monitoredDirLocation,
      int parserThreads, int manifestTimeThreshold) {
    this.osdGatewayHostname = osdGatewayHostname;
    this.osdGatewayPort = osdGatewayPort;
    this.monitoredDirLocation = monitoredDirLocation;
    this.parserThreads = parserThreads;
    this.manifestTimeThreshold = manifestTimeThreshold;
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
   * Constructs instances of {@link DataframeParserConfig} following the builder pattern
   */
  public static class Builder {

    private String osdGatewayHostname;
    private int osdGatewayPort;
    private String monitoredDirLocation;
    private int parserThreads;
    private int manifestTimeThreshold;

    private Builder() {
      this.osdGatewayHostname = DEFAULT_OSD_GATEWAY_HOSTNAME;
      this.osdGatewayPort = DEFAULT_OSD_GATEWAY_PORT;
      this.monitoredDirLocation = DEFAULT_MONITORED_DIR_LOCATION;
      this.parserThreads = DEFAULT_PARSER_THREADS;
      this.manifestTimeThreshold = DEFAULT_MANIFEST_TIME_THRESHOLD;
    }

    /**
     * Construct the {@link DataframeParserConfig}
     *
     * @return Configuration built from this {@link Builder}, not null
     * @throws IllegalArgumentException if osdGatewayPort is negative; if osdGatewayPort is beyond the
     * valid range
     */
    public DataframeParserConfig build() {
      Validate.notBlank(osdGatewayHostname);
      Cd11Validator.validPortNumber(osdGatewayPort);
      Validate.isTrue(manifestTimeThreshold >= 0);

      return new DataframeParserConfig(osdGatewayHostname, osdGatewayPort, monitoredDirLocation,
          parserThreads, manifestTimeThreshold);
    }

    /**
     * Set the service's base URL
     *
     * @param osdGatewayHostname osdGatewayHostname name of the service, not null
     * @return this {@link Builder}
     * @throws NullPointerException if the osdGatewayHostname parameter is null
     */
    public Builder setOsdGatewayHostname(String osdGatewayHostname) {
      this.osdGatewayHostname = osdGatewayHostname;
      return this;
    }

    /**
     * Set the service's osdGatewayPort
     *
     * @param osdGatewayPort service's osdGatewayPort
     * @return this {@link Builder}
     */
    public Builder setOsdGatewayPort(int osdGatewayPort) {
      this.osdGatewayPort = osdGatewayPort;
      return this;
    }

    /**
     * Set the service's monitoredDirLocation
     *
     * @param monitoredDirLocation location of RawStationDataFrame files
     * @return this {@link Builder}
     */
    public Builder setMonitoredDirLocation(String monitoredDirLocation) {
      this.monitoredDirLocation = monitoredDirLocation;
      return this;
    }

    /**
     * Set the service's parserThreads
     *
     * @param parserThreads the number of parser threads to run in parallel
     * @return this {@link Builder}
     */
    public Builder setParserThreads(int parserThreads) {
      this.parserThreads = parserThreads;
      return this;
    }

    public Builder setManifestTimeThreshold(int manifestTimeThreshold) {
      this.manifestTimeThreshold = manifestTimeThreshold;
      return this;
    }
  }
}

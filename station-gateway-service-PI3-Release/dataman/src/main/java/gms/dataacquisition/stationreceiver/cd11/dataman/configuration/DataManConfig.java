package gms.dataacquisition.stationreceiver.cd11.dataman.configuration;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11Validator;
import java.io.File;
import org.apache.commons.lang3.Validate;

public class DataManConfig {

  public static final String DEFAULT_OSD_GATEWAY_HOSTNAME = "localhost";
  public static final int DEFAULT_OSD_GATEWAY_PORT = 8080;
  public static final String DEFAULT_FS_OUTPUT_DIRECTORY = null;
  public static final String DEFAULT_DATA_PROVIDER_IP_ADDRESS = "127.0.0.1";
  public static final String DEFAULT_DATA_CONSUMER_IP_ADDRESS = "127.0.0.1";

  public final String osdGatewayHostname;
  public final int osdGatewayPort;
  public final String fsOutputDirectory;
  public final String expectedDataProviderIpAddress;
  public final String dataConsumerIpAddress;

  private DataManConfig(
      String osdGatewayHostname, int osdGatewayPort,
      String fsOutputDirectory,
      String expectedDataProviderIpAddress, String dataConsumerIpAddress) {
    this.osdGatewayHostname = osdGatewayHostname;
    this.osdGatewayPort = osdGatewayPort;
    this.fsOutputDirectory = fsOutputDirectory;
    this.expectedDataProviderIpAddress = expectedDataProviderIpAddress;
    this.dataConsumerIpAddress = dataConsumerIpAddress;
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
   * Constructs instances of {@link DataManConfig} following the builder pattern
   */
  public static class Builder {

    private String osdGatewayHostname;
    private int osdGatewayPort;
    private String fsOutputDirectory;
    private String expectedDataProviderIpAddress;
    private String dataConsumerIpAddress;

    private Builder() {
      this.osdGatewayHostname = DEFAULT_OSD_GATEWAY_HOSTNAME;
      this.osdGatewayPort = DEFAULT_OSD_GATEWAY_PORT;
      this.fsOutputDirectory = DEFAULT_FS_OUTPUT_DIRECTORY;
      this.expectedDataProviderIpAddress = DEFAULT_DATA_PROVIDER_IP_ADDRESS;
      this.dataConsumerIpAddress = DEFAULT_DATA_CONSUMER_IP_ADDRESS;
    }

    /**
     * Construct the {@link DataManConfig}
     *
     * @return Configuration built from this {@link Builder}, not null
     * @throws IllegalArgumentException if minThreads, maxThreads, idleTimeOutMillis, or osdGatewayPort
     * are negative; if minThreads is greater than maxThreads; if osdGatewayPort is beyond the valid
     * range
     */
    public DataManConfig build() {
      Validate.notBlank(osdGatewayHostname);
      Cd11Validator.validNonZeroPortNumber(osdGatewayPort);

      // Validate the directory path.
      if (fsOutputDirectory != null) {
        Validate.notBlank(fsOutputDirectory);

        // Check that the directory exists.
        File f = new File(fsOutputDirectory);
        Validate.isTrue(f.exists(), "Directory path provided does not exist.");
        Validate.isTrue(f.isDirectory(), "Path provided does not map to a directory.");

        // Ensure that the directory path ends with a slash.
        if (!fsOutputDirectory.endsWith("/")) {
          fsOutputDirectory += "/";
        }
      }

      Cd11Validator.validIpAddress(expectedDataProviderIpAddress);
      Cd11Validator.validIpAddress(dataConsumerIpAddress);

      return new DataManConfig(
          osdGatewayHostname, osdGatewayPort,
          fsOutputDirectory,
          expectedDataProviderIpAddress, dataConsumerIpAddress);
    }

    /**
     * Set the service's base URL
     *
     * @param value osdGatewayHostname name of the service, not null
     * @return this {@link Builder}
     * @throws NullPointerException if the osdGatewayHostname parameter is null
     */
    public Builder setOsdGatewayHostname(String value) {
      this.osdGatewayHostname = value;
      return this;
    }

    /**
     * Set the service's osdGatewayPort
     *
     * @param value service's osdGatewayPort
     * @return this {@link Builder}
     */
    public Builder setOsdGatewayPort(int value) {
      this.osdGatewayPort = value;
      return this;
    }

    /**
     * Directory where raw station data frames are written as flat JSON files.
     *
     * @param value Path to the output directory (default: null).
     * @return this {@link Builder}
     */
    public Builder setFsOutputDirectory(String value) {
      this.fsOutputDirectory = value;
      return this;
    }

    /**
     * IP Address of the data provider.
     *
     * @param value IP address.
     * @return this {@link Builder}
     */
    public Builder setExpectedDataProviderIpAddress(String value) {
      this.expectedDataProviderIpAddress = value;
      return this;
    }

    /**
     * IP Address of the data consumer.
     *
     * @param value IP address.
     * @return this {@link Builder}
     */
    public Builder setDataConsumerIpAddress(String value) {
      this.dataConsumerIpAddress = value;
      return this;
    }
  }
}

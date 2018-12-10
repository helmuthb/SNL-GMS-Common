package gms.dataacquisition.cssloader.configuration;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to load a {@link CssLoaderConfiguration} based on a hierarchy of:
 * Environment variables
 * System properties (i.e. as set with java -D command line flags)
 * application.properties file properties
 *
 * Properties in higher levels override properties from lower levels
 */
public class CssLoaderConfigurationLoader {

  private static final Logger logger = LoggerFactory.getLogger(CssLoaderConfigurationLoader.class);

  public static final String SERVICE_HOST_KEY = "serviceHost";
  public static final String SERVICE_PORT_KEY = "servicePort";

  /*
   * Statically load properties since the ConfigurationManager can only be initialized once
   */
  static {

    // Load properties from the application.properties file.  Throws an unchecked exception
    // on failure in order to fail fast if the properties can't be found.
    final String propFile = "gms/dataacquisition/cssloader/application.properties";

    ConcurrentMapConfiguration propertiesFileConfig;
    try {
      propertiesFileConfig = new ConcurrentMapConfiguration(new PropertiesConfiguration(propFile));
    } catch (ConfigurationException e) {
      logger.error("Could not load configuration from " + propFile, e);
      throw new RuntimeException(
          "ConfigurationLoader could not load configuration file " + propFile, e);
    }

    // System properties configuration (e.g. as set via -D command line flags)
    ConcurrentMapConfiguration systemPropertiesConfig = new ConcurrentMapConfiguration(
        new SystemConfiguration());

    // Environment variable configuration
    ConcurrentMapConfiguration environmentVariablesConfig = new ConcurrentMapConfiguration(
        new EnvironmentConfiguration());

    // System Config overrides fileConfig
    ConcurrentCompositeConfiguration config = new ConcurrentCompositeConfiguration();
    config.addConfiguration(environmentVariablesConfig, "environmentConfig");
    config.addConfiguration(systemPropertiesConfig, "systemConfig");
    config.addConfiguration(propertiesFileConfig, "fileConfig");

    ConfigurationManager.install(config);
  }

  /**
   * Obtains a {@link CssLoaderConfiguration} based on the properties
   * hierarchy.
   *
   * @return a constructed Configuration, not null
   */
  public static CssLoaderConfiguration load() {
    return CssLoaderConfiguration.builder()
        .setPort(getShort(SERVICE_PORT_KEY, CssLoaderConfiguration.DEFAULT_SERVICE_PORT))
        .setHost(getString(SERVICE_HOST_KEY, CssLoaderConfiguration.DEFAULT_SERVICE_HOST))
        .build();
  }

  private static String getString(String key, String defaultValue) {
    return DynamicPropertyFactory.getInstance().getStringProperty(key, defaultValue).get();
  }

  private static short getShort(String key, short defaultValue) {
    int val = DynamicPropertyFactory.getInstance().getIntProperty(key, defaultValue).get();
    return (short) val;
  }
}

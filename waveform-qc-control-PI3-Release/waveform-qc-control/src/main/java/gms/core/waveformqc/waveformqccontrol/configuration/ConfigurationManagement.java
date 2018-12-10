package gms.core.waveformqc.waveformqccontrol.configuration;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.ConfigurationManager;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationManagement {

  private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagement.class);

  private ConfigurationManagement() {
  }

  public static void initialize() {
    // Load properties from the application.properties file.  Throws an unchecked exception
    // on failure in order to fail fast if the properties can't be found.
    final String propFile = "application.properties";

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

    // System properties configuration (e.g. as set via -D command line flags)
    ConcurrentMapConfiguration environmentVariablesConfig = new ConcurrentMapConfiguration(
        new EnvironmentConfiguration());

    // System Config overrides fileConfig
    ConcurrentCompositeConfiguration config = new ConcurrentCompositeConfiguration();
    config.addConfiguration(environmentVariablesConfig, "environmentConfig");
    config.addConfiguration(systemPropertiesConfig, "systemConfig");
    config.addConfiguration(propertiesFileConfig, "fileConfig");

    ConfigurationManager.install(config);
  }
}

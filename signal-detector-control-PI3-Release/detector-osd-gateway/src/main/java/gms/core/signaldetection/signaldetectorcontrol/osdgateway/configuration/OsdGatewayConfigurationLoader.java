package gms.core.signaldetection.signaldetectorcontrol.osdgateway.configuration;

import gms.core.signaldetection.signaldetectorcontrol.osdgateway.util.ConfigurationLoader;
import java.net.URL;
import java.util.MissingResourceException;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsdGatewayConfigurationLoader {

  private static final Logger logger = LoggerFactory
      .getLogger(HttpServiceConfigurationLoader.class);

  private static final String PERSISTENCE_WAVEFORM_URL_KEY = "persistence_waveform_url";
  private static final String PROVENANCE_PERSISTENCE_URL_KEY = "provenance_persistence_url";
  private static final String SIGNAL_DETECTION_URL_KEY = "signal_detection_url";

  /**
   * Obtains a {@link HttpServiceConfiguration} based on the {@link HttpServiceConfigurationLoader}
   * properties hierarchy.
   *
   * @return a constructed HttpServiceConfiguration, not null
   */
  public static OsdGatewayConfiguration load() {

    // Get URL to the application.properties file
    final String path = "gms/core/signaldetection/signaldetectorcontrol/osdgateway/util/application.properties";
    final URL propFileUrl = Thread.currentThread().getContextClassLoader().getResource(path);

    if (null == propFileUrl) {
      final String message =
          "HttpServiceConfigurationLoader can't load properties file from resources: " + path;
      logger.error(message);
      throw new MissingResourceException(message, "HttpServiceConfigurationLoader", path);
    }

    // Load configuration and use to create the HttpServiceConfiguration
    final Configuration config = ConfigurationLoader.load(propFileUrl);

    return OsdGatewayConfiguration.builder()
        .setWaveformPersistenceUrl(config.getString(PERSISTENCE_WAVEFORM_URL_KEY,
            OsdGatewayConfiguration.DEFAULT_PERSISTENCE_WAVEFORM_URL))
        .setProvenancePersistenceUrl(config.getString(PROVENANCE_PERSISTENCE_URL_KEY,
            OsdGatewayConfiguration.DEFAULT_PROVENANCE_PERSISTENCE_URL))
        .setSignalDetectionUrl(config.getString(SIGNAL_DETECTION_URL_KEY,
            OsdGatewayConfiguration.DEFAULT_SIGNAL_DETECTION_URL))
        .build();
  }
}

package gms.core.waveformqc.waveformqccontrol.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic plugin configuration, able to be parsed by any plugin for potentially useful values.
 */
public class PluginConfiguration {

  private static final Logger logger = LoggerFactory
      .getLogger(PluginConfiguration.class);

  private final Map<String, Object> configurationParameters;

  private PluginConfiguration(Map<String, Object> configurationParameters) {
    this.configurationParameters = configurationParameters;

    for(String key : configurationParameters.keySet()) {
      logger.info("Plugin configuration param: " + key + "=" + configurationParameters.get(key));
    }
  }

  /**
   * Factory method for creating plugin configuration
   */
  public static PluginConfiguration from(Map<String, Object> configurationParameters) {
    Objects.requireNonNull(configurationParameters,
        "Error creating PluginConfiguration, cannot create from null configuration parameters.");
    return new PluginConfiguration(new HashMap<>(configurationParameters));
  }

  public Optional<Object> getParameter(String key) {
    return Optional.ofNullable(configurationParameters.get(key));
  }

  public Set<String> keySet() {
    return configurationParameters.keySet();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private final Map<String, Object> parameters;

    private Builder() {
      this.parameters = new HashMap<>();
    }

    public Builder add(String key, Object parameter) {
      parameters.put(key, parameter);
      return this;
    }

    public PluginConfiguration build() {
      return new PluginConfiguration(new HashMap<>(parameters));
    }
  }

  @Override
  public String toString() {
    return "PluginConfiguration{" +
        "configurationParameters=" + configurationParameters +
        '}';
  }
}

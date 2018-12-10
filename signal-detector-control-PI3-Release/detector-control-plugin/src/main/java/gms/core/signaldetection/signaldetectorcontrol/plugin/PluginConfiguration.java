package gms.core.signaldetection.signaldetectorcontrol.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class PluginConfiguration {

  private final Map<String, Object> configurationParameters;

  private PluginConfiguration(Map<String, Object> configurationParameters) {
    this.configurationParameters = configurationParameters;
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

  @Override
  public String toString() {
    return "PluginConfiguration{" +
        "configurationParameters=" + configurationParameters +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PluginConfiguration that = (PluginConfiguration) o;

    return configurationParameters.equals(that.configurationParameters);
  }

  @Override
  public int hashCode() {
    return configurationParameters.hashCode();
  }
}

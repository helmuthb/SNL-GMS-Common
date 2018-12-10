package gms.core.waveformqc.waveformqccontrol.plugin;

import gms.core.waveformqc.waveformqccontrol.objects.RegistrationInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Waveform QC plugin registry. It takes the registration information (name and version) and
 * registers it here.
 */
public class WaveformQcPluginRegistry {

  private static final Logger logger = LoggerFactory.getLogger(WaveformQcPluginRegistry.class);

  private final Map<RegistrationInfo, WaveformQcPlugin> pluginMap;

  /**
   * Creates new HashMap to store the registration information
   */
  public WaveformQcPluginRegistry() {
    this.pluginMap = new HashMap<>();
  }

  /**
   * Gets registration information from {@link RegistrationInfo} and checks if null. If there is no
   * information and error is thrown, else it returns a HashMap of the registration information
   *
   * @param registrationInfo The name and version information collected in {@link RegistrationInfo}
   * @return A HashMap containing the registration information
   */
  public Optional<WaveformQcPlugin> lookup(RegistrationInfo registrationInfo) {
    if (registrationInfo == null) {
      throw new IllegalArgumentException(
          "Error retrieving plugin: null is an invalid RegistrationInformation key");
    }
    return Optional.ofNullable(pluginMap.get(registrationInfo));
  }

  /**
   * Takes registration information and pairs it with the plugin, then puts it into a HashMap,
   * completing the registration.
   *
   * @param plugin The waveform qc plugin component
   * @return The registration information (name and version) associated with the plugin
   */
  public RegistrationInfo register(WaveformQcPlugin plugin) {
    RegistrationInfo registrationInfo = RegistrationInfo
        .from(plugin.getName(), plugin.getVersion());

    logger.info("Registering {} {}", plugin.getName(), plugin.getVersion());

    pluginMap.put(registrationInfo, plugin);
    return registrationInfo;
  }

  public Set<Entry> entrySet() {
    return pluginMap.entrySet().stream().map(Entry::from).collect(Collectors.toSet());
  }

  public static class Entry {

    private final RegistrationInfo registration;
    private final WaveformQcPlugin plugin;

    private Entry(RegistrationInfo registration,
        WaveformQcPlugin plugin) {
      this.registration = registration;
      this.plugin = plugin;
    }

    public static Entry create(RegistrationInfo registration, WaveformQcPlugin plugin) {
      Objects.requireNonNull(registration, "Cannot create Entry from null RegistrationInfo");
      Objects.requireNonNull(plugin, "Cannot create Entry from null RegistrationInfo");
      return new Entry(registration, plugin);
    }

    public static Entry from(Map.Entry<RegistrationInfo, WaveformQcPlugin> mapEntry) {
      return create(mapEntry.getKey(), mapEntry.getValue());
    }

    public RegistrationInfo getRegistration() {
      return registration;
    }

    public WaveformQcPlugin getPlugin() {
      return plugin;
    }
  }
}

package gms.core.signaldetection.signaldetectorcontrol.plugin;

import gms.core.signaldetection.signaldetectorcontrol.objects.RegistrationInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignalDetectorControlPluginRegistry {

  private static final Logger logger = LoggerFactory.getLogger(SignalDetectorControlPluginRegistry.class);

  private final Map<RegistrationInfo, SignalDetectorPlugin> pluginMap;

  /**
   * Creates new HashMap to store the registration information
   */
  public SignalDetectorControlPluginRegistry() {
    this.pluginMap = new HashMap<>();
  }

  /**
   * Gets registration information from {@link RegistrationInfo} and checks if null. If there is no
   * information and error is thrown, else it returns a HashMap of the registration information
   *
   * @param registrationInfo The name and version information collected in {@link RegistrationInfo}
   * @return A HashMap containing the registration information
   */
  public Optional<SignalDetectorPlugin> lookup(RegistrationInfo registrationInfo) {
    Objects.requireNonNull(registrationInfo,
        "Error retrieving plugin: null is an invalid RegistrationInformation key");
    return Optional.ofNullable(pluginMap.get(registrationInfo));
  }

  /**
   * Takes registration information and pairs it with the plugin, then puts it into a HashMap,
   * completing the registration.
   *
   * @param plugin The signal detector plugin component
   * @return The registration information (name and version) associated with the plugin
   */
  public RegistrationInfo register(SignalDetectorPlugin plugin) {
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
    private final SignalDetectorPlugin plugin;

    private Entry(RegistrationInfo registration,
        SignalDetectorPlugin plugin) {
      this.registration = registration;
      this.plugin = plugin;
    }

    public static Entry create(RegistrationInfo registration, SignalDetectorPlugin plugin) {
      Objects.requireNonNull(registration, "Cannot create Entry from null RegistrationInfo");
      Objects.requireNonNull(plugin, "Cannot create Entry from null RegistrationInfo");
      return new Entry(registration, plugin);
    }

    public static Entry from(Map.Entry<RegistrationInfo, SignalDetectorPlugin> mapEntry) {
      return create(mapEntry.getKey(), mapEntry.getValue());
    }

    public RegistrationInfo getRegistration() {
      return registration;
    }

    public SignalDetectorPlugin getPlugin() {
      return plugin;
    }
  }

}

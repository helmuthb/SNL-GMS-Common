package gms.core.signaldetection.signaldetectorcontrol.objects;

import java.util.Objects;

/**
 * Plugin registration information that is used in the plugin registry
 */
public class RegistrationInfo {

  private final String name;
  private final PluginVersion version;

  public String getName() {
    return name;
  }

  public PluginVersion getVersion() {
    return version;
  }

  /**
   * Waveform plugin information that contains a version and name that can be used to registered.
   *
   * @param name The name of the plugin
   * @param version The plugin version (Major:Minor:Patch)
   */
  private RegistrationInfo(String name, PluginVersion version) {
    this.name = name;
    this.version = version;
  }

  /**
   * Checks registration information making sure the name or version is not null. If it is, it
   * throws an error and logs it in the logger
   *
   * @param name The name of the plugin
   * @param version The plugin version (Major:Minor:Patch)
   * @return New registration information object containing the name and version.
   */
  public static RegistrationInfo from(String name, PluginVersion version) {
    Objects.requireNonNull(name,
        "Error instantiating RegistrationInfo, name cannot be null");
    Objects.requireNonNull(version,
        "Error instantiating RegistrationInfo, version cannot be null");

    return new RegistrationInfo(name, version);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RegistrationInfo that = (RegistrationInfo) o;

    return (name != null ? name.equals(that.name) : that.name == null) && (version != null ? version
        .equals(that.version) : that.version == null);
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (version != null ? version.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "RegistrationInfo{" +
        "name='" + name + '\'' +
        ", version=" + version +
        '}';
  }

}
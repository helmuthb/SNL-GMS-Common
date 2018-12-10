package gms.core.signaldetection.signaldetectorcontrol.objects;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginVersion {

  private final Integer major;
  private final Integer minor;
  private final Integer patch;

  private static final Logger logger = LoggerFactory.getLogger(PluginVersion.class);

  public Integer getMinor() {
    return minor;
  }

  public Integer getPatch() {
    return patch;
  }

  public static Logger getLogger() {
    return logger;
  }

  /**
   * Creates plugin version with a major, minor and patch version
   *
   * @param major The major version
   * @param minor The minor version
   * @param patch The patch version
   */
  private PluginVersion(Integer major, Integer minor, Integer patch) {
    this.major = major;
    this.minor = minor;

    this.patch = patch;
  }

  public Integer getMajor() {
    return major;
  }

  /**
   * Checks that the plugin versions (Major, Minor, Patch). If any of the values are null it throws
   * an error and logs it in the logger.
   *
   * @param major The major version
   * @param minor The minor version
   * @param patch The patch version
   */
  public static PluginVersion from(Integer major, Integer minor, Integer patch) {
    Objects.requireNonNull(major,
        "Error instantiating plugin version, major value cannot be null.");
    Objects.requireNonNull(minor,
        "Error instantiating plugin version, minor value cannot be null.");
    Objects.requireNonNull(patch,
        "Error instantiating plugin version, patch value cannot be null.");

    return new PluginVersion(major, minor, patch);
  }

  /**
   * Checks to see if the plugin version is not null and matches the versions created by {@link
   * PluginVersion}
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PluginVersion that = (PluginVersion) o;

    return (major != null ? major.equals(that.major) : that.major == null) && (minor != null ? minor
        .equals(that.minor) : that.minor == null) && (patch != null ? patch.equals(that.patch)
        : that.patch == null);
  }

  @Override
  public int hashCode() {
    int result = major != null ? major.hashCode() : 0;
    result = 31 * result + (minor != null ? minor.hashCode() : 0);
    result = 31 * result + (patch != null ? patch.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "PluginVersion{" +
        "major=" + major +
        ", minor=" + minor +
        ", patch=" + patch +
        '}';
  }

}
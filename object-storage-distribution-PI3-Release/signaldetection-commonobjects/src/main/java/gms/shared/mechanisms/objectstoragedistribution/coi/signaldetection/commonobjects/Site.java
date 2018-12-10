package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.Validate;


/**
 * Represents a limited set of site information used during the acquisition of data streams.
 */
public final class Site {

  private final UUID id;
  private final String name;
  private final double latitude;
  private final double longitude;
  private final double elevation;
  private final List<Channel> channels;

  /**
   * Create an instance of the class.
   *
   * @param name The name of the site, not null nor an empty string.
   * @param channels A collection of channels related to this site.
   * @return a ProcessingSite
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static Site create(String name, double latitude, double longitude, double elevation,
      List<Channel> channels) {
    return new Site(UUID.randomUUID(), name, latitude, longitude, elevation, channels);
  }

  /**
   * Recreates a ProcessingSite given all params.
   *
   * @param id the id of the site
   * @param name The name of the site, not null nor an empty string.
   * @param channels A collection of channels related to this site.
   * @return a ProcessingSite
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static Site from(UUID id, String name, double latitude, double longitude,
      double elevation, List<Channel> channels) {
    return new Site(id, name, latitude, longitude, elevation, channels);
  }

  /**
   * Private constructor.
   *
   * @param id the identifier to use
   * @param name The name of the site, not null nor an empty string.
   * @param channels A collection of channels related to this site.
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  private Site(UUID id, String name, double latitude, double longitude, double elevation,
      List<Channel> channels) {

    Validate.notBlank(name);
    this.name = Objects.requireNonNull(name).toUpperCase().trim();
    this.id = Objects.requireNonNull(id);
    this.latitude = latitude;
    this.longitude = longitude;
    this.elevation = elevation;
    this.channels = Collections.unmodifiableList(Objects.requireNonNull(channels));
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public double getElevation() {
    return elevation;
  }

  public List<Channel> getChannels() {
    return channels;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Site site = (Site) o;

    if (Double.compare(site.latitude, latitude) != 0) {
      return false;
    }
    if (Double.compare(site.longitude, longitude) != 0) {
      return false;
    }
    if (Double.compare(site.elevation, elevation) != 0) {
      return false;
    }
    if (id != null ? !id.equals(site.id) : site.id != null) {
      return false;
    }
    if (name != null ? !name.equals(site.name) : site.name != null) {
      return false;
    }
    return channels != null ? channels.equals(site.channels) : site.channels == null;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = id != null ? id.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    temp = Double.doubleToLongBits(latitude);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(longitude);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(elevation);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (channels != null ? channels.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Site{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        ", elevation=" + elevation +
        ", channels=" + channels +
        '}';
  }
}

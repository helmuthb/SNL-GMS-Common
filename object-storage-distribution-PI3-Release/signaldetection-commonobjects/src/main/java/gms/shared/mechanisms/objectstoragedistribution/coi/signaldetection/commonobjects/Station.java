package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.Validate;

/**
 * Represents a limited set of station information used during the acquisition and
 * processing of data streams.
 *
 *
 */
public final class Station {
  private final UUID id;
  private final String name;
  private final double latitude;
  private final double longitude;
  private final double elevation;
  private final List<Site> sites;

  /**
   * Create a new Station.
   * @param name The name of the station, not null nor an empty string.
   * @param latitude The station's latitude.
   * @param longitude The station's longitude.
   * @param elevation The station's elevation.
   * @param sites A collection of associated processing site UUIDs.
   * @throws IllegalArgumentException
   * @throws NullPointerException
   */
  public static Station create(String name, double latitude, double longitude, double elevation,
      List<Site> sites) {
    return new Station(UUID.randomUUID(), name, latitude, longitude, elevation, sites);
  }

  /**
   * Create a Station from existing data.
   * @param id The UUID assigned to the object.
   * @param name The name of the station, not null nor an empty string.
   * @param latitude The station's latitude.
   * @param longitude The station's longitude.
   * @param elevation The station's elevation.
   * @param sites A collection of associated processing site UUIDs.
   * @throws IllegalArgumentException
   * @throws NullPointerException
   */
  public static Station from(UUID id, String name, double latitude, double longitude, double elevation,
      List<Site> sites) {
    return new Station(id, name, latitude, longitude, elevation, sites);

  }
  /**
   * Private constructor.
   * @param id The UUID assigned to the object.
   * @param name The name of the station, not null nor an empty string.
   * @param latitude The station's latitude.
   * @param longitude The station's longitude.
   * @param elevation The station's elevation.
   * @param sites A collection of associated processing site UUIDs.
   * @throws IllegalArgumentException
   * @throws NullPointerException
   */
  private Station(UUID id, String name, double latitude, double longitude,
      double elevation, List<Site> sites)
      throws IllegalArgumentException {

    Validate.notBlank(name);
    this.id = Objects.requireNonNull(id);
    this.name = Objects.requireNonNull(name).toUpperCase().trim();
    this.latitude = latitude;
    this.longitude = longitude;
    this.elevation = elevation;
    this.sites = Objects.requireNonNull(sites);
  }

  public UUID getId() { return id;  }

  public String getName() {
    return name;
  }

  public List<Site> getSites() {
    return this.sites;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Station station = (Station) o;

    if (Double.compare(station.latitude, latitude) != 0) {
      return false;
    }
    if (Double.compare(station.longitude, longitude) != 0) {
      return false;
    }
    if (Double.compare(station.elevation, elevation) != 0) {
      return false;
    }
    if (id != null ? !id.equals(station.id) : station.id != null) {
      return false;
    }
    if (name != null ? !name.equals(station.name) : station.name != null) {
      return false;
    }
    return sites != null ? sites.equals(station.sites) : station.sites == null;
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
    result = 31 * result + (sites != null ? sites.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Station{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        ", elevation=" + elevation +
        ", sites=" + sites +
        '}';
  }
}

package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.Validate;

/**
 *  Define a class to represent a GMS remote monitoring station.
 *
 */
public final class ReferenceStation {

  private final UUID entityId;
  private final UUID versionId;
  private final String name;
  private final String description;
  private final StationType stationType;
  private final String comment;
  private final InformationSource source;
  private final double latitude;
  private final double longitude;
  private final double elevation;
  private final Instant actualChangeTime;
  private final Instant systemChangeTime;
  private final List<ReferenceAlias> aliases;

  /**
   * Create a new ReferenceStation.
   *
   * @param name The name for the station, must be unique, and not empty.
   * @param stationType The station type.
   * @param source The source of this information.
   * @param comment A comment.
   * @param latitude The station's latitude.
   * @param longitude The station's longitude.
   * @param elevation The station's elevation.
   * @param actualTime The date and time the information was originally generated.
   * @param aliases the aliases of the station
   * @return A new ReferenceStation object.
   */
  public static ReferenceStation create(String name, String description, StationType stationType,
      InformationSource source, String comment,
      double latitude, double longitude, double elevation,
      Instant actualTime, List<ReferenceAlias> aliases) {

    return new ReferenceStation(UUID.randomUUID(), UUID.randomUUID(), name,
        description, stationType, source, comment,
        latitude, longitude, elevation, actualTime,
        Instant.now(), aliases);
  }

  /**
   * Create a new ReferenceStation, as a version of an existing entity.
   *
   * @param entityId the id of the entity
   * @param name The name for the station, must be unique, and not empty.
   * @param stationType The station type.
   * @param source The source of this information.
   * @param comment A comment.
   * @param latitude The station's latitude.
   * @param longitude The station's longitude.
   * @param elevation The station's elevation.
   * @param actualTime The date and time the information was originally generated.
   * @param aliases the aliases of the station
   * @return A new ReferenceStation object.
   */
  public static ReferenceStation createNewVersion(UUID entityId, String name,
      String description, StationType stationType,
      InformationSource source, String comment,
      double latitude, double longitude, double elevation,
      Instant actualTime, List<ReferenceAlias> aliases) {

    return new ReferenceStation(entityId, UUID.randomUUID(), name,
        description, stationType, source, comment,
        latitude, longitude, elevation, actualTime, Instant.now(), aliases);
  }

  /**
   * Create a ReferenceStation from existing information.
   *
   * @param entityId id of the entity
   * @param versionId id of the version of the entity
   * @param name The name for the station, must be unique, and not empty.
   * @param stationType The station type.
   * @param source The source of this information.
   * @param comment A comment.
   * @param latitude The station's latitude.
   * @param longitude The station's longitude.
   * @param elevation The station's elevation.
   * @param actualTime The date and time the information was originally generated.
   * @param systemTime The date and time the information was entered into the system.
   * @param aliases the aliases of the station
   * @return A new ReferenceStation object.
   */
  public static ReferenceStation from(UUID entityId, UUID versionId,
      String name, String description, StationType stationType,
      InformationSource source, String comment,
      double latitude, double longitude, double elevation,
      Instant actualTime, Instant systemTime, List<ReferenceAlias> aliases) {

    return new ReferenceStation(entityId, versionId, name, description,
        stationType, source, comment,
        latitude, longitude, elevation, actualTime, systemTime, aliases);
  }


  /**
   * Private constructor.
   */
  private ReferenceStation(UUID entityId, UUID versionId, String name,
      String description, StationType stationType, InformationSource source, String comment,
      double latitude, double longitude, double elevation,
      Instant actualTime, Instant systemTime, List<ReferenceAlias> aliases)
      throws NullPointerException, InvalidParameterException {

    this.entityId = Objects.requireNonNull(entityId);
    this.versionId = Objects.requireNonNull(versionId);
    Validate.notEmpty(name);
    this.name = Objects.requireNonNull(name).toUpperCase();
    this.description = Objects.requireNonNull(description);
    this.stationType = Objects.requireNonNull(stationType);
    this.source = Objects.requireNonNull(source);
    this.comment = Objects.requireNonNull(comment);
    this.latitude = latitude;
    this.longitude = longitude;
    this.elevation = elevation;
    this.actualChangeTime = Objects.requireNonNull(actualTime);
    this.systemChangeTime = Objects.requireNonNull(systemTime);
    this.aliases = Objects.requireNonNull(aliases);
  }

  /**
   * Add an alias to the list if it's not a duplicate.
   * @param alias
   */
  public void addAlias(ReferenceAlias alias) {
    this.aliases.add(alias);
  }

  public UUID getEntityId() {
    return entityId;
  }

  public UUID getVersionId() {
    return versionId;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public StationType getStationType() {
    return stationType;
  }

  public String getComment() {
    return comment;
  }

  public InformationSource getSource() {
    return source;
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

  public Instant getActualChangeTime() {
    return actualChangeTime;
  }

  public Instant getSystemChangeTime() {
    return systemChangeTime;
  }

  public List<ReferenceAlias> getAliases() {
    return aliases;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReferenceStation that = (ReferenceStation) o;

    if (Double.compare(that.latitude, latitude) != 0) {
      return false;
    }
    if (Double.compare(that.longitude, longitude) != 0) {
      return false;
    }
    if (Double.compare(that.elevation, elevation) != 0) {
      return false;
    }
    if (entityId != null ? !entityId.equals(that.entityId) : that.entityId != null) {
      return false;
    }
    if (versionId != null ? !versionId.equals(that.versionId) : that.versionId != null) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (description != null ? !description.equals(that.description) : that.description != null) {
      return false;
    }
    if (stationType != that.stationType) {
      return false;
    }
    if (comment != null ? !comment.equals(that.comment) : that.comment != null) {
      return false;
    }
    if (source != null ? !source.equals(that.source) : that.source != null) {
      return false;
    }
    if (actualChangeTime != null ? !actualChangeTime.equals(that.actualChangeTime)
        : that.actualChangeTime != null) {
      return false;
    }
    if (systemChangeTime != null ? !systemChangeTime.equals(that.systemChangeTime)
        : that.systemChangeTime != null) {
      return false;
    }
    return aliases != null ? aliases.equals(that.aliases) : that.aliases == null;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = entityId != null ? entityId.hashCode() : 0;
    result = 31 * result + (versionId != null ? versionId.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (stationType != null ? stationType.hashCode() : 0);
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + (source != null ? source.hashCode() : 0);
    temp = Double.doubleToLongBits(latitude);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(longitude);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(elevation);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (actualChangeTime != null ? actualChangeTime.hashCode() : 0);
    result = 31 * result + (systemChangeTime != null ? systemChangeTime.hashCode() : 0);
    result = 31 * result + (aliases != null ? aliases.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceStation{" +
        "entityId=" + entityId +
        ", versionId=" + versionId +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", stationType=" + stationType +
        ", comment='" + comment + '\'' +
        ", source=" + source +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        ", elevation=" + elevation +
        ", actualChangeTime=" + actualChangeTime +
        ", systemChangeTime=" + systemChangeTime +
        ", aliases=" + aliases +
        '}';
  }
}



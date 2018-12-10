package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.Validate;


/**
 * Define a class to represent an instrument channel.
 * A Channel is an identifier for a data stream from a sensor measuring a particular aspect
 * of some physical phenomenon (e.g., ground motion or air pressure). A Channel has metadata,
 * such as a name (e.g., “BHZ” is broadband ground motion in the vertical direction), on time
 * and off time, and a channel type that encodes the type of data recorded by that sensor.
 * There are different conventions for Channel naming, so a Channel can have Aliases.
 * The Channel class also includes information about how the sensor was placed and
 * oriented: depth (relative to the elevation of the associated Site), horizontal angle,
 * and vertical angle.
 */
public final class ReferenceChannel {

  private final UUID entityId;
  private final UUID versionId;
  private final String name;
  private final ChannelType type;
  private final ChannelDataType dataType;
  private final int locationCode;
  private final double latitude;
  private final double longitude;
  private final double elevation;
  private final double depth;
  private final double verticalAngle;
  private final double horizontalAngle;
  private final double nominalSampleRate;
  private final Instant actualTime;
  private final Instant systemTime;
  private final InformationSource informationSource;
  private final String comment;
  private final RelativePosition position;
  private final List<ReferenceAlias> aliases;


  /**
   * Create a new ReferenceChannel.
   *
   * @param name The name for the channel, must be unique, and not empty.
   * @param type The channel type.
   * @param dataType The channel data type.
   * @param locationCode The channel's location code.
   * @param latitude The channel's latitude.
   * @param longitude The channel's longitude.
   * @param elevation The channel's elevation.
   * @param depth The channel's depth
   * @param verticalAngle The channel's vertical orientation.
   * @param horizontalAngle The channel's horizontal orientation.
   * @param nominalSampleRate The channel's nominal sample rate.
   * @param actualTime The date and time the information was originally generated.
   * @param informationSource The source of this information.
   * @param comment A comment.
   * @param position This channel's relative position.
   * @param aliases The list of this channel's aliases.
   * @return A new ReferenceChannel object.
   */
  public static ReferenceChannel create(String name, ChannelType type,
      ChannelDataType dataType, int locationCode,
      double latitude, double longitude, double elevation, double depth,
      double verticalAngle, double horizontalAngle, double nominalSampleRate,
      Instant actualTime, InformationSource informationSource, String comment,
      RelativePosition position, List<ReferenceAlias> aliases) {

    return new ReferenceChannel(UUID.randomUUID(), UUID.randomUUID(), name, type, dataType,
        locationCode, latitude, longitude, elevation, depth, verticalAngle, horizontalAngle,
        nominalSampleRate, actualTime, Instant.now(), informationSource, comment,
        position, aliases);
  }

  /**
   * Create a ReferenceChannel version of an existing entity.
   *
   * @param entityId the id of the entity
   * @param name The name for the channel, must be unique, and not empty.
   * @param type The channel type.
   * @param dataType The channel data type.
   * @param locationCode The channel's location code.
   * @param latitude The channel's latitude.
   * @param longitude The channel's longitude.
   * @param elevation The channel's elevation.
   * @param depth The channel's depth
   * @param verticalAngle The channel's vertical orientation.
   * @param horizontalAngle The channel's horizontal orientation.
   * @param nominalSampleRate The channel's nominal sample rate.
   * @param actualTime The date and time the information was originally generated.
   * @param informationSource The source of this information.
   * @param comment A comment.
   * @param position This channel's relative position.
   * @param aliases The list of this channel's aliases.
   * @return A new ReferenceChannel object.
   */
  public static ReferenceChannel createNewVersion(UUID entityId,
      String name, ChannelType type, ChannelDataType dataType, int locationCode,
      double latitude, double longitude, double elevation, double depth,
      double verticalAngle, double horizontalAngle, double nominalSampleRate,
      Instant actualTime, InformationSource informationSource, String comment,
      RelativePosition position, List<ReferenceAlias> aliases) {

    return new ReferenceChannel(entityId, UUID.randomUUID(), name, type, dataType,
        locationCode, latitude, longitude, elevation, depth, verticalAngle, horizontalAngle,
        nominalSampleRate, actualTime, Instant.now(), informationSource, comment,
        position, aliases);
  }

  /**
   * Create a ReferenceChannel from existing information.
   *
   * @param entityId the id of the entity
   * @param versionId the id of the version of the entity
   * @param name The name for the channel, must be unique, and not empty.
   * @param type The channel type.
   * @param dataType The channel data type.
   * @param locationCode The channel's location code.
   * @param latitude The channel's latitude.
   * @param longitude The channel's longitude.
   * @param elevation The channel's elevation.
   * @param depth The channel's depth
   * @param verticalAngle The channel's vertical orientation.
   * @param horizontalAngle The channel's horizontal orientation.
   * @param nominalSampleRate The channel's nominal sample rate.
   * @param actualTime The date and time the information was originally generated.
   * @param systemTime The date and time the information was entered into the system.
   * @param informationSource The source of this information.
   * @param comment A comment.
   * @param position This channel's relative position.
   * @param aliases The list of this channel's aliases.
   * @return A new ReferenceChannel object.
   */
  public static ReferenceChannel from(UUID entityId, UUID versionId,
      String name, ChannelType type, ChannelDataType dataType, int locationCode,
      double latitude, double longitude, double elevation, double depth,
      double verticalAngle, double horizontalAngle, double nominalSampleRate,
      Instant actualTime, Instant systemTime,
      InformationSource informationSource, String comment,
      RelativePosition position, List<ReferenceAlias> aliases) {

    return new ReferenceChannel(entityId, versionId, name, type, dataType,
        locationCode, latitude, longitude, elevation, depth, verticalAngle, horizontalAngle,
        nominalSampleRate, actualTime, systemTime, informationSource, comment,
        position, aliases);
  }

  /**
   * Private constructor.
   */
  private ReferenceChannel(UUID entityId, UUID versionId, String name, ChannelType channelType,
      ChannelDataType channelDataType, int locationCode,
      double latitude, double longitude, double elevation, double depth,
      double verticalAngle, double horizontalAngle, double nominalSampleRate,
      Instant actualTime, Instant systemTime,
      InformationSource informationSource, String comment,
      RelativePosition position, List<ReferenceAlias> aliases)
      throws NullPointerException, InvalidParameterException {

    Validate.notEmpty(name);
    this.entityId = Objects.requireNonNull(entityId);
    this.versionId = Objects.requireNonNull(versionId);
    this.name = Objects.requireNonNull(name).toUpperCase();
    this.type = Objects.requireNonNull(channelType);
    this.dataType = Objects.requireNonNull(channelDataType);
    this.locationCode = locationCode;
    this.latitude = latitude;
    this.longitude = longitude;
    this.elevation = elevation;
    this.depth = depth;
    this.verticalAngle = verticalAngle;
    this.horizontalAngle = horizontalAngle;
    this.nominalSampleRate = nominalSampleRate;
    this.actualTime = Objects.requireNonNull(actualTime);
    this.systemTime = Objects.requireNonNull(systemTime);
    this.informationSource = Objects.requireNonNull(informationSource);
    this.comment = Objects.requireNonNull(comment);
    this.position = Objects.requireNonNull(position);
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

  public UUID getVersionId() { return versionId; }

  public String getName() {
    return name;
  }

  public ChannelType getType() {
    return type;
  }

  public ChannelDataType getDataType() { return dataType; }

  public int getLocationCode() { return locationCode; }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public double getElevation() {
    return elevation;
  }

  public double getDepth() { return depth; }

  public double getVerticalAngle() { return verticalAngle; }

  public double getHorizontalAngle() { return horizontalAngle; }

  public double  getNominalSampleRate() { return nominalSampleRate; }

  public Instant getActualTime() {
    return actualTime;
  }

  public Instant getSystemTime() {
    return systemTime;
  }

  public String getComment() {
    return comment;
  }

  public InformationSource getInformationSource() { return informationSource; }

  public RelativePosition getPosition() { return position; }

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

    ReferenceChannel that = (ReferenceChannel) o;

    if (locationCode != that.locationCode) {
      return false;
    }
    if (Double.compare(that.latitude, latitude) != 0) {
      return false;
    }
    if (Double.compare(that.longitude, longitude) != 0) {
      return false;
    }
    if (Double.compare(that.elevation, elevation) != 0) {
      return false;
    }
    if (Double.compare(that.depth, depth) != 0) {
      return false;
    }
    if (Double.compare(that.verticalAngle, verticalAngle) != 0) {
      return false;
    }
    if (Double.compare(that.horizontalAngle, horizontalAngle) != 0) {
      return false;
    }
    if (Double.compare(that.nominalSampleRate, nominalSampleRate) != 0) {
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
    if (type != that.type) {
      return false;
    }
    if (dataType != that.dataType) {
      return false;
    }
    if (actualTime != null ? !actualTime.equals(that.actualTime) : that.actualTime != null) {
      return false;
    }
    if (systemTime != null ? !systemTime.equals(that.systemTime) : that.systemTime != null) {
      return false;
    }
    if (informationSource != null ? !informationSource.equals(that.informationSource)
        : that.informationSource != null) {
      return false;
    }
    if (comment != null ? !comment.equals(that.comment) : that.comment != null) {
      return false;
    }
    if (position != null ? !position.equals(that.position) : that.position != null) {
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
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
    result = 31 * result + locationCode;
    temp = Double.doubleToLongBits(latitude);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(longitude);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(elevation);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(depth);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(verticalAngle);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(horizontalAngle);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(nominalSampleRate);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (actualTime != null ? actualTime.hashCode() : 0);
    result = 31 * result + (systemTime != null ? systemTime.hashCode() : 0);
    result = 31 * result + (informationSource != null ? informationSource.hashCode() : 0);
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + (position != null ? position.hashCode() : 0);
    result = 31 * result + (aliases != null ? aliases.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceChannel{" +
        "entityId=" + entityId +
        ", versionId=" + versionId +
        ", name='" + name + '\'' +
        ", type=" + type +
        ", dataType=" + dataType +
        ", locationCode=" + locationCode +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        ", elevation=" + elevation +
        ", depth=" + depth +
        ", verticalAngle=" + verticalAngle +
        ", horizontalAngle=" + horizontalAngle +
        ", nominalSampleRate=" + nominalSampleRate +
        ", actualTime=" + actualTime +
        ", systemTime=" + systemTime +
        ", informationSource=" + informationSource +
        ", comment='" + comment + '\'' +
        ", position=" + position +
        ", aliases=" + aliases +
        '}';
  }
}

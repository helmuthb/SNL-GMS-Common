package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.Validate;

/**
 * Represents a limited set of channel information used during the acquisition and
 * processing of data streams.
 */
public final class Channel {

  private final UUID id;
  private final String name;
  private final ChannelType channelType;
  private final ChannelDataType dataType;
  private final double latitude;
  private final double longitude;
  private final double elevation;
  private final double depth;
  private final double verticalAngle;
  private final double horizontalAngle;
  private final double sampleRate;
  private final Response response;
  private final Calibration calibration;

  /**
   * Create an instance of the class.
   *
   * @param name The name of the channel, not empty or null.
   * @param channelType the type of the channel
   * @param calibration The associated calibration information passed in the processed data. This
   * may be null.
   * @return a ProcessingChannel
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static Channel create(String name, ChannelType channelType, ChannelDataType dataType,
      double latitude, double longitude, double elevation, double depth, double verticalAngle,
      double horizontalAngle, double sampleRate, Response response,
      Calibration calibration) {

    return new Channel(UUID.randomUUID(), name, channelType, dataType,
        latitude, longitude, elevation, depth, verticalAngle, horizontalAngle,
        sampleRate, response, calibration);
  }

  /**
   * Recreates a ProcessingChannel given all params
   *
   * @param id the id of the channel
   * @param name The name of the channel, not empty or null.
   * @param channelType the type of the channel
   * @param calibration The associated calibration information passed in the processed data. This
   * may be null.
   * @return a ProcessingChannel
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static Channel from(UUID id, String name, ChannelType channelType, ChannelDataType dataType,
      double latitude, double longitude, double elevation, double depth, double verticalAngle,
      double horizontalAngle, double sampleRate, Response response,
      Calibration calibration) {
    return new Channel(id, name, channelType, dataType,
        latitude, longitude, elevation, depth, verticalAngle, horizontalAngle,
        sampleRate, response, calibration);
  }

  /**
   * Create an instance of the class.
   *
   * @param id the identifier for this ProcessingChannel
   * @param name The name of the channel, not empty or null.
   * @param channelType the type of the channel
   * @param calibration The associated calibration information passed in the processed data. This
   * may be null.
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  private Channel(UUID id, String name, ChannelType channelType, ChannelDataType dataType,
      double latitude, double longitude, double elevation, double depth, double verticalAngle,
      double horizontalAngle, double sampleRate, Response response,
      Calibration calibration) {

    Validate.notBlank(name);
    this.id = Objects.requireNonNull(id);
    this.name = Objects.requireNonNull(name);
    this.channelType = Objects.requireNonNull(channelType);
    this.dataType = Objects.requireNonNull(dataType);
    this.latitude = latitude;
    this.longitude = longitude;
    this.elevation = elevation;
    this.depth = depth;
    this.verticalAngle = verticalAngle;
    this.horizontalAngle = horizontalAngle;
    this.sampleRate = sampleRate;
    this.response = Objects.requireNonNull(response);
    this.calibration = Objects.requireNonNull(calibration);
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public ChannelType getChannelType() {
    return channelType;
  }

  public ChannelDataType getDataType() {
    return dataType;
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

  public double getDepth() {
    return depth;
  }

  public double getVerticalAngle() {
    return verticalAngle;
  }

  public double getHorizontalAngle() {
    return horizontalAngle;
  }

  public double getSampleRate() {
    return sampleRate;
  }

  public Response getResponse() {
    return response;
  }

  public Calibration getCalibration() {
    return calibration;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Channel channel = (Channel) o;

    if (Double.compare(channel.latitude, latitude) != 0) {
      return false;
    }
    if (Double.compare(channel.longitude, longitude) != 0) {
      return false;
    }
    if (Double.compare(channel.elevation, elevation) != 0) {
      return false;
    }
    if (Double.compare(channel.depth, depth) != 0) {
      return false;
    }
    if (Double.compare(channel.verticalAngle, verticalAngle) != 0) {
      return false;
    }
    if (Double.compare(channel.horizontalAngle, horizontalAngle) != 0) {
      return false;
    }
    if (Double.compare(channel.sampleRate, sampleRate) != 0) {
      return false;
    }
    if (id != null ? !id.equals(channel.id) : channel.id != null) {
      return false;
    }
    if (name != null ? !name.equals(channel.name) : channel.name != null) {
      return false;
    }
    if (channelType != channel.channelType) {
      return false;
    }
    if (dataType != channel.dataType) {
      return false;
    }
    if (response != null ? !response.equals(channel.response) : channel.response != null) {
      return false;
    }
    return calibration != null ? calibration.equals(channel.calibration)
        : channel.calibration == null;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = id != null ? id.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (channelType != null ? channelType.hashCode() : 0);
    result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
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
    temp = Double.doubleToLongBits(sampleRate);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (response != null ? response.hashCode() : 0);
    result = 31 * result + (calibration != null ? calibration.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Channel{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", channelType=" + channelType +
        ", dataType=" + dataType +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        ", elevation=" + elevation +
        ", depth=" + depth +
        ", verticalAngle=" + verticalAngle +
        ", horizontalAngle=" + horizontalAngle +
        ", sampleRate=" + sampleRate +
        ", response=" + response +
        ", calibration=" + calibration +
        '}';
  }
}

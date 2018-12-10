package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.Validate;

/**
 * Represents a frame of data from a station; could be received via various protocols. It includes
 * the start/end time of the data, a reference by ID to the channel the data is for, the time it was
 * received, a raw payload (bytes) - this represents the whole raw frame, and the status of its
 * authentication.
 */
public final class RawStationDataFrame {

  private final UUID id;
  private final UUID stationId;
  private final AcquisitionProtocol acquisitionProtocol;
  private final String stationName;
  private final Instant payloadDataStartTime;
  private final Instant payloadDataEndTime;
  private final Instant receptionTime;
  private final byte[] rawPayload;
  private final AuthenticationStatus authenticationStatus;
  private final CreationInfo creationInfo;

  /**
   * Enum for the status of authentication of a frame.
   */
  public enum AuthenticationStatus {
    NOT_APPLICABLE,
    AUTHENTICATION_FAILED,
    AUTHENTICATION_SUCCEEDED,
    NOT_YET_AUTHENITCATED
  }

  /**
   * Creates a RawStationDataFrame anew, generating an ID.
   *
   * @param stationId the station ID
   * @param acquisitionProtocol acquisition protocol
   * @param stationName name of the station
   * @param payloadDataStartTime start time of the payload data
   * @param payloadDataEndTime end time of the payload data
   * @param receptionTime the time the frame was received
   * @param rawPayload the raw data of the frame
   * @param authenticationStatus the status of authentication for the frame
   * @param creationInfo provenance info about the frame
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static RawStationDataFrame create(UUID stationId,
      AcquisitionProtocol acquisitionProtocol,
      String stationName, Instant payloadDataStartTime,
      Instant payloadDataEndTime, Instant receptionTime, byte[] rawPayload,
      AuthenticationStatus authenticationStatus, CreationInfo creationInfo) {

    return new RawStationDataFrame(UUID.randomUUID(), stationId, acquisitionProtocol,
        stationName, payloadDataStartTime, payloadDataEndTime,
        receptionTime, rawPayload, authenticationStatus, creationInfo);
  }

  /**
   * Recreates a RawStationDataFrame with the provided id
   *
   * @param stationId the station ID
   * @param acquisitionProtocol acquisition protocol
   * @param stationName name of the station
   * @param payloadDataStartTime start time of the payload data
   * @param payloadDataEndTime end time of the payload data
   * @param receptionTime the time the frame was received
   * @param rawPayload the raw data of the frame
   * @param authenticationStatus the status of authentication for the frame
   * @param creationInfo provenance info about the frame
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static RawStationDataFrame from(UUID id, UUID stationId,
      AcquisitionProtocol acquisitionProtocol,
      String stationName, Instant payloadDataStartTime,
      Instant payloadDataEndTime, Instant receptionTime, byte[] rawPayload,
      AuthenticationStatus authenticationStatus, CreationInfo creationInfo) {

    return new RawStationDataFrame(id, stationId, acquisitionProtocol, stationName,
        payloadDataStartTime, payloadDataEndTime,
        receptionTime, rawPayload, authenticationStatus, creationInfo);
  }

  /**
   * Creates a RawStationDataFrame given all fields.
   *
   * @param id the identifier of the frame
   * @param stationId the station ID
   * @param acquisitionProtocol acquisition protocol
   * @param stationName name of the station
   * @param payloadDataStartTime start time of the payload data
   * @param payloadDataEndTime end time of the payload data
   * @param receptionTime the time the frame was received
   * @param rawPayload the raw data of the frame
   * @param authenticationStatus the status of authentication for the frame
   * @param creationInfo provenance info about the frame
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  private RawStationDataFrame(UUID id, UUID stationId, AcquisitionProtocol acquisitionProtocol,
      String stationName, Instant payloadDataStartTime, Instant payloadDataEndTime,
      Instant receptionTime, byte[] rawPayload,
      AuthenticationStatus authenticationStatus, CreationInfo creationInfo) {

    Validate.isTrue(payloadDataEndTime.isAfter(payloadDataStartTime));
    this.id = Objects.requireNonNull(id);
    this.stationId = Objects.requireNonNull(stationId);
    this.acquisitionProtocol = Objects.requireNonNull(acquisitionProtocol);
    this.stationName = Objects.requireNonNull(stationName);
    this.payloadDataStartTime = Objects.requireNonNull(payloadDataStartTime);
    this.payloadDataEndTime = Objects.requireNonNull(payloadDataEndTime);
    this.receptionTime = Objects.requireNonNull(receptionTime);
    this.rawPayload = Objects.requireNonNull(rawPayload);
    this.authenticationStatus = Objects.requireNonNull(authenticationStatus);
    this.creationInfo = Objects.requireNonNull(creationInfo);
  }

  public UUID getId() {
    return id;
  }

  public UUID getStationId() {
    return stationId;
  }

  public AcquisitionProtocol getAcquisitionProtocol() {
    return acquisitionProtocol;
  }

  public String getStationName() {
    return stationName;
  }

  public Instant getPayloadDataStartTime() {
    return payloadDataStartTime;
  }

  public Instant getPayloadDataEndTime() {
    return payloadDataEndTime;
  }

  public Instant getReceptionTime() {
    return receptionTime;
  }

  public byte[] getRawPayload() {
    return rawPayload;
  }

  public AuthenticationStatus getAuthenticationStatus() {
    return authenticationStatus;
  }

  public CreationInfo getCreationInfo() {
    return creationInfo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RawStationDataFrame that = (RawStationDataFrame) o;

    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (stationId != null ? !stationId.equals(that.stationId) : that.stationId != null) {
      return false;
    }
    if (acquisitionProtocol != that.acquisitionProtocol) {
      return false;
    }
    if (stationName != null ? !stationName.equals(that.stationName) : that.stationName != null) {
      return false;
    }
    if (payloadDataStartTime != null ? !payloadDataStartTime.equals(that.payloadDataStartTime)
        : that.payloadDataStartTime != null) {
      return false;
    }
    if (payloadDataEndTime != null ? !payloadDataEndTime.equals(that.payloadDataEndTime)
        : that.payloadDataEndTime != null) {
      return false;
    }
    if (receptionTime != null ? !receptionTime.equals(that.receptionTime)
        : that.receptionTime != null) {
      return false;
    }
    if (!Arrays.equals(rawPayload, that.rawPayload)) {
      return false;
    }
    if (authenticationStatus != that.authenticationStatus) {
      return false;
    }
    return creationInfo != null ? creationInfo.equals(that.creationInfo)
        : that.creationInfo == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (stationId != null ? stationId.hashCode() : 0);
    result = 31 * result + (acquisitionProtocol != null ? acquisitionProtocol.hashCode() : 0);
    result = 31 * result + (stationName != null ? stationName.hashCode() : 0);
    result = 31 * result + (payloadDataStartTime != null ? payloadDataStartTime.hashCode() : 0);
    result = 31 * result + (payloadDataEndTime != null ? payloadDataEndTime.hashCode() : 0);
    result = 31 * result + (receptionTime != null ? receptionTime.hashCode() : 0);
    result = 31 * result + Arrays.hashCode(rawPayload);
    result = 31 * result + (authenticationStatus != null ? authenticationStatus.hashCode() : 0);
    result = 31 * result + (creationInfo != null ? creationInfo.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "RawStationDataFrame{" +
        "id=" + id +
        ", stationId=" + stationId +
        ", acquisitionProtocol=" + acquisitionProtocol +
        ", stationName='" + stationName + '\'' +
        ", payloadDataStartTime=" + payloadDataStartTime +
        ", payloadDataEndTime=" + payloadDataEndTime +
        ", receptionTime=" + receptionTime +
        ", rawPayload=" + Arrays.toString(rawPayload) +
        ", authenticationStatus=" + authenticationStatus +
        ", creationInfo=" + creationInfo +
        '}';
  }
}

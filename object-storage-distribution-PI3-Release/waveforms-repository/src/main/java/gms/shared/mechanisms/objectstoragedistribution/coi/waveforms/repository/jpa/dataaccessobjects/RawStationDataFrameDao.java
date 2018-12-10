package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects.CreationInfoDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import javax.persistence.*;
import org.apache.commons.lang3.Validate;

@Entity
@Table(name = "raw_station_data_frame")
public class RawStationDataFrameDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(unique = true)
  private UUID id;

  @Column(name = "station_id")
  private UUID stationId;

  @Column(name = "acquisition_protocol")
  private AcquisitionProtocol acquisitionProtocol;

  @Column(name = "station_name")
  private String stationName;

  @Column(nullable=false, name = "payload_data_start_time", columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant payloadDataStartTime;

  @Column(nullable=false, name = "payload_data_end_time", columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant payloadDataEndTime;

  @Column(nullable=false, name = "reception_time")
  private Instant receptionTime;

  @Lob
  @Column(nullable=false, name = "raw_payload_blob")
  private byte[] rawPayload;

  @Column(nullable=false, name = "authentication_status")
  private AuthenticationStatus authenticationStatus;

  @Embedded
  private CreationInfoDao creationInfo;

  /**
   * Default no-arg constructor (for use by JPA)
   */
  public RawStationDataFrameDao() {}


  public RawStationDataFrameDao(RawStationDataFrame df) {
    Validate.notNull(df);
    this.id = df.getId();
    this.stationId = df.getStationId();
    this.acquisitionProtocol = df.getAcquisitionProtocol();
    this.stationName = df.getStationName();
    this.payloadDataStartTime = df.getPayloadDataStartTime();
    this.payloadDataEndTime = df.getPayloadDataEndTime();
    this.receptionTime = df.getReceptionTime();
    this.rawPayload = df.getRawPayload();
    this.authenticationStatus = df.getAuthenticationStatus();
    this.creationInfo = new CreationInfoDao(df.getCreationInfo());
  }

  public RawStationDataFrame toCoi() {
    return RawStationDataFrame.from(this.id, this.stationId,
        this.acquisitionProtocol, this.stationName,
        this.payloadDataStartTime, this.payloadDataEndTime,
        this.receptionTime, this.rawPayload,
        this.authenticationStatus, this.creationInfo.toCoi());
  }


  public long getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(long primaryKey) {
    this.primaryKey = primaryKey;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getStationId() {
    return stationId;
  }

  public void setStationId(UUID stationId) {
    this.stationId = stationId;
  }

  public AcquisitionProtocol getAcquisitionProtocol() {
    return acquisitionProtocol;
  }

  public void setAcquisitionProtocol(
      AcquisitionProtocol acquisitionProtocol) {
    this.acquisitionProtocol = acquisitionProtocol;
  }

  public String getStationName() {
    return stationName;
  }

  public void setStationName(String stationName) {
    this.stationName = stationName;
  }

  public Instant getPayloadDataStartTime() {
    return payloadDataStartTime;
  }

  public void setPayloadDataStartTime(Instant payloadDataStartTime) {
    this.payloadDataStartTime = payloadDataStartTime;
  }

  public Instant getPayloadDataEndTime() {
    return payloadDataEndTime;
  }

  public void setPayloadDataEndTime(Instant payloadDataEndTime) {
    this.payloadDataEndTime = payloadDataEndTime;
  }

  public Instant getReceptionTime() {
    return receptionTime;
  }

  public void setReceptionTime(Instant receptionTime) {
    this.receptionTime = receptionTime;
  }

  public byte[] getRawPayload() {
    return rawPayload;
  }

  public void setRawPayload(byte[] rawPayload) {
    this.rawPayload = rawPayload;
  }

  public AuthenticationStatus getAuthenticationStatus() {
    return authenticationStatus;
  }

  public void setAuthenticationStatus(AuthenticationStatus authenticationStatus) {
    this.authenticationStatus = authenticationStatus;
  }

  public CreationInfoDao getCreationInfo() {
    return creationInfo;
  }

  public void setCreationInfo(
      CreationInfoDao creationInfo) {
    this.creationInfo = creationInfo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RawStationDataFrameDao that = (RawStationDataFrameDao) o;

    if (primaryKey != that.primaryKey) {
      return false;
    }
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
    int result = (int) (primaryKey ^ (primaryKey >>> 32));
    result = 31 * result + (id != null ? id.hashCode() : 0);
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
    return "RawStationDataFrameDao{" +
        "primaryKey=" + primaryKey +
        ", id=" + id +
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

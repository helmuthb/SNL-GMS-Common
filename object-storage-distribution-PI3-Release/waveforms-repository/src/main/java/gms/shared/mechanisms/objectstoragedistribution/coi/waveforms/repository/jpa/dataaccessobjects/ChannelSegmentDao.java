package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects;
import java.time.Instant;
import java.util.SortedSet;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects.CreationInfoDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import org.apache.commons.lang3.Validate;

/**
 * Define a Data Access Object to allow access to the relational database.
 */

@Entity
@Table(name = "channel_segment",
        uniqueConstraints = {
        // TODO: "processing_channel_id" should be added to this list.  However, CSS loader service
        // would probably need to query for a ProcessingChannel from the OSD first and if found,
        // use that ID in the ChannelSegment it receives from the CSS loader client.  Without this check,
        // the attempt to store an identical ChannelSegment except with different processingChannelId will succeed.
        // In summary, the issue is when CSS loader creates a ChannelSegment with a new processingChannelId even though
        // the ProcessingChannel actually exists in the OSD.
                @UniqueConstraint(columnNames = {"name", "segmentType", "startTime", "endTime"})})
public class ChannelSegmentDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(unique = true)
  private UUID id;

  @Column(name = "processing_channel_id")
  private UUID processingChannelId;

  @Column(name = "name", nullable=false)
  private String name;

  @Column(name = "segmentType", nullable=false)
  private ChannelSegment.ChannelSegmentType segmentType;

  @Column(name = "startTime", nullable=false)
  private Instant startTime;

  @Column(name = "endTime", nullable=false)
  private Instant endTime;

  @Embedded
  private CreationInfoDao creationInfo;

  @Transient
  private SortedSet<Waveform> waveforms;
  @Transient
  private ChannelSegment channelSegment;

  /**
   * Default constructor for use by JPA
   */
  public ChannelSegmentDao() {
  }

  /**
   * Create this DAO from the COI object.
   *
   * @param channelSegment  COI object
   */
  public ChannelSegmentDao(ChannelSegment channelSegment) throws NullPointerException {
    Validate.notNull(channelSegment);
    this.id = channelSegment.getId();
    this.processingChannelId = channelSegment.getProcessingChannelId();
    this.name = channelSegment.getName();
    this.segmentType = channelSegment.getSegmentType();
    this.startTime = channelSegment.getStartTime();
    this.endTime = channelSegment.getEndTime();
    this.waveforms = channelSegment.getWaveforms();
    this.creationInfo = new CreationInfoDao(channelSegment.getCreationInfo());
    this.channelSegment = channelSegment;
  }

  /**
   * Convert this DAO into the associated COI object.
   *
   * @return An instance of the ChannelSegment object.
   */
  public ChannelSegment toCoi() {
    return ChannelSegment.from(this.id, this.processingChannelId, this.name,
        this.segmentType, this.startTime, this.endTime, this.waveforms,
        this.creationInfo.toCoi());
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getProcessingChannelId() {
    return processingChannelId;
  }

  public void setProcessingChannelId(UUID processingChannelId) {
    this.processingChannelId = processingChannelId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ChannelSegment.ChannelSegmentType getSegmentType() {
    return segmentType;
  }

  public void setSegmentType(
      ChannelSegment.ChannelSegmentType segmentType) {
    this.segmentType = segmentType;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(Instant endTime) {
    this.endTime = endTime;
  }

  public SortedSet<Waveform> getWaveforms() {
    return waveforms;
  }

  public void setWaveforms(
      SortedSet<Waveform> waveforms) {
    this.waveforms = waveforms;
  }

  public ChannelSegment getChannelSegment() {
    return channelSegment;
  }

  public void setChannelSegment(
      ChannelSegment channelSegment) {
    this.channelSegment = channelSegment;
  }

  public CreationInfoDao getCreationInfo() {
    return creationInfo;
  }

  public void setCreationInfo(CreationInfoDao creationInfo) {
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

    ChannelSegmentDao that = (ChannelSegmentDao) o;

    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (processingChannelId != null ? !processingChannelId.equals(that.processingChannelId) : that.processingChannelId != null) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (segmentType != that.segmentType) {
      return false;
    }
    if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) {
      return false;
    }
    if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) {
      return false;
    }
    if (creationInfo != null ? !creationInfo.equals(that.creationInfo)
        : that.creationInfo != null) {
      return false;
    }
    if (waveforms != null ? !waveforms.equals(that.waveforms) : that.waveforms != null) {
      return false;
    }
    return channelSegment != null ? channelSegment.equals(that.channelSegment)
        : that.channelSegment == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (primaryKey ^ (primaryKey >>> 32));
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (processingChannelId != null ? processingChannelId.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (segmentType != null ? segmentType.hashCode() : 0);
    result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
    result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
    result = 31 * result + (creationInfo != null ? creationInfo.hashCode() : 0);
    result = 31 * result + (waveforms != null ? waveforms.hashCode() : 0);
    result = 31 * result + (channelSegment != null ? channelSegment.hashCode() : 0);
    return result;
  }
}

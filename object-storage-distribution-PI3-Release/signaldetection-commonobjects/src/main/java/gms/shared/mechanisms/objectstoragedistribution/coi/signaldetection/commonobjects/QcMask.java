package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.ParameterValidation;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Represents an interval of one or more unprocessed channel segments where there are data quality
 * issues, e.g., missing data, spikes, etc. This interval contains n-number from QC Mask Versions
 * and the processing channel this QcMask has been created for.
 */
public class QcMask {

  private final UUID id;
  private final UUID processingChannelId;
  private List<QcMaskVersion> qcMaskVersions;

  /**
   * Obtains an instance from QcMask from the processingChannelId.
   *
   * @param id The {@link UUID} id assigned to the new QcMask.
   * @param processingChannelId The identifier from the ProcessingChannel this QcMask is to be used
   * on.
   * @param qcMaskVersions The set of {@link QcMaskVersion} objects assigned to the new QcMask.
   */
  private QcMask(UUID id, UUID processingChannelId,
      List<QcMaskVersion> qcMaskVersions) {

    this.id = id;
    this.processingChannelId = processingChannelId;
    this.qcMaskVersions = qcMaskVersions;
  }

  /**
   * Recreation factory method (sets the QcMask entity identity). Handles parameter validation. Used
   * for deserialization and recreating from persistence.
   *
   * @param id Id for the QcMask.
   * @param processingChannelId Id for the Processing Channel this QcMask was created for.
   * @param qcMaskVersions Historical version of this QcMask.
   * @return QcMask representing all the input parameters.
   * @throws IllegalArgumentException if id, processingChannelId, or qcMaskVersions are null
   */
  public static QcMask from(UUID id,
      UUID processingChannelId,
      List<QcMaskVersion> qcMaskVersions) {
    Objects.requireNonNull(id, "Cannot create QcMask from a null id");
    Objects.requireNonNull(processingChannelId,
        "Cannot create QcMask from a null ProcessingChannel id");
    Objects.requireNonNull(qcMaskVersions, "Cannot create QcMask from null QcMaskVersions");

    return new QcMask(id, processingChannelId, new ArrayList<>(qcMaskVersions));
  }

  /**
   * Factory method for creating a brand new QcMask with an initial QcMaskVersion.  Assigns the
   * QcMask entity identity.
   *
   * @param processingChannelId Id for the Processing Channel this QcMask was created for, not null
   * @param parents collection of parents for the initial version of this mask, not null
   * @param channelSegmentIdList Collection of Ids for Channel Segments this QcMask was created on,
   * not null
   * @param category Category of the QcMask.
   * @param type Type of the QcMask, not null
   * @param rationale Rationale for creating the QcMask. Empty string is allowed.
   * @param startTime Start of the time range this QcMask is masking.
   * @param endTime End of the time range this QcMask is masking.
   * @param creationInfoId Information pertaining to the creation of this QcMask (e.g. author,
   * creation time), not null
   * @return New QcMask with a initial version representing the input.
   */
  public static QcMask create(UUID processingChannelId,
      Collection<QcMaskVersionReference> parents,
      List<UUID> channelSegmentIdList, QcMaskCategory category, QcMaskType type,
      String rationale, Instant startTime, Instant endTime, UUID creationInfoId) {

    ParameterValidation.requireFalse(QcMaskCategory.REJECTED::equals, category,
        "Cannot create QcMask with REJECTED QcMaskCategory");

    return from(UUID.randomUUID(), processingChannelId,
        Collections.singletonList(
            QcMaskVersion.create(0L, parents,
                channelSegmentIdList, category, type, rationale, startTime, endTime,
                creationInfoId)));
  }

  /**
   * Adds a new version instance {@link QcMaskVersion} for this mask. This method assumes the parent
   * version is the current last version of this QcMask (i.e. the last entry in the qcMaskVersions
   * list).
   *
   * @param channelSegmentIds The list of channel segment ids for which this version is created.
   * @param category The QcMask category
   * @param type The QcMask type
   * @param rationale The descriptive reason for creating this version.
   * @param startTime The start time of this version.
   * @param endTime The end time of this version
   * @param creationInfoId This version creation info id.
   * @throws IllegalArgumentException if any from the input parameters are null
   */
  public void addQcMaskVersion(List<UUID> channelSegmentIds, QcMaskCategory category,
      QcMaskType type,
      String rationale, Instant startTime, Instant endTime,
      UUID creationInfoId) {
    ParameterValidation.requireFalse(QcMaskCategory.REJECTED::equals, category,
        "Cannot add QcMaskVersion with REJECTED QcMaskCategory");

    //validation checks happen in QcMaskVersion creation
    this.qcMaskVersions.add(
        QcMaskVersion.create(getCurrentQcMaskVersion().getVersion() + 1,
            Collections.singletonList(getCurrentVersionAsReference()), channelSegmentIds,
            category, type, rationale, startTime, endTime, creationInfoId));
  }

  /**
   * Creates a new rejected {@link QcMaskVersion} for this mask. This method assumes the parent
   * version is the current last version from this QcMask (i.e. the last entry in the qcMaskVersions
   * list).
   *
   * @param rationale The descriptive reason for creating this rejected version.
   * @param channelSegmentIds The list of channel segment ids related to the qc mask rejection
   * @param creationInfoId The creation info for this QcMaskVersion.
   * @throws IllegalArgumentException if rationale is null
   */
  public void reject(String rationale,
      List<UUID> channelSegmentIds, UUID creationInfoId) {
    //null checks happen in QcMaskVersion creation
    this.qcMaskVersions.add(
        QcMaskVersion.from(
            getCurrentQcMaskVersion().getVersion() + 1,
            Collections.singletonList(getCurrentVersionAsReference()),
            channelSegmentIds, QcMaskCategory.REJECTED, null, rationale, null, null, creationInfoId)
    );
  }

  /**
   * Creates a {@link QcMaskVersionReference} using this QcMask's current version as the parent
   *
   * @return QcMask's current version as a QcMaskVersionReference, not null
   */
  private QcMaskVersionReference getCurrentVersionAsReference() {
    return QcMaskVersionReference.from(this.getId(), this.getCurrentQcMaskVersion().getVersion());
  }

  /**
   * Gets the {@link UUID} for this QcMask object.
   *
   * @return unique id for a QcMask, not null.
   */
  public UUID getId() {

    return this.id;
  }

  /**
   * Gets the ProcessingChannelId for this QcMask object.
   *
   * @return the QcMask's ProcessingChannelId, not null.
   */
  public UUID getProcessingChannelId() {

    return this.processingChannelId;
  }

  /**
   * Gets the current {@link QcMaskVersion} of this mask
   *
   * @return current QcMaskVersion, not null
   */
  public QcMaskVersion getCurrentQcMaskVersion() {
    return this.qcMaskVersions.get(this.qcMaskVersions.size() - 1);
  }

  /**
   * Returns the stream of {@link QcMaskVersion} objects owned by this {@link QcMask}.
   *
   * @return The stream of {@link QcMaskVersion} objects owned by this {@link QcMask}.
   */
  public Stream<QcMaskVersion> qcMaskVersions() {
    return this.qcMaskVersions.stream();
  }

  /**
   * Returns the list of QcMaskVersions for this QcMask.
   *
   * @return The list of QcMaskVersions for this QcMask
   */
  public List<QcMaskVersion> getQcMaskVersions() {
    return this.qcMaskVersions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QcMask qcMask = (QcMask) o;
    return Objects.equals(id, qcMask.id) &&
        Objects.equals(processingChannelId, qcMask.processingChannelId) &&
        Objects.equals(qcMaskVersions, qcMask.qcMaskVersions);
  }

  @Override
  public int hashCode() {

    return Objects.hash(id, processingChannelId, qcMaskVersions);
  }

  @Override
  public String toString() {
    return "QcMask{" +
        "id=" + id +
        ", processingChannelId=" + processingChannelId +
        ", qcMaskVersions=" + qcMaskVersions +
        '}';
  }
}
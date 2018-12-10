package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.ParameterValidation;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a version of the {@link QcMask} to identify where there are data quality issues, e.g.,
 * missing data, spikes, etc.
 */
public class QcMaskVersion {

  private final long version;
  private final Collection<QcMaskVersionReference> parentQcMasks;
  private final List<UUID> channelSegmentIds;
  private final QcMaskCategory category;
  private final QcMaskType type;
  private final String rationale;
  private final Instant startTime;
  private final Instant endTime;
  private final UUID creationInfoId;

  /**
   * Obtains an instance of a package protected QcMaskVersion for a given a version Identity, a
   * parentQcMasks reference {@link QcMaskVersionReference}, and a list of channelSegmentIds. These
   * objects are created exclusively by {@link QcMask}.
   *
   * @param version A unique sequential number identifying an instance of a QcMaskVersion for a
   * given {@link QcMask}.
   * @param parentQcMasks An identifier for the {@link QcMaskVersion} which contains this
   * QcMaskVersion.
   * @param channelSegmentIds A list of channelSegmentIds this QcMaskVersion is used on.
   * @param category Represents the category of {@link QcMask}.
   * @param type Represents the type of {@link QcMask}.
   * @param rationale The justification for creating this QcMaskVersion, either from an Analyst's
   * notes or the automatic System.
   * @param startTime The starting time the [@link QcMask} takes affect.
   * @param endTime The ending time the [@link QcMask} takes affect.
   * @param creationInfoId An identifier representing this object's provenance.
   */
  private QcMaskVersion(
      long version,
      Collection<QcMaskVersionReference> parentQcMasks,
      List<UUID> channelSegmentIds,
      QcMaskCategory category, QcMaskType type, String rationale,
      Instant startTime,
      Instant endTime, UUID creationInfoId) {
    this.version = version;
    this.parentQcMasks = parentQcMasks;
    this.channelSegmentIds = channelSegmentIds;
    this.category = category;
    this.type = type;
    this.rationale = rationale;
    this.startTime = startTime;
    this.endTime = endTime;
    this.creationInfoId = creationInfoId;
  }

  /**
   * Default factory method used to create a QcMaskVersion. Primarily used by other factory methods
   * and for serialization.
   *
   * @param version The version Identity of the created QcMaskVersion.
   * @param parentQcMasks The {@link QcMaskVersion} parentQcMasks identifier.
   * @param channelSegmentIds A list of channel segment ids for which this QcMaskVersion applies.
   * @param category Represents the category of {@link QcMask}.
   * @param type The type of {@link QcMask}.
   * @param rationale Any rationale for creating the QcMaskVersion.
   * @param startTime The start time of the QcMaskVersion.
   * @param endTime The end time of the QcMaskVersion.
   * @param creationInfoId The creation info for this QcMaskVersion.
   * @return The QcMaskVersion representing the input.
   */
  public static QcMaskVersion from(long version,
      Collection<QcMaskVersionReference> parentQcMasks,
      List<UUID> channelSegmentIds, QcMaskCategory category, QcMaskType type,
      String rationale, Instant startTime,
      Instant endTime, UUID creationInfoId) {
    Objects.requireNonNull(version, "QCMaskVersion's version cannot be null");
    Objects.requireNonNull(parentQcMasks, "QCMaskVersion's parentQcMasks cannot be null");
    Objects.requireNonNull(channelSegmentIds,
        "QCMaskVersion's channel segment id list cannot be null");
    Objects.requireNonNull(category, "QCMaskVersion's QcMaskCategory cannot be null");
    Objects.requireNonNull(rationale, "QCMaskVersion's rationale cannot be null");

    if (!QcMaskCategory.REJECTED.equals(category)) {
      Objects.requireNonNull(type, "QCMaskVersion's QcMaskType cannot be null");
      Objects.requireNonNull(startTime, "QCMaskVersion's start time cannot be null");
      Objects.requireNonNull(endTime, "QCMaskVersion's end time cannot be null");
      ParameterValidation.requireFalse(Instant::isAfter, startTime, endTime,
          "QcMaskVersion's start time must be before or equal to end time.");
    }

    Objects.requireNonNull(creationInfoId, "QCMaskVersion's creation info id cannot be null");

    ParameterValidation.requireTrue(category::isValidType, type,
        "QCMaskVersion's QcMaskType is not valid for the input QcMaskCategory");

    ParameterValidation
        .requireTrue(parents -> parents.stream().distinct().count() == parents.size(),
            parentQcMasks, "QcMaskVersion's parentQcMasks cannot contain duplicates");

    return new QcMaskVersion(version, parentQcMasks, channelSegmentIds, category, type, rationale,
        startTime, endTime, creationInfoId);
  }

  //TODO: Remove this method
  /**
   * Create a new non-rejected version of a QcMask
   *
   * @param version The version Identity of the created QcMaskVersion.
   * @param parentQcMasks The {@link QcMaskVersion} parentQcMasks identifier.
   * @param channelSegmentIds A list of channel segment ids for which this QcMaskVersion applies.
   * @param category Represents the category of {@link QcMask}.
   * @param type The type from {@link QcMask}.
   * @param rationale Any rationale for creating the QcMaskVersion
   * @param startTime The start time of the QcMaskVersion.
   * @param endTime The end time of the QcMaskVersion.
   * @param creationInfoId The creation info for this QcMaskVersion.
   * @return The new NON-Rejected QcMaskVersion
   */
  public static QcMaskVersion create(long version,
      Collection<QcMaskVersionReference> parentQcMasks,
      List<UUID> channelSegmentIds, QcMaskCategory category, QcMaskType type,
      String rationale, Instant startTime,
      Instant endTime, UUID creationInfoId) {

    //validation checks in from method
    return from(version, parentQcMasks, channelSegmentIds, category, type, rationale, startTime,
        endTime, creationInfoId);
  }

  /**
   * Returns true if parentQcMasks does not equal QcMaskVersionReference.noParent.
   *
   * @return True if parentQcMasks does not equal QcMaskVersionReference.noParent.
   */
  public boolean hasParent() {
    return (!parentQcMasks.isEmpty());
  }

  public long getVersion() {
    return version;
  }

  public Collection<QcMaskVersionReference> getParentQcMasks() {
    return parentQcMasks;
  }

  public List<UUID> getChannelSegmentIds() {
    return channelSegmentIds;
  }

  public Optional<QcMaskType> getType() {
    return Optional.ofNullable(type);
  }

  public QcMaskCategory getCategory() {
    return category;
  }

  public String getRationale() {
    return rationale;
  }

  public Optional<Instant> getStartTime() {
    return Optional.ofNullable(startTime);
  }

  public Optional<Instant> getEndTime() {
    return Optional.ofNullable(endTime);
  }

  public boolean isRejected() {
    return QcMaskCategory.REJECTED.equals(category);
  }

  public UUID getCreationInfoId() {
    return creationInfoId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QcMaskVersion that = (QcMaskVersion) o;
    return version == that.version &&
        Objects.equals(parentQcMasks, that.parentQcMasks) &&
        Objects.equals(channelSegmentIds, that.channelSegmentIds) &&
        category == that.category &&
        type == that.type &&
        Objects.equals(rationale, that.rationale) &&
        Objects.equals(startTime, that.startTime) &&
        Objects.equals(endTime, that.endTime) &&
        Objects.equals(creationInfoId, that.creationInfoId);
  }

  @Override
  public int hashCode() {

    return Objects
        .hash(version, parentQcMasks, channelSegmentIds, category, type, rationale, startTime,
            endTime, creationInfoId);
  }

  @Override
  public String toString() {
    return "QcMaskVersion{" +
        "version=" + version +
        ", parentQcMasks=" + parentQcMasks +
        ", channelSegmentIds=" + channelSegmentIds +
        ", type=" + type +
        ", category=" + category +
        ", rationale='" + rationale + '\'' +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", creationInfoId=" + creationInfoId +
        '}';
  }
}

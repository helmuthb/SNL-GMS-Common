package gms.core.waveformqc.waveformqccontrol.objects;

import static java.util.Objects.requireNonNull;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Waveform Quality Control Processing specific data object holding equivalent information to {@link
 * gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh}
 * in a form more suitable for QC Mask creation.  WaveformQcChannelSohStatus is for a single
 * combination of {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel},
 * {@link QcMaskType}, and {@link ChannelSohSubtype} and includes a list of statuses (each status is
 * {@link Instant} start time, end time, and {@link StatusState} status value).  There is only a new
 * status when the status value changes but there is always at least one status.  The
 * ChannelSohSubtype has a string representation which corresponds to a QcMaskVersion's {@link
 * QcMaskVersion#getRationale()}.
 */
public class WaveformQcChannelSohStatus {

  private final UUID processingChannelId;
  private final QcMaskType qcMaskType;
  private final ChannelSohSubtype channelSohSubtype;
  private final List<Status> statusChanges;

  /**
   * Acquired status bits can be set, unset, or missing
   */
  public enum StatusState {
    SET, UNSET, MISSING;

    public static StatusState of(boolean status) {
      return status ? SET : UNSET;
    }
  }

  /**
   * Combines a status {@link Instant} time and a enum status value.
   */
  public static class Status {

    final Instant startTime;
    final Instant endTime;
    final StatusState status;

    private Status(Instant startTime, Instant endTime, StatusState status) {
      this.startTime = startTime;
      this.endTime = endTime;
      this.status = status;
    }

    /**
     * Obtains a new {@link Status} from the provided startTime, endTime, and status
     *
     * @param startTime {@link Instant} status start time, not null
     * @param endTime status end time, not null
     * @param status {@link StatusState} for this status, not null
     * @return a Status object, not null
     * @throws NullPointerException if startTime, endTime, or status are null
     * @throws IllegalArgumentException if startTime is after endTime
     */
    public static Status create(Instant startTime, Instant endTime, StatusState status) {
      Objects.requireNonNull(startTime, "Status cannot have a null startTime");
      Objects.requireNonNull(endTime, "Status cannot have a null endTime");
      Objects.requireNonNull(status, "Status cannot have a null status");
      if (startTime.isAfter(endTime)) {
        throw new IllegalArgumentException("Status cannot have endTime before startTime");
      }

      return new Status(startTime, endTime, status);
    }

    /**
     * Obtains a new Status with the same startTime and status as this object but with an updated
     * endTime.  Does not affect the current object.
     *
     * @param updatedEndTime endTime used in the new Status object
     * @return a new Status
     */
    private Status updateEndTime(final Instant updatedEndTime) {
      return new Status(startTime, updatedEndTime, status);
    }

    /**
     * Obtains the {@link Instant} startTime for this status.  The status condition begins at this
     * time.
     *
     * @return time as an Instant, not null
     */
    public Instant getStartTime() {
      return startTime;
    }

    /**
     * Obtains the {@link Instant} endTime for this status.  The status condition ends at this time.
     *
     * @return time as an Instant, not null
     */
    public Instant getEndTime() {
      return endTime;
    }

    /**
     * Obtain the boolean status value for this status
     *
     * @return value as a boolean, not null
     */
    public StatusState getStatus() {
      return status;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Status)) {
        return false;
      }

      Status status1 = (Status) o;

      return startTime.equals(status1.startTime) && endTime.equals(status1.endTime)
          && status == status1.status;
    }

    @Override
    public int hashCode() {
      int result = startTime.hashCode();
      result = 31 * result + endTime.hashCode();
      result = 31 * result + status.hashCode();
      return result;
    }

    @Override
    public String toString() {
      return "Status{" +
          "startTime=" + startTime +
          ", endTime=" + endTime +
          ", status=" + status +
          '}';
    }
  }

  /**
   * Constructs a {@link WaveformQcChannelSohStatus} from the provided values.  All of the values
   * are assumed non-null and consistent since this constructor is invoked by the {@link Builder}
   *
   * @param processingChannelId UUID to a ProcessingChannel, not null
   * @param qcMaskType a QcMaskType, not null
   * @param channelSohSubtype a ChannelSohSubtype, not null
   * @param statusChanges list of all Status changes, contains at least one entry and no repeats
   */
  private WaveformQcChannelSohStatus(UUID processingChannelId, QcMaskType qcMaskType,
      ChannelSohSubtype channelSohSubtype, List<Status> statusChanges) {

    this.processingChannelId = processingChannelId;
    this.qcMaskType = qcMaskType;
    this.channelSohSubtype = channelSohSubtype;
    this.statusChanges = statusChanges;
  }

  /**
   * Obtain a {@link WaveformQcChannelSohStatus} from the {@link UUID} processingChannelId,
   * {@link QcMaskType} qcMaskType, {@link ChannelSohSubtype} channelSohSubtype, and list of {@link
   * Status}es.
   *
   * This is a recreation factory which assumes the provided statuses satisfy the contract
   * provided by WaveformQcChannelSohStatus, namely:
   * 1. At least one status entry
   * 2. No adjacent status entries with the same {@link StatusState}
   * 3. Statuses ordered by time and non-overlapping
   *
   * @param processingChannelId UUID to a ProcessingChannel, not null
   * @param qcMaskType a QcMaskType, not null
   * @param channelSohSubtype a ChannelSohSubtype, not null
   * @param statuses status changes, contains at least one entry and no repeats
   * @return WaveformQcChannelSohStatus, not null
   * @throws NullPointerException if any parameters are null
   * @throws IllegalArgumentException if statuses break the WaveformQcChannelSohStatus contract
   */
  public static WaveformQcChannelSohStatus from(UUID processingChannelId,
      QcMaskType qcMaskType, ChannelSohSubtype channelSohSubtype, List<Status> statuses) {

    Objects.requireNonNull(processingChannelId,
        "WaveformQcChannelSohStatus cannot have a null processingChannelId");
    Objects.requireNonNull(qcMaskType, "WaveformQcChannelSohStatus cannot have a null qcMaskType");
    Objects.requireNonNull(channelSohSubtype,
        "WaveformQcChannelSohStatus cannot have a null channelSohSubtype");
    Objects.requireNonNull(statuses, "WaveformQcChannelSohStatus cannot have null statuses");

    // Statuses must not be empty
    if (statuses.isEmpty()) {
      throw new IllegalArgumentException("WaveformQcChannelSohStatus cannot have empty statuses");
    }

    // Status values must be ordered by start time
    final IntPredicate ordered = i -> statuses.get(i).getStartTime()
        .isBefore(statuses.get(i + 1).getStartTime());
    validateStatuses(statuses, ordered,
        "WaveformQcChannelSohStatus cannot have out of order statuses");

    // Status values cannot overlap in time
    final BiPredicate<Instant, Instant> beforeOrEqual = (a, b) -> a.isBefore(b) || a.equals(b);
    final IntPredicate subsequent = i -> beforeOrEqual
        .test(statuses.get(i).getEndTime(), statuses.get(i + 1).getStartTime());
    validateStatuses(statuses, subsequent,
        "WaveformQcChannelSohStatus cannot have overlapping statuses");

    // Adjacent status values must have different state
    final IntPredicate differentStatus = i -> !statuses.get(i).getStatus()
        .equals(statuses.get(i + 1).getStatus());
    validateStatuses(statuses, differentStatus,
        "WaveformQcChannelSohStatus cannot have adjacent equal StatusStates");

    return new WaveformQcChannelSohStatus(processingChannelId, qcMaskType, channelSohSubtype,
        statuses);
  }

  /**
   * Validates all of the statuses satisfy the provided predicate.  This operation provides indexes
   * to entries in the statuses list to the IntPredicate.
   *
   * @param statuses {@link Status} to validate
   * @param test validation {@link IntPredicate} applied to each status
   * @param message exception message used when statuses fail the predicate
   * @throws IllegalArgumentException if statuses fail the predicate
   */
  private static void validateStatuses(List<Status> statuses, IntPredicate test, String message) {
    if (!IntStream.range(0, statuses.size() - 1).allMatch(test)) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Obtain an {@link UUID} to this status object's {@link Channel}
   *
   * @return UUID to a ProcessingChannel, not null
   */
  public UUID getProcessingChannelId() {
    return processingChannelId;
  }

  /**
   * Obtain the type of waveform quality problem tracked by this status as a {@link QcMaskType}
   *
   * @return QcMaskType for this status, not null
   */
  public QcMaskType getQcMaskType() {
    return qcMaskType;
  }

  /**
   * Obtain the {@link ChannelSohSubtype} of the waveform quality problem tracked by this status.
   * This corresponds to a value that could be returned by a {@link QcMaskVersion#getRationale()}
   *
   * @return ChannelSohSubtype for this status, not null
   */
  public ChannelSohSubtype getChannelSohSubtype() {
    return channelSohSubtype;
  }

  /**
   * Obtain a stream of all of the status changes ({@link Instant} time and boolean value).  Always
   * contains at least one {@link Status}
   *
   * @return stream of Status, not null
   */
  public Stream<Status> getStatusChanges() {
    return statusChanges.stream();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof WaveformQcChannelSohStatus)) {
      return false;
    }

    WaveformQcChannelSohStatus that = (WaveformQcChannelSohStatus) o;

    return processingChannelId.equals(that.processingChannelId) && qcMaskType == that.qcMaskType
        && channelSohSubtype == that.channelSohSubtype && statusChanges.equals(that.statusChanges);
  }

  @Override
  public int hashCode() {
    int result = processingChannelId.hashCode();
    result = 31 * result + qcMaskType.hashCode();
    result = 31 * result + channelSohSubtype.hashCode();
    result = 31 * result + statusChanges.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "WaveformQcChannelSohStatus{" +
        "processingChannelId=" + processingChannelId +
        ", qcMaskType=" + qcMaskType +
        ", channelSohSubtype=" + channelSohSubtype +
        ", statusChanges=" + statusChanges +
        '}';
  }

  /**
   * Get a {@link Builder} to construct a {@link WaveformQcChannelSohStatus} object.  Requires
   * parameters used to set the {@link UUID} to a {@link Channel},
   * the {@link QcMaskType}, the {@link ChannelSohSubtype},the initial {@link StatusState}, and
   * {@link Integer} missing Status adjacentThreshold.
   *
   * adjacentThreshold is inclusive.  If one status value ends exactly adjacentThreshold before the
   * next status begins then the two status values are considered adjacent.  If the second status is
   * just beyond adjacentThreshold then it is assumed a status value is missing.
   *
   * @param processingChannelId UUID to a ProcessingChannel, not null
   * @param qcMaskType a QcMaskType, not null
   * @param channelSohSubtype a ChannelSohSubtype, not null
   * @param firstStatusStartTime an Instant startTime for the initial status, not null
   * @param firstStatusEndTime an Instant endTime for the initial status, not null
   * @param firstStatus boolean initial status
   * @param adjacentThreshold maximum duration between status values not resulting in missing
   * status
   * @return Builder to construct a new WaveformQcChannelSohStatus, not null
   * @throws NullPointerException if processingChannelId, qcMaskType, channelSohSubtype, or
   * initialTime are null
   */
  public static Builder builder(
      UUID processingChannelId, QcMaskType qcMaskType, ChannelSohSubtype channelSohSubtype,
      Instant firstStatusStartTime, Instant firstStatusEndTime, boolean firstStatus,
      Duration adjacentThreshold) {

    requireNonNull(processingChannelId,
        "WaveformQcChannelSohStatus cannot have a null ProcessingChannelId");
    requireNonNull(qcMaskType, "WaveformQcChannelSohStatus cannot have a null QcMaskType");
    requireNonNull(channelSohSubtype,
        "WaveformQcChannelSohStatus cannot have a null ChannelSohSubtype");
    requireNonNull(firstStatusStartTime,
        "WaveformQcChannelSohStatus cannot have a null initial startTime");
    requireNonNull(firstStatusEndTime,
        "WaveformQcChannelSohStatus cannot have a null initial endTime");
    requireNonNull(adjacentThreshold,
        "WaveformQcChannelSohStatus cannot have a null adjacentThreshold");

    validateTypeConsistency(qcMaskType, channelSohSubtype);

    return new Builder(processingChannelId, qcMaskType,
        channelSohSubtype, firstStatusStartTime, firstStatusEndTime, firstStatus,
        adjacentThreshold);
  }

  /**
   * Get a {@link Builder} to construct a {@link WaveformQcChannelSohStatus} object.  Requires
   * parameters used to set the {@link UUID} to a {@link Channel},
   * the {@link AcquiredChannelSohType}, the initial {@link Status}, and {@link Duration}
   * adjacentThreshold for missing status
   *
   * adjacentThreshold is inclusive.  If one status value ends exactly adjacentThreshold before the
   * next status begins then the two status values are considered adjacent.  If the second status is
   * just beyond adjacentThreshold then it is assumed a status value is missing.
   *
   * @param processingChannelId UUID to a ProcessingChannel, not null
   * @param acquiredChannelSohType an AcquiredChannelSohType, not null
   * @param firstStatusStartTime an Instant time for the initial status, not null
   * @param firstStatusEndTime an Instant time for the initial status, not null
   * @param firstStatus boolean initial status value
   * @param adjacentThreshold maximum duration between status values not resulting in missing
   * status
   * @return Builder to construct a new WaveformQcChannelSohStatus, not null
   * @throws NullPointerException if processingChannelId, acquiredChannelSohType, or initialTime are
   * null
   */
  public static Builder builder(
      UUID processingChannelId, AcquiredChannelSohType acquiredChannelSohType,
      Instant firstStatusStartTime, Instant firstStatusEndTime, boolean firstStatus,
      Duration adjacentThreshold) {

    requireNonNull(acquiredChannelSohType,
        "WaveformQcChannelSohStatus cannot have a null AcquiredChannelSohType");

    return builder(processingChannelId, correspondingQcMaskType(acquiredChannelSohType),
        correspondingChannelSohSubtype(acquiredChannelSohType), firstStatusStartTime,
        firstStatusEndTime, firstStatus, adjacentThreshold);
  }

  /**
   * Obtain the {@link QcMaskType} corresponding to a {@link AcquiredChannelSohType}
   *
   * @param acquiredChannelSohType get the QcMaskType for this AcquiredChannelSohType
   * @return an QcMaskType, not null
   * @throws IllegalArgumentException if the AcquiredChannelSohType does not correspond to any
   * QcMaskType
   */
  public static QcMaskType correspondingQcMaskType(AcquiredChannelSohType acquiredChannelSohType) {

    requireNonNull(acquiredChannelSohType,
        "WaveformQcChannelSohStatus.correspondingQcMaskType cannot convert a null AcquiredChannelSohType");

    switch (acquiredChannelSohType) {
      case DEAD_SENSOR_CHANNEL:
      case ZEROED_DATA:
      case CLIPPED:
        return QcMaskType.SENSOR_PROBLEM;

      case MAIN_POWER_FAILURE:
      case BACKUP_POWER_UNSTABLE:
        return QcMaskType.STATION_PROBLEM;

      case CALIBRATION_UNDERWAY:
      case DIGITIZER_ANALOG_INPUT_SHORTED:
      case DIGITIZER_CALIBRATION_LOOP_BACK:
        return QcMaskType.CALIBRATION;

      case EQUIPMENT_HOUSING_OPEN:
      case DIGITIZING_EQUIPMENT_OPEN:
      case VAULT_DOOR_OPENED:
      case AUTHENTICATION_SEAL_BROKEN:
      case EQUIPMENT_MOVED:
        return QcMaskType.STATION_SECURITY;

      case CLOCK_DIFFERENTIAL_TOO_LARGE:
      case GPS_RECEIVER_OFF:
      case GPS_RECEIVER_UNLOCKED:
      case CLOCK_DIFFERENTIAL_IN_MICROSECONDS_OVER_THRESHOLD:
      case DATA_TIME_MINUS_TIME_LAST_GPS_SYNCHRONIZATION_OVER_THRESHOLD:
        return QcMaskType.TIMING;

      default:
        throw new IllegalArgumentException(
            acquiredChannelSohType + " is an unknown literal from AcquiredChannelSohType");
    }
  }

  /**
   * Obtain the {@link ChannelSohSubtype} corresponding to a {@link AcquiredChannelSohType}
   *
   * @param acquiredChannelSohType get the ChannelSohSubtype for this AcquiredChannelSohType
   * @return an ChannelSohSubtype, not null
   * @throws IllegalArgumentException if the AcquiredChannelSohType does not correspond to any
   * ChannelSohSubtype
   */
  public static ChannelSohSubtype correspondingChannelSohSubtype(
      AcquiredChannelSohType acquiredChannelSohType) {

    requireNonNull(acquiredChannelSohType,
        "WaveformQcChannelSohStatus.correspondingChannelSohSubtype cannot convert a null AcquiredChannelSohType");

    switch (acquiredChannelSohType) {
      case DEAD_SENSOR_CHANNEL:
        return ChannelSohSubtype.DEAD_SENSOR_CHANNEL;
      case ZEROED_DATA:
        return ChannelSohSubtype.ZEROED_DATA;
      case CLIPPED:
        return ChannelSohSubtype.CLIPPED;

      case MAIN_POWER_FAILURE:
        return ChannelSohSubtype.MAIN_POWER_FAILURE;
      case BACKUP_POWER_UNSTABLE:
        return ChannelSohSubtype.BACKUP_POWER_UNSTABLE;

      case CALIBRATION_UNDERWAY:
        return ChannelSohSubtype.CALIBRATION_UNDERWAY;
      case DIGITIZER_ANALOG_INPUT_SHORTED:
        return ChannelSohSubtype.DIGITIZER_ANALOG_INPUT_SHORTED;
      case DIGITIZER_CALIBRATION_LOOP_BACK:
        return ChannelSohSubtype.DIGITIZER_CALIBRATION_LOOP_BACK;

      case EQUIPMENT_HOUSING_OPEN:
        return ChannelSohSubtype.EQUIPMENT_HOUSING_OPEN;
      case DIGITIZING_EQUIPMENT_OPEN:
        return ChannelSohSubtype.DIGITIZING_EQUIPMENT_OPEN;
      case VAULT_DOOR_OPENED:
        return ChannelSohSubtype.VAULT_DOOR_OPENED;
      case AUTHENTICATION_SEAL_BROKEN:
        return ChannelSohSubtype.AUTHENTICATION_SEAL_BROKEN;
      case EQUIPMENT_MOVED:
        return ChannelSohSubtype.EQUIPMENT_MOVED;

      case CLOCK_DIFFERENTIAL_TOO_LARGE:
        return ChannelSohSubtype.CLOCK_DIFFERENTIAL_TOO_LARGE;
      case GPS_RECEIVER_OFF:
        return ChannelSohSubtype.GPS_RECEIVER_OFF;
      case GPS_RECEIVER_UNLOCKED:
        return ChannelSohSubtype.GPS_RECEIVER_UNLOCKED;
      case CLOCK_DIFFERENTIAL_IN_MICROSECONDS_OVER_THRESHOLD:
        return ChannelSohSubtype.CLOCK_DIFFERENTIAL_OVER_THRESHOLD;
      case DATA_TIME_MINUS_TIME_LAST_GPS_SYNCHRONIZATION_OVER_THRESHOLD:
        return ChannelSohSubtype.DATA_TIME_GPS_SYNCHRONIZATION_TIME_DELTA_OVER_THRESHOLD;

      default:
        throw new IllegalArgumentException(
            acquiredChannelSohType + " is an unknown literal from AcquiredChannelSohType");
    }
  }

  /**
   * Checks the provided {@link QcMaskType} and {@link ChannelSohSubtype} combination form a valid
   * type of Channel Soh issue.  Throws an IllegalArgumentException if the combination is invalid.
   *
   * @param qcMaskType QcMaskType to validate with the ChannelSohSubtype, not null
   * @param channelSohSubtype ChannelSohSubtype to validate with the QcMaskType, not null
   * @throws IllegalArgumentException if the QcMaskType and ChannelSohSubtype combination is not a
   * valid channel soh data quality issue.
   */
  private static void validateTypeConsistency(QcMaskType qcMaskType,
      ChannelSohSubtype channelSohSubtype) {

    switch (qcMaskType) {
      case SENSOR_PROBLEM:
        if (channelSohSubtype != ChannelSohSubtype.DEAD_SENSOR_CHANNEL &&
            channelSohSubtype != ChannelSohSubtype.ZEROED_DATA &&
            channelSohSubtype != ChannelSohSubtype.CLIPPED) {
          throwCombinationIllegalException(qcMaskType, channelSohSubtype);
        }
        break;

      case STATION_PROBLEM:
        if (channelSohSubtype != ChannelSohSubtype.MAIN_POWER_FAILURE &&
            channelSohSubtype != ChannelSohSubtype.BACKUP_POWER_UNSTABLE) {
          throwCombinationIllegalException(qcMaskType, channelSohSubtype);
        }
        break;

      case CALIBRATION:
        if (channelSohSubtype != ChannelSohSubtype.CALIBRATION_UNDERWAY &&
            channelSohSubtype != ChannelSohSubtype.DIGITIZER_ANALOG_INPUT_SHORTED &&
            channelSohSubtype != ChannelSohSubtype.DIGITIZER_CALIBRATION_LOOP_BACK) {
          throwCombinationIllegalException(qcMaskType, channelSohSubtype);
        }
        break;

      case STATION_SECURITY:
        if (channelSohSubtype != ChannelSohSubtype.EQUIPMENT_HOUSING_OPEN &&
            channelSohSubtype != ChannelSohSubtype.DIGITIZING_EQUIPMENT_OPEN &&
            channelSohSubtype != ChannelSohSubtype.VAULT_DOOR_OPENED &&
            channelSohSubtype != ChannelSohSubtype.AUTHENTICATION_SEAL_BROKEN &&
            channelSohSubtype != ChannelSohSubtype.EQUIPMENT_MOVED) {
          throwCombinationIllegalException(qcMaskType, channelSohSubtype);
        }
        break;

      case TIMING:
        if (channelSohSubtype != ChannelSohSubtype.CLOCK_DIFFERENTIAL_TOO_LARGE &&
            channelSohSubtype != ChannelSohSubtype.GPS_RECEIVER_OFF &&
            channelSohSubtype != ChannelSohSubtype.GPS_RECEIVER_UNLOCKED &&
            channelSohSubtype
                != ChannelSohSubtype.DATA_TIME_GPS_SYNCHRONIZATION_TIME_DELTA_OVER_THRESHOLD &&
            channelSohSubtype != ChannelSohSubtype.CLOCK_DIFFERENTIAL_OVER_THRESHOLD) {
          throwCombinationIllegalException(qcMaskType, channelSohSubtype);
        }
        break;

      default:
        throw new IllegalArgumentException(
            qcMaskType + " is not a valid QcMaskType for a channel soh mask");
    }
  }

  /**
   * Throws an IllegalArgumentException using the parameters for the exception message.
   *
   * @param type {@link QcMaskType} used in the exception message, not null
   * @param subtype {@link ChannelSohSubtype} used in the exception message, not null
   * @throws IllegalArgumentException with the input parameters as part of the message
   */
  private static void throwCombinationIllegalException(QcMaskType type, ChannelSohSubtype subtype) {
    throw new IllegalArgumentException("QcMaskType " + type + " and ChannelSohSubtype " + subtype
        + " is not a valid combination of QcMaskType and ChannelSohSubtype");
  }

  /**
   * Builder class for constructing {@link WaveformQcChannelSohStatus} objects.
   */
  public static class Builder {

    private final UUID processingChannelId;
    private final QcMaskType qcMaskType;
    private final ChannelSohSubtype channelSohSubtype;
    private final TreeMap<Instant, Status> statuses;
    private final Duration adjacentThreshold;

    /**
     * Constructs a {@link Builder} with the values required to create any {@link
     * WaveformQcChannelSohStatus}.  Values assumed to be non-null and valid since this operation is
     * only invoked by the {@link WaveformQcChannelSohStatus#builder(UUID, QcMaskType,
     * ChannelSohSubtype, Instant, Instant, boolean, Duration)} which performs these checks.
     * AcquiredChannelSohType, Instant, StatusState)} which performs these checks.
     *
     * adjacentThreshold is inclusive.  If one status value ends exactly adjacentThreshold before
     * the next status begins then the two status values are considered adjacent.  If the second
     * status is just beyond adjacentThreshold then it is assumed a status value is missing.
     *
     * @param processingChannelId UUID to a ProcessingChannel, not null
     * @param qcMaskType a QcMaskType, not null
     * @param channelSohSubtype a ChannelSohSubtype, not null
     * @param startTime an Instant startTime for the initial status, not null
     * @param endTime an Instant endTime for the initial status, not null
     * @param status boolean initial status
     * @param adjacentThreshold maximum duration between status values not resulting in missing
     * status
     */
    private Builder(UUID processingChannelId, QcMaskType qcMaskType,
        ChannelSohSubtype channelSohSubtype, Instant startTime, Instant endTime,
        boolean status, Duration adjacentThreshold) {

      this.processingChannelId = processingChannelId;
      this.qcMaskType = qcMaskType;
      this.channelSohSubtype = channelSohSubtype;
      this.statuses = new TreeMap<>();
      this.statuses.put(startTime, new Status(startTime, endTime, StatusState.of(status)));
      this.adjacentThreshold = adjacentThreshold;
    }

    /**
     * Construct the {@link WaveformQcChannelSohStatus} based on the information set in this
     * builder
     *
     * @return a constructed WaveformQcChannelSohStatus, not null
     * @throws IllegalArgumentException if statusEndTime is equal or before any status change time
     */
    public WaveformQcChannelSohStatus build() {
      final List<Status> statusChanges = filterRepeatedStatusValuesAndAddMissingStatuses(statuses,
          adjacentThreshold);

      return new WaveformQcChannelSohStatus(this.processingChannelId, this.qcMaskType,
          this.channelSohSubtype, statusChanges);
    }

    /**
     * Adds a new {@link Status} to this builder.  Only adds the status if it is a change from the
     * previous status value.
     *
     * @param startTime status startTime
     * @param endTime status endTime
     * @param status status value
     * @throws NullPointerException if the {@link Instant} time is null
     * @throws IllegalArgumentException if the time has already been added with a different status
     */
    public void addStatusChange(Instant startTime, Instant endTime, boolean status) {
      requireNonNull(startTime,
          "WaveformQcChannelSohStatus cannot have a null status change startTime");
      requireNonNull(endTime,
          "WaveformQcChannelSohStatus cannot have a null status change endTime");

      final Status newStatus = new Status(startTime, endTime, StatusState.of(status));
      final Status curStatusForStartTime = this.statuses.get(startTime);
      if (null != curStatusForStartTime && !newStatus.equals(curStatusForStartTime)) {
        throw new IllegalArgumentException(
            "WaveformQcChannelSohStatus cannot have two Status for the same time");
      } else {
        this.statuses.put(startTime, newStatus);
      }
    }

    /**
     * Filters the input {@link Status} objects to remove repeated adjacent statuses with the same
     * status value. Also adding Statuses with StatusState set to missing, when data is missing.
     * Does not modify the input list.  The returned list has at least one element.
     *
     * @param inputs collection of Status to filter
     * @param adjacentThreshold threshold beyond which two status values are no longer adjacent
     * @return list of at least 1 Status with no adjacent entries containing repeated stats values,
     * not null
     */
    private static List<Status> filterRepeatedStatusValuesAndAddMissingStatuses(
        Map<Instant, Status> inputs, Duration adjacentThreshold) {

      // Always include the initial status
      Iterator<Entry<Instant, Status>> inputIterator = inputs.entrySet().iterator();
      List<Status> filtered = new ArrayList<>();

      Status curStatus = statusFromEntry(inputIterator.next());

      // Add additional Status whenever the value changes or if a status is missing
      while (inputIterator.hasNext()) {
        final Entry<Instant, Status> thisStatus = inputIterator.next();

        // Missing an expected acquired status entry.  First add an entry to the filtered list
        // ending the previous entry, add a missing status entry, and then start the next status.
        if (thisStatus.getKey().isAfter(curStatus.getEndTime().plus(adjacentThreshold))) {
          filtered.add(curStatus);
          filtered
              .add(new Status(curStatus.getEndTime(), thisStatus.getKey(), StatusState.MISSING));
          curStatus = statusFromEntry(thisStatus);
        }

        // Status changed from the previous state.  Add an entry ending the previous status and
        // then start the next status.
        else if (thisStatus.getValue().getStatus() != curStatus.getStatus()) {
          filtered.add(curStatus);
          curStatus = statusFromEntry(thisStatus);
        }

        // No change to status state, extend the current status
        else {
          curStatus = curStatus.updateEndTime(thisStatus.getValue().getEndTime());
        }
      }

      // Add the final status entry
      filtered.add(curStatus);

      return filtered;
    }

    /**
     * Obtain a {@link Status} from the entry
     *
     * @param entry {@link Instant} and boolean tuple, not null
     * @return Status representation of the input entry, not null
     */
    private static Status statusFromEntry(Entry<Instant, Status> entry) {
      final Status status = entry.getValue();
      return new Status(status.getStartTime(), status.getEndTime(), status.getStatus());
    }
  }
}

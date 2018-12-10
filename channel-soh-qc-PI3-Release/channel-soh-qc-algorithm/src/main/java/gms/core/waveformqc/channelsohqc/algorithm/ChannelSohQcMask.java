package gms.core.waveformqc.channelsohqc.algorithm;

import gms.core.waveformqc.waveformqccontrol.objects.ChannelSohSubtype;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import java.time.Instant;
import java.util.Objects;

/**
 * Data object specific to {@link WaveformChannelSohStatusInterpreter}. It outputs the results
 * of the algorithm, to the plugin where it is converted into QcMasks
 */
public class ChannelSohQcMask {

  private final QcMaskType qcMaskType;
  private final ChannelSohSubtype channelSohSubtype;
  private final Instant startTime;
  private final Instant endTime;

  private ChannelSohQcMask(
      QcMaskType qcMaskType,
      ChannelSohSubtype channelSohSubtype, Instant startTime, Instant endTime) {
    this.qcMaskType = qcMaskType;
    this.channelSohSubtype = channelSohSubtype;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  /**
   * Creates a ChannelSohQcMask insuring all the parameters are not null
   * and that the StartTime is before the EndTime
   *
   * @return ChannelSohQcMask
   */

  public static ChannelSohQcMask create(QcMaskType qcMaskType,
      ChannelSohSubtype channelSohSubtype, Instant startTime, Instant endTime) {

    Objects.requireNonNull(qcMaskType,
        "Error creating ChannelSohQcMask: qcMaskType cannot be null");
    Objects.requireNonNull(channelSohSubtype,
        "Error creating ChannelSohQcMask: channelSohSubtype cannot be null");
    Objects.requireNonNull(startTime,
        "Error creating ChannelSohQcMask: startTime cannot be null");
    Objects.requireNonNull(endTime,
        "Error creating ChannelSohQcMask: endTime cannot be null");

    if (!startTime.isBefore(endTime)) {
      throw new IllegalArgumentException(
          "Error creating ChannelSohQcMask: startTime must be before endTime");
    }

    return new ChannelSohQcMask(qcMaskType, channelSohSubtype, startTime, endTime);
  }

  /**
   * Obtain the {@link QcMaskType} of this ChannelSohQcMask
   *
   * @return QcMaskType for this ChannelSohQcMask
   */
  public QcMaskType getQcMaskType() {
    return qcMaskType;
  }

  /**
   * Obtain the {@link ChannelSohSubtype}  of this ChannelSohQcMask
   *
   * @return ChannelSohSubtype for this ChannelSohQcMask
   */
  public ChannelSohSubtype getChannelSohSubtype() {
    return channelSohSubtype;
  }

  /**
   * Obtain the StartTime of this ChannelSohQcMask
   *
   * @return Instant StartTime for this ChannelSohQcMask
   */
  public Instant getStartTime() {
    return startTime;
  }

  /**
   * Obtain the EndTime of this ChannelSohQcMask
   *
   * @return Instant EndTime for this ChannelSohQcMask
   */
  public Instant getEndTime() {
    return endTime;
  }
}

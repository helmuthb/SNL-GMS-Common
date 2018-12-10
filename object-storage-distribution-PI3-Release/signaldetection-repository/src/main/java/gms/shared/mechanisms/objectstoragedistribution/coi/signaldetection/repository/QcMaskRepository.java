package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface QcMaskRepository {

  /**
   * Store for the first time the provided {@link QcMask} and all of its {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion}.
   *
   * @param qcMask store this QcMask and its versions, not null
   */
  void store(QcMask qcMask);

  /**
   * Retrieves the current version of all QcMasks associated with the provided {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel}
   * Id and are valid between the provided time range.
   *
   * @param processingChannelId Processing Channel Id
   * @param startTime Start of the time range, inclusive
   * @param endTime End of the time range, inclusive
   * @return QcMasks created for the Processing Channel
   */
  List<QcMask> findCurrentByProcessingChannelIdAndTimeRange(UUID processingChannelId,
      Instant startTime, Instant endTime);
}
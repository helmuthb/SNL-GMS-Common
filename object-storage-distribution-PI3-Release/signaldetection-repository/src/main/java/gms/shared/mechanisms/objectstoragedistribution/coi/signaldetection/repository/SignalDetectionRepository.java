package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SignalDetectionRepository {

  /**
   * Store for the first time the provided {@link SignalDetection} and all of its {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis}
   * and {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement}
   * objects.
   *
   * @param signalDetection store this SignalDetection and its supporting hypotheses, not null
   */
  void store(SignalDetection signalDetection);

  /**
   * Retrieves all of the {@link SignalDetection}s stored in this {@link
   * SignalDetectionRepository}
   *
   * @return collection of SignalDetections, not null
   */
  Collection<SignalDetection> retrieveAll();

  /**
   * Retrieves the {@link SignalDetection} with the supplied id, or null if not found.
   * @param id The UUID of the requested SignalDetection.
   * @return  SignalDetection or null.
   */
  Optional<SignalDetection> findSignalDetectionById(UUID id);
}

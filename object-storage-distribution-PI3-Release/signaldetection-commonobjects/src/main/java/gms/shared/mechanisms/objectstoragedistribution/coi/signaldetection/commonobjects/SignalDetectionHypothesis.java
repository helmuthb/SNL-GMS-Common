package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * See {@link SignalDetection} and {@link FeatureMeasurement} for detailed description of
 * SignalDetectionHypothesis.
 */
public class SignalDetectionHypothesis {

  private final UUID id;
  private final UUID parentSignalDetectionId;
  private final String phase;
  private final boolean rejected;
  private final List<FeatureMeasurement> featureMeasurements;
  private final UUID creationInfoId;

  /**
   * Obtains an instance from SignalDetectionHypothesis.
   *
   * @param id The {@link UUID} id assigned to the new SignalDetectionHypothesis.
   * @param parentSignalDetectionId This hypothesis' parent SignalDetection object.
   * @param phase The detection's phase
   * @param rejected Determines if this is a valid hypothesis
   * @param featureMeasurements The measurements used to make this hypothesis
   * @param creationInfoId An identifier representing this object's provenance.
   */
  private SignalDetectionHypothesis(UUID id, UUID parentSignalDetectionId, String phase,
      boolean rejected, List<FeatureMeasurement> featureMeasurements, UUID creationInfoId) {

    this.id = id;
    this.parentSignalDetectionId = parentSignalDetectionId;
    this.phase = phase;
    this.rejected = rejected;
    this.creationInfoId = creationInfoId;

    // Making a copy of the feature measurement array so it is immutable.
    this.featureMeasurements = featureMeasurements.stream().collect(Collectors.toList());
  }

  /**
   * Recreation factory method (sets the SignalDetection entity identity). Handles parameter validation. Used
   * for deserialization and recreating from persistence.
   *
   * @param id The {@link UUID} id assigned to the new SignalDetection.
   * @param parentSignalDetectionId This hypothesis' parent SignalDetection object.
   * @param phase The detection's phase
   * @param isRejected Determines if this is a valid hypothesis
   * @param featureMeasurements The measurements used to make this hypothesis
   * @param creationInfoId An identifier representing this object's provenance.
   * @throws IllegalArgumentException if any of the parameters are null or featureMeasurements does not contain an arrival time.
   */
  public static SignalDetectionHypothesis from(UUID id, UUID parentSignalDetectionId,
      String phase, boolean isRejected, List<FeatureMeasurement> featureMeasurements, UUID creationInfoId) {

    Objects.requireNonNull(id, "Cannot create SignalDetectionHypothesis from a null id");
    Objects.requireNonNull(parentSignalDetectionId, "Cannot create SignalDetectionHypothesis from a null parentSignalDetectionId");
    Objects.requireNonNull(phase, "Cannot create SignalDetectionHypothesis from a null phase");
    Objects.requireNonNull(featureMeasurements, "Cannot create SignalDetectionHypothesis from null featureMeasurements");
    Objects.requireNonNull(creationInfoId, "SignalDetectionHypothesis's creation info id cannot be null");
    verifyFeatureMeasurmentsContainsArrivalTime(featureMeasurements);

    return new SignalDetectionHypothesis(id, parentSignalDetectionId, phase, isRejected, featureMeasurements, creationInfoId);
  }

  public UUID getId() {
    return id;
  }

  public UUID getParentSignalDetectionId() { return parentSignalDetectionId; }

  public String getPhase() {
    return phase;
  }

  public boolean isRejected() {
    return rejected;
  }

  public UUID getCreationInfoId() {
    return creationInfoId;
  }

  /**
   * Returns an unmodifiable list of feature measurements.
   * @return
   */
  public List<FeatureMeasurement> getFeatureMeasurements() {

    return Collections.unmodifiableList(featureMeasurements);
  }

  /**
   * Verifies the featureMeasurements contains an arrival time.
   * @param featureMeasurements The measurements used to make this hypothesis
   * @throws IllegalArgumentException if featureMeasurements does not contain an arrival time.
   */
  private static void verifyFeatureMeasurmentsContainsArrivalTime(
      List<FeatureMeasurement> featureMeasurements) throws IllegalArgumentException
  {
    if(1 != featureMeasurements.stream().map(FeatureMeasurement::getFeatureMeasurementType).filter(FeatureMeasurementType.ARRIVAL_TIME::equals).count()) {
      throw new IllegalArgumentException("Feature Measurements must contain an Arrival Time.");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SignalDetectionHypothesis that = (SignalDetectionHypothesis) o;

    if (rejected != that.rejected) {
      return false;
    }
    if (!id.equals(that.id)) {
      return false;
    }
    if (!parentSignalDetectionId.equals(that.parentSignalDetectionId)) {
      return false;
    }
    if (!phase.equals(that.phase)) {
      return false;
    }
    if (!featureMeasurements.equals(that.featureMeasurements)) {
      return false;
    }
    return creationInfoId.equals(that.creationInfoId);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + parentSignalDetectionId.hashCode();
    result = 31 * result + phase.hashCode();
    result = 31 * result + (rejected ? 1 : 0);
    result = 31 * result + featureMeasurements.hashCode();
    result = 31 * result + creationInfoId.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "SignalDetectionHypothesis{" +
        "id=" + id +
        ", parentSignalDetectionId=" + parentSignalDetectionId +
        ", phase='" + phase + '\'' +
        ", rejected=" + rejected +
        ", featureMeasurements=" + featureMeasurements +
        ", creationInfoId=" + creationInfoId +
        '}';
  }
}

package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * A Signal Detection Hypothesis typically will have many measurements associated with it, captured
 * with the Feature Measurement class. Feature Measurement has been made generic to accommodate any
 * new types of measurement that may be added in the future. Each Feature Measurement has a type
 * indicated with the feature measurement type attribute, a value, and a reference to the Channel
 * Segment on which it was calculated. As shown in the association above, each Signal Detection
 * Hypothesis is required to have at least an arrival time Feature Measurement. The additional
 * Feature Measurements are a “zero to many” relationship, because they are not required by the
 * system.
 */
public class FeatureMeasurement {

  private final UUID id;
  private final FeatureMeasurementType featureMeasurementType;
  private final double featureMeasurementValue;
  private final UUID creationInfoId;

  /**
   * Obtains an instance from FeatureMeasurement.
   *
   * @param id The {@link UUID} id assigned to the new FeatureMeasurement.
   * @param featureMeasurementType The measurement's type
   * @param featureMeasurementValue The measurement's value
   * @param creationInfoId An identifier representing this object's provenance.
   */
  private FeatureMeasurement(UUID id, FeatureMeasurementType featureMeasurementType,
      double featureMeasurementValue, UUID creationInfoId) {

    this.id = id;
    this.featureMeasurementType = featureMeasurementType;
    this.featureMeasurementValue = featureMeasurementValue;
    this.creationInfoId = creationInfoId;
  }

  /**
   * Recreation factory method (sets the FeatureMeasurement entity identity). Handles parameter validation. Used
   * for deserialization and recreating from persistence.
   *
   * @param id The {@link UUID} id assigned to the new FeatureMeasurement.
   * @param featureMeasurementType The measurement's type
   * @param featureMeasurementValue The measurement's value
   * @param creationInfoId An identifier representing this object's provenance.
   * @throws IllegalArgumentException if any of the parameters are null
   */
  public static FeatureMeasurement from(UUID id, FeatureMeasurementType featureMeasurementType,
      double featureMeasurementValue, UUID creationInfoId) {
    Objects.requireNonNull(id, "Cannot create FeatureMeasurement from a null id");
    Objects.requireNonNull(featureMeasurementType, "Cannot create FeatureMeasurement from a null featureMeasurementType");
    Objects.requireNonNull(creationInfoId, "FeatureMeasurement's creation info id cannot be null");

    return new FeatureMeasurement(id, featureMeasurementType, featureMeasurementValue, creationInfoId);
  }


  /**
   * Creation factory method for a new FeatureMeasurement.
   *
   * @param featureMeasurementType The measurement's type
   * @param featureMeasurementValue The measurement's value
   * @param creationInfoId An identifier representing this object's provenance.
   * @throws IllegalArgumentException if any of the parameters are null
   */
  public static FeatureMeasurement create(FeatureMeasurementType featureMeasurementType,
      double featureMeasurementValue, UUID creationInfoId) {

    return FeatureMeasurement.from(UUID.randomUUID(), featureMeasurementType, featureMeasurementValue, creationInfoId);
  }

  public UUID getId() {
    return id;
  }

  public FeatureMeasurementType getFeatureMeasurementType() {
    return featureMeasurementType;
  }

  public double getFeatureMeasurementValue() {
    return featureMeasurementValue;
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

    FeatureMeasurement that = (FeatureMeasurement) o;

    if (Double.compare(that.featureMeasurementValue, featureMeasurementValue) != 0) {
      return false;
    }
    if (!id.equals(that.id)) {
      return false;
    }
    if (featureMeasurementType != that.featureMeasurementType) {
      return false;
    }
    return creationInfoId.equals(that.creationInfoId);
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = id.hashCode();
    result = 31 * result + featureMeasurementType.hashCode();
    temp = Double.doubleToLongBits(featureMeasurementValue);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + creationInfoId.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "FeatureMeasurement{" +
        "id=" + id +
        ", featureMeasurementType=" + featureMeasurementType +
        ", featureMeasurementValue=" + featureMeasurementValue +
        ", creationInfoId=" + creationInfoId +
        '}';
  }
}

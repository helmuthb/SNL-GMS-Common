package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import java.util.UUID;

/**
 * DTO for {@link FeatureMeasurement}
 */
public interface FeatureMeasurementDto {

  @JsonCreator
  static FeatureMeasurement from(
      @JsonProperty("id") UUID id,
      @JsonProperty("featureMeasurementType") FeatureMeasurementType featureMeasurementType,
      @JsonProperty("featureMeasurementValue") double featureMeasurementValue,
      @JsonProperty("creationInfoId") UUID creationInfoId) {
    return FeatureMeasurement.from(id, featureMeasurementType, featureMeasurementValue, creationInfoId);
  }
}

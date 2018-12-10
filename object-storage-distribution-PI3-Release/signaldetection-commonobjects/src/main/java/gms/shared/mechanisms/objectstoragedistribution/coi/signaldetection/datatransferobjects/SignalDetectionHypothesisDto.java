package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link SignalDetectionHypothesis}
 */
public interface SignalDetectionHypothesisDto {

  @JsonCreator
  static SignalDetectionHypothesis from(
      @JsonProperty("id") UUID id,
      @JsonProperty("parentSignalDetectionId") UUID parentSignalDetectionId,
      @JsonProperty("phase") String phase,
      @JsonProperty("isRejected") boolean isRejected,
      @JsonProperty("featureMeasurements") List<FeatureMeasurement> featureMeasurements,
      @JsonProperty("creationInfoId") UUID creationInfoId){
    return SignalDetectionHypothesis.from(id, parentSignalDetectionId, phase, isRejected, featureMeasurements, creationInfoId);
  }
}

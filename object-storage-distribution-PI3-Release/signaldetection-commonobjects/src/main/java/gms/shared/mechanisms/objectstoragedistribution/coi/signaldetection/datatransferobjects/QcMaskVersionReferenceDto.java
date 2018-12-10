package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionReference;
import java.util.UUID;

/**
 * DTO for {@link gms.shared.mechanisms.objectstoragedistribution.coi.dataobject.stationprocessing.QcMaskVersionReference}
 * Added @JsonIgnoreProperties(ignoreUnknown=true) to ignore NO_PARENT
 *
 * Created by jrhipp on 9/7/17.
 */
public interface QcMaskVersionReferenceDto {

  @JsonCreator
  static QcMaskVersionReference from(
      @JsonProperty("qcMaskId") UUID qcMaskId,
      @JsonProperty("qcMaskVersionId") long qcMaskVersionId) {

    return QcMaskVersionReference.from(qcMaskId, qcMaskVersionId);
  }

}

package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.AnalystActionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingStepReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * DTO for the {@link InformationSource}
 */
public interface InformationSourceDto {

  @JsonCreator
  static InformationSource from(
      @JsonProperty("originatingOrganization") String originatingOrganization,
      @JsonProperty("informationTime") Instant informationTime,
      @JsonProperty("reference") String reference) {
    return InformationSource.from(originatingOrganization, informationTime, reference);
  }
}


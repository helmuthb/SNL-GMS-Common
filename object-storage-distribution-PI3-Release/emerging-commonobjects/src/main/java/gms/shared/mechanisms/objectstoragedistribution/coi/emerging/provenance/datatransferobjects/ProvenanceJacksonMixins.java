package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import java.util.Objects;

/**
 * Utility operations to configure a Jackson {@link ObjectMapper} to correctly serialize and
 * deserialize business objects from the Provenance domain.
 */
public class ProvenanceJacksonMixins {

  /**
   * Registers an {@link ObjectMapper} with mixins for Provenance domain objects
   *
   * @param objectMapper register mixins with this ObjectMapper, non null
   */
  public static void register(ObjectMapper objectMapper) {

    Objects.requireNonNull(objectMapper,
        "CommonJacksonMixins.register requires non-null objectMapper");

    objectMapper
        .addMixIn(CreationInfo.class, CreationInfoDto.class)
        .addMixIn(CreationInformation.class, CreationInformationDto.class)
        .addMixIn(SoftwareComponentInfo.class, SoftwareComponentInfoDto.class)
        .addMixIn(InformationSource.class, InformationSourceDto.class);
  }
}

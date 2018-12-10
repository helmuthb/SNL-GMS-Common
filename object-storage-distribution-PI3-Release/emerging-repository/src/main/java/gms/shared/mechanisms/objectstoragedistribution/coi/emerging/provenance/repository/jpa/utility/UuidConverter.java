package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.utility;

import java.util.UUID;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class UuidConverter implements AttributeConverter<UUID, UUID> {

  @Override
  public UUID convertToDatabaseColumn(UUID attribute) {
    return attribute;
  }

  @Override
  public UUID convertToEntityAttribute(UUID dbData) {
    return dbData;
  }

}
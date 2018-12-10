package gms.core.signalenhancement.waveformfiltering.objects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class FilterParameters {

  private final UUID processingChannelStepId;
  private final Map<RegistrationInfo, List<FilterDefinition>> infoListMap;

  public static FilterParameters create(UUID processingChannelId,
      Map<RegistrationInfo, List<FilterDefinition>> infoListMap) {
    Objects.requireNonNull(processingChannelId,
        "Error instantiating FilterParameters, processing channel ID cannot be null");
    Objects.requireNonNull(infoListMap,
        "Error instantiating FilterParameters, infoListMap cannot be null");
    return new FilterParameters(processingChannelId, infoListMap);
  }

  private FilterParameters(UUID processingChannelId,
      Map<RegistrationInfo, List<FilterDefinition>> infoListMap) {
    this.processingChannelStepId = processingChannelId;
    this.infoListMap = infoListMap;
  }

  public UUID getProcessingChannelId() {
    return processingChannelStepId;
  }

  public Map<RegistrationInfo, List<FilterDefinition>> getInfoListMap() {
    return infoListMap;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    FilterParameters that = (FilterParameters) o;

    if (!processingChannelStepId.equals(that.processingChannelStepId)) {
      return false;
    }
    return infoListMap.equals(that.infoListMap);
  }

  @Override
  public int hashCode() {
    int result = processingChannelStepId.hashCode();
    result = 31 * result + infoListMap.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "FilterParameters{" +
        "processingChannelId=" + processingChannelStepId +
        ", infoListMap=" + infoListMap +
        '}';
  }
}

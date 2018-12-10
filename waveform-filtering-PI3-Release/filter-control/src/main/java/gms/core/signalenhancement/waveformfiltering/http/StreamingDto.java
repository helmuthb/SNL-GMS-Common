package gms.core.signalenhancement.waveformfiltering.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.ParameterValidation;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Data Transfer Object for the request body used in streaming invocations of {@link
 * gms.core.signalenhancement.waveformfiltering.control.FilterControl} via {@link
 * FilterControlRouteHandler#streaming(ContentType, byte[], ContentType)}
 */
public class StreamingDto {

  private List<ChannelSegment> mapChannelSegmentKeys;
  private List<UUID> mapChannelIdValues;
  private FilterDefinition filterDefinition;
  private ProcessingContext processingContext;

  public StreamingDto() {
  }

  /**
   * Returns a Map<ChannelSegment, UUID> required by the ExecuteStreamingCommand to build itself
   * properly from a dto.
   *
   * @return The streaming command map.
   */
  @JsonIgnore
  public Map<ChannelSegment, UUID> getChannelSegmentToOutputChannelIdMap() {
    Objects.requireNonNull(mapChannelSegmentKeys,
        "ChannelSegment to output Channel Id map cannot be constructed with null mapChannelSegmentKeys");
    Objects.requireNonNull(mapChannelIdValues,
        "ChannelSegment to output Channel Id map cannot be constructed with null mapChannelIdValues");
    ParameterValidation
        .requireTrue((list1, list2) -> list1.size() == list2.size(), mapChannelSegmentKeys,
            mapChannelIdValues,
            "ChannelSegment to Channel Id map must have the same list length for keys and values");

    Map<ChannelSegment, UUID> channelSegmentToOutputChannelIdMap =
        new HashMap<>();
    for (int i = 0; i < mapChannelSegmentKeys.size(); ++i) {
      channelSegmentToOutputChannelIdMap.put(mapChannelSegmentKeys.get(i),
          mapChannelIdValues.get(i));
    }

    return channelSegmentToOutputChannelIdMap;
  }

  public void setMapChannelSegmentKeys(List<ChannelSegment> mapChannelSegmentKeys) {
    this.mapChannelSegmentKeys = mapChannelSegmentKeys;
  }

  public List<ChannelSegment> getMapChannelSegmentKeys() {
    return mapChannelSegmentKeys;
  }

  public void setMapChannelIdValues(List<UUID> mapChannelIdValues) {
    this.mapChannelIdValues = mapChannelIdValues;
  }

  public List<UUID> getMapChannelIdValues() {
    return mapChannelIdValues;
  }

  public FilterDefinition getFilterDefinition() {
    return filterDefinition;
  }

  public void setFilterDefinition(
      FilterDefinition filterDefinition) {
    this.filterDefinition = filterDefinition;
  }

  public ProcessingContext getProcessingContext() {
    return processingContext;
  }

  public void setProcessingContext(
      ProcessingContext processingContext) {
    this.processingContext = processingContext;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    StreamingDto that = (StreamingDto) o;
    return (mapChannelSegmentKeys != null ? mapChannelSegmentKeys.equals(that.mapChannelSegmentKeys)
        : that.mapChannelSegmentKeys == null) &&
        (mapChannelIdValues != null ? mapChannelIdValues.equals(that.mapChannelIdValues)
            : that.mapChannelIdValues == null) &&
        (filterDefinition != null ? filterDefinition
            .equals(that.filterDefinition) : that.filterDefinition == null) &&
        (processingContext != null ? processingContext.equals(that.processingContext)
            : that.processingContext == null);
  }

  @Override
  public int hashCode() {
    int result = mapChannelSegmentKeys != null ? mapChannelSegmentKeys.hashCode() : 0;
    result = 31 * result + (mapChannelSegmentKeys != null ? mapChannelSegmentKeys.hashCode() : 0);
    result = 31 * result + (filterDefinition != null ? filterDefinition.hashCode() : 0);
    result = 31 * result + (processingContext != null ? processingContext.hashCode() : 0);
    return result;
  }
}

package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;

import java.time.Instant;
import java.util.SortedSet;
import java.util.UUID;

/**
 * A Data Transfer Object (DTO) for the ChannelSegment class. Annotates some properties so that
 * Jackson knows how to deserialize them - part of the reason this is necessary is that the original
 * ChannelSegment class is immutable.
 * This class is a Jackson 'Mix-in annotations' class.
 */
public interface ChannelSegmentDto {

  @JsonCreator
  static ChannelSegment from(
      @JsonProperty("id") UUID id,
      @JsonProperty("processingChannelId") UUID processingChannelId,
      @JsonProperty("name") String name,
      @JsonProperty("segmentType") ChannelSegment.ChannelSegmentType type,
      @JsonProperty("startTime") Instant start,
      @JsonProperty("endTime") Instant end,
      @JsonProperty("waveforms") SortedSet<Waveform> wfs,
      @JsonProperty("creationInfo") CreationInfo creationInfo) {

    return ChannelSegment.from(id, processingChannelId, name, type,
        start, end, wfs, creationInfo);
  }
}
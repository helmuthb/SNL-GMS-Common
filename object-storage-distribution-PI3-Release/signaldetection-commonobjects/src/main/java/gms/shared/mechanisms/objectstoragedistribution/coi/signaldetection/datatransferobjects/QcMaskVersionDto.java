package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionReference;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link QcMaskVersion}
 *
 * Created by jrhipp on 9/7/17.
 */
public interface QcMaskVersionDto {

  @JsonCreator
  static QcMaskVersion from(
      @JsonProperty("version") long version,
      @JsonProperty("parentQcMasks") Collection<QcMaskVersionReference> parentQcMasks,
      @JsonProperty("channelSegmentIds") List<UUID> channelSegmentIds,
      @JsonProperty("category") QcMaskCategory category,
      @JsonProperty("type") QcMaskType type,
      @JsonProperty("rationale") String rationale,
      @JsonProperty("startTime") Instant startTime,
      @JsonProperty("endTime") Instant endTime,
      @JsonProperty("creationInfoId") UUID creationInfoId) {
    return QcMaskVersion
        .from(version, parentQcMasks, channelSegmentIds, category, type, rationale,
            startTime, endTime, creationInfoId);
  }

  @JsonIgnore
  boolean isRejected();

}

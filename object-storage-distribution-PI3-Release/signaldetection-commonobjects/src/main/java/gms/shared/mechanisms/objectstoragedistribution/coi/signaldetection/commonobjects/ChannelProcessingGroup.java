package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ChannelProcessingGroup {

  private final UUID id;
  private final ChannelProcessingGroupType type;
  private final Set<UUID> referenceChannelIds;
  private final Instant actualChangeTime;
  private final Instant systemChangeTime;
  private final String status;
  private final String comment;

  public enum ChannelProcessingGroupType {
    SINGLE_CHANNEL, THREE_COMPONENT, BEAM
  }

  public static ChannelProcessingGroup create(
      ChannelProcessingGroupType type, Set<UUID> referenceChannelIds, Instant actualChangeTime,
      Instant systemChangeTime, String status, String comment) {

    Objects.requireNonNull(type,
        "ChannelProcessingGroup expects non-null channel processing type.");
    Objects.requireNonNull(referenceChannelIds,
        "ChannelProcessingGroup expects non-null set of reference channel ids.");
    Objects.requireNonNull(actualChangeTime,
        "ChannelProcessingGroup expects non-null actual change time.");
    Objects.requireNonNull(systemChangeTime,
        "ChannelProcessingGroup expects non-null system change time.");
    Objects.requireNonNull(status, "ChannelProcessingGroup expects non-null status.");
    Objects.requireNonNull(comment, "ChannelProcessingGroup expects non-null comment.");

    return new ChannelProcessingGroup(UUID.randomUUID(), type, referenceChannelIds,
        actualChangeTime, systemChangeTime, status, comment);
  }

  private ChannelProcessingGroup(UUID id,
      ChannelProcessingGroupType type, Set<UUID> referenceChannelIds, Instant actualChangeTime,
      Instant systemChangeTime, String status, String comment) {
    this.id = id;
    this.type = type;
    this.referenceChannelIds = referenceChannelIds;
    this.actualChangeTime = actualChangeTime;
    this.systemChangeTime = systemChangeTime;
    this.status = status;
    this.comment = comment;
  }

  public UUID getId() {
    return id;
  }

  public ChannelProcessingGroupType getType() {
    return type;
  }

  public Set<UUID> getReferenceChannelIds() {
    return referenceChannelIds;
  }

  public Instant getActualChangeTime() {
    return actualChangeTime;
  }

  public Instant getSystemChangeTime() {
    return systemChangeTime;
  }

  public String getStatus() {
    return status;
  }

  public String getComment() {
    return comment;
  }
}

package gms.core.signalenhancement.waveformfiltering.control;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Wrapper class containing all needed data in order to execute {@link FilterControl} via claim
 * check.
 */
public class ExecuteClaimCheckCommand {

  private final Map<UUID, UUID> inputToOutputChannelIds;
  private final UUID channelProcessingStepId;
  private final Instant startTime;
  private final Instant endTime;
  private final ProcessingContext processingContext;

  /**
   * Factory method for creating a standard ExecuteClaimCheckCommand. inputToOutputChannelIds maps
   * describes the relationship between Channels providing the input waveforms to be filtered and
   * the output Channels associated with the filtered waveforms.  No validation occurs on this
   * mapping which must be correct and valid when input to this factory.
   *
   * @param inputToOutputChannelIds maps input Channel ids to their corresponding output derived
   * Channel ids, not null
   * @param startTime Start of the time range to run filter, not null
   * @param endTime End of the time range to run filter, not null
   * @param processingContext Context in which we are running filter, not null
   * @return {@link ExecuteClaimCheckCommand} used for executing {@link FilterControl}
   */
  public static ExecuteClaimCheckCommand create(Map<UUID, UUID> inputToOutputChannelIds,
      UUID channelProcessingStepId, Instant startTime, Instant endTime,
      ProcessingContext processingContext) {

    Objects.requireNonNull(inputToOutputChannelIds,
        "Error creating ExecuteClaimCheckCommand: inputToOutputChannelIds cannot be null cannot be null");
    Objects.requireNonNull(channelProcessingStepId,
        "Error creating ExecuteClaimCheckCommand: Processing Step Id cannot be null");
    Objects.requireNonNull(startTime,
        "Error creating ExecuteClaimCheckCommand: Start Time cannot be null");
    Objects.requireNonNull(endTime,
        "Error creating ExecuteClaimCheckCommand: End Time cannot be null");
    Objects.requireNonNull(processingContext,
        "Error creating ExecuteClaimCheckCommand: Processing Context cannot be null");

    return new ExecuteClaimCheckCommand(inputToOutputChannelIds, channelProcessingStepId,
        startTime, endTime, processingContext);
  }

  private ExecuteClaimCheckCommand(Map<UUID, UUID> inputToOutputChannelIds,
      UUID channelProcessingStepId, Instant startTime, Instant endTime,
      ProcessingContext processingContext) {

    this.inputToOutputChannelIds = inputToOutputChannelIds;
    this.channelProcessingStepId = channelProcessingStepId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.processingContext = processingContext;
  }

  public Map<UUID, UUID> getInputToOutputChannelIds() {
    return inputToOutputChannelIds;
  }

  public UUID getChannelProcessingStepId() {
    return channelProcessingStepId;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public ProcessingContext getProcessingContext() {
    return processingContext;
  }

  @Override
  public String toString() {
    return "ExecuteClaimCheckCommand{" +
        "inputToOutputChannelIds=" + inputToOutputChannelIds +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", processingContext=" + processingContext +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ExecuteClaimCheckCommand that = (ExecuteClaimCheckCommand) o;

    return (inputToOutputChannelIds != null ? inputToOutputChannelIds
        .equals(that.inputToOutputChannelIds) : that.inputToOutputChannelIds == null) && (
        channelProcessingStepId != null ? channelProcessingStepId
            .equals(that.channelProcessingStepId) : that.channelProcessingStepId == null) && (
        startTime != null ? startTime.equals(that.startTime) : that.startTime == null) && (
        endTime != null ? endTime.equals(that.endTime) : that.endTime == null) && (
        processingContext != null ? processingContext.equals(that.processingContext)
            : that.processingContext == null);
  }

  @Override
  public int hashCode() {
    int result = inputToOutputChannelIds != null ? inputToOutputChannelIds.hashCode() : 0;
    result =
        31 * result + (channelProcessingStepId != null ? channelProcessingStepId.hashCode() : 0);
    result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
    result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
    result = 31 * result + (processingContext != null ? processingContext.hashCode() : 0);
    return result;
  }
}

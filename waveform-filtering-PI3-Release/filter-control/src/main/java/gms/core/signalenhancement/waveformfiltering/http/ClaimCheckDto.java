package gms.core.signalenhancement.waveformfiltering.http;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Data Transfer Object for the request body used in claim check invocations of {@link
 * gms.core.signalenhancement.waveformfiltering.control.FilterControl} via {@link
 * FilterControlRouteHandler#claimCheck(ContentType, byte[], ContentType)}
 */
public class ClaimCheckDto {

  private UUID channelProcessingStepId;
  private Map<UUID, UUID> inputToOutputChannelIds;
  private Instant startTime;
  private Instant endTime;
  private ProcessingContext processingContext;

  public ClaimCheckDto() {
  }

  public UUID getChannelProcessingStepId() {
    return channelProcessingStepId;
  }

  public void setChannelProcessingStepId(UUID channelProcessingStepId) {
    this.channelProcessingStepId = channelProcessingStepId;
  }

  public Map<UUID, UUID> getInputToOutputChannelIds() {
    return inputToOutputChannelIds;
  }

  public void setInputToOutputChannelIds(
      Map<UUID, UUID> inputToOutputChannelIds) {
    this.inputToOutputChannelIds = inputToOutputChannelIds;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(Instant endTime) {
    this.endTime = endTime;
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

    ClaimCheckDto that = (ClaimCheckDto) o;

    return (channelProcessingStepId != null ? channelProcessingStepId
        .equals(that.channelProcessingStepId) : that.channelProcessingStepId == null) && (
        inputToOutputChannelIds != null ? inputToOutputChannelIds
            .equals(that.inputToOutputChannelIds) : that.inputToOutputChannelIds == null) && (
        startTime != null ? startTime.equals(that.startTime) : that.startTime == null) && (
        endTime != null ? endTime.equals(that.endTime) : that.endTime == null) && (
        processingContext != null ? processingContext.equals(that.processingContext)
            : that.processingContext == null);
  }

  @Override
  public int hashCode() {
    int result = channelProcessingStepId != null ? channelProcessingStepId.hashCode() : 0;
    result =
        31 * result + (inputToOutputChannelIds != null ? inputToOutputChannelIds.hashCode() : 0);
    result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
    result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
    result = 31 * result + (processingContext != null ? processingContext.hashCode() : 0);
    return result;
  }
}

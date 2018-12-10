package gms.core.waveformqc.waveformqccontrol.control;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Input DTO object used to convert from JSON and convert to a {@link ExecuteCommand} for use by
 * {@link WaveformQcControl}.
 *
 * Contains all the necessary data to perform QC.
 */
public class ControlInvokeDto {

  private Set<UUID> processingChannelIds;
  private Instant startTime;
  private Instant endTime;
  private ProcessingContext processingContext;

  /**
   * Default empty constructor required for serialization.
   */
  public ControlInvokeDto() {
  }

  public ControlInvokeDto(
      Set<UUID> processingChannelIds, Instant startTime, Instant endTime,
      ProcessingContext processingContext) {
    this.processingChannelIds = processingChannelIds;
    this.startTime = startTime;
    this.endTime = endTime;
    this.processingContext = processingContext;
  }

  public Set<UUID> getProcessingChannelIds() {
    return processingChannelIds;
  }

  public void setProcessingChannelIds(
      Set<UUID> processingChannelIds) {
    this.processingChannelIds = processingChannelIds;
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
}

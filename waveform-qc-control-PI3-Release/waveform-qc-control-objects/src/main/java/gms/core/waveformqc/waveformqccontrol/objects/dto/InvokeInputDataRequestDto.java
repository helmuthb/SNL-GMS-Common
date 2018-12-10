package gms.core.waveformqc.waveformqccontrol.objects.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * DTO containing the request body for the Waveform Qc Control OSD Gateway loadInputData operation.
 */
public class InvokeInputDataRequestDto {

  private Set<UUID> processingChannelIds;
  private Instant startTime;
  private Instant endTime;

  public InvokeInputDataRequestDto() {
  }

  public InvokeInputDataRequestDto(
      Set<UUID> processingChannelIds, Instant startTime, Instant endTime) {
    this.processingChannelIds = processingChannelIds;
    this.startTime = startTime;
    this.endTime = endTime;
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

  @Override
  public String toString() {
    return "InvokeInputDataRequestDto{" +
        "processingChannelIds=" + processingChannelIds +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        '}';
  }
}

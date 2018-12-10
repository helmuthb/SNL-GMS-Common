package gms.core.signaldetection.signaldetectorcontrol.objects.dto;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

/**
 * DTO containing the request body for SignalDetectorControl OSD Gateway loadInputData operation.
 */
public class InvokeInputDataRequestDto {

  private Collection<UUID> channelIds;
  private Instant startTime;
  private Instant endTime;

  public InvokeInputDataRequestDto() {
  }

  public InvokeInputDataRequestDto(Collection<UUID> channelIds, Instant startTime,
      Instant endTime) {

    this.channelIds = channelIds;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public Collection<UUID> getChannelIds() {
    return channelIds;
  }

  public void setChannelIds(Collection<UUID> channelIds) {
    this.channelIds = channelIds;
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
        "channelIds=" + channelIds +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        '}';
  }
}


package gms.core.signaldetection.signaldetectorcontrol.http;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object for the request body used in claim check invocations of {@link
 * gms.core.signaldetection.signaldetectorcontrol.control.SignalDetectorControl} via {@link
 * SignalDetectorControlRouteHandler#claimCheck(ContentType, byte[], ContentType)}
 */
public class ClaimCheckDto {

  private UUID stationId;
  private Instant startTime;
  private Instant endTime;
  private ProcessingContext processingContext;

  public ClaimCheckDto() {
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

  public UUID getStationId() {
    return stationId;
  }

  public void setStationId(UUID stationId) {
    this.stationId = stationId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ClaimCheckDto)) {
      return false;
    }

    ClaimCheckDto that = (ClaimCheckDto) o;

    return (stationId != null ? stationId
        .equals(that.stationId) : that.stationId == null) && (
        startTime != null ? startTime.equals(that.startTime) : that.startTime == null) && (
        endTime != null ? endTime.equals(that.endTime) : that.endTime == null) && (
        processingContext != null ? processingContext.equals(that.processingContext)
            : that.processingContext == null);
  }

  @Override
  public int hashCode() {
    int result = getStationId().hashCode();
    result = 31 * result + getStartTime().hashCode();
    result = 31 * result + getEndTime().hashCode();
    result = 31 * result + getProcessingContext().hashCode();
    return result;
  }
}
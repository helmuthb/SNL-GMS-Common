package gms.core.signaldetection.signaldetectorcontrol.control;

import gms.core.signaldetection.signaldetectorcontrol.objects.SignalDetectorParameters;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class ExecuteClaimCheckCommand {

  private final UUID stationId;
  private final Instant startTime;
  private final Instant endTime;
  private final ProcessingContext processingContext;

  /**
   * Factory method for creating a standard ExecuteClaimCheckCommand
   *
   * @param stationId The station to retrieve and its Channel Segments for
   * signal detection processing
   * @param startTime The start of the signal detection processing timeframe
   * @param endTime The end of the signal detection processing timeframe
   * @param processingContext Context in which we are running signal detection
   * including processing step, analyst action, and storage visability
   * @return A standard command object used for executing signal detection
   */
  public static ExecuteClaimCheckCommand create(
      UUID stationId,
      Instant startTime,
      Instant endTime,
      ProcessingContext processingContext) {

    Objects.requireNonNull(stationId,
        "Error creating ExecuteClaimCheckCommand: Station Id cannot be null");
    Objects.requireNonNull(startTime,
        "Error creating ExecuteClaimCheckCommand: Start Time cannot be null");
    Objects.requireNonNull(endTime,
        "Error creating ExecuteClaimCheckCommand: End Time cannot be null");
    Objects.requireNonNull(processingContext,
        "Error creating ExecuteClaimCheckCommand: Processing Context cannot be null");

    return new ExecuteClaimCheckCommand(stationId, startTime, endTime, processingContext);
  }

  private ExecuteClaimCheckCommand(UUID stationId, Instant startTime, Instant endTime, ProcessingContext processingContext) {
    this.stationId = stationId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.processingContext = processingContext;
  }

  public UUID getStationId() {
    return stationId;
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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ExecuteClaimCheckCommand)) {
      return false;
    }

    ExecuteClaimCheckCommand that = (ExecuteClaimCheckCommand) o;

    if (!stationId.equals(that.stationId)) {
      return false;
    }
    if (!startTime.equals(that.startTime)) {
      return false;
    }
    if (!endTime.equals(that.endTime)) {
      return false;
    }
    return processingContext.equals(that.processingContext);
  }

  @Override
  public int hashCode() {
    int result = stationId.hashCode();
    result = 31 * result + startTime.hashCode();
    result = 31 * result + endTime.hashCode();
    result = 31 * result + processingContext.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ExecuteClaimCheckCommand{" +
        "stationId=" + stationId +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", processingContext=" + processingContext +
        '}';
  }
}
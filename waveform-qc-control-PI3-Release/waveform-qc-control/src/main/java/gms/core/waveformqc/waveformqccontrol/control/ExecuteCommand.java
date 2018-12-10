package gms.core.waveformqc.waveformqccontrol.control;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


/**
 * Wrapper class containing all needed data in order to execute {@link WaveformQcControl}.
 */
public class ExecuteCommand {

  private final Set<UUID> processingChannelIds;
  private final Instant startTime;
  private final Instant endTime;
  private final ProcessingContext processingContext;

  private ExecuteCommand(
      Set<UUID> processingChannelIds, Instant startTime, Instant endTime,
      ProcessingContext processingContext) {
    this.processingChannelIds = processingChannelIds;
    this.startTime = startTime;
    this.endTime = endTime;
    this.processingContext = processingContext;
  }

  /**
   * Factory method for creating a standard ExecuteCommand
   *
   * @param processingChannelIds Ids for the Processing Channels we want to execute QC on.
   * @param startTime Start of the time range to run QC.
   * @param endTime End of the time range to run QC.
   * @param processingContext Context in which we are running QC.
   * @return A standard Command object used for executing QC.
   */
  public static ExecuteCommand create(Set<UUID> processingChannelIds,
      Instant startTime,
      Instant endTime,
      ProcessingContext processingContext) {

    Objects.requireNonNull(processingChannelIds,
        "Error creating ExecuteCommand: Processing Channel Ids cannot be null");
    Objects.requireNonNull(startTime,
        "Error creating ExecuteCommand: Start Time cannot be null");
    Objects.requireNonNull(endTime,
        "Error creating ExecuteCommand: End Time cannot be null");
    Objects.requireNonNull(processingContext,
        "Error creating ExecuteCommand: Processing Context cannot be null");

    return new ExecuteCommand(new HashSet<>(processingChannelIds),
        startTime, endTime, processingContext);
  }

  public Set<UUID> getProcessingChannelIds() {
    return processingChannelIds;
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
    return "ExecuteCommand{" +
        "processingChannelIds=" + processingChannelIds +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", processingContext=" + processingContext +
        '}';
  }
}

package gms.core.signalenhancement.waveformfiltering.control;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Wrapper class containing all needed data in order to execute {@link FilterControl} via
 * streaming.
 */
public class ExecuteStreamingCommand {

  private final Map<ChannelSegment, UUID> inputChannelSegmentToOutputChannelIds;

  private FilterDefinition filterDefinition;
  private ProcessingContext processingContext;

  /**
   * Factory method for creating a standard ExecuteStreamingCommand
   *
   * @param channelSegmentOutputChannelMap Map of channel segments to output channel ids
   * upon which the filtering operation is performed and output to
   * @param filterDefinition Filter definition to use
   * @param processingContext Context in which we are running filter
   * @return A standard command object used for executing a filter
   */
  public static ExecuteStreamingCommand create(
      Map<ChannelSegment, UUID> channelSegmentOutputChannelMap,
      FilterDefinition filterDefinition, ProcessingContext processingContext) {

    Objects.requireNonNull(channelSegmentOutputChannelMap,
        "Error creating ExecuteStreamingCommand: Channel Segment to Output Channel ID map cannot be null");
    Objects.requireNonNull(filterDefinition,
        "Error creating ExecuteStreamingCommand: Filter Definition cannot be null");
    Objects.requireNonNull(processingContext,
        "Error creating ExecuteStreamingCommand: Processing Context cannot be null");

    return new ExecuteStreamingCommand(channelSegmentOutputChannelMap, filterDefinition,
        processingContext);
  }

  private ExecuteStreamingCommand(Map<ChannelSegment, UUID> channelSegmentOutputChannelMap,
      FilterDefinition filterDefinition, ProcessingContext processingContext) {
    this.inputChannelSegmentToOutputChannelIds = channelSegmentOutputChannelMap;
    this.filterDefinition = filterDefinition;
    this.processingContext = processingContext;
  }

  public Map<ChannelSegment, UUID> getInputChannelSegmentToOutputChannelIds() {
    return inputChannelSegmentToOutputChannelIds;
  }

  public FilterDefinition getFilterDefinition() {
    return filterDefinition;
  }

  public ProcessingContext getProcessingContext() {
    return processingContext;
  }

  @Override
  public String toString() {
    return "ExecuteStreamingCommand{" +
        "channelSegmentToOutputChannelMap=" +
        inputChannelSegmentToOutputChannelIds +
        ", filterDefinition=" + filterDefinition +
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

    ExecuteStreamingCommand that = (ExecuteStreamingCommand) o;

    return (inputChannelSegmentToOutputChannelIds != null ? inputChannelSegmentToOutputChannelIds
        .equals(that.inputChannelSegmentToOutputChannelIds)
        : that.inputChannelSegmentToOutputChannelIds == null) && (filterDefinition != null
        ? filterDefinition.equals(that.filterDefinition) : that.filterDefinition == null) && (
        processingContext != null ? processingContext.equals(that.processingContext)
            : that.processingContext == null);
  }

  @Override
  public int hashCode() {
    int result =
        inputChannelSegmentToOutputChannelIds != null ? inputChannelSegmentToOutputChannelIds
            .hashCode() : 0;
    result = 31 * result + (filterDefinition != null ? filterDefinition.hashCode() : 0);
    result = 31 * result + (processingContext != null ? processingContext.hashCode() : 0);
    return result;
  }
}

package gms.core.signalenhancement.waveformfiltering.control;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterCausality;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterPassBandType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ExecuteStreamingCommandTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testCreateNullChannelSegmentsExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error creating ExecuteStreamingCommand: Channel Segment to Output Channel ID map cannot be null");
    ExecuteStreamingCommand.create(null,
        FilterDefinition.from(
            "", "", FilterType.IIR_BUTTERWORTH, FilterPassBandType.BAND_PASS, 1.0, 2.0, 1,
            FilterSource.SYSTEM, FilterCausality.CAUSAL, true, 40.0, .01, new double[]{1, 2},
            new double[]{1, 2}, 1.0
        ),
        ProcessingContext.createInteractive(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            StorageVisibility.PRIVATE));
  }

  @Test
  public void testCreateNullFilterDefinitionExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error creating ExecuteStreamingCommand: Filter Definition cannot be null");
    ExecuteStreamingCommand.create(Map.of(),
        null,
        ProcessingContext.createInteractive(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            StorageVisibility.PRIVATE));
  }


  @Test
  public void testCreateNullProcessingContextExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error creating ExecuteStreamingCommand: Processing Context cannot be null");
    ExecuteStreamingCommand.create(Map.of(),
        FilterDefinition.from(
            "", "", FilterType.IIR_BUTTERWORTH, FilterPassBandType.BAND_PASS, 1.0, 2.0, 1,
            FilterSource.SYSTEM, FilterCausality.CAUSAL, true, 40.0, .01, new double[]{1, 2},
            new double[]{1, 2}, 1.0
        ),
        null);
  }

  @Test
  public void testCreate() {
    ChannelSegment channelSegment1 = ChannelSegment
        .create(UUID.randomUUID(), "test1", ChannelSegment.ChannelSegmentType.RAW, Instant.EPOCH,
            Instant.EPOCH.plusSeconds(20), new TreeSet<>(List.of()), CreationInfo.DEFAULT);

    ChannelSegment channelSegment2 = ChannelSegment
        .create(UUID.randomUUID(), "test2", ChannelSegment.ChannelSegmentType.RAW, Instant.EPOCH,
            Instant.EPOCH.plusSeconds(10), new TreeSet<>(List.of()), CreationInfo.DEFAULT);

    FilterDefinition filterDefinition = FilterDefinition.from(
        "", "", FilterType.IIR_BUTTERWORTH, FilterPassBandType.BAND_PASS, 1.0, 2.0, 1,
        FilterSource.SYSTEM, FilterCausality.CAUSAL, true, 40.0, .01, new double[]{1, 2},
        new double[]{1, 2}, 1.0
    );

    ProcessingContext processingContext = ProcessingContext
        .createInteractive(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            StorageVisibility.PRIVATE);

    Map<ChannelSegment, UUID> channelSegmentChannelIdMap = Map
        .of(channelSegment1, UUID.randomUUID(),
            channelSegment2, UUID.randomUUID());

    ExecuteStreamingCommand command = ExecuteStreamingCommand
        .create(channelSegmentChannelIdMap, filterDefinition, processingContext);

    assertNotNull(command);
    assertTrue(filterDefinition.equals(command.getFilterDefinition()));
    assertTrue(processingContext.equals(command.getProcessingContext()));
    assertTrue(command.getInputChannelSegmentToOutputChannelIds().keySet()
        .containsAll(channelSegmentChannelIdMap.keySet()) && channelSegmentChannelIdMap.keySet()
        .containsAll(command.getInputChannelSegmentToOutputChannelIds().keySet()));
    assertTrue(command.getInputChannelSegmentToOutputChannelIds().values()
        .containsAll(channelSegmentChannelIdMap.values()) && channelSegmentChannelIdMap.values()
        .containsAll(command.getInputChannelSegmentToOutputChannelIds().values()));
  }

}

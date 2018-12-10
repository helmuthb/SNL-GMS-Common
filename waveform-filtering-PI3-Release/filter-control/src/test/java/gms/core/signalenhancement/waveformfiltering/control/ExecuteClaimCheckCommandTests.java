package gms.core.signalenhancement.waveformfiltering.control;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ExecuteClaimCheckCommandTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private static final ProcessingContext defaultProcessingContext = ProcessingContext
      .createInteractive(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
          StorageVisibility.PRIVATE);

  @Test
  public void testCreateNullInputToOutputChannelIdsExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error creating ExecuteClaimCheckCommand: inputToOutputChannelIds cannot be null");
    ExecuteClaimCheckCommand
        .create(null, UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(100),
            defaultProcessingContext);
  }

  @Test
  public void testCreateNullProcessingStepIdExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error creating ExecuteClaimCheckCommand: Processing Step Id cannot be null");
    ExecuteClaimCheckCommand
        .create(Map.of(), null, Instant.now(), Instant.now().plusSeconds(100),
            defaultProcessingContext);
  }

  @Test
  public void testCreateNullStartTimeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Error creating ExecuteClaimCheckCommand: Start Time cannot be null");
    ExecuteClaimCheckCommand.create(Map.of(), UUID.randomUUID(), null, Instant.now(),
        defaultProcessingContext);
  }

  @Test
  public void testCreateNullEndTimeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Error creating ExecuteClaimCheckCommand: End Time cannot be null");
    ExecuteClaimCheckCommand
        .create(Map.of(), UUID.randomUUID(), Instant.now(), null, defaultProcessingContext);
  }

  @Test
  public void testCreateNullProcessingContextExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error creating ExecuteClaimCheckCommand: Processing Context cannot be null");
    ExecuteClaimCheckCommand
        .create(Map.of(), UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(100), null);
  }

  @Test
  public void testCreate() {
    Map<UUID, UUID> inToOutChannelIds = Map
        .of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

    UUID channelProcessingStepId = UUID.randomUUID();
    Instant startTime = Instant.now();
    Instant endTime = Instant.now().plusSeconds(100);

    ExecuteClaimCheckCommand command = ExecuteClaimCheckCommand
        .create(inToOutChannelIds, channelProcessingStepId, startTime, endTime,
            defaultProcessingContext);

    assertEquals(inToOutChannelIds, command.getInputToOutputChannelIds());
    assertEquals(channelProcessingStepId, command.getChannelProcessingStepId());
    assertEquals(startTime, command.getStartTime());
    assertEquals(endTime, command.getEndTime());
    assertEquals(defaultProcessingContext, command.getProcessingContext());
  }
}
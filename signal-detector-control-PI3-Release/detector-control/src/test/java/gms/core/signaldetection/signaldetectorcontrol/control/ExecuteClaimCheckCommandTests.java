package gms.core.signaldetection.signaldetectorcontrol.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import java.time.Instant;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ExecuteClaimCheckCommandTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testCreateNullStationIdExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error creating ExecuteClaimCheckCommand: Station Id cannot be null");
    ExecuteClaimCheckCommand.create(null,
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(100),
        ProcessingContext.createInteractive(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            StorageVisibility.PRIVATE));
  }

  @Test
  public void testCreateNullStartTimeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error creating ExecuteClaimCheckCommand: Start Time cannot be null");
    ExecuteClaimCheckCommand.create(UUID.randomUUID(),
        null,
        Instant.EPOCH.plusSeconds(100),
        ProcessingContext.createInteractive(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            StorageVisibility.PRIVATE));
  }


  @Test
  public void testCreateNullEndTimeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error creating ExecuteClaimCheckCommand: End Time cannot be null");
    ExecuteClaimCheckCommand.create(UUID.randomUUID(),
        Instant.EPOCH,
        null,
        ProcessingContext.createInteractive(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            StorageVisibility.PRIVATE));
  }

  @Test
  public void testCreateNullProcessingContextExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error creating ExecuteClaimCheckCommand: Processing Context cannot be null");
    ExecuteClaimCheckCommand.create(UUID.randomUUID(),
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(100),
        null);
  }

  @Test
  public void testCreate() {
    UUID stationId = UUID.randomUUID();
    Instant startTime = Instant.EPOCH;
    Instant endTime = Instant.EPOCH.plusSeconds(100);
    //TODO: Fix SignalDetectorParameters create() once SignalDetectorParameters is implemented
    //SignalDetectorParameters.create(XXX);
    ProcessingContext processingContext = ProcessingContext
        .createInteractive(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            StorageVisibility.PRIVATE);
    ExecuteClaimCheckCommand command = ExecuteClaimCheckCommand
        .create(stationId, startTime, endTime, processingContext);

    assertNotNull(command);
    assertEquals(stationId, command.getStationId());
    assertEquals(startTime, command.getStartTime());
    assertEquals(endTime, command.getEndTime());
    assertEquals(processingContext, command.getProcessingContext());
  }
}

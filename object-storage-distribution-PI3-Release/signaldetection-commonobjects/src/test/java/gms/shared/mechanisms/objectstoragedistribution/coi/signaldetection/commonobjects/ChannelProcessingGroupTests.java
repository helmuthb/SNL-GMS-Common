package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroup.ChannelProcessingGroupType;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class ChannelProcessingGroupTests {

  private final ChannelProcessingGroupType type = ChannelProcessingGroupType.BEAM;
  private final Instant actualChangeTime = Instant.now().minusSeconds(500);
  private final Instant systemChangeTime = Instant.now();
  private final String status = "This is tha status";
  private final String comment = "This is a comment";
  private final Set<UUID> referenceChannelIds = Set.of(UUID.randomUUID());


  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testCreate() {
    ChannelProcessingGroup group = ChannelProcessingGroup
        .create(type, referenceChannelIds, actualChangeTime, systemChangeTime, status, comment);

    assertEquals(group.getType(), type);
    assertEquals(group.getReferenceChannelIds(), referenceChannelIds);
    assertEquals(group.getActualChangeTime(), actualChangeTime);
    assertEquals(group.getSystemChangeTime(), systemChangeTime);
    assertEquals(group.getStatus(), status);
    assertEquals(group.getComment(), comment);
  }

  @Test
  public void testCreateNullTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingGroup expects non-null channel processing type.");
    ChannelProcessingGroup group = ChannelProcessingGroup
        .create(null, referenceChannelIds, actualChangeTime, systemChangeTime, status, comment);
  }

  @Test
  public void testCreateNullReferenceChannelIdsExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingGroup expects non-null set of reference channel ids.");
    ChannelProcessingGroup group = ChannelProcessingGroup
        .create(type, null, actualChangeTime, systemChangeTime, status, comment);
  }

  @Test
  public void testCreateNullActualTimeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingGroup expects non-null actual change time.");
    ChannelProcessingGroup group = ChannelProcessingGroup
        .create(type, referenceChannelIds, null, systemChangeTime, status, comment);
  }

  @Test
  public void testCreateNullSystemTimeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingGroup expects non-null system change time.");
    ChannelProcessingGroup group = ChannelProcessingGroup
        .create(type, referenceChannelIds, actualChangeTime, null, status, comment);
  }

  @Test
  public void testCreateNullStatusExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingGroup expects non-null status.");
    ChannelProcessingGroup group = ChannelProcessingGroup
        .create(type, referenceChannelIds, actualChangeTime, systemChangeTime, null, comment);
  }

  @Test
  public void testCreateNullCommentExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingGroup expects non-null comment.");
    ChannelProcessingGroup group = ChannelProcessingGroup
        .create(type, referenceChannelIds, actualChangeTime, systemChangeTime, status, null);
  }

}


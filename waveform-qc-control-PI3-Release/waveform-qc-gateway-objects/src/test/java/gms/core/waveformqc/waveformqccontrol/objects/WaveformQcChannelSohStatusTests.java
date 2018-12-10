package gms.core.waveformqc.waveformqccontrol.objects;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus.Builder;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus.Status;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus.StatusState;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WaveformQcChannelSohStatusTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private final UUID expectedProcessingChannelId = UUID.randomUUID();
  private final AcquiredChannelSohType acquiredChannelSohType = AcquiredChannelSohType.CLIPPED;
  private final QcMaskType expectedQcMaskType = QcMaskType.SENSOR_PROBLEM;
  private final ChannelSohSubtype expectedChannelSohSubtype = ChannelSohSubtype.CLIPPED;
  private final Instant expectedStartTime = Instant.now();
  private final Duration statusDuration = Duration.ofSeconds(10);
  private final Instant expectedEndTime = expectedStartTime.plus(statusDuration);
  private final boolean initialStatus = true;
  private final StatusState expectedInitialStatus = StatusState.of(initialStatus);
  private final Duration threshold = Duration.ofSeconds(5);

  /**
   * Tests basic {@link Builder} functionality with a single status
   */
  @Test
  public void testBuilderFromAcquiredChannelSohType() throws Exception {

    Builder builder = WaveformQcChannelSohStatus
        .builder(expectedProcessingChannelId, acquiredChannelSohType,
            expectedStartTime, expectedEndTime, initialStatus, threshold);

    WaveformQcChannelSohStatus status = builder.build();

    assertEquals(expectedProcessingChannelId, status.getProcessingChannelId());
    assertEquals(expectedQcMaskType, status.getQcMaskType());
    assertEquals(expectedChannelSohSubtype, status.getChannelSohSubtype());
    assertEquals(1, status.getStatusChanges().count());

    Optional<Status> actualStatus = status.getStatusChanges().findFirst();
    assertTrue(actualStatus.isPresent());

    assertEquals(expectedStartTime, actualStatus.get().getStartTime());
    assertEquals(expectedInitialStatus, actualStatus.get().getStatus());
  }

  @Test
  public void testBuilderAnalogSohExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("is an unknown literal from AcquiredChannelSohType");
    WaveformQcChannelSohStatus
        .builder(expectedProcessingChannelId, AcquiredChannelSohType.STATION_POWER_VOLTAGE,
            expectedStartTime, expectedEndTime, initialStatus, threshold);
  }

  /**
   * Tests correct translation of {@link AcquiredChannelSohType} into {@link QcMaskType} and {@link
   * ChannelSohSubtype}
   */
  @Test
  public void testSohEnumConversions() throws Exception {
    verifyConversion(AcquiredChannelSohType.DEAD_SENSOR_CHANNEL, QcMaskType.SENSOR_PROBLEM,
        ChannelSohSubtype.DEAD_SENSOR_CHANNEL);
    verifyConversion(AcquiredChannelSohType.ZEROED_DATA, QcMaskType.SENSOR_PROBLEM,
        ChannelSohSubtype.ZEROED_DATA);
    verifyConversion(AcquiredChannelSohType.CLIPPED, QcMaskType.SENSOR_PROBLEM,
        ChannelSohSubtype.CLIPPED);

    verifyConversion(AcquiredChannelSohType.MAIN_POWER_FAILURE, QcMaskType.STATION_PROBLEM,
        ChannelSohSubtype.MAIN_POWER_FAILURE);
    verifyConversion(AcquiredChannelSohType.BACKUP_POWER_UNSTABLE, QcMaskType.STATION_PROBLEM,
        ChannelSohSubtype.BACKUP_POWER_UNSTABLE);

    verifyConversion(AcquiredChannelSohType.CALIBRATION_UNDERWAY, QcMaskType.CALIBRATION,
        ChannelSohSubtype.CALIBRATION_UNDERWAY);
    verifyConversion(AcquiredChannelSohType.DIGITIZER_ANALOG_INPUT_SHORTED, QcMaskType.CALIBRATION,
        ChannelSohSubtype.DIGITIZER_ANALOG_INPUT_SHORTED);
    verifyConversion(AcquiredChannelSohType.DIGITIZER_CALIBRATION_LOOP_BACK, QcMaskType.CALIBRATION,
        ChannelSohSubtype.DIGITIZER_CALIBRATION_LOOP_BACK);

    verifyConversion(AcquiredChannelSohType.EQUIPMENT_HOUSING_OPEN, QcMaskType.STATION_SECURITY,
        ChannelSohSubtype.EQUIPMENT_HOUSING_OPEN);
    verifyConversion(AcquiredChannelSohType.DIGITIZING_EQUIPMENT_OPEN, QcMaskType.STATION_SECURITY,
        ChannelSohSubtype.DIGITIZING_EQUIPMENT_OPEN);
    verifyConversion(AcquiredChannelSohType.VAULT_DOOR_OPENED, QcMaskType.STATION_SECURITY,
        ChannelSohSubtype.VAULT_DOOR_OPENED);
    verifyConversion(AcquiredChannelSohType.AUTHENTICATION_SEAL_BROKEN, QcMaskType.STATION_SECURITY,
        ChannelSohSubtype.AUTHENTICATION_SEAL_BROKEN);
    verifyConversion(AcquiredChannelSohType.EQUIPMENT_MOVED, QcMaskType.STATION_SECURITY,
        ChannelSohSubtype.EQUIPMENT_MOVED);

    verifyConversion(AcquiredChannelSohType.CLOCK_DIFFERENTIAL_TOO_LARGE, QcMaskType.TIMING,
        ChannelSohSubtype.CLOCK_DIFFERENTIAL_TOO_LARGE);
    verifyConversion(AcquiredChannelSohType.GPS_RECEIVER_OFF, QcMaskType.TIMING,
        ChannelSohSubtype.GPS_RECEIVER_OFF);
    verifyConversion(AcquiredChannelSohType.GPS_RECEIVER_UNLOCKED, QcMaskType.TIMING,
        ChannelSohSubtype.GPS_RECEIVER_UNLOCKED);
    verifyConversion(
        AcquiredChannelSohType.DATA_TIME_MINUS_TIME_LAST_GPS_SYNCHRONIZATION_OVER_THRESHOLD,
        QcMaskType.TIMING,
        ChannelSohSubtype.DATA_TIME_GPS_SYNCHRONIZATION_TIME_DELTA_OVER_THRESHOLD);
    verifyConversion(AcquiredChannelSohType.CLOCK_DIFFERENTIAL_IN_MICROSECONDS_OVER_THRESHOLD,
        QcMaskType.TIMING, ChannelSohSubtype.CLOCK_DIFFERENTIAL_OVER_THRESHOLD);
  }

  private void verifyConversion(AcquiredChannelSohType acquiredChannelSohType,
      QcMaskType expectedQcMaskType, ChannelSohSubtype expectedChannelSohSubtype) {
    final UUID id = new UUID(0L, 0L);
    final Instant time = Instant.now();
    final Instant endTime = time.plusSeconds(20);
    WaveformQcChannelSohStatus actual = WaveformQcChannelSohStatus
        .builder(id, acquiredChannelSohType, endTime, time, true, threshold).build();
    assertEquals(expectedQcMaskType, actual.getQcMaskType());
    assertEquals(expectedChannelSohSubtype, actual.getChannelSohSubtype());

    assertEquals(expectedQcMaskType,
        WaveformQcChannelSohStatus.correspondingQcMaskType(acquiredChannelSohType));
    assertEquals(expectedChannelSohSubtype,
        WaveformQcChannelSohStatus.correspondingChannelSohSubtype(acquiredChannelSohType));
  }

  @Test
  public void testCorrespondingQcMaskTypeExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformQcChannelSohStatus.correspondingQcMaskType cannot convert a null AcquiredChannelSohType");
    WaveformQcChannelSohStatus.correspondingQcMaskType(null);
  }

  @Test
  public void testCorrespondingQcMaskTypeBadTypeExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("is an unknown literal from AcquiredChannelSohType");
    WaveformQcChannelSohStatus
        .correspondingQcMaskType(AcquiredChannelSohType.STATION_POWER_VOLTAGE);
  }

  @Test
  public void testCorrespondingChannelSohSubtypeExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "WaveformQcChannelSohStatus.correspondingChannelSohSubtype cannot convert a null AcquiredChannelSohType");
    WaveformQcChannelSohStatus.correspondingChannelSohSubtype(null);
  }

  @Test
  public void testCorrespondingChannelSohSubtypeBadTypeExpectIllegalArgumentException()
      throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("is an unknown literal from AcquiredChannelSohType");
    WaveformQcChannelSohStatus
        .correspondingChannelSohSubtype(AcquiredChannelSohType.STATION_POWER_VOLTAGE);
  }

  @Test
  public void testStatusStateOf() throws Exception {
    assertEquals(StatusState.SET, StatusState.of(true));
    assertEquals(StatusState.UNSET, StatusState.of(false));
  }

  /**
   * Tests basic {@link Builder} functionality with a single status
   */
  @Test
  public void testBuilderFromQcMaskType() throws Exception {

    Builder builder = WaveformQcChannelSohStatus
        .builder(expectedProcessingChannelId, expectedQcMaskType, expectedChannelSohSubtype,
            expectedStartTime, expectedEndTime, initialStatus, threshold);

    WaveformQcChannelSohStatus status = builder.build();

    assertEquals(expectedProcessingChannelId, status.getProcessingChannelId());
    assertEquals(expectedQcMaskType, status.getQcMaskType());
    assertEquals(expectedChannelSohSubtype, status.getChannelSohSubtype());

    assertEquals(1, status.getStatusChanges().count());
    Optional<Status> actualStatus = status.getStatusChanges().findFirst();
    assertTrue(actualStatus.isPresent());

    assertEquals(expectedStartTime, actualStatus.get().getStartTime());
    assertEquals(expectedEndTime, actualStatus.get().getEndTime());
    assertEquals(expectedInitialStatus, actualStatus.get().getStatus());
  }

  @Test
  public void testBuilderInvalidTypeCombinationsExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("is not a valid combination of QcMaskType and ChannelSohSubtype");
    WaveformQcChannelSohStatus
        .builder(expectedProcessingChannelId, QcMaskType.CALIBRATION, ChannelSohSubtype.CLIPPED,
            expectedStartTime, expectedEndTime, initialStatus, threshold);
  }

  @Test
  public void testBuilderInvalidQcMaskTypeExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("is not a valid QcMaskType for a channel soh mask");
    WaveformQcChannelSohStatus
        .builder(expectedProcessingChannelId, QcMaskType.SPIKE, ChannelSohSubtype.CLIPPED,
            expectedStartTime, expectedEndTime, initialStatus, threshold);
  }

  /**
   * Tests basic {@link Builder} functionality with multiple distinct statuses
   */
  @Test
  public void testBuilderMultipleStatuses() throws Exception {

    Builder builder = WaveformQcChannelSohStatus
        .builder(expectedProcessingChannelId, acquiredChannelSohType,
            expectedStartTime, expectedEndTime, initialStatus, threshold);

    final Instant expectedStartTime2 = expectedStartTime.plusSeconds(10);
    final Instant expectedEndTime2 = expectedStartTime.plusSeconds(20);
    final StatusState expectedStatus2 = StatusState.UNSET;
    builder.addStatusChange(expectedStartTime2, expectedEndTime2, false);

    WaveformQcChannelSohStatus status = builder.build();

    assertEquals(expectedProcessingChannelId, status.getProcessingChannelId());
    assertEquals(expectedQcMaskType, status.getQcMaskType());
    assertEquals(expectedChannelSohSubtype, status.getChannelSohSubtype());

    assertEquals(2, status.getStatusChanges().count());
    assertArrayEquals(new Instant[]{expectedStartTime, expectedStartTime2},
        status.getStatusChanges().map(Status::getStartTime).toArray());

    assertArrayEquals(new Instant[]{expectedEndTime, expectedEndTime2},
        status.getStatusChanges().map(Status::getEndTime).toArray());

    assertArrayEquals(
        new StatusState[]{expectedInitialStatus, expectedStatus2},
        status.getStatusChanges().map(Status::getStatus).toArray());
  }

  /**
   * Tests the builder drops repeated adjacent status values.
   */
  @Test
  public void testBuilderIgnoresRepeatedStatusValues() throws Exception {

    final Instant[] startTimes = new Instant[]{
        expectedStartTime,
        expectedStartTime.plusSeconds(10),
        expectedStartTime.plusSeconds(20),
        expectedStartTime.plusSeconds(30),
        expectedStartTime.plusSeconds(40)
    };

    Function<Instant, Instant> plusDuration = i -> i.plus(statusDuration);
    final Instant[] endTimes = Arrays.stream(startTimes).map(plusDuration).toArray(Instant[]::new);

    boolean statuses[] = new boolean[]{initialStatus, false, false, true, true};

    final Instant[] expectedStartTimes =
        new Instant[]{expectedStartTime, startTimes[1], startTimes[3]};
    final Instant[] expectedEndTimes = new Instant[]{expectedEndTime, endTimes[2], endTimes[4]};
    final StatusState[] expectedStatusStates =
        new StatusState[]{StatusState.SET, StatusState.UNSET, StatusState.SET};

    verifyBuilder(startTimes, endTimes, statuses, expectedStartTimes, expectedEndTimes,
        expectedStatusStates);
  }

  /**
   * Tests the builder accepts out of order status entries
   */
  @Test
  public void testBuilderOutOfOrderTimes() throws Exception {

    final Instant[] startTimes = new Instant[]{
        expectedStartTime,
        expectedStartTime.plusSeconds(40),
        expectedStartTime.plusSeconds(30),
        expectedStartTime.plusSeconds(20),
        expectedStartTime.plusSeconds(10)};

    final Instant[] endTimes = new Instant[]{
        expectedEndTime,
        expectedEndTime.plusSeconds(40),
        expectedEndTime.plusSeconds(30),
        expectedEndTime.plusSeconds(20),
        expectedEndTime.plusSeconds(10)};

    // In correct time order the statuses are: [t, f, f, t, t]
    final boolean[] statuses = new boolean[]{true, true, true, false, false};

    final Instant[] expectedStartTimes = new Instant[]{startTimes[0], startTimes[4], startTimes[2]};
    final Instant[] expectedEndTimes = new Instant[]{endTimes[0], endTimes[3], endTimes[1]};
    final StatusState[] expectedStatusStates = new StatusState[]{StatusState.SET, StatusState.UNSET,
        StatusState.SET};

    this.verifyBuilder(startTimes, endTimes, statuses, expectedStartTimes, expectedEndTimes,
        expectedStatusStates);
  }

  /**
   * Tests the builder adds missing statuses
   */
  @Test
  public void testBuilderAddMissingStatus() {

    // All status SET but there is a missing status just before the last two status values.
    // The provided start and end times are:
    // (0-10), (15-25), (30.000000001-40.000000001), (41-51)
    // Resulting status: SET(0-25), MISSING(25-30.000000001), SET(30.000000001-51)
    final Instant start = Instant.ofEpochSecond(0);
    final long statusLengthSecs = this.statusDuration.getSeconds();

    final Instant[] startTimes = new Instant[]{
        start,
        start.plusSeconds(statusLengthSecs).plus(threshold), // no missing status
        start.plusSeconds(statusLengthSecs * 3).plusNanos(1), // missing status
        start.plusSeconds((statusLengthSecs * 4) + 1)};

    final Instant[] endTimes = Arrays.stream(startTimes)
        .map(i -> i.plusSeconds(statusLengthSecs)).toArray(Instant[]::new);

    final boolean[] statuses = new boolean[startTimes.length];
    Arrays.fill(statuses, true);

    final Instant[] expectedStartTimes = new Instant[]{startTimes[0], endTimes[1], startTimes[2]};
    final Instant[] expectedEndTimes = new Instant[]{endTimes[1], startTimes[2], endTimes[3]};
    final StatusState[] expectedStatusStates = new StatusState[]{StatusState.SET,
        StatusState.MISSING, StatusState.SET};

    verifyBuilder(startTimes, endTimes, statuses, expectedStartTimes, expectedEndTimes,
        expectedStatusStates);
  }

  /**
   * Verifies the {@link WaveformQcChannelSohStatus#builder(UUID, AcquiredChannelSohType,
   * Instant, Instant, boolean, Duration)} correctly constructs the {@link
   * WaveformQcChannelSohStatus} from the input parameters by checking the results match the
   * expectations.
   *
   * @param startTimes status start times provided to builder
   * @param endTimes status end times provided to builder
   * @param statuses status values provided to builder
   * @param expectedStartTimes status start times expected in the built WaveformQcChannelSohStatus
   * @param expectedEndTimes status end times expected in the built WaveformQcChannelSohStatus
   * @param expectedStatusStates status valuesexpected in the built WaveformQcChannelSohStatus
   */
  private void verifyBuilder(Instant[] startTimes, Instant[] endTimes, boolean[] statuses,
      Instant[] expectedStartTimes, Instant[] expectedEndTimes,
      StatusState[] expectedStatusStates) {

    Builder builder = WaveformQcChannelSohStatus
        .builder(expectedProcessingChannelId, acquiredChannelSohType, startTimes[0], endTimes[0],
            statuses[0], threshold);

    for (int i = 1; i < startTimes.length; ++i) {
      builder.addStatusChange(startTimes[i], endTimes[i], statuses[i]);
    }

    WaveformQcChannelSohStatus status = builder.build();

    assertEquals(expectedStatusStates.length, status.getStatusChanges().count());
    assertArrayEquals(expectedStartTimes,
        status.getStatusChanges().map(Status::getStartTime).toArray());
    assertArrayEquals(expectedEndTimes,
        status.getStatusChanges().map(Status::getEndTime).toArray());
    assertArrayEquals(expectedStatusStates,
        status.getStatusChanges().map(Status::getStatus).toArray());
  }

  /**
   * Tests the builder is resilient to duplicate entries (same time and status value)
   */
  @Test
  public void testBuilderAddDuplicateStatus() throws Exception {
    Builder builder = WaveformQcChannelSohStatus
        .builder(expectedProcessingChannelId, acquiredChannelSohType,
            expectedStartTime, expectedEndTime, initialStatus, threshold);

    builder.addStatusChange(expectedStartTime, expectedEndTime, true);
    WaveformQcChannelSohStatus status = builder.build();

    assertEquals(1, status.getStatusChanges().count());

    Optional<Status> actualStatus = status.getStatusChanges().findFirst();
    assertTrue(actualStatus.isPresent());
    assertEquals(expectedStartTime, actualStatus.get().getStartTime());
    assertEquals(expectedEndTime, actualStatus.get().getEndTime());
    assertEquals(expectedInitialStatus, actualStatus.get().getStatus());
  }

  /**
   * Tests the builder rejects entries with the same time but different values
   */
  @Test
  public void testBuilderAddDuplicateTimeExpectIllegalArgumentException() throws Exception {
    Builder builder = WaveformQcChannelSohStatus
        .builder(expectedProcessingChannelId, acquiredChannelSohType,
            expectedStartTime, expectedEndTime, initialStatus, threshold);

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("WaveformQcChannelSohStatus cannot have two Status for the same time");
    builder.addStatusChange(expectedStartTime, expectedEndTime, false);
    builder.build();
  }

  @Test
  public void testBuilderNullProcessingChannelIdExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformQcChannelSohStatus cannot have a null ProcessingChannelId");
    WaveformQcChannelSohStatus
        .builder(null, acquiredChannelSohType, expectedStartTime, expectedEndTime,
            initialStatus, threshold);
  }

  @Test
  public void testBuilderNullAcquiredChannelSohTypeExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformQcChannelSohStatus cannot have a null AcquiredChannelSohType");
    WaveformQcChannelSohStatus
        .builder(expectedProcessingChannelId, null, expectedStartTime, expectedEndTime,
            initialStatus, threshold);
  }

  @Test
  public void testBuilderNullQcMaskTypeExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformQcChannelSohStatus cannot have a null QcMaskType");
    WaveformQcChannelSohStatus
        .builder(expectedProcessingChannelId, null, expectedChannelSohSubtype,
            expectedStartTime, expectedEndTime, initialStatus, threshold);
  }

  @Test
  public void testBuilderNullChannelSohSubTypeExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformQcChannelSohStatus cannot have a null ChannelSohSubtype");
    WaveformQcChannelSohStatus
        .builder(expectedProcessingChannelId, expectedQcMaskType, null,
            expectedStartTime, expectedEndTime, initialStatus, threshold);
  }

  @Test
  public void testBuilderNullInitialTimeExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformQcChannelSohStatus cannot have a null initial startTime");
    WaveformQcChannelSohStatus
        .builder(expectedProcessingChannelId, acquiredChannelSohType, null, expectedEndTime,
            initialStatus, threshold);
  }

  @Test
  public void testBuilderNullStatusEndTimeExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformQcChannelSohStatus cannot have a null initial endTime");
    WaveformQcChannelSohStatus
        .builder(expectedProcessingChannelId, expectedQcMaskType, expectedChannelSohSubtype,
            expectedStartTime, null, initialStatus, threshold);
  }

  @Test
  public void testBuilderNullAdjacentThresholdExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformQcChannelSohStatus cannot have a null adjacentThreshold");
    WaveformQcChannelSohStatus
        .builder(expectedProcessingChannelId, expectedQcMaskType, expectedChannelSohSubtype,
            expectedStartTime, expectedEndTime, initialStatus, null);
  }

  @Test
  public void testBuilderAddStatusChangeNullStartTimeExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception
        .expectMessage("WaveformQcChannelSohStatus cannot have a null status change startTime");
    Builder builder = WaveformQcChannelSohStatus
        .builder(expectedProcessingChannelId, acquiredChannelSohType,
            expectedStartTime, expectedEndTime,
            initialStatus, threshold);
    builder.addStatusChange(null, expectedEndTime, false);
  }

  @Test
  public void testBuilderAddStatusChangeNullEndTimeExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformQcChannelSohStatus cannot have a null status change endTime");
    Builder builder = WaveformQcChannelSohStatus
        .builder(expectedProcessingChannelId, acquiredChannelSohType,
            expectedStartTime, expectedEndTime,
            initialStatus, threshold);
    builder.addStatusChange(expectedStartTime, null, false);
  }

  @Test
  public void testEqualsHashCode() throws Exception {
    Builder builder = WaveformQcChannelSohStatus
        .builder(expectedProcessingChannelId, acquiredChannelSohType,
            expectedStartTime, expectedEndTime, initialStatus, threshold);

    final WaveformQcChannelSohStatus status1 = builder.build();
    final WaveformQcChannelSohStatus status2 = builder.build();

    assertTrue(status1.equals(status2));
    assertTrue(status1.hashCode() == status2.hashCode());
  }

  @Test
  public void testStatusCreate() throws Exception {
    Status status = Status.create(expectedStartTime, expectedEndTime, expectedInitialStatus);

    assertEquals(expectedStartTime, status.getStartTime());
    assertEquals(expectedEndTime, status.getEndTime());
    assertEquals(expectedInitialStatus, status.getStatus());
  }

  @Test
  public void testStatusCreateNullStartTimeExpectNullArgumentException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Status cannot have a null startTime");
    Status.create(null, expectedEndTime, expectedInitialStatus);
  }

  @Test
  public void testStatusCreateNullEndTimeExpectNullArgumentException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Status cannot have a null endTime");
    Status.create(expectedStartTime, null, expectedInitialStatus);
  }

  @Test
  public void testStatusCreateNullStatusExpectNullArgumentException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Status cannot have a null status");
    Status.create(expectedStartTime, expectedEndTime, null);
  }

  @Test
  public void testStatusCreateEndTimeBeforeStartTimeExpectIllegalArgumentException()
      throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Status cannot have endTime before startTime");
    Status.create(expectedEndTime, expectedStartTime, expectedInitialStatus);
  }

  @Test
  public void testFrom() throws Exception {
    List<Status> statusChanges = Arrays.asList(
        Status.create(Instant.ofEpochSecond(0), Instant.ofEpochSecond(10), StatusState.SET),
        Status.create(Instant.ofEpochSecond(10), Instant.ofEpochSecond(20), StatusState.MISSING),
        Status.create(Instant.ofEpochSecond(20), Instant.ofEpochSecond(30), StatusState.UNSET));

    WaveformQcChannelSohStatus status = WaveformQcChannelSohStatus
        .from(expectedProcessingChannelId, expectedQcMaskType, expectedChannelSohSubtype,
            statusChanges);

    assertEquals(expectedProcessingChannelId, status.getProcessingChannelId());
    assertEquals(expectedQcMaskType, status.getQcMaskType());
    assertEquals(expectedChannelSohSubtype, status.getChannelSohSubtype());
    assertArrayEquals(statusChanges.toArray(), status.getStatusChanges().toArray());
  }

  @Test
  public void testFromNullProcessingChannelIdExpectNullArgumentException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformQcChannelSohStatus cannot have a null processingChannelId");
    WaveformQcChannelSohStatus
        .from(null, QcMaskType.REPAIRABLE_GAP, ChannelSohSubtype.DEAD_SENSOR_CHANNEL,
            Collections.emptyList());
  }

  @Test
  public void testFromNullQcMaskTypeExpectNullArgumentException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformQcChannelSohStatus cannot have a null qcMaskType");
    WaveformQcChannelSohStatus.from(new UUID(0L, 0L), null, ChannelSohSubtype.DEAD_SENSOR_CHANNEL,
        Collections.emptyList());
  }

  @Test
  public void testFromNullChannelSohSubtypeExpectNullArgumentException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformQcChannelSohStatus cannot have a null channelSohSubtype");
    WaveformQcChannelSohStatus
        .from(new UUID(0L, 0L), QcMaskType.REPAIRABLE_GAP, null, Collections.emptyList());
  }

  @Test
  public void testFromNullStatusChangesExpectNullArgumentException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("WaveformQcChannelSohStatus cannot have null statuses");
    WaveformQcChannelSohStatus
        .from(new UUID(0L, 0L), QcMaskType.REPAIRABLE_GAP, ChannelSohSubtype.DEAD_SENSOR_CHANNEL,
            null);
  }

  @Test
  public void testFromEmptyStatusChangesExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("WaveformQcChannelSohStatus cannot have empty statuses");
    WaveformQcChannelSohStatus
        .from(new UUID(0L, 0L), QcMaskType.REPAIRABLE_GAP, ChannelSohSubtype.DEAD_SENSOR_CHANNEL,
            Collections.emptyList());
  }

  @Test
  public void testFromStatusesOverlapExpectIllegalArgumentException() throws Exception {
    List<Status> statusChanges = Arrays.asList(
        Status.create(Instant.ofEpochSecond(0), Instant.ofEpochSecond(10), StatusState.SET),
        Status.create(Instant.ofEpochSecond(9), Instant.ofEpochSecond(20), StatusState.MISSING));

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("WaveformQcChannelSohStatus cannot have overlapping statuses");
    WaveformQcChannelSohStatus
        .from(expectedProcessingChannelId, expectedQcMaskType, expectedChannelSohSubtype,
            statusChanges);
  }

  @Test
  public void testFromStatusesOutOfOrderExpectIllegalArgumentException() throws Exception {
    List<Status> statusChanges = Arrays.asList(
        Status.create(Instant.ofEpochSecond(10), Instant.ofEpochSecond(20), StatusState.MISSING),
        Status.create(Instant.ofEpochSecond(0), Instant.ofEpochSecond(10), StatusState.SET));

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("WaveformQcChannelSohStatus cannot have out of order statuses");
    WaveformQcChannelSohStatus
        .from(expectedProcessingChannelId, expectedQcMaskType, expectedChannelSohSubtype,
            statusChanges);
  }

  @Test
  public void testFromAdjacentEqualStatusesExpectIllegalArgumentException() throws Exception {
    List<Status> statusChanges = Arrays.asList(
        Status.create(Instant.ofEpochSecond(0), Instant.ofEpochSecond(10), StatusState.SET),
        Status.create(Instant.ofEpochSecond(10), Instant.ofEpochSecond(20), StatusState.SET));

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("WaveformQcChannelSohStatus cannot have adjacent equal StatusStates");
    WaveformQcChannelSohStatus
        .from(expectedProcessingChannelId, expectedQcMaskType, expectedChannelSohSubtype,
            statusChanges);
  }

  @Test
  public void testEqualsExpectInequality() throws Exception {

    final UUID procChanId = UUID.fromString("b38ae749-2833-4197-a8cb-4609ddd4342f");
    final AcquiredChannelSohType acquiredSohType = AcquiredChannelSohType.CLIPPED;
    final Instant firstTime = Instant.now();
    final Instant endTime = firstTime.plusSeconds(10);

    final WaveformQcChannelSohStatus status1 = WaveformQcChannelSohStatus
        .builder(procChanId, acquiredSohType, endTime, firstTime, true, threshold).build();

    // Different processing chanel ids
    final UUID procChanId2 = UUID
        .fromString("5d3e7012-2133-4674-a3a0-cee0ea873986");
    WaveformQcChannelSohStatus status2 = WaveformQcChannelSohStatus
        .builder(procChanId2, acquiredSohType, endTime, firstTime, true, threshold).build();

    assertFalse(status1.equals(status2));

    // Different AcquiredChannelSohType
    final AcquiredChannelSohType acquiredSohType2 = AcquiredChannelSohType.DEAD_SENSOR_CHANNEL;
    status2 = WaveformQcChannelSohStatus
        .builder(procChanId, acquiredSohType2, endTime, firstTime, true, threshold).build();

    assertFalse(status1.equals(status2));

    // Different end status time
    final Instant endTime2 = firstTime.plusSeconds(20);
    status2 = WaveformQcChannelSohStatus
        .builder(procChanId, acquiredSohType, endTime2, firstTime, true, threshold).build();

    assertFalse(status1.equals(status2));

    // Different first status time
    final Instant firstTime2 = firstTime.plusSeconds(1);
    status2 = WaveformQcChannelSohStatus
        .builder(procChanId, acquiredSohType, endTime, firstTime2, true, threshold).build();

    assertFalse(status1.equals(status2));

    // Different initial status
    status2 = WaveformQcChannelSohStatus
        .builder(procChanId, acquiredSohType, endTime, firstTime, false, threshold).build();

    assertFalse(status1.equals(status2));
  }
}

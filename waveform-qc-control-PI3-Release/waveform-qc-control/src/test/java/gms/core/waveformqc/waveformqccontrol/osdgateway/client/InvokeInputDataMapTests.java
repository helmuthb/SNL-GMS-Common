package gms.core.waveformqc.waveformqccontrol.osdgateway.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.core.waveformqc.waveformqccontrol.objects.ChannelSohSubtype;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus.Builder;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcParameters;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

//import gms.core.waveformqc.waveformqccontrol.plugin.WaveformQcPlugin;

/**
 * Tests for {@link InvokeInputDataMap}
 */
public class InvokeInputDataMapTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private UUID knownProcChanId;
  private UUID unknownProcChanId;

  private Set<QcMask> qcMasks = new HashSet<>();
  private Set<WaveformQcChannelSohStatus> waveformQcChannelSohStatuses = new HashSet<>();
  private Set<WaveformQcParameters> waveformQcParameters = new HashSet<>();

  @Before
  public void setUp() throws Exception {
    knownProcChanId = UUID.randomUUID();
    unknownProcChanId = UUID.randomUUID();

    qcMasks = setUpQcMasks(knownProcChanId);
    waveformQcChannelSohStatuses = setUpChannelSohStatuses(knownProcChanId);
    waveformQcParameters = setUpChannelConfigurations(knownProcChanId);
  }

  @After
  public void tearDown() throws Exception {
    knownProcChanId = null;
    unknownProcChanId = null;
    qcMasks = null;
    waveformQcChannelSohStatuses = null;
  }

  @Test
  public void testCreate() throws Exception {

    InvokeInputDataMap inputData = InvokeInputDataMap
        .create(Collections.emptySet(), qcMasks, waveformQcChannelSohStatuses);

    // TODO: add ChannelSegments once the object is ready for use and once it is required by QC
    Optional<Set<ChannelSegment>> actualChanSegs = inputData.getChannelSegments(knownProcChanId);
    assertNotNull(actualChanSegs);
    assertFalse(actualChanSegs.isPresent());

    // Verify the correct QcMasks can be retrieved from the inputData
    Optional<Set<QcMask>> optionalQcMasks = inputData.getQcMasks(knownProcChanId);
    assertNotNull(optionalQcMasks);
    assertTrue(optionalQcMasks.isPresent());

    Set<QcMask> actualQcMasks = optionalQcMasks.get();

    assertEquals(1, actualQcMasks.size());
    assertTrue(actualQcMasks.containsAll(qcMasks));

    // Verify the correct WaveformQcChannelSohStatus can be retrieved from the inputData
    assertFalse(inputData.getWaveformQcChannelSohStatuses(unknownProcChanId).isPresent());

    Optional<Set<WaveformQcChannelSohStatus>> optionalSohStatuses = inputData
        .getWaveformQcChannelSohStatuses(knownProcChanId);
    assertTrue(optionalSohStatuses.isPresent());

    Set<WaveformQcChannelSohStatus> actualSohStatuses = optionalSohStatuses.get();
    assertEquals(2, actualSohStatuses.size());
    assertTrue(actualSohStatuses.containsAll(waveformQcChannelSohStatuses));
  }

  @Test
  public void testCreateNullChannelSegmentsExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("InvokeInputDataMap cannot accept null ChannelSegments");
    InvokeInputDataMap.create(null, new HashSet<>(), new HashSet<>());
  }

  @Test
  public void testCreateNullQcMasksExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("InvokeInputDataMap cannot accept null QcMasks");
    InvokeInputDataMap.create(new HashSet<>(), null, new HashSet<>());
  }

  @Test
  public void testCreateNullWaveformQcChannelSohStatusesExpectNullPointerException()
      throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("InvokeInputDataMap cannot accept null WaveformQcChannelSohStatuses");
    InvokeInputDataMap.create(new HashSet<>(), new HashSet<>(), null);
  }

  @Test
  public void testGetQcMasksNullIdExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Cannot get QcMasks from InvokeInputDataMap for a null ProcessingChannel identifier");
    InvokeInputDataMap inputData = InvokeInputDataMap
        .create(new HashSet<>(), new HashSet<>(), new HashSet<>());
    inputData.getQcMasks(null);
  }

  @Test
  public void testGetChannelSegmentsNullIdExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Cannot get ChannelSegments from InvokeInputDataMap for a null ProcessingChannel identifier");
    InvokeInputDataMap inputData = InvokeInputDataMap
        .create(new HashSet<>(), new HashSet<>(), new HashSet<>());
    inputData.getChannelSegments(null);
  }

  @Test
  public void testGetWaveformQcChannelSohStatusesNullIdExpectNullPointerException()
      throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Cannot get WaveformQcChannelSohStatuses from InvokeInputDataMap for a null ProcessingChannel identifier");
    InvokeInputDataMap inputData = InvokeInputDataMap
        .create(new HashSet<>(), new HashSet<>(), new HashSet<>());
    inputData.getWaveformQcChannelSohStatuses(null);
  }

  @Test
  public void testEqualsHashCode() throws Exception {
    InvokeInputDataMap inputData1 = InvokeInputDataMap
        .create(Collections.emptySet(), qcMasks, waveformQcChannelSohStatuses);

    InvokeInputDataMap inputData2 = InvokeInputDataMap
        .create(Collections.emptySet(), qcMasks, waveformQcChannelSohStatuses);

    assertTrue(inputData1.equals(inputData2));
    assertTrue(inputData1.hashCode() == inputData2.hashCode());
  }

  @Test
  public void testEqualsExpectInequality() throws Exception {

    // TODO: add ChannelSegments to this test once the object is ready for use and required by QC

    final InvokeInputDataMap inputData1 = InvokeInputDataMap
        .create(Collections.emptySet(), qcMasks, waveformQcChannelSohStatuses);

    // Different qcMasks
    InvokeInputDataMap inputData2 = InvokeInputDataMap
        .create(Collections.emptySet(), Collections.emptySet(), waveformQcChannelSohStatuses);
    assertFalse(inputData1.equals(inputData2));

    // Different waveformQcChannelSohStatuses
    inputData2 = InvokeInputDataMap
        .create(Collections.emptySet(), qcMasks, Collections.emptySet());
    assertFalse(inputData1.equals(inputData2));

  }

  private static Set<QcMask> setUpQcMasks(UUID knownProcChanId) {
    final List<QcMaskVersionReference> parentReferences = Collections.emptyList();
    final List<UUID> channelSegmentIdList = Arrays
        .asList(UUID.randomUUID(), UUID.randomUUID());
    final QcMaskType qcMaskType = QcMaskType.CALIBRATION;
    final QcMaskCategory qcMaskCategory = QcMaskCategory.STATION_SOH;
    final String rationale = "Rationale";
    final Instant startTime = Instant.parse("2007-12-03T10:15:30.00Z");
    final Instant endTime = Instant.parse("2007-12-03T11:15:30.00Z");
    final UUID creationInfoId = UUID.randomUUID();

    QcMask qcMask = QcMask.create(knownProcChanId, parentReferences,
        channelSegmentIdList, qcMaskCategory, qcMaskType,
        rationale, startTime, endTime, creationInfoId);

    qcMask
        .addQcMaskVersion(channelSegmentIdList, qcMaskCategory, qcMaskType,
            "Rationale SPIKE",
            Instant.parse("2007-12-03T10:35:30.00Z"),
            Instant.parse("2007-12-03T11:45:30.00Z"),
            UUID.randomUUID());

    return Collections.singleton(qcMask);
  }

  private static Set<WaveformQcChannelSohStatus> setUpChannelSohStatuses(
      UUID knownProcChanId) {

    final int frameSecs = 10;
    final int framesPerMinute = 60 / frameSecs;
    final int numFrames = 5 * framesPerMinute; // 5 minutes
    final QcMaskType qcMaskType1 = QcMaskType.SENSOR_PROBLEM;
    final QcMaskType qcMaskType2 = QcMaskType.CALIBRATION;
    final ChannelSohSubtype channelSohSubtype1 = ChannelSohSubtype.CLIPPED;
    final ChannelSohSubtype channelSohSubtype2 = ChannelSohSubtype.CALIBRATION_UNDERWAY;
    final Instant initialTime = Instant.now();

    // Create WaveformQcChannelSohStatus that switches state every minute.  The two builders
    // have opposite status.
    Builder builder1 = WaveformQcChannelSohStatus
        .builder(knownProcChanId, qcMaskType1, channelSohSubtype1, initialTime,
            initialTime.plusSeconds(frameSecs), true, Duration.ofSeconds(5));
    Builder builder2 = WaveformQcChannelSohStatus
        .builder(knownProcChanId, qcMaskType2, channelSohSubtype2, initialTime,
            initialTime.plusSeconds(frameSecs), true, Duration.ofSeconds(5));

    boolean curStatus = true;
    for (int i = 1; i < numFrames; ++i) {
      if ((i % framesPerMinute) == 0) {
        curStatus = !curStatus;
      }

      final Instant startTime = initialTime.plusSeconds(frameSecs * i);
      final Instant endTime = initialTime.plusSeconds(frameSecs * (i + 1)).minusMillis(25);

      builder1.addStatusChange(startTime, endTime, curStatus);
      builder2.addStatusChange(startTime, endTime, !curStatus);
    }

    return Stream.of(builder1.build(), builder2.build()).collect(Collectors.toSet());
  }

  private static Set<WaveformQcParameters> setUpChannelConfigurations(
      UUID knownProcChanId) {
    WaveformQcParameters waveformQcParameters = WaveformQcParameters
        .create(knownProcChanId, Collections.emptyList());
    return Stream.of(waveformQcParameters).collect(Collectors.toSet());
  }
}

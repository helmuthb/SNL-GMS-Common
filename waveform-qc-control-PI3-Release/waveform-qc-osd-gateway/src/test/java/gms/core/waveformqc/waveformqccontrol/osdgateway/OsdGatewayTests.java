package gms.core.waveformqc.waveformqccontrol.osdgateway;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import gms.core.waveformqc.waveformqccontrol.objects.ChannelSohSubtype;
import gms.core.waveformqc.waveformqccontrol.objects.InvokeInputData;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus.Status;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus.StatusState;
import gms.core.waveformqc.waveformqccontrol.osdgateway.gateway.OsdGateway;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.ProvenanceRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.QcMaskRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.ChannelSegmentType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.StationSohRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepositoryInterface;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests the {@link OsdGateway} of the {@link
 * OsdGateway} These tests will eventually be replaced since the operations are
 * currently placeholders to build out the Waveform QC Control class.
 */
@RunWith(MockitoJUnitRunner.class)
public class OsdGatewayTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private WaveformRepositoryInterface waveformRepository;

  @Mock
  private QcMaskRepository qcMaskRepository;

  @Mock
  private StationSohRepositoryInterface sohRepository;

  @Mock
  private ProvenanceRepository provenanceRepository;

  private OsdGateway osd;

  @Before
  public void setUp() {
    osd = new OsdGateway(waveformRepository, sohRepository, qcMaskRepository, provenanceRepository);
  }

  @After
  public void tearDown() {
    osd = null;
  }

  @Test
  public void testLoadInvokeInputData() throws Exception {
    UUID processingChannelId = UUID.randomUUID();
    Instant startTime = Instant.parse("2017-09-19T07:45:00.00Z");
    Instant endTime = Instant.parse("2017-09-19T07:50:00.00Z");

    given(sohRepository.retrieveBooleanSohByProcessingChannelAndTimeRange(processingChannelId,
        startTime, endTime)).willReturn(
        Collections.singletonList(AcquiredChannelSohBoolean.create(processingChannelId,
            AcquiredChannelSohType.VAULT_DOOR_OPENED, startTime, endTime, true,
            CreationInfo.DEFAULT)));

    final ChannelSegment mockSegment = ChannelSegment
        .create(processingChannelId, "ChanName", ChannelSegmentType.ACQUIRED, startTime,
            endTime, new TreeSet<>(), CreationInfo.DEFAULT);
    given(waveformRepository.retrieveChannelSegment(processingChannelId, startTime, endTime, true))
        .willReturn(Optional.ofNullable(mockSegment));

    InvokeInputData input = osd
        .loadInvokeInputData(Collections.singleton(processingChannelId), startTime, endTime);
    assertNotNull(input);

    Set<WaveformQcChannelSohStatus> statuses = input
        .waveformQcChannelSohStatuses().collect(Collectors.toSet());

    assertEquals(1, statuses.size());

    WaveformQcChannelSohStatus status = statuses.iterator().next();

    assertEquals(processingChannelId, status.getProcessingChannelId());
    assertEquals(QcMaskType.STATION_SECURITY, status.getQcMaskType());
    assertEquals(ChannelSohSubtype.VAULT_DOOR_OPENED, status.getChannelSohSubtype());

    assertEquals(1, status.getStatusChanges().count());

    Optional<Status> optionalStatusValue = status.getStatusChanges().findFirst();
    assertTrue(optionalStatusValue.isPresent());
    Status statusValue = optionalStatusValue.get();

    assertEquals(StatusState.SET, statusValue.getStatus());
    assertEquals(startTime, statusValue.getStartTime());
    assertEquals(endTime, statusValue.getEndTime());

    assertEquals(1, input.getChannelSegments().size());
    ChannelSegment segment = input.channelSegments().findFirst().get();
    assertEquals(mockSegment, segment);
  }

  @Test
  public void testLoadInvokeInputDataEmptyChannelSegment() throws Exception {
    // This test guards against a regression.  There was a bug where
    // waveformRepository.retrieveChannelSegment returned null.
    UUID processingChannelId = UUID.randomUUID();
    Instant startTime = Instant.parse("2017-09-19T07:45:00.00Z");
    Instant endTime = Instant.parse("2017-09-19T07:50:00.00Z");

    given(sohRepository.retrieveBooleanSohByProcessingChannelAndTimeRange(processingChannelId,
        startTime, endTime)).willReturn(
        Collections.singletonList(AcquiredChannelSohBoolean.create(processingChannelId,
            AcquiredChannelSohType.VAULT_DOOR_OPENED, startTime, endTime, true,
            CreationInfo.DEFAULT)));

    ChannelSegment
        .create(processingChannelId, "ChanName", ChannelSegmentType.ACQUIRED, startTime, endTime,
            new TreeSet<>(), CreationInfo.DEFAULT);
    given(waveformRepository.retrieveChannelSegment(processingChannelId, startTime, endTime, true))
        .willReturn(Optional.ofNullable(null));

    InvokeInputData input = osd
        .loadInvokeInputData(Collections.singleton(processingChannelId), startTime, endTime);
    assertNotNull(input);
    assertNotNull(input.getChannelSegments());
    assertEquals(0, input.getChannelSegments().size());
  }

  @Test
  public void testLoadInvokeInputDataExceptionExpectEmpty() throws Exception {

    UUID processingChannelId = UUID.randomUUID();
    Instant startTime = Instant.parse("2017-09-19T07:45:00.00Z");
    Instant endTime = Instant.parse("2017-09-19T07:50:00.00Z");

    given(sohRepository.retrieveBooleanSohByProcessingChannelAndTimeRange(processingChannelId,
        startTime, endTime)).willThrow(Exception.class);

    given(waveformRepository.retrieveChannelSegment(processingChannelId, startTime, endTime, true))
        .willThrow(Exception.class);

    InvokeInputData invokeInputData = osd.loadInvokeInputData(Collections.singleton(processingChannelId), startTime, endTime);
    assertEquals(InvokeInputData.create(Set.of(), Set.of(), Set.of()), invokeInputData);
  }

  @Test
  public void testLoadInvokeInputDataNullProcessingChannelIds() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Error loading InvokeInputData, Processing Channel Ids cannot be null");
    osd.loadInvokeInputData(null, Instant.MIN, Instant.MAX);
  }

  @Test
  public void testLoadInvokeInputDataNullStartTime() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Error loading InvokeInputData, Start Time cannot be null");
    osd.loadInvokeInputData(Collections.emptySet(), null, Instant.MAX);
  }

  @Test
  public void testLoadInvokeInputDataNullEndTime() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Error loading InvokeInputData, End Time cannot be null");
    osd.loadInvokeInputData(Collections.emptySet(), Instant.MIN, null);
  }


  @Test
  public void testStorePrivateContext() {
    Instant startTime = Instant.parse("2017-09-19T07:45:00.00Z");
    Instant endTime = Instant.parse("2017-09-19T07:50:00.00Z");

    osd.store(Collections.singletonList(QcMask.create(new UUID(0L, 0L), Collections.emptyList(),
        Collections.emptyList(), QcMaskCategory.STATION_SOH, QcMaskType.STATION_SECURITY, "",
        startTime, endTime, new UUID(0L, 0L))),
        Collections.emptyList(),
        StorageVisibility.PRIVATE);

    verify(qcMaskRepository, times(1)).store(any());
  }

  @Test
  public void testStorePublicContext() {
    Instant startTime = Instant.parse("2017-09-19T07:45:00.00Z");
    Instant endTime = Instant.parse("2017-09-19T07:50:00.00Z");

    osd.store(Collections.singletonList(QcMask.create(new UUID(0L, 0L), Collections.emptyList(),
        Collections.emptyList(), QcMaskCategory.STATION_SOH, QcMaskType.STATION_SECURITY, "",
        startTime, endTime, new UUID(0L, 0L))),
        Collections.emptyList(),
        StorageVisibility.PUBLIC);

    verify(qcMaskRepository, times(1)).store(any());
  }

  @Test
  public void testStoreEmptyPrivateContext() {
    osd.store(Collections.emptyList(), Collections.emptyList(), StorageVisibility.PRIVATE);
    verify(qcMaskRepository, never()).store(any());
  }

  @Test
  public void testStoreEmptyPublicContext() {
    osd.store(Collections.emptyList(), Collections.emptyList(), StorageVisibility.PUBLIC);
    verify(qcMaskRepository, never()).store(any());
  }

  @Test
  public void testConstructNullWaveformRepositoryExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "OsdGateway requires a non-null WaveformRepositoryInterface");
    new OsdGateway(null, sohRepository, qcMaskRepository,
        provenanceRepository);
  }

  @Test
  public void testConstructNullStationSohRepositoryExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "OsdGateway requires a non-null StationSohPersistenceInterface");
    new OsdGateway(waveformRepository, null, qcMaskRepository,
        provenanceRepository);
  }

  @Test
  public void testConstructNullQcMaskRepositoryExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "OsdGateway requires a non-null QcMaskRepository");
    new OsdGateway(waveformRepository, sohRepository, null,
        provenanceRepository);
  }

  @Test
  public void testConstructNullProvenanceRepositoryExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "OsdGateway requires a non-null ProvenanceRepository");
    new OsdGateway(waveformRepository, sohRepository,
        qcMaskRepository, null);
  }

  @Test
  public void testStoreNullQcMasksExpectIllegalArgumentException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("QC Masks cannot be null");
    osd.store(null, Collections.emptyList(), StorageVisibility.PRIVATE);
  }

  @Test
  public void testStoreNullCreationInfosExpectIllegalArgumentException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Creation Information cannot be null");
    osd.store(Collections.emptyList(), null, StorageVisibility.PRIVATE);
  }

  @Test
  public void testStoreNullStorageVisibilityExpectIllegalArgumentException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Storage Visibility cannot be null");
    osd.store(Collections.emptyList(), Collections.emptyList(), null);
  }
}

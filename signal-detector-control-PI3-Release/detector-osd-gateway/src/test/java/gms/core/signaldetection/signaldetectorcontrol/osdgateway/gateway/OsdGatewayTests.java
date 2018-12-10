package gms.core.signaldetection.signaldetectorcontrol.osdgateway.gateway;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.SignalDetectionRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.ChannelSegmentType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepositoryInterface;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests the {@link OsdGateway} of the {@link OsdGateway} These tests will eventually be replaced
 * since the operations are currently placeholders to build out the Signal Detector Control class.
 */
@RunWith(MockitoJUnitRunner.class)
public class OsdGatewayTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private WaveformRepositoryInterface mockWaveformRepository;

  @Mock
  private SignalDetectionRepository mockSignalDetectionRepository;

  private OsdGateway osd;

  @Before
  public void setUp() {
    osd = OsdGateway.create(mockWaveformRepository, mockSignalDetectionRepository);
  }

  @After
  public void tearDown() {
    osd = null;
  }

  @Test
  public void testLoadInvokeInputData() throws Exception{
    final Instant startTime = Instant.parse("2017-09-19T07:45:00.00Z");
    final Instant endTime = Instant.parse("2017-09-19T07:50:00.00Z");

    final UUID channelIdA = UUID.randomUUID();
    final UUID channelIdB = UUID.randomUUID();
    final List<UUID> channelIds = List.of(channelIdA, channelIdB);
    final ChannelSegment mockSegmentA = createMockChannelSegment(channelIdA, startTime, endTime);
    final ChannelSegment mockSegmentB = createMockChannelSegment(channelIdB, startTime, endTime);

    given(mockWaveformRepository.retrieveChannelSegment(channelIdA, startTime, endTime, true))
        .willReturn(Optional.of(mockSegmentA));
    given(mockWaveformRepository.retrieveChannelSegment(channelIdB, startTime, endTime, true))
        .willReturn(Optional.of(mockSegmentB));

    Collection<ChannelSegment> input = osd.loadInvokeInputData(channelIds, startTime, endTime);
    assertNotNull(input);

    assertEquals(2, input.size());
    assertTrue(input.containsAll(List.of(mockSegmentA, mockSegmentB)));
  }

  private ChannelSegment createMockChannelSegment(UUID channelIdA, Instant startTime,
      Instant endTime) {
    return ChannelSegment
        .create(channelIdA, "mockSegment", ChannelSegmentType.ACQUIRED, startTime,
            endTime, new TreeSet<>(), CreationInfo.DEFAULT);
  }

  @Test
  public void testLoadInvokeInputDataNullChannelIdExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception
        .expectMessage("Error loading InvokeInputData, channelIds cannot be null");
    osd.loadInvokeInputData(null, Instant.MIN, Instant.MAX);
  }

  @Test
  public void testLoadInvokeInputDataNullStartTime() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Error loading InvokeInputData, Start Time cannot be null");
    osd.loadInvokeInputData(List.of(UUID.randomUUID()), null, Instant.MAX);
  }

  @Test
  public void testLoadInvokeInputDataNullEndTime() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Error loading InvokeInputData, End Time cannot be null");
    osd.loadInvokeInputData(List.of(UUID.randomUUID()), Instant.MIN, null);
  }

  @Test
  public void testStoreNumberOfInvocations() {
    Instant startTime = Instant.parse("2017-09-19T07:45:00.00Z");
    Instant endTime = Instant.parse("2017-09-19T07:50:00.00Z");
    double samplesPerSec = 40.0;

    int numSamples = (int) (Duration.between(startTime, endTime).getSeconds() * samplesPerSec) + 1;
    double[] values = new double[numSamples];
    Arrays.fill(values, 1.0);

    Waveform waveform = Waveform.create(startTime, endTime, samplesPerSec, numSamples, values);

    osd.store(Collections.singletonList(SignalDetection.create("monitoringOrganization",
        UUID.randomUUID(), "phase",
        List.of(FeatureMeasurement.create(FeatureMeasurementType.ARRIVAL_TIME,
            1.0, UUID.randomUUID())),
        UUID.randomUUID())), StorageVisibility.PUBLIC);
    verify(mockSignalDetectionRepository, times(1)).store(any());
  }

  @Test
  public void testCreate() {
    OsdGateway gateway = OsdGateway.create(mock(WaveformRepositoryInterface.class),
        mock(SignalDetectionRepository.class));
    assertNotNull(gateway);
  }

  @Test
  public void testCreateNullWaveformRepositoryExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "OsdGateway requires a non-null WaveformRepository");
    OsdGateway.create(null, mock(SignalDetectionRepository.class));
  }

  @Test
  public void testCreateNullSignalDetectionRepositoryExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "OsdGateway requires a non-null SignalDetectionRepository");
    OsdGateway.create(mock(WaveformRepositoryInterface.class), null);
  }

  @Test
  public void testStoreNullChannelSegmentsExpectIllegalArgumentException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Signal Detections cannot be null");
    osd.store(null, StorageVisibility.PUBLIC);
  }

  @Test
  public void testStoreNullStorageVisibilityExpectIllegalArgumentException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Storage Visibility cannot be null");
    osd.store(List.of(SignalDetection.create("monitoringOrganization",
        UUID.randomUUID(), "phase",
        List.of(FeatureMeasurement.create(FeatureMeasurementType.ARRIVAL_TIME,
            1.0, UUID.randomUUID())),
        UUID.randomUUID())), null);
  }
}

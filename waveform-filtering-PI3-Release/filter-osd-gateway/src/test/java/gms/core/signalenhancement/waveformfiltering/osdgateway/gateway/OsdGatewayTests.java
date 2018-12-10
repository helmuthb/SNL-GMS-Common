package gms.core.signalenhancement.waveformfiltering.osdgateway.gateway;

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
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
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
 * since the operations are currently placeholders to build out the Filter Control class.
 */
@RunWith(MockitoJUnitRunner.class)
public class OsdGatewayTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private WaveformRepositoryInterface mockWaveformRepository;

  private OsdGateway osd;

  @Before
  public void setUp() {
    osd = OsdGateway.create(mockWaveformRepository);
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

    // TODO: mock returned ChannelSegments when we implement the loadInvokeInputData operation
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
  public void testLoadInvokeInputDataNullChannelProcessingGrouplId() {
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
  public void testStoreNumberOfInvocations() throws Exception{
    Instant startTime = Instant.parse("2017-09-19T07:45:00.00Z");
    Instant endTime = Instant.parse("2017-09-19T07:50:00.00Z");
    double samplesPerSec = 40.0;

    int numSamples = (int) (Duration.between(startTime, endTime).getSeconds() * samplesPerSec) + 1;
    double[] values = new double[numSamples];
    Arrays.fill(values, 1.0);

    Waveform waveform = Waveform.create(startTime, endTime, samplesPerSec, numSamples, values);

    osd.store(Collections.singletonList(ChannelSegment.create(new UUID(0L, 0L), "Test Channel",
        ChannelSegmentType.FILTER, startTime, endTime, new TreeSet<>(List.of(waveform)),
        new CreationInfo("test",
            Instant.now(), new SoftwareComponentInfo("test", "test")))), StorageVisibility.PUBLIC);

    verify(mockWaveformRepository, times(1)).storeChannelSegment(any());
  }

  @Test
  public void testCreate() {
    OsdGateway gateway = OsdGateway.create(mock(WaveformRepositoryInterface.class));
    assertNotNull(gateway);
  }

  @Test
  public void testCreateNullWaveformRepositoryExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "OsdGateway requires a non-null WaveformRepositoryInterface");
    OsdGateway.create(null);
  }

  @Test
  public void testStoreNullChannelSegmentsExpectIllegalArgumentException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Channel Segments cannot be null");
    osd.store(null, StorageVisibility.PUBLIC);
  }
}

package gms.core.waveformqc.waveformqccontrol.osdgateway.objects;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.core.waveformqc.waveformqccontrol.objects.InvokeInputData;
import gms.core.waveformqc.waveformqccontrol.objects.PluginVersion;
import gms.core.waveformqc.waveformqccontrol.objects.RegistrationInfo;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus.Builder;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcParameters;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests for the {@link InvokeInputData} class
 */
public class InvokeInputDataTests {

  private ChannelSegment channelSegment;
  private WaveformQcChannelSohStatus waveformQcChannelSohStatus;
  private QcMask qcMask;
  private WaveformQcParameters waveformQcParameters;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    final double sampleRate = 100.0;
    final int sampleCount = 5;
    final Instant start = Instant.parse("1996-01-02T03:04:00Z");
    final Instant middle = start.plusMillis(50);
    final Instant end = middle.plusMillis(50);

    final Waveform waveform1 = Waveform.create(
        start, middle, sampleRate, sampleCount, new double[]{1.1, 2.2, 3.3, 4.4, 5.5});

    final Waveform waveform2 = Waveform.create(
        middle, end, sampleRate, sampleCount, new double[]{6.6, 7.7, 8.8, 9.9, 10.10});

    final SortedSet<Waveform> waveforms = new TreeSet<Waveform>() {{
      add(waveform1);
      add(waveform2);
    }};

    channelSegment = ChannelSegment.create(
        UUID.randomUUID(),
        "segmentName",
        ChannelSegment.ChannelSegmentType.RAW,
        start, end, waveforms,
        new CreationInfo("test", Instant.now(), new SoftwareComponentInfo("test", "test")));

    final Instant initialStatus = Instant.now();
    final Instant endTime = initialStatus.plusSeconds(10);
    final Duration threshold = Duration.ofSeconds(5);
    Builder builder = WaveformQcChannelSohStatus
        .builder(UUID.randomUUID(), AcquiredChannelSohType.CLIPPED, endTime,
            initialStatus, true, threshold);

    waveformQcChannelSohStatus = builder.build();

    List<UUID> channelSegmentIdList = Arrays
        .asList(UUID.randomUUID(), UUID.randomUUID());

    qcMask = QcMask
        .create(UUID.randomUUID(), Collections.emptyList(), channelSegmentIdList,
            QcMaskCategory.STATION_SOH, QcMaskType.CALIBRATION, "Rationale",
            Instant.parse("2007-12-03T10:15:30.00Z"),
            Instant.parse("2007-12-03T11:15:30.00Z"), UUID.randomUUID());

    waveformQcParameters = WaveformQcParameters.create(UUID.randomUUID(),
        Collections.singletonList(RegistrationInfo.from("mock", PluginVersion.from(1, 0, 0))));
  }

  @Test
  public void testCreate() throws Exception {
    final InvokeInputData data = InvokeInputData.create(
        Collections.singleton(channelSegment),
        Collections.singleton(qcMask),
        Collections.singleton(waveformQcChannelSohStatus));

    assertNotNull(data);

    assertEquals(1, data.getChannelSegments().size());
    assertTrue(data.getChannelSegments().contains(channelSegment));

    assertEquals(1, data.getQcMasks().size());
    assertTrue(data.getQcMasks().contains(qcMask));

    assertEquals(1, data.getWaveformQcChannelSohStatuses().size());
    assertTrue(data.getWaveformQcChannelSohStatuses().contains(waveformQcChannelSohStatus));
  }

  @Test
  public void testEqualsHashCode() throws Exception {
    InvokeInputData data1 = InvokeInputData.create(
        Collections.singleton(channelSegment),
        Collections.singleton(qcMask),
        Collections.singleton(waveformQcChannelSohStatus));

    InvokeInputData data2 = InvokeInputData.create(
        Collections.singleton(channelSegment),
        Collections.singleton(qcMask),
        Collections.singleton(waveformQcChannelSohStatus));

    assertTrue(data1.equals(data2));
    assertTrue(data1.hashCode() == data2.hashCode());
  }

  @Test
  public void testEqualsExpectInequality() throws Exception {
    final InvokeInputData data1 = InvokeInputData.create(
        Collections.singleton(channelSegment),
        Collections.singleton(qcMask),
        Collections.singleton(waveformQcChannelSohStatus));

    // Different channelSegment set
    InvokeInputData data2 = InvokeInputData
        .create(Collections.emptySet(), Collections.singleton(qcMask),
            Collections.singleton(waveformQcChannelSohStatus));
    assertFalse(data1.equals(data2));

    // Different qcMask set
    data2 = InvokeInputData.create(Collections.singleton(channelSegment), Collections.emptySet(),
        Collections.singleton(waveformQcChannelSohStatus));
    assertFalse(data1.equals(data2));

    // Different waveformQcChannelSohStatus set
    data2 = InvokeInputData
        .create(Collections.singleton(channelSegment), Collections.singleton(qcMask),
            Collections.emptySet());
    assertFalse(data1.equals(data2));
  }

  @Test
  public void testCreateNullChannelSegmentsExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("InvokeInputData cannot accept null ChannelSegments");
    InvokeInputData.create(
        null,
        Collections.singleton(qcMask),
        Collections.singleton(waveformQcChannelSohStatus));
  }

  @Test
  public void testCreateNullQcMasksExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("InvokeInputData cannot accept null QcMasks");
    InvokeInputData.create(
        Collections.singleton(channelSegment),
        null,
        Collections.singleton(waveformQcChannelSohStatus));
  }

  @Test
  public void testCreateNullWaveformQcChannelSohStatusExpectNullPointerException()
      throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("InvokeInputData cannot accept null WaveformQcChannelSohStatuses");
    InvokeInputData.create(
        Collections.singleton(channelSegment),
        Collections.singleton(qcMask),
        null);
  }
}

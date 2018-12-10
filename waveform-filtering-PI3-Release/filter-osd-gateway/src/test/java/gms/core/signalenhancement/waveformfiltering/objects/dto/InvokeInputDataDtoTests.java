package gms.core.signalenhancement.waveformfiltering.objects.dto;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.Before;

public class InvokeInputDataDtoTests {

  private ChannelSegment channelSegment;

  @Before
  public void setUp() {
    final double sampleRate = 100.0;
    final int sampleCount = 5;
    final Instant start = Instant.parse("1996-01-02T03:04:00Z");
    final Instant middle = start.plusMillis(50);
    final Instant end = middle.plusMillis(50);

    final Waveform waveform1 = Waveform.create(
        start, middle, sampleRate, sampleCount, new double[]{1.1, 2.2, 3.3, 4.4, 5.5});

    final Waveform waveform2 = Waveform.create(
        middle, end, sampleRate, sampleCount, new double[]{6.6, 7.7, 8.8, 9.9, 10.10});

    final SortedSet<Waveform> waveforms = new TreeSet<>();
    waveforms.add(waveform1);
    waveforms.add(waveform2);

    channelSegment = ChannelSegment.create(
        UUID.randomUUID(),
        "segmentName",
        ChannelSegment.ChannelSegmentType.RAW,
        start, end, waveforms,
        new CreationInfo("test", Instant.now(), new SoftwareComponentInfo("test", "test")));
  }
}
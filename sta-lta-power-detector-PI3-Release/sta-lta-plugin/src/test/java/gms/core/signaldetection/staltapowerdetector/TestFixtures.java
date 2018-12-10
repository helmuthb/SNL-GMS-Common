package gms.core.signaldetection.staltapowerdetector;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.IntStream;

public class TestFixtures {

  public static ChannelSegment randomChannelSegment() {
    final Waveform wf1 = Waveform.withInferredEndTime(Instant.EPOCH, 2, 20, randoms(20));

    return ChannelSegment.from(UUID.randomUUID(), UUID.randomUUID(), "ChannelName",
        ChannelSegment.ChannelSegmentType.RAW, wf1.getStartTime(), wf1.getEndTime(),
        new TreeSet<>(List.of(wf1)), CreationInfo.DEFAULT);
  }

  private static double[] randoms(int length) {
    double[] random = new double[length];
    IntStream.range(0, length).forEach(i -> random[i] = Math.random());
    return random;
  }
}

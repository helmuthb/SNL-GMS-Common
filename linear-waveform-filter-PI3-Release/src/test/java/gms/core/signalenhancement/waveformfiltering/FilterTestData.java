package gms.core.signalenhancement.waveformfiltering;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterCausality;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterPassBandType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.ChannelSegmentType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

public class FilterTestData {

  public final static double[] FORWARD_COEFFS = new double[]{5.5, 4.4, 3.3, 2.2, 1.1, -6.6};

  public final static FilterDefinition FIR_FILTER_DEF = FilterDefinition
      .createFir("libTest", "libTestDesc", FilterType.FIR_HAMMING, FilterPassBandType.BAND_PASS,
          1.0, 3.0, 4, FilterSource.USER, FilterCausality.CAUSAL, false, 20.0, 1.0,
          FORWARD_COEFFS, 3.0);

  private final static SortedSet<Waveform> WAVEFORMS = new TreeSet<Waveform>();
  private final static SortedSet<Waveform> WAVEFORMS2 = new TreeSet<Waveform>();
  private final static double[] DATA = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0};

  static {
    WAVEFORMS.add(
        Waveform.withInferredEndTime(Instant.EPOCH, 20.0, DATA.length, DATA));

    WAVEFORMS2.add(
        Waveform.withInferredEndTime(Instant.EPOCH, 20.0, DATA.length, DATA));
    WAVEFORMS2.add(
        Waveform
            .withInferredEndTime(WAVEFORMS2.first().getEndTime().plusMillis((long) (1000.0 / 20.0)),
                20.0, DATA.length, DATA));
  }

  public final static ChannelSegment CHANNEL_SEGMENT = ChannelSegment.create(UUID.randomUUID(),
      "TEST", ChannelSegmentType.FILTER,
      Instant.EPOCH, WAVEFORMS.first().getEndTime(), WAVEFORMS, CreationInfo.DEFAULT);

  public final static ChannelSegment CHANNEL_SEGMENT2 = ChannelSegment.create(UUID.randomUUID(),
      "TEST2", ChannelSegmentType.FILTER,
      Instant.EPOCH, WAVEFORMS2.last().getEndTime(), WAVEFORMS2, CreationInfo.DEFAULT);
}

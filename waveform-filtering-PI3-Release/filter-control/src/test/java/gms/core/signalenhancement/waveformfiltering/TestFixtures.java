package gms.core.signalenhancement.waveformfiltering;

import gms.core.signalenhancement.waveformfiltering.http.ClaimCheckDto;
import gms.core.signalenhancement.waveformfiltering.http.StreamingDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterCausality;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterPassBandType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Objects used in filter control HTTP testing
 */
public class TestFixtures {

  public static StreamingDto getStreamingDto() {
    StreamingDto dto = new StreamingDto();

    // build a dual list representation of a map (keys and values) to initialize the dto
    List<ChannelSegment> dtoMapChannelSegmentKeys = List
        .of(randomChannelSegment(), randomChannelSegment());
    List<UUID> dtoMapChannelIdValues = List.of(UUID.randomUUID(), UUID.randomUUID());

    dto.setMapChannelSegmentKeys(dtoMapChannelSegmentKeys);
    dto.setMapChannelIdValues(dtoMapChannelIdValues);
    dto.setFilterDefinition(getFilterDefinition());
    dto.setProcessingContext(getProcessingContext());

    return dto;
  }

  public static ClaimCheckDto getClaimCheckDto() {
    ClaimCheckDto dto = new ClaimCheckDto();
    dto.setInputToOutputChannelIds(
        Map.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
    dto.setChannelProcessingStepId(UUID.randomUUID());
    dto.setStartTime(Instant.EPOCH);
    dto.setEndTime(Instant.EPOCH.plusSeconds(1000));
    dto.setProcessingContext(getProcessingContext());

    return dto;
  }

  public static ChannelSegment randomChannelSegment() {
    final Waveform wf1 = Waveform.withInferredEndTime(Instant.EPOCH, 2, 20, randoms(20));

    return ChannelSegment.from(UUID.randomUUID(), UUID.randomUUID(), "ChannelName",
        ChannelSegment.ChannelSegmentType.RAW, wf1.getStartTime(), wf1.getEndTime(),
        new TreeSet<>(List.of(wf1)), CreationInfo.DEFAULT);
  }

  public static ChannelSegment channelSegmentFromWaveforms(List<Waveform> waveforms) {
    return ChannelSegment.from(UUID.randomUUID(), UUID.randomUUID(), "ChannelName",
        ChannelSegment.ChannelSegmentType.RAW, waveforms.get(0).getStartTime(),
        waveforms.get(waveforms.size() - 1).getEndTime(),
        new TreeSet<>(waveforms), CreationInfo.DEFAULT);
  }

  private static double[] randoms(int length) {
    double[] random = new double[length];
    IntStream.range(0, length).forEach(i -> random[i] = Math.random());
    return random;
  }

  public static FilterDefinition getFilterDefinition() {
    return FilterDefinition
        .createFir("filter name string", "filter description", FilterType.FIR_HAMMING,
            FilterPassBandType.BAND_PASS, 12.34, 99.99, 12, FilterSource.USER,
            FilterCausality.CAUSAL, false, 42.42, 24.24, new double[]{-12.34, 57.89, 64.0},
            4687.3574);
  }

  public static ProcessingContext getProcessingContext() {
    return ProcessingContext
        .createAutomatic(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            StorageVisibility.PRIVATE);
  }

  public static Waveform createWaveform(Instant start, Instant end, double samplesPerSec) {
    if (0 != Duration.between(start, end).getNano()) {
      throw new IllegalArgumentException(
          "Test can't create waveform where the sample rate does not evenly divide the duration");
    }

    int numSamples = (int) (Duration.between(start, end).getSeconds() * samplesPerSec) + 1;
    double[] values = new double[numSamples];
    Arrays.fill(values, 1.0);

    return Waveform.create(start, end, samplesPerSec, numSamples, values);
  }

}

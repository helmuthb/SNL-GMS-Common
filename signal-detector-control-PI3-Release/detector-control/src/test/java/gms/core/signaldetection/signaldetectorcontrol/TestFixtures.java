package gms.core.signaldetection.signaldetectorcontrol;

import gms.core.signaldetection.signaldetectorcontrol.http.ClaimCheckDto;
import gms.core.signaldetection.signaldetectorcontrol.http.StreamingDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Objects used in Signal Detector Control HTTP testing
 */
public class TestFixtures {

  public static StreamingDto getStreamingDto() {
    StreamingDto dto = new StreamingDto();

    dto.setChannelSegment(TestFixtures.randomChannelSegment());
    dto.setStartTime(Instant.EPOCH);
    dto.setEndTime(Instant.EPOCH.plusSeconds(100));
    //TODO: to be included when SignalDetectorParameters is implemented
    //dto.setSignalDetectorParameters(getSignalDetectorParameters());
    dto.setProcessingContext(getProcessingContext());

    return dto;
  }

  public static ClaimCheckDto getClaimCheckDto() {
    ClaimCheckDto dto = new ClaimCheckDto();

    dto.setStationId(UUID.randomUUID());
    dto.setStartTime(Instant.EPOCH);
    dto.setEndTime(Instant.EPOCH.plusSeconds(100));
    dto.setProcessingContext(getProcessingContext());

    return dto;
  }

  public static ChannelSegment randomChannelSegment() {
    final Waveform wf1 = Waveform.withInferredEndTime(Instant.EPOCH, 2, 20, randoms(20));

    return ChannelSegment.from(UUID.randomUUID(), UUID.randomUUID(), "ChannelName",
        ChannelSegment.ChannelSegmentType.RAW, wf1.getStartTime(), wf1.getEndTime(),
        new TreeSet<>(List.of(wf1)), CreationInfo.DEFAULT);
  }

  public static SignalDetection randomSignalDetection() {
    return SignalDetection.create("monitoringOrganization",
      UUID.randomUUID(), "phase",
      List.of(FeatureMeasurement.create(FeatureMeasurementType.ARRIVAL_TIME,
      1.0, UUID.randomUUID())),
      UUID.randomUUID());
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

  public static ProcessingContext getProcessingContext() {
    return ProcessingContext
        .createAutomatic(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            StorageVisibility.PRIVATE);
  }

  //TODO: Fix SignalDetectorParameters create() once SignalDetectorParameters is implemented
  //public static SignalDetectorParameters getSignalDetectorParameters() {
  //  return SignalDetectorParameters
  //      .create(XXX);
  //}

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

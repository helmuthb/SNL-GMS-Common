package gms.core.signaldetection.staltapowerdetector;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps algorithm logic to perform sta/lta power detection on waveforms
 */
public class StaLtaPowerDetectorPlugin {

  private static final Logger logger = LoggerFactory.getLogger(StaLtaPowerDetectorPlugin.class);

  private final Algorithm staLta;

  private final StaLtaConfiguration configuration;

  private StaLtaPowerDetectorPlugin(Algorithm staLtaAlgorithm,
      StaLtaConfiguration staLtaConfiguration) {

    this.staLta = staLtaAlgorithm;
    configuration = staLtaConfiguration;
  }

  /**
   * Obtains a new {@link StaLtaPowerDetectorPlugin} which delegates detection logic to the
   * {@link Algorithm}
   *
   * @param staLtaAlgorithm {@link Algorithm} containing the STA/LTA implementation, not null
   * @param staLtaConfiguration {@link StaLtaConfiguration} configuration for STA/LTA, not null
   * @return StaLtaPowerDetectorPlugin, not null
   * @throws NullPointerException if staLtaAlgorithm is null; if staLtaConfiguration is null
   */
  public static StaLtaPowerDetectorPlugin create(Algorithm staLtaAlgorithm,
      StaLtaConfiguration staLtaConfiguration) {

    Objects.requireNonNull(staLtaAlgorithm, "STA/LTA algorithm cannot be null");
    Objects.requireNonNull(staLtaConfiguration, "STA/LTA configuration cannot be null");

    return new StaLtaPowerDetectorPlugin(staLtaAlgorithm, staLtaConfiguration);
  }

  /**
   * Executes the STA/LTA signal detector {@link Algorithm} on the {@link ChannelSegment}.  Uses
   * parameters obtained from the {@link StaLtaPowerDetectorPlugin#configuration}
   *
   * @param channelSegment {@link ChannelSegment} to process for STA/LTA detections, not null
   * @return Set of {@link Instant} signal detection times, not null
   */
  public Set<Instant> detect(ChannelSegment channelSegment) {

    logger.info("Executing STA/LTA signal detection on ChannelSegment: {}", channelSegment);

    Objects.requireNonNull(channelSegment, "STA/LTA plugin cannot process null channelSegment");

    final StaLtaParameters parameters = configuration.createParameters(channelSegment.getId());

    // Assuming each Waveform has a sample rate within a small delta of a nominal sample rate,
    // just use the first Waveform's sample rate to convert from time to sample counts
    final double presumedNominalSampleRate = channelSegment.getWaveforms().stream()
        .mapToDouble(Waveform::getSampleRate).findFirst().orElse(0.0);
    final double maxInterpolatedGapSampleCount = fractionalSamplesFromDuration(
        presumedNominalSampleRate, parameters.getLtaLength());

    // Condition channelSegment
    final ChannelSegment conditioned = channelSegment
        .interpolateWaveformGap(parameters.getInterpolateGapsSampleRateTolerance(),
            maxInterpolatedGapSampleCount)
        .mergeWaveforms(parameters.getMergeWaveformsSampleRateTolerance(),
            fractionalSamplesFromDuration(presumedNominalSampleRate,
                parameters.getMergeWaveformsMinLength()));

    // Waveform conditioning merges waveforms where psosible, so process each waveform independently
    final Set<Instant> triggerTimes = new HashSet<>();

    conditioned.getWaveforms().forEach(wf -> {

      // Use sampleRate to convert the STA and LTA window parameters from Durations to sample counts
      final double samplesPerSec = wf.getSampleRate();
      final int staLead = samplesFromDuration(samplesPerSec, parameters.getStaLead());
      final int staLength = samplesFromDuration(samplesPerSec, parameters.getStaLength());
      final int ltaLead = samplesFromDuration(samplesPerSec, parameters.getLtaLead());
      final int ltaLength = samplesFromDuration(samplesPerSec, parameters.getLtaLength());

      // Invoke STA/LTA and convert trigger indices to trigger times
      final Set<Integer> triggerIndices = this.staLta
          .staLta(parameters.getAlgorithmType(), parameters.getWaveformTransformation(),
              staLead, staLength, ltaLead, ltaLength, parameters.getTriggerThreshold(),
              parameters.getDetriggerThreshold(), wf.getValues());

      triggerIndices.stream().map(wf::timeForSample).forEach(triggerTimes::add);
    });

    return triggerTimes;
  }

  /**
   * Computes the closest integer number of samples occurring at samplesPerSec required to span the
   * duration.
   *
   * @param samplesPerSec sample rate
   * @param duration a {@link Duration}
   * @return integer number of samples
   */
  private static int samplesFromDuration(double samplesPerSec, Duration duration) {
    return (int) Math.round(fractionalSamplesFromDuration(samplesPerSec, duration));
  }

  /**
   * Computes the number of samples (including fractions of samples) occurring at samplesPerSec
   * required to span the duration.
   *
   * @param samplesPerSec sample rate
   * @param duration a {@link Duration}
   * @return double number of samples
   */
  private static double fractionalSamplesFromDuration(double samplesPerSec, Duration duration) {
    final double secondsPart = samplesPerSec * duration.getSeconds();
    final double nanosPart = samplesPerSec * duration.getNano() / 1.0e9;
    return secondsPart + nanosPart;
  }
}

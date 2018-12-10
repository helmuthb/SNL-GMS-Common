package gms.core.signaldetection.staltapowerdetector;

import gms.core.signaldetection.staltapowerdetector.Algorithm.AlgorithmType;
import gms.core.signaldetection.staltapowerdetector.Algorithm.WaveformTransformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.util.Objects;

/**
 * Parameters used in a call to the STA/LTA {@link Algorithm}
 */
public class StaLtaParameters {

  final AlgorithmType algorithmType;
  final WaveformTransformation waveformTransformation;
  final Duration staLead;
  final Duration staLength;
  final Duration ltaLead;
  final Duration ltaLength;
  final double triggerThreshold;
  final double detriggerThreshold;
  final double interpolateGapsSampleRateTolerance;
  final double mergeWaveformsSampleRateTolerance;
  final Duration mergeWaveformsMinLength;

  private StaLtaParameters(AlgorithmType algorithmType,
      WaveformTransformation waveformTransformation, Duration staLead, Duration staLength,
      Duration ltaLead, Duration ltaLength, double triggerThreshold, double detriggerThreshold,
      double interpolateGapsSampleRateTolerance, double mergeWaveformsSampleRateTolerance,
      Duration mergeWaveformsMinLength) {

    this.algorithmType = algorithmType;
    this.waveformTransformation = waveformTransformation;
    this.staLead = staLead;
    this.staLength = staLength;
    this.ltaLead = ltaLead;
    this.ltaLength = ltaLength;
    this.triggerThreshold = triggerThreshold;
    this.detriggerThreshold = detriggerThreshold;
    this.interpolateGapsSampleRateTolerance = interpolateGapsSampleRateTolerance;
    this.mergeWaveformsSampleRateTolerance = mergeWaveformsSampleRateTolerance;
    this.mergeWaveformsMinLength = mergeWaveformsMinLength;
  }

  /**
   * Obtains a new {@link StaLtaParameters} from the provided values.
   *
   * @param algorithmType an {@link AlgorithmType}, not null
   * @param waveformTransformation an {@link WaveformTransformation}, not null
   * @param staLead {@link Duration} the STA window leads the transformed sample, not null
   * @param staLength {@link Duration} of the STA window, > 0, not null
   * @param ltaLead {@link Duration} of the LTA window leads the transformed sample, not null
   * @param ltaLength {@link Duration} of the LTA window, > 0, not null
   * @param triggerThreshold minimum waveform value not causing a trigger, > 0
   * @param detriggerThreshold maximum waveform value not causing a detrigger, > 0
   * @param interpolateGapsSampleRateTolerance before STA/LTA, fill gaps within a {@link
   * ChannelSegment} of duration < ltaLength if the {@link Waveform}s on each side of the gap have a
   * {@link Waveform#getSampleRate()} difference of less than this value
   * @param mergeWaveformsSampleRateTolerance before STA/LTA, merge {@link Waveform}s within a
   * {@link ChannelSegment} if their gap is < 1 sample and if the {@link Waveform}s on each side of
   * the gap have a {@link Waveform#getSampleRate()} difference of less than this value
   * @param mergeWaveformsMinLength minimum {@link Duration} of a gap to merge, exclusive, not null
   * @return {@link StaLtaParameters, not null}
   * @throws NullPointerException if algorithmType, waveformTransformation, staLead, staLength,
   * ltaLead, ltaLength, or mergeWaveformsMinLength are null
   * @throws IllegalArgumentException if staLength, ltaLength, triggerThreshold, or
   * detriggerThreshold are <= 0
   */
  public static StaLtaParameters create(AlgorithmType algorithmType,
      WaveformTransformation waveformTransformation, Duration staLead, Duration staLength,
      Duration ltaLead, Duration ltaLength, double triggerThreshold, double detriggerThreshold,
      double interpolateGapsSampleRateTolerance, double mergeWaveformsSampleRateTolerance,
      Duration mergeWaveformsMinLength) {

    Objects.requireNonNull(algorithmType, "StaLtaParameters cannot have a null algorithmType");
    Objects.requireNonNull(waveformTransformation,
        "StaLtaParameters cannot have a null waveformTransformation");
    Objects.requireNonNull(staLead, "StaLtaParameters cannot have a null staLead");
    Objects.requireNonNull(staLength, "StaLtaParameters cannot have a null staLength");
    Objects.requireNonNull(ltaLead, "StaLtaParameters cannot have a null ltaLead");
    Objects.requireNonNull(ltaLength, "StaLtaParameters cannot have a null ltaLength");
    Objects.requireNonNull(mergeWaveformsMinLength,
        "StaLtaParameters cannot have a null mergeWaveformsMinLength");
    ParameterValidation.validateWindowLengths(staLength, ltaLength, d -> !d.isNegative());
    ParameterValidation.validateTriggerThresholds(triggerThreshold, detriggerThreshold);

    return new StaLtaParameters(algorithmType, waveformTransformation, staLead, staLength, ltaLead,
        ltaLength, triggerThreshold, detriggerThreshold, interpolateGapsSampleRateTolerance,
        mergeWaveformsSampleRateTolerance, mergeWaveformsMinLength);
  }

  /**
   * Obtains the {@link AlgorithmType}
   *
   * @return {@link AlgorithmType}, not null
   */
  public AlgorithmType getAlgorithmType() {
    return algorithmType;
  }

  /**
   * Obtains the {@link WaveformTransformation}
   *
   * @return {@link WaveformTransformation}, not null
   */
  public WaveformTransformation getWaveformTransformation() {
    return waveformTransformation;
  }

  /**
   * Obtains the {@link Duration} the STA window leads the transformed sample
   *
   * @return {@link Duration}, not null
   */
  public Duration getStaLead() {
    return staLead;
  }

  /**
   * Obtains the {@link Duration} of the STA window, > 0
   *
   * @return {@link Duration}, not null
   */
  public Duration getStaLength() {
    return staLength;
  }

  /**
   * Obtains the {@link Duration} the LTA window leads the transformed sample
   *
   * @return {@link Duration}, not null
   */
  public Duration getLtaLead() {
    return ltaLead;
  }

  /**
   * Obtains the {@link Duration} of the LTA window, > 0
   *
   * @return {@link Duration}, not null
   */
  public Duration getLtaLength() {
    return ltaLength;
  }

  /**
   * Obtains the minimum waveform value not causing a trigger, > 0
   *
   * @return trigger threshold as a double
   */
  public double getTriggerThreshold() {
    return triggerThreshold;
  }

  /**
   * Before STA/LTA, fill gaps within a {@link ChannelSegment} of duration < ltaLength if the {@link
   * Waveform}s on each side of the gap have a{@link Waveform#getSampleRate()} difference of less
   * than this value
   *
   * @return interpolateGapsSampleRateTolerance as a double
   */
  public double getInterpolateGapsSampleRateTolerance() {
    return interpolateGapsSampleRateTolerance;
  }

  /**
   * Before STA/LTA merge {@link Waveform}s within a {@link ChannelSegment} if their gap is < 1
   * sample and if the {@link Waveform}s on each side of the gap
   *
   * @return mergeWaveformsSampleRateTolerance as a double
   */
  public double getMergeWaveformsSampleRateTolerance() {
    return mergeWaveformsSampleRateTolerance;
  }

  /**
   * Obtains the minimum {@link Duration} (exclusive) of a merged waveform gap.
   *
   * @return {@link Duration}, not null
   */
  public Duration getMergeWaveformsMinLength() {
    return mergeWaveformsMinLength;
  }

  /**
   * Obtains the maximum waveform value not causing a detrigger, > 0
   *
   * @return detrigger threshold as a double
   */
  public double getDetriggerThreshold() {
    return detriggerThreshold;
  }

  @Override
  public String toString() {
    return "StaLtaParameters{" +
        "algorithmType=" + algorithmType +
        ", waveformTransformation=" + waveformTransformation +
        ", staLead=" + staLead +
        ", staLength=" + staLength +
        ", ltaLead=" + ltaLead +
        ", ltaLength=" + ltaLength +
        ", triggerThreshold=" + triggerThreshold +
        ", detriggerThreshold=" + detriggerThreshold +
        ", interpolateGapsSampleRateTolerance=" + interpolateGapsSampleRateTolerance +
        ", mergeWaveformsSampleRateTolerance=" + mergeWaveformsSampleRateTolerance +
        ", mergeWaveformsMinLength=" + mergeWaveformsMinLength +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    StaLtaParameters that = (StaLtaParameters) o;

    return Double.compare(that.triggerThreshold, triggerThreshold) == 0
        && Double.compare(that.detriggerThreshold, detriggerThreshold) == 0 &&
        Double.compare(that.interpolateGapsSampleRateTolerance, interpolateGapsSampleRateTolerance)
            == 0
        && Double.compare(that.mergeWaveformsSampleRateTolerance, mergeWaveformsSampleRateTolerance)
        == 0 && algorithmType == that.algorithmType
        && waveformTransformation == that.waveformTransformation && staLead.equals(that.staLead)
        && staLength.equals(that.staLength) && ltaLead.equals(that.ltaLead) && ltaLength
        .equals(that.ltaLength) && mergeWaveformsMinLength.equals(that.mergeWaveformsMinLength);
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = algorithmType.hashCode();
    result = 31 * result + waveformTransformation.hashCode();
    result = 31 * result + staLead.hashCode();
    result = 31 * result + staLength.hashCode();
    result = 31 * result + ltaLead.hashCode();
    result = 31 * result + ltaLength.hashCode();
    temp = Double.doubleToLongBits(triggerThreshold);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(detriggerThreshold);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(interpolateGapsSampleRateTolerance);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(mergeWaveformsSampleRateTolerance);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + mergeWaveformsMinLength.hashCode();
    return result;
  }
}

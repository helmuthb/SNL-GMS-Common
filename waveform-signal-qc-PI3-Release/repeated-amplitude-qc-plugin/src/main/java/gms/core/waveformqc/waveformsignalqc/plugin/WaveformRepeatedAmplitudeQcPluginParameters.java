package gms.core.waveformqc.waveformsignalqc.plugin;

/**
 * Parameters for a particular invocation of the {@link WaveformRepeatedAmplitudeQcPlugin}
 */
public class WaveformRepeatedAmplitudeQcPluginParameters {

  private final int minSeriesLengthInSamples;

  private final double maxDeltaFromStartAmplitude;

  private final double maskMergeThresholdSeconds;

  /**
   * Obtains a {@link WaveformRepeatedAmplitudeQcPluginParameters} from the provided minimum
   * repeated amplitude series length and the maximum sample deviation from the series' initial
   * amplitude
   *
   * @param minSeriesLengthInSamples minimum number of samples in a repeated adjacent amplitude
   * values mask, > 1
   * @param maxDeltaFromStartAmplitude maximum deviation a sample may have from the repeated
   * adjacent amplitude series' initial amplitude value to still be considered a repeated value, >=
   * 0.0
   * @param maskMergeThresholdSeconds exclusive duration (i.e. time difference between masks must be
   * less than this duration) between two repeated adjacent amplitude value masks that should be
   * merged into a single mask, >= 0.0
   * @return WaveformRepeatedAmplitudeQcPluginParameters, not null
   * @throws IllegalArgumentException if minSeriesLengthInSamples is <= 1;
   * maxDeltaFromStartAmplitude is < 0.0; or maskMergeThresholdSeconds is < 0.0
   */
  public static WaveformRepeatedAmplitudeQcPluginParameters create(int minSeriesLengthInSamples,
      double maxDeltaFromStartAmplitude, double maskMergeThresholdSeconds) {

    if (minSeriesLengthInSamples <= 1) {
      throw new IllegalArgumentException(
          "WaveformRepeatedAmplitudeQcPluginParameters requires minSeriesLengthInSamples >= 2");
    }

    if (maxDeltaFromStartAmplitude < 0.0) {
      throw new IllegalArgumentException(
          "WaveformRepeatedAmplitudeQcPluginParameters requires maxDeltaFromStartAmplitude >= 0.0");
    }

    if (maskMergeThresholdSeconds < 0.0) {
      throw new IllegalArgumentException(
          "WaveformRepeatedAmplitudeQcPluginParameters requires maskMergeThresholdSeconds >= 0.0");
    }

    return new WaveformRepeatedAmplitudeQcPluginParameters(minSeriesLengthInSamples,
        maxDeltaFromStartAmplitude, maskMergeThresholdSeconds);
  }

  private WaveformRepeatedAmplitudeQcPluginParameters(int minSeriesLengthInSamples,
      double maxDeltaFromStartAmplitude, double maskMergeThresholdSeconds) {

    this.minSeriesLengthInSamples = minSeriesLengthInSamples;
    this.maxDeltaFromStartAmplitude = maxDeltaFromStartAmplitude;
    this.maskMergeThresholdSeconds = maskMergeThresholdSeconds;
  }

  /**
   * Minimum number of samples in a repeated adjacent amplitude values mask.
   *
   * @return a positive integer
   */
  public int getMinSeriesLengthInSamples() {
    return minSeriesLengthInSamples;
  }

  /**
   * Maximum deviation a sample may have from the repeated adjacent amplitude series' initial
   * amplitude value to still be considered a repeated value
   *
   * @return a double >= 0.0
   */
  public double getMaxDeltaFromStartAmplitude() {
    return maxDeltaFromStartAmplitude;
  }

  /**
   * Exclusive duration between two repeated adjacent amplitude value QcMasks that should be merged
   * into a single mask.  Since the duration is exclusive two masks exactly this duration apart
   * will not be merged.
   *
   * @return a double >= 0.0
   */
  public double getMaskMergeThresholdSeconds() {
    return maskMergeThresholdSeconds;
  }
}

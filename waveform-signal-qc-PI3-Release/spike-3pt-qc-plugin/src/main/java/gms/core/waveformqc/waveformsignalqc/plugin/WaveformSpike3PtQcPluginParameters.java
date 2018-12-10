package gms.core.waveformqc.waveformsignalqc.plugin;

import java.util.List;

/**
 * Parameters for a particular invocation of the {@link WaveformSpike3PtQcPlugin}
 */
public class WaveformSpike3PtQcPluginParameters {

  private final double minConsecutiveSampleDifferenceSpikeThreshold;
  private final double rmsAmplitudeRatioThreshold;
  private final int rmsLeadSampleDifferences;
  private final int rmsLagSampleDifferences;

  /**
   * Obtains a {@link WaveformSpike3PtQcPluginParameters} from the provided threshold defining a
   * spike between 3 consecutive points.
   *
   * @param minConsecutiveSampleDifferenceSpikeThreshold minimum threshold for a spike, must be positive
   * @return WaveformSpike3PtQcPluginParameters, not null
   * @throws IllegalArgumentException if minConsecutiveSampleDifferenceSpikeThreshold is not positive
   */
  public static WaveformSpike3PtQcPluginParameters create(double minConsecutiveSampleDifferenceSpikeThreshold,
      double rmsAmplitudeRatioThreshold, int rmsLeadSampleDifferences, int rmsLagSampleDifferences) {

    if (minConsecutiveSampleDifferenceSpikeThreshold <= 0
        || minConsecutiveSampleDifferenceSpikeThreshold >= 1.0) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginParameters requires minConsecutiveSampleDifferenceSpikeThreshold > 0.0 and < 1.0");
    }

    if (rmsLeadSampleDifferences < 0) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginParameters requires rmsLeadSampleDifferences >= 0");
    }

    if (rmsLagSampleDifferences < 0) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginParameters requires rmsLagSampleDifferences >= 0");
    }

    if (rmsLeadSampleDifferences + rmsLagSampleDifferences < 2) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginParameters requires (rmsLeadSampleDifferences + rmsLagSampleDifferences) >= 2");
    }

    if (rmsAmplitudeRatioThreshold <= 1.0) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginParameters requires rmsAmplitudeRatioThreshold > 1.0");
    }

    return new WaveformSpike3PtQcPluginParameters(minConsecutiveSampleDifferenceSpikeThreshold,
        rmsAmplitudeRatioThreshold, rmsLeadSampleDifferences, rmsLagSampleDifferences);
  }

  private WaveformSpike3PtQcPluginParameters(double minConsecutiveSampleDifferenceSpikeThreshold,
      double rmsAmplitudeRatioThreshold, int rmsLeadSampledifferences, int rmsLagSampledifferences) {
    this.minConsecutiveSampleDifferenceSpikeThreshold = minConsecutiveSampleDifferenceSpikeThreshold;
    this.rmsAmplitudeRatioThreshold = rmsAmplitudeRatioThreshold;
    this.rmsLeadSampleDifferences = rmsLeadSampledifferences;
    this.rmsLagSampleDifferences = rmsLagSampledifferences;
  }

  /**
   * Threshold defining a spike when the ratio of minimum absolute difference of
   * adjacent samples with the maximum absolute difference is less than this threshold.
   *
   * @return a positive integer
   */
  public double getMinConsecutiveSampleDifferenceSpikeThreshold() {
    return minConsecutiveSampleDifferenceSpikeThreshold;
  }

  /**
   * Threshold defining a spike when the ratio of minimum absolute difference of
   * adjacent samples with the maximum absolute difference is less than this threshold.
   *
   * @return a positive integer
   */
  public double getRmsAmplitudeRatioThreshold() {
    return rmsAmplitudeRatioThreshold;
  }

  /**
   * Threshold defining a spike when the ratio of minimum absolute difference of
   * adjacent samples with the maximum absolute difference is less than this threshold.
   *
   * @return a positive integer
   */
  public int getRmsLeadSampleDifferences() {
    return rmsLeadSampleDifferences;
  }

  /**
   * Threshold defining a spike when the ratio of minimum absolute difference of
   * adjacent samples with the maximum absolute difference is less than this threshold.
   *
   * @return a positive integer
   */
  public int getRmsLagSampleDifferences() {
    return rmsLagSampleDifferences;
  }

}

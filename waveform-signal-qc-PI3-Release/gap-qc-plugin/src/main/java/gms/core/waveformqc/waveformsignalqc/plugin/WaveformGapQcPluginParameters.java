package gms.core.waveformqc.waveformsignalqc.plugin;

/**
 * Parameters for a particular invocation of the {@link WaveformGapQcPlugin}
 */
public class WaveformGapQcPluginParameters {

  private final int minLongGapLengthInSamples;

  /**
   * Obtains a {@link WaveformGapQcPluginParameters} from the provided threshold separating long and
   * repairable data gaps
   *
   * @param minLongGapLengthInSamples minimum length of a long gap, must be positive
   * @return WaveformGapQcPluginParameters, not null
   * @throws IllegalArgumentException if minLongGapLengthInSamples is not positive
   */
  public static WaveformGapQcPluginParameters create(int minLongGapLengthInSamples) {

    if (minLongGapLengthInSamples <= 0) {
      throw new IllegalArgumentException(
          "WaveformGapQcPluginParameters requires a positive minLongGapLengthInSamples");
    }

    return new WaveformGapQcPluginParameters(minLongGapLengthInSamples);
  }

  private WaveformGapQcPluginParameters(int minLongGapLengthInSamples) {
    this.minLongGapLengthInSamples = minLongGapLengthInSamples;
  }

  /**
   * Threshold separating long and repairable data gaps.  This is the minimum length of a long gap.
   *
   * @return a positive integer
   */
  public int getMinLongGapLengthInSamples() {
    return minLongGapLengthInSamples;
  }
}

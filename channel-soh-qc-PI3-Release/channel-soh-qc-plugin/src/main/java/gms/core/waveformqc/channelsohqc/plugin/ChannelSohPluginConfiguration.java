package gms.core.waveformqc.channelsohqc.plugin;

import gms.core.waveformqc.waveformqccontrol.objects.ChannelSohSubtype;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration to create the {@link ChannelSohPluginParameters} for {@link ChannelSohQcPlugin}.  Clients
 * provide settings for {@link QcMask}s
 * created from particular {@link AcquiredChannelSohType} literals.  PluginCOnfiguration translates
 * these settings based on the {@link ChannelSohSubtype} used by ChannelSohQcPlugin.
 *
 * Guarantees the created ChannelSohPluginParameters parameter lookup operations {@link
 * ChannelSohPluginParameters#shouldCreateQcMask(ChannelSohSubtype)} and {@link
 * ChannelSohPluginParameters#getMergeThreshold(ChannelSohSubtype)} will return a valid value for any
 * ChannelSohSubtype
 *
 * Includes default values for mergeThreshold and shouldCreateQcMask and provides a way to override
 * those values for particular AcquiredChannelSohType
 */
public class ChannelSohPluginConfiguration {

  /**
   * Default used when no shouldCreateQcMask set for an AcquiredChannelSohType literal
   */
  private final boolean shouldCreateQcMaskDefault;

  /**
   * Overrides to shouldCreateQcMaskDefault
   */
  private Map<ChannelSohSubtype, Boolean> shouldCreateQcMaskOverrideBySubtype;

  /**
   * Default used when no mergeThreshold set for an AcquiredChannelSohType literal
   */
  private final Duration mergeThresholdDefault;

  /**
   * Overrides to mergeThresholdDefault
   */
  private Map<ChannelSohSubtype, Duration> mergeThresholdOverrideBySubtype;

  /**
   * Construct an immutable ChannelSohPluginConfiguration.  Parameter validation occurs in {@link Builder}
   *
   * @param shouldCreateQcMaskDefault default shouldCreateQcMask used for {@link ChannelSohSubtype}s
   * without an overridden setting
   * @param shouldCreateQcMaskOverrideBySubtype overridden shouldCreateQcMask settings by
   * ChannelSohSubtype, not null
   * @param mergeThresholdDefault default mergeThreshold used for ChannelSohSubtypes without an
   * overridden setting, positive, not null
   * @param mergeThresholdOverrideBySubtype overridden mergeThreshold settings by ChannelSohSubtype,
   * not null
   */
  private ChannelSohPluginConfiguration(boolean shouldCreateQcMaskDefault,
      Map<ChannelSohSubtype, Boolean> shouldCreateQcMaskOverrideBySubtype,
      Duration mergeThresholdDefault,
      Map<ChannelSohSubtype, Duration> mergeThresholdOverrideBySubtype) {

    this.shouldCreateQcMaskDefault = shouldCreateQcMaskDefault;
    this.mergeThresholdDefault = mergeThresholdDefault;

    this.mergeThresholdOverrideBySubtype = Collections
        .unmodifiableMap(new HashMap<>(mergeThresholdOverrideBySubtype));
    this.shouldCreateQcMaskOverrideBySubtype = Collections
        .unmodifiableMap(new HashMap<>(shouldCreateQcMaskOverrideBySubtype));
  }

  /**
   * Obtains a {@link Builder} to construct an {@link ChannelSohPluginConfiguration}.  Accepts configuration's
   * default settings.
   *
   * @param defaultCreateQcMask default shouldCreateQcMask used for {@link ChannelSohSubtype}s
   * without an overridden setting
   * @param defaultMergeThreshold default mergeThreshold used for ChannelSohSubtypes without an
   * overridden setting, positive, not null
   * @return a Builder, not null
   */
  public static Builder builder(boolean defaultCreateQcMask, Duration defaultMergeThreshold) {
    return new Builder(defaultCreateQcMask, defaultMergeThreshold);
  }

  /**
   * Obtains {@link ChannelSohPluginParameters} from the settings in this {@link ChannelSohPluginConfiguration}
   *
   * @return immutable ChannelSohPluginParameters, not null
   */
  public ChannelSohPluginParameters createParameters() {
    ChannelSohPluginParameters.Builder paramsBuilder = ChannelSohPluginParameters.builder();

    // Set defaults for each ChannelSohSubtype
    Arrays.stream(ChannelSohSubtype.values()).forEach(t -> {
      paramsBuilder.setCreateQcMask(t, shouldCreateQcMaskDefault);
      paramsBuilder.setMergeThreshold(t, mergeThresholdDefault);
    });

    // Override defaults
    shouldCreateQcMaskOverrideBySubtype.forEach(paramsBuilder::setCreateQcMask);
    mergeThresholdOverrideBySubtype.forEach(paramsBuilder::setMergeThreshold);

    return paramsBuilder.build();
  }

  /**
   * Builder to construct {@link ChannelSohPluginConfiguration}
   */
  public static class Builder {

    /**
     * Default used when no shouldCreateQcMask set for an AcquiredChannelSohType literal
     */
    private final boolean shouldCreateQcMaskDefault;

    /**
     * Overrides to shouldCreateQcMaskDefault
     */
    private Map<ChannelSohSubtype, Boolean> shouldCreateQcMaskOverrideBySubtype;

    /**
     * Default used when no mergeThreshold set for an AcquiredChannelSohType literal
     */
    private final Duration mergeThresholdDefault;

    /**
     * Overrides to mergeThresholdDefault
     */
    private Map<ChannelSohSubtype, Duration> mergeThresholdOverrideBySubtype;

    /**
     * Constructs a Builder with default shouldCreateQcMask and mergeThreshold settings
     *
     * @param shouldCreateQcMaskDefault default shouldCreateQcMask used for {@link
     * AcquiredChannelSohType}s without an overridden setting
     * @param mergeThresholdDefault default mergeThreshold used for AcquiredChannelSohTypes without
     * an overridden setting, positive, not null
     * @throws NullPointerException if mergeThresholdDefault is null
     * @throws IllegalArgumentException if mergeThresholdDefault is negative
     */
    public Builder(boolean shouldCreateQcMaskDefault, Duration mergeThresholdDefault) {

      Objects.requireNonNull(mergeThresholdDefault,
          "ChannelSohPluginConfiguration.Builder requires non-null mergeThresholdDefault");
      if (mergeThresholdDefault.isNegative()) {
        throw new IllegalArgumentException(
            "ChannelSohPluginConfiguration.Builder requires positive mergeThresholdDefault");
      }

      this.shouldCreateQcMaskDefault = shouldCreateQcMaskDefault;
      this.mergeThresholdDefault = mergeThresholdDefault;

      this.shouldCreateQcMaskOverrideBySubtype = new HashMap<>();
      this.mergeThresholdOverrideBySubtype = new HashMap<>();
    }

    /**
     * Sets a mergeThreshold override for an {@link AcquiredChannelSohType}
     *
     * @param acquiredChannelSohType override mergeThreshold for this AcquiredChannelSohType, not
     * null
     * @param mergeThreshold override mergeThreshold setting, positive, not null
     * @throws NullPointerException if acquiredChannelSohType or mergeThreshold is negative
     * @throws IllegalArgumentException if mergeThreshold is negative
     */
    public void setMergeThreshold(AcquiredChannelSohType acquiredChannelSohType,
        Duration mergeThreshold) {

      Objects.requireNonNull(acquiredChannelSohType,
          "ChannelSohPluginConfiguration.Builder setMergeThreshold requires non-null acquiredChannelSohType");
      Objects.requireNonNull(mergeThreshold,
          "ChannelSohPluginConfiguration.Builder setMergeThreshold requires non-null mergeThreshold");
      if (mergeThreshold.isNegative()) {
        throw new IllegalArgumentException(
            "ChannelSohPluginConfiguration.Builder setMergeThreshold requires positive mergeThreshold");
      }

      this.mergeThresholdOverrideBySubtype
          .put(WaveformQcChannelSohStatus.correspondingChannelSohSubtype(acquiredChannelSohType),
              mergeThreshold);
    }

    /**
     * Sets a shouldCreateQcMask override for an {@link AcquiredChannelSohType}
     *
     * @param acquiredChannelSohType override shouldCreateQcMask for this AcquiredChannelSohType,
     * not null
     * @param shouldCreateQcMask override shouldCreateQcMask setting
     * @throws NullPointerException if acquiredChannelSohType or mergeThreshold is negative
     */
    public void setCreateQcMask(AcquiredChannelSohType acquiredChannelSohType,
        boolean shouldCreateQcMask) {

      Objects.requireNonNull(acquiredChannelSohType,
          "ChannelSohPluginConfiguration.Builder setCreateQcMask requires non-null acquiredChannelSohType");

      this.shouldCreateQcMaskOverrideBySubtype
          .put(WaveformQcChannelSohStatus.correspondingChannelSohSubtype(acquiredChannelSohType),
              shouldCreateQcMask);
    }

    /**
     * Obtains a {@link ChannelSohPluginConfiguration} from this {@link Builder}
     *
     * @return an immutable ChannelSohPluginConfiguration, not null
     */
    public ChannelSohPluginConfiguration build() {
      return new ChannelSohPluginConfiguration(
          this.shouldCreateQcMaskDefault, this.shouldCreateQcMaskOverrideBySubtype,
          this.mergeThresholdDefault, this.mergeThresholdOverrideBySubtype);
    }
  }
}

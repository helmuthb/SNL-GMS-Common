package gms.core.waveformqc.channelsohqc.plugin;

import gms.core.waveformqc.waveformqccontrol.objects.ChannelSohSubtype;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable parameters controlling how {@link ChannelSohQcPlugin} creates {@link
 * gms.shared.mechanisms.objectstoragedistribution.coi.dataobject.stationprocessing.QcMask}s.
 */
public class ChannelSohPluginParameters {

  private final Map<ChannelSohSubtype, Duration> mergeThresholdBySubtype;
  private final Map<ChannelSohSubtype, Boolean> shouldCreateQcMaskBySubtype;

  /**
   * Fully constructs the {@link ChannelSohPluginParameters} with the provided values.  Validation
   * occurs in the {@link Builder}.  Copies the provided values into new maps to make the parameters
   * immutable.
   *
   * @param mergeThresholdBySubtype maps each {@link ChannelSohSubtype} literal to a the minimum
   * {@link Duration} between two QcMasks that {@link ChannelSohQcPlugin} will not merged into a
   * single QcMask
   * @param shouldCreateQcMaskBySubtype maps each ChannelSohSubtype literal to whether the @link
   * ChannelSohQcPlugin creates QcMasks for the corresponding SOH issue
   */
  private ChannelSohPluginParameters(Map<ChannelSohSubtype, Duration> mergeThresholdBySubtype,
      Map<ChannelSohSubtype, Boolean> shouldCreateQcMaskBySubtype) {

    this.mergeThresholdBySubtype = Collections
        .unmodifiableMap(new HashMap<>(mergeThresholdBySubtype));
    this.shouldCreateQcMaskBySubtype = Collections
        .unmodifiableMap(new HashMap<>(shouldCreateQcMaskBySubtype));
  }

  /**
   * Obtains a {@link Builder} to construct a {@link ChannelSohPluginParameters}
   *
   * @return a Builder, not null
   */
  public static Builder builder() {
    return new Builder();
  }

  public boolean shouldCreateQcMask(WaveformQcChannelSohStatus channelSohStatus) {
    Objects.requireNonNull(channelSohStatus,
        "shouldCreateQcMask requires non-null channelSohStatus");

    return shouldCreateQcMaskBySubtype.get(channelSohStatus.getChannelSohSubtype());
  }

  /**
   * Obtains whether the {@link ChannelSohQcPlugin} creates {@link gms.shared.mechanisms.objectstoragedistribution.coi.dataobject.stationprocessing.QcMask}s
   * for a particular {@link ChannelSohSubtype}
   *
   * @param channelSohSubtype a ChannelSohSubtype, not null
   * @return true if QcMasks should be created for the ChannelSohSubtype and false otherwise
   * @throws NullPointerException if channelSohSubtype is null
   */
  public boolean shouldCreateQcMask(ChannelSohSubtype channelSohSubtype) {
    Objects
        .requireNonNull(channelSohSubtype,
            "shouldCreateQcMask requires non-null channelSohSubtype");
    return shouldCreateQcMaskBySubtype.get(channelSohSubtype);
  }

  /**
   * Obtains the smallest {@link Duration} between two {@link gms.shared.mechanisms.objectstoragedistribution.coi.dataobject.stationprocessing.QcMask}s
   * created from the same {@link ChannelSohSubtype} that will not be merged. Alternatively, {@link
   * ChannelSohQcPlugin} considers QcMasks closer than this Duration to be adjacent and will merge
   * them into a single mask.
   *
   * @param channelSohSubtype a ChannelSohSubtype, not null
   * @return an adjacent threshold, non-inclusive (do not merge masks separated by exactly this
   * Duration), not null
   * @throws NullPointerException if channelSohSubtype is null
   */
  public Duration getMergeThreshold(ChannelSohSubtype channelSohSubtype) {
    Objects
        .requireNonNull(channelSohSubtype, "getMergeThreshold requires non-null channelSohSubtype");
    return mergeThresholdBySubtype.get(channelSohSubtype);
  }

  /**
   * A builder constructing {@link ChannelSohPluginParameters} objects
   */
  public static class Builder {

    private final Map<ChannelSohSubtype, Duration> mergeThresholdBySubtype;

    private final Map<ChannelSohSubtype, Boolean> shouldCreateQcMaskBySubtype;

    private Builder() {
      mergeThresholdBySubtype = new HashMap<>();
      shouldCreateQcMaskBySubtype = new HashMap<>();
    }

    /**
     * Sets whether a {@link ChannelSohSubtype} leads to {@link gms.shared.mechanisms.objectstoragedistribution.coi.dataobject.stationprocessing.QcMask}
     * creation
     *
     * @param channelSohSubtype a ChannelSohSubtype, not null
     * @param createQcMask boolean set to true when the channelSohSubtype leads to QcMasks
     * @throws NullPointerException if channelSohSubtype is null
     */
    public void setCreateQcMask(ChannelSohSubtype channelSohSubtype, boolean createQcMask) {
      Objects.requireNonNull(channelSohSubtype,
          "ChannelSohPluginParameters.Builder setCreateQcMask requires non-null channelSohSubtype");
      shouldCreateQcMaskBySubtype.put(channelSohSubtype, createQcMask);
    }

    /**
     * Sets the smallest {@link Duration} between two {@link gms.shared.mechanisms.objectstoragedistribution.coi.dataobject.stationprocessing.QcMask}s
     * created from the same {@link ChannelSohSubtype} that will not be merged. Alternatively,
     * {@link ChannelSohQcPlugin} considers QcMasks closer than this Duration to be adjacent and
     * will merge them into a single mask.
     *
     * @param channelSohSubtype a ChannelSohSubtype, not null
     * @param duration an adjacent threshold, non-inclusive (do not merge masks separated by exactly
     * this Duration), not null
     * @throws NullPointerException if channelSohSubtype or duration are null
     * @throws IllegalArgumentException if duration is negative
     */
    public void setMergeThreshold(ChannelSohSubtype channelSohSubtype, Duration duration) {
      Objects.requireNonNull(channelSohSubtype,
          "ChannelSohPluginParameters.Builder setMergeThreshold requires non-null channelSohSubtype");
      Objects.requireNonNull(duration,
          "ChannelSohPluginParameters.Builder setMergeThreshold requires non-null channelSohSubtype");

      if (duration.isNegative()) {
        throw new IllegalArgumentException(
            "ChannelSohPluginParameters.Builder setMergeThreshold requires positive threshold");
      }

      mergeThresholdBySubtype.put(channelSohSubtype, duration);
    }

    /**
     * Obtains a {@link ChannelSohPluginParameters} from this {@link Builder}.  Verifies there is a
     * mergeThreshold and a createQcMasks setting for each {@link ChannelSohSubtype}
     *
     * @return PluginParmaters, not null
     * @throws IllegalStateException if mergeThreshold or createQcMasks settings do not exist for
     * any ChannelSohSubtype
     */
    public ChannelSohPluginParameters build() {
      verifyValuesPresentForAllSubtypes(mergeThresholdBySubtype.keySet(),
          "ChannelSohPluginParameters.Builder requires a merge threshold for every ChannelSohSubtype but was missing values for ");
      verifyValuesPresentForAllSubtypes(shouldCreateQcMaskBySubtype.keySet(),
          "ChannelSohPluginParameters.Builder requires a createQcMask value for every ChannelSohSubtype but was missing values for ");

      return new ChannelSohPluginParameters(this.mergeThresholdBySubtype,
          this.shouldCreateQcMaskBySubtype);
    }

    /**
     * Verifies that subtypesWithValues contains all of the {@link ChannelSohSubtype} literals.
     *
     * @param subtypesWithValues set of ChannelSohSubtype, not null
     * @param messagePrefix exception message prefix to use if any ChannelSohSubtype literals do not
     * appear in subtypesWithValues
     * @throws IllegalStateException if any ChannelSohSubtype literals do not appear in
     * subtypesWithValues
     */
    private void verifyValuesPresentForAllSubtypes(Set<ChannelSohSubtype> subtypesWithValues,
        String messagePrefix) {

      Set<ChannelSohSubtype> missing = new HashSet<>(Arrays.asList(ChannelSohSubtype.values()));
      missing.removeAll(subtypesWithValues);

      if (!missing.isEmpty()) {
        StringBuilder msgBuilder = new StringBuilder(messagePrefix);
        msgBuilder.append(missing.size()).append(" subtypes: {").append(missing.iterator().next());
        missing.stream().skip(1).forEach(s -> msgBuilder.append(", ").append(s));
        msgBuilder.append("}");

        throw new IllegalStateException(msgBuilder.toString());
      }
    }
  }
}

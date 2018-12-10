package gms.core.signalenhancement.waveformfiltering.objects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Parameters used for streaming invocations to the filter-control application.
 */
public class StreamingFilterPluginParameters {

  private final Map<FilterType, RegistrationInfo> filterTypeToRegistrationInfo;

  private StreamingFilterPluginParameters(
      Map<FilterType, RegistrationInfo> filterTypeToRegistrationInfo) {
    this.filterTypeToRegistrationInfo = new HashMap<>(filterTypeToRegistrationInfo);
  }

  /**
   * Obtains the {@link RegistrationInfo} describing the plugin to use when applying a filterType
   * waveform filter
   *
   * @param filterType a {@link FilterType}, not null
   * @return a {@link RegistrationInfo}, not null
   */
  public RegistrationInfo lookupPlugin(FilterType filterType) {
    return filterTypeToRegistrationInfo.get(filterType);
  }

  @Override
  public String toString() {
    return "StreamingFilterPluginParameters{" +
        "filterTypeToRegistrationInfo=" + filterTypeToRegistrationInfo +
        '}';
  }

  /**
   * Obtains a {@link Builder} to construct a {@link StreamingFilterPluginParameters}
   *
   * @return a {@link Builder}, not null
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Implements the builder design pattern to construct instances of {@link
   * StreamingFilterPluginParameters}
   */
  public static class Builder {

    private final Map<FilterType, RegistrationInfo> filterTypeToRegistrationInfo;

    private Builder() {
      this.filterTypeToRegistrationInfo = new HashMap<>();
    }

    /**
     * Adds a mapping from the {@link FilterType} to the {@link RegistrationInfo}
     *
     * @param filterType a {@link FilterType}, not null
     * @param registrationInfo a {@link RegistrationInfo}, not null
     * @throws NullPointerException if filterType or registrationInfo are null
     */
    public void addFilterTypeMapping(FilterType filterType, RegistrationInfo registrationInfo) {

      Objects.requireNonNull(filterType,
          "StreamingFilterPluginParameters.Builder addFilterTypeMapping cannot accept null filterType");
      Objects.requireNonNull(registrationInfo,
          "StreamingFilterPluginParameters.Builder addFilterTypeMapping cannot accept null registrationInfo");

      this.filterTypeToRegistrationInfo.put(filterType, registrationInfo);
    }

    /**
     * Obtains the {@link StreamingFilterPluginParameters} from the settings in this {@link Builder}
     *
     * @return {@link StreamingFilterPluginParameters}, not null
     * @throws IllegalStateException if any {@link FilterType} literals are not mapped to a {@link
     * RegistrationInfo}
     */
    public StreamingFilterPluginParameters build() {

      // Find missing FilterType -> RegistrationInfo mappings.
      final List<FilterType> missingMappings = Arrays.stream(FilterType.values())
          .filter(t -> !filterTypeToRegistrationInfo.keySet().contains(t))
          .collect(Collectors.toList());

      // Missing mappings cause an IllegalStateException
      if (missingMappings.size() > 0) {
        StringBuilder message = new StringBuilder();
        message.append(
            "StreamingFilterPluginParameters.Builder requires plugin RegistrationInfo mappings for all FilterTypes but is missing mappings for these FilterTypes: (");

        missingMappings.forEach(t -> {
          message.append(t);
          message.append(", ");
        });

        message.replace(message.length() - 2, message.length(), ")");
        throw new IllegalStateException(message.toString());
      }

      return new StreamingFilterPluginParameters(this.filterTypeToRegistrationInfo);
    }
  }
}

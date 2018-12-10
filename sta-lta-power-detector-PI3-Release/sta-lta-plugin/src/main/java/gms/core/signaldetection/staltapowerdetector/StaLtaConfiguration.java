package gms.core.signaldetection.staltapowerdetector;


import gms.core.signaldetection.signaldetectorcontrol.plugin.PluginConfiguration;
import gms.core.signaldetection.staltapowerdetector.Algorithm.AlgorithmType;
import gms.core.signaldetection.staltapowerdetector.Algorithm.WaveformTransformation;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * {@link StaLtaPowerDetectorPlugin} configuration.  Currently only contains a default configuration
 * but will need to be extended to handle the full configuration range (e.g.
 * region/station/site/channel/time)
 */
public class StaLtaConfiguration {

  private final static String DEFAULT_KEY = "default";
  private final static String ALGORITHM_TYPE_KEY = "algorithmType";
  private final static String WAVEFORM_TRANSFORMATION_TYPE_KEY = "waveformTransformation";
  private final static String STA_LEAD_KEY = "staLead";
  private final static String STA_LENGTH_KEY = "staLength";
  private final static String LTA_LEAD_KEY = "ltaLead";
  private final static String LTA_LENGTH_KEY = "ltaLength";
  private final static String TRIGGER_THRESHOLD_KEY = "triggerThreshold";
  private final static String DETRIGGER_THRESHOLD_KEY = "detriggerThreshold";
  private final static String INTERPOLATE_GAPS_SAMPLE_RATE_TOLERANCE_KEY = "interpolateGapsSampleRateTolerance";
  private final static String MERGE_WAVEFORMS_SAMPLE_RATE_TOLERANCE_KEY = "mergeWaveformsSampleRateTolerance";
  private final static String MERGE_WAVEFORMS_MIN_LENGTH_KEY = "mergeWaveformsMinLength";

  // TODO: complete implementation will need to provide different parameters for different Channels
  private final StaLtaParameters staLtaParameters;

  private StaLtaConfiguration(StaLtaParameters staLtaParameters) {
    this.staLtaParameters = staLtaParameters;
  }

  /**
   * Obtain a specific {@link StaLtaConfiguration} from a generic {@link
   * PluginConfiguration}
   *
   * The PluginConfiguration must have a {@link StaLtaParameters} value for key  {@link
   * StaLtaConfiguration#DEFAULT_KEY}.
   *
   * @param pluginConfiguration PluginConfiguration, not null
   * @return StaLtaConfiguration, not null
   * @throws NullPointerException if pluginConfiguration is null
   * @throws IllegalArgumentException if the value for {@link StaLtaConfiguration#DEFAULT_KEY}} is
   * not present, is not an integer, or is not positive
   */
  public static StaLtaConfiguration from(PluginConfiguration pluginConfiguration) {
    Objects.requireNonNull(pluginConfiguration,
        "StaLtaConfiguration cannot be created from null PluginConfiguration");

    // DEFAULT_KEY must be present and map to an StaLtaParameters value
    StaLtaParameters defaultParameters;
    try {
      Map<String, Object> defaults = (Map<String, Object>) pluginConfiguration
          .getParameter("default")
          .orElseThrow(() -> new IllegalArgumentException(
              "StaLtaConfiguration.from requires PluginConfiguration with a configuration for key \"default\""));

      defaultParameters = StaLtaParameters.create(
          getValue(defaults, ALGORITHM_TYPE_KEY, AlgorithmType::valueOf, "AlgorithmType"),
          getValue(defaults, WAVEFORM_TRANSFORMATION_TYPE_KEY, WaveformTransformation::valueOf,
              "WaveformTransformation"),
          getValue(defaults, STA_LEAD_KEY, Duration::parse, "Duration"),
          getValue(defaults, STA_LENGTH_KEY, Duration::parse, "Duration"),
          getValue(defaults, LTA_LEAD_KEY, Duration::parse, "Duration"),
          getValue(defaults, LTA_LENGTH_KEY, Duration::parse, "Duration"),
          getValue(defaults, TRIGGER_THRESHOLD_KEY, Double.class),
          getValue(defaults, DETRIGGER_THRESHOLD_KEY, Double.class),
          getValue(defaults, INTERPOLATE_GAPS_SAMPLE_RATE_TOLERANCE_KEY, Double.class),
          getValue(defaults, MERGE_WAVEFORMS_SAMPLE_RATE_TOLERANCE_KEY, Double.class),
          getValue(defaults, MERGE_WAVEFORMS_MIN_LENGTH_KEY, Duration::parse, "Duration")
      );
    } catch (ClassCastException e) {

      throw new IllegalArgumentException(
          "StaLtaConfiguration.from requires a map(string, object) containing StaLtaParameters for key \""
              + DEFAULT_KEY + "\"", e);
    }

    return new StaLtaConfiguration(defaultParameters);
  }

  /**
   * Gets the value corresponding to the key from the parameters map.  Uses the {@link Class#cast(Object)} from type
   * to perform the conversion.
   * @param parameters parameters map, not null
   * @param key parameter key, not null
   * @param type {@link Class} for the value, not null
   * @param <T> value type
   * @return value mapped to key in parameters, coerced to a T
   * @throws  IllegalArgumentException if the key is not present in parameters or cannot be cast to a T
   */
  private static <T> T getValue(Map<String, Object> parameters, String key, Class<T> type) {
    checkParameterExists(parameters, key);

    try {
      return type.cast(parameters.get(key));
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(
          "StaLtaConfiguration.from requires a default configuration with a " + type + " for key \""
              + key + "\"", e);
    }
  }

  /**
   * Gets the value corresponding to a key from the parameters map.  Uses the fromString Function to perform the conversion.
   * @param parameters parameters map, not null
   * @param key parameter key, not null
   * @param fromString {@link Function} converting a String to a T, not null
   * @param type class type as a string, not null
   * @param <T> value type
   * @return value mapped to key in parameters, coerced to a T
   * @throws  IllegalArgumentException if the key is not present in parameters or cannot be cast to a T
   */
  private static <T> T getValue(Map<String, Object> parameters, String key,
      Function<String, T> fromString, final String type) {
    checkParameterExists(parameters, key);

    try {
      return fromString.apply((String) parameters.get(key));
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(
          "StaLtaConfiguration.from requires a default configuration with a String serialized "
              + type + " for key \"" + key + "\"", e);
    }
  }

  /**
   * Checks if a mapping exists in parameters for key.
   * @param parameters parameters map, not null
   * @param key parameter key, not null
   * @throws  IllegalArgumentException if key is not present in parameters
   */
  private static void checkParameterExists(Map<String, Object> parameters, String key) {
    if (!parameters.containsKey(key)) {
      throw new IllegalArgumentException(
          "StaLtaConfiguration.from requires default configuration with a value for key \"" + key
              + "\"");
    }
  }

  /**
   * Obtains the {@link StaLtaParameters} used for STA/LTA on the channel with channelId. This is a
   * notional operation that will need to be replaced when implementing full configuration.
   *
   * @param channelId UUID to a channel, not null
   * @return {@link StaLtaParameters}, not null
   * @throws NullPointerException if channelId is null
   */
  public StaLtaParameters createParameters(UUID channelId) {

    Objects.requireNonNull(channelId,
        "StaLtaConfiguration cannot create StaLtaParameters for null channelId");

    return staLtaParameters;
  }
}

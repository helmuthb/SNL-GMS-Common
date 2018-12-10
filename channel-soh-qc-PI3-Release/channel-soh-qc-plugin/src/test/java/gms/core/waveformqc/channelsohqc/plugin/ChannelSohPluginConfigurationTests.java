package gms.core.waveformqc.channelsohqc.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import gms.core.waveformqc.waveformqccontrol.objects.ChannelSohSubtype;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ChannelSohPluginConfigurationTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testBuilderSetsDefaults() throws Exception {
    ChannelSohPluginConfiguration.Builder builder = ChannelSohPluginConfiguration
        .builder(true, Duration.ofMillis(37));
    ChannelSohPluginParameters params = builder.build().createParameters();

    verifyAllSubtypes(params::shouldCreateQcMask, Boolean.TRUE::equals);
    verifyAllSubtypes(params::getMergeThreshold, Duration.ofMillis(37)::equals);
  }

  @Test
  public void testBuilderNullDefaultMergeThresholdExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelSohPluginConfiguration.Builder requires non-null mergeThresholdDefault");
    ChannelSohPluginConfiguration.builder(true, null);
  }

  @Test
  public void testBuilderNegativeDefaultMergeThresholdExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("ChannelSohPluginConfiguration.Builder requires positive mergeThresholdDefault");
    ChannelSohPluginConfiguration.builder(true, Duration.ofNanos(-1));
  }

  @Test
  public void testBuilderOverridesDefaults() throws Exception {
    ChannelSohPluginConfiguration.Builder builder = ChannelSohPluginConfiguration
        .builder(false, Duration.ofMillis(100));

    builder.setMergeThreshold(AcquiredChannelSohType.CALIBRATION_UNDERWAY, Duration.ofSeconds(2));
    builder.setCreateQcMask(AcquiredChannelSohType.CALIBRATION_UNDERWAY, true);

    ChannelSohPluginParameters params = builder.build().createParameters();
    assertTrue(params.shouldCreateQcMask(ChannelSohSubtype.CALIBRATION_UNDERWAY));
    assertEquals(Duration.ofSeconds(2),
        params.getMergeThreshold(ChannelSohSubtype.CALIBRATION_UNDERWAY));

    List<ChannelSohSubtype> subtypes = new ArrayList<>(Arrays.asList(ChannelSohSubtype.values()));
    subtypes.remove(ChannelSohSubtype.CALIBRATION_UNDERWAY);

    verifyAllSubtypes(subtypes, params::shouldCreateQcMask, Boolean.FALSE::equals);
    verifyAllSubtypes(subtypes, params::getMergeThreshold, Duration.ofMillis(100)::equals);
  }

  @Test
  public void testBuilderSetMergeThresholdNullTypeExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "ChannelSohPluginConfiguration.Builder setMergeThreshold requires non-null acquiredChannelSohType");
    ChannelSohPluginConfiguration.builder(false, Duration.ofMillis(100))
        .setMergeThreshold(null, Duration.ofSeconds(1));
  }

  @Test
  public void testBuilderSetMergeThresholdNullDurationExpectNullPointerException()
      throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "ChannelSohPluginConfiguration.Builder setMergeThreshold requires non-null mergeThreshold");
    ChannelSohPluginConfiguration.builder(false, Duration.ofMillis(100))
        .setMergeThreshold(AcquiredChannelSohType.DEAD_SENSOR_CHANNEL, null);
  }

  @Test
  public void testBuilderSetMergeThresholdNegativeDurationExpectIllegalArgumentException()
      throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "ChannelSohPluginConfiguration.Builder setMergeThreshold requires positive mergeThreshold");
    ChannelSohPluginConfiguration.builder(false, Duration.ofMillis(100))
        .setMergeThreshold(AcquiredChannelSohType.DEAD_SENSOR_CHANNEL, Duration.ofSeconds(-1));
  }

  @Test
  public void testBuilderSetCreateQcMaskNullTypeExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "ChannelSohPluginConfiguration.Builder setCreateQcMask requires non-null acquiredChannelSohType");
    ChannelSohPluginConfiguration.builder(false, Duration.ofMillis(100))
        .setCreateQcMask(null, true);
  }

  @Test
  public void testBuilderCreatesImmutableConfiguration() throws Exception {
    // Builder initially produces the correct configuration
    ChannelSohPluginConfiguration.Builder builder = ChannelSohPluginConfiguration
        .builder(false, Duration.ofMillis(100));

    ChannelSohPluginConfiguration config = builder.build();
    ChannelSohPluginParameters params = config.createParameters();

    verifyAllSubtypes(params::shouldCreateQcMask, Boolean.FALSE::equals);
    verifyAllSubtypes(params::getMergeThreshold, Duration.ofMillis(100)::equals);

    // Update the builder
    builder.setMergeThreshold(AcquiredChannelSohType.CALIBRATION_UNDERWAY, Duration.ofSeconds(2));
    builder.setCreateQcMask(AcquiredChannelSohType.CALIBRATION_UNDERWAY, true);

    // Verify parameters still contain the previous values
    params = config.createParameters();

    verifyAllSubtypes(params::shouldCreateQcMask, Boolean.FALSE::equals);
    verifyAllSubtypes(params::getMergeThreshold, Duration.ofMillis(100)::equals);

    // New parameters get the new values
    params = builder.build().createParameters();
    assertEquals(Duration.ofSeconds(2),
        params.getMergeThreshold(ChannelSohSubtype.CALIBRATION_UNDERWAY));
    assertEquals(true, params.shouldCreateQcMask(ChannelSohSubtype.CALIBRATION_UNDERWAY));
  }

  private static <T> void verifyAllSubtypes(Function<ChannelSohSubtype, T> mapper,
      Predicate<T> test) {

    verifyAllSubtypes(Arrays.asList(ChannelSohSubtype.values()), mapper, test);
  }

  private static <T> void verifyAllSubtypes(Collection<ChannelSohSubtype> subtypes,
      Function<ChannelSohSubtype, T> mapper, Predicate<T> test) {

    assertTrue(subtypes.stream().map(mapper).allMatch(test));
  }
}

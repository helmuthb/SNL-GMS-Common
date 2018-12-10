package gms.core.waveformqc.channelsohqc.plugin;

import static org.junit.Assert.assertEquals;

import gms.core.waveformqc.waveformqccontrol.objects.ChannelSohSubtype;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ChannelSohChannelSohPluginParametersTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private ChannelSohPluginParameters.Builder builder;

  /**
   * Initialize the builder with default values for all parameters.  Individual tests override the
   * values as needed for their test.
   */
  @Before
  public void setUp() throws Exception {
    builder = ChannelSohPluginParameters.builder();

    Arrays.stream(ChannelSohSubtype.values()).forEach(s -> {
      builder.setCreateQcMask(s, false);
      builder.setMergeThreshold(s, Duration.ofMillis(100));
    });
  }

  @Test
  public void testShouldCreateQcMaskNullSubtypeExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("shouldCreateQcMask requires non-null channelSohSubtype");
    builder.build().shouldCreateQcMask((ChannelSohSubtype) null);
  }

  @Test
  public void testGetMergeThresholdNullSubtypeExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("getMergeThreshold requires non-null channelSohSubtype");
    builder.build().getMergeThreshold(null);
  }

  @Test
  public void testBuilderSetCreateQcMask() throws Exception {
    builder.setCreateQcMask(ChannelSohSubtype.ZEROED_DATA, true);

    builder.setCreateQcMask(ChannelSohSubtype.AUTHENTICATION_SEAL_BROKEN, true);
    builder.setCreateQcMask(ChannelSohSubtype.AUTHENTICATION_SEAL_BROKEN, false);

    ChannelSohPluginParameters parameters = builder.build();
    assertEquals(true, parameters.shouldCreateQcMask(ChannelSohSubtype.ZEROED_DATA));
    assertEquals(false,
        parameters.shouldCreateQcMask(ChannelSohSubtype.AUTHENTICATION_SEAL_BROKEN));
  }

  @Test
  public void testBuilderSetCreateQcMaskNullSubtypeExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "ChannelSohPluginParameters.Builder setCreateQcMask requires non-null channelSohSubtype");
    builder.setCreateQcMask(null, true);
  }

  @Test
  public void testBuilderSetMergeThreshold() throws Exception {
    builder.setMergeThreshold(ChannelSohSubtype.GPS_RECEIVER_OFF, Duration.ofMillis(300));

    ChannelSohPluginParameters parameters = builder.build();
    assertEquals(Duration.ofMillis(300),
        parameters.getMergeThreshold(ChannelSohSubtype.GPS_RECEIVER_OFF));
  }

  @Test
  public void testBuilderSetMergeThresholdNullSubtypeExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "ChannelSohPluginParameters.Builder setMergeThreshold requires non-null channelSohSubtype");
    builder.setMergeThreshold(null, Duration.ofMillis(100));
  }

  @Test
  public void testBuilderSetMergeThresholdNullThresholdExpectNullPointerException()
      throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "ChannelSohPluginParameters.Builder setMergeThreshold requires non-null channelSohSubtype");
    builder.setMergeThreshold(ChannelSohSubtype.GPS_RECEIVER_OFF, null);
  }

  @Test
  public void testBuilderSetMergeThresholdNegativeDurationExpectIllegalArgumentException()
      throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception
        .expectMessage(
            "ChannelSohPluginParameters.Builder setMergeThreshold requires positive threshold");
    builder.setMergeThreshold(ChannelSohSubtype.GPS_RECEIVER_OFF, Duration.ofMinutes(-1));
  }

  /**
   * Creates a {@link ChannelSohPluginParameters.Builder} which sets values for all parameters
   * except for two mergeThresholds.  Verifies the builder throws the correct exception.
   */
  @Test
  public void testBuilderSubtypeMergeThresholdNotSetExpectIllegalStateException() throws Exception {

    // Get all of the subtypes
    List<ChannelSohSubtype> subtypes = new ArrayList<>(Arrays.asList(ChannelSohSubtype.values()));

    // Set createQcMask parameter for each
    ChannelSohPluginParameters.Builder builder = ChannelSohPluginParameters.builder();
    subtypes.forEach(s -> builder.setCreateQcMask(s, true));

    // Remove two subtypes; set mergeThreshold for the remaining subtypes
    ChannelSohSubtype removed1 = subtypes.remove(0);
    ChannelSohSubtype removed2 = subtypes.remove(0);
    subtypes.forEach(s -> builder.setMergeThreshold(s, Duration.ofHours(1)));

    exception.expect(IllegalStateException.class);
    exception.expectMessage(
        "ChannelSohPluginParameters.Builder requires a merge threshold for every ChannelSohSubtype but was missing values for 2 subtypes:");
    exception.expectMessage(removed1.toString());
    exception.expectMessage(removed2.toString());
    builder.build();
  }

  /**
   * Creates a {@link ChannelSohPluginParameters.Builder} which sets values for all parameters
   * except for two mergeThresholds.  Verifies the builder throws the correct exception.
   */
  @Test
  public void testBuilderSubtypeCreateQcMaskNotSetExpectIllegalStateException() throws Exception {

    // Get all of the subtypes
    List<ChannelSohSubtype> subtypes = new ArrayList<>(Arrays.asList(ChannelSohSubtype.values()));

    // Set mergeThreshold parameter for each
    ChannelSohPluginParameters.Builder builder = ChannelSohPluginParameters.builder();
    subtypes.forEach(s -> builder.setMergeThreshold(s, Duration.ofMinutes(3)));

    // Remove two subtypes; set createQcMask for the remaining subtypes
    ChannelSohSubtype removed1 = subtypes.remove(0);
    ChannelSohSubtype removed2 = subtypes.remove(subtypes.size() - 1);
    subtypes.forEach(s -> builder.setCreateQcMask(s, false));

    exception.expect(IllegalStateException.class);
    exception.expectMessage(
        "ChannelSohPluginParameters.Builder requires a createQcMask value for every ChannelSohSubtype but was missing values for 2 subtypes:");
    exception.expectMessage(removed1.toString());
    exception.expectMessage(removed2.toString());
    builder.build();
  }

  @Test
  public void testBuilderCreatesImmutableParameters() throws Exception {
    // Verify the builder initially produces the correct parameters
    builder.setMergeThreshold(ChannelSohSubtype.GPS_RECEIVER_OFF, Duration.ofMillis(300));
    builder.setCreateQcMask(ChannelSohSubtype.ZEROED_DATA, true);
    ChannelSohPluginParameters parameters = builder.build();
    assertEquals(Duration.ofMillis(300),
        parameters.getMergeThreshold(ChannelSohSubtype.GPS_RECEIVER_OFF));
    assertEquals(true, parameters.shouldCreateQcMask(ChannelSohSubtype.ZEROED_DATA));

    // Update the builder
    builder.setMergeThreshold(ChannelSohSubtype.GPS_RECEIVER_OFF, Duration.ofMillis(200));
    builder.setCreateQcMask(ChannelSohSubtype.ZEROED_DATA, false);

    // Verify parameters still contain the previous values
    assertEquals(Duration.ofMillis(300),
        parameters.getMergeThreshold(ChannelSohSubtype.GPS_RECEIVER_OFF));
    assertEquals(true, parameters.shouldCreateQcMask(ChannelSohSubtype.ZEROED_DATA));

    // New parameters get the new values
    parameters = builder.build();
    assertEquals(Duration.ofMillis(200),
        parameters.getMergeThreshold(ChannelSohSubtype.GPS_RECEIVER_OFF));
    assertEquals(false, parameters.shouldCreateQcMask(ChannelSohSubtype.ZEROED_DATA));
  }
}

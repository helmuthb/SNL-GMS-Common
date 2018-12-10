package gms.core.waveformqc.channelsohqc.plugin;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import gms.core.waveformqc.waveformqccontrol.objects.ChannelSohSubtype;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ChannelSohQcPluginTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private static final long statusLength = 10;
  private TestData testData;
  private List<WaveformQcChannelSohStatus> acquiredChannelSohs;
  private ChannelSohQcPlugin plugin;


  @Before
  public void setUp() throws Exception {
    testData = new TestData();

    acquiredChannelSohs = Collections.singletonList(
        createMockWaveformQcChannelSohStatus(true, 10, new UUID( 0L , 0L ), QcMaskType.TIMING,
            ChannelSohSubtype.CLOCK_DIFFERENTIAL_OVER_THRESHOLD));

    plugin = ChannelSohQcPlugin
        .create(ChannelSohPluginConfiguration.builder(true, Duration.ofMillis(37)).build());
  }

  @Test
  public void testCreateNullConfigurationExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelSohQcPlugin create cannot accept null pluginConfiguration");
    ChannelSohQcPlugin.create(null);
  }

  @Test
  public void testCreateQcMasksSetsCreationInfo() throws Exception {
    final Collection<QcMask> existingMasks = Arrays
        .asList(testData.qcMaskChan1AfterGap, testData.qcMaskChan1, testData.qcMaskChan1NoOverlap,
            testData.qcMaskChan1DiffType, testData.qcMaskChan2);

    UUID creationInfoId = UUID.randomUUID();

    List<QcMask> qcMasks = plugin
        .createQcMasks(testData.overlapWaveformQcChannelSohStatus, existingMasks, creationInfoId)
        .collect(Collectors.toList());

    assertTrue(qcMasks.stream()
        .map(QcMask::getCurrentQcMaskVersion)
        .map(QcMaskVersion::getCreationInfoId)
        .allMatch(creationInfoId::equals));
  }

  /**
   * Tests {@link ChannelSohQcPlugin#createQcMasks} correctly merges new and existing masks
   */
  @Test
  public void testCreateQcMasksWithExisting() throws Exception {
    final Collection<QcMask> existingMasks = Arrays
        .asList(testData.qcMaskChan1AfterGap, testData.qcMaskChan1, testData.qcMaskChan1NoOverlap,
            testData.qcMaskChan1DiffType, testData.qcMaskChan2);

    List<QcMask> qcMasks = plugin
        .createQcMasks(testData.overlapWaveformQcChannelSohStatus, existingMasks, new UUID( 0L , 0L ))
        .collect(Collectors.toList());

    testData.verifyPluginMergeQcMasks(qcMasks);
  }

  /**
   * Tests {@link ChannelSohQcPlugin#createQcMasks} does not merge with rejected existing masks
   */
  @Test
  public void testCreateQcMasksRejectedExisting() throws Exception {

    testData.qcMaskChan1AfterGap.reject("Rejected", Collections.emptyList(), new UUID( 0L , 0L ));
    final Collection<QcMask> existingMasks = Arrays
        .asList(testData.qcMaskChan1AfterGap, testData.qcMaskChan1, testData.qcMaskChan1NoOverlap);

    List<QcMask> qcMasks = plugin
        .createQcMasks(testData.overlapWaveformQcChannelSohStatus, existingMasks, new UUID( 0L , 0L ))
        .collect(Collectors.toList());

    // Expect 1 returned mask: qcMaskChan1 extended by the new mask
    assertEquals(1, qcMasks.size());
    assertTrue(qcMasks.contains(testData.qcMaskChan1));
    assertEquals(2, qcMasks.get(0).getQcMaskVersions().size());
    assertEquals(testData.start1, qcMasks.get(0).getCurrentQcMaskVersion().getStartTime().get());
    assertEquals(testData.endMerged, qcMasks.get(0).getCurrentQcMaskVersion().getEndTime().get());
  }

  /**
   * Tests {@link ChannelSohQcPlugin#createQcMasks} correctly creates new masks
   */
  @Test
  public void testCreateQcMasksNoExisting() throws Exception {
    List<QcMask> qcMasks = plugin
        .createQcMasks(acquiredChannelSohs, Collections.emptyList(), new UUID( 0L , 0L ))
        .collect(Collectors.toList());
    assertEquals(5, qcMasks.size());

    for (int i = 0; i < qcMasks.size(); i++) {
      assertEquals(1, qcMasks.get(i).getQcMaskVersions().size());
      assertEquals(new UUID( 0L , 0L ), qcMasks.get(i).getProcessingChannelId());
      assertEquals(ChannelSohSubtype.CLOCK_DIFFERENTIAL_OVER_THRESHOLD.getRationale(),
          qcMasks.get(i).getCurrentQcMaskVersion().getRationale());
      assertEquals(QcMaskCategory.STATION_SOH,
          qcMasks.get(i).getCurrentQcMaskVersion().getCategory());

      assertEquals(Instant.ofEpochSecond(2 * i * statusLength),
          qcMasks.get(i).getCurrentQcMaskVersion().getStartTime().get());
      assertEquals(Instant.ofEpochSecond((2 * i + 1) * statusLength),
          qcMasks.get(i).getCurrentQcMaskVersion().getEndTime().get());
    }
  }

  /**
   * Creates a {@link ChannelSohQcPlugin} configured to not create QcMasks for any {@link
   * ChannelSohSubtype}, then verifies the plugin does not create QcMasks
   */
  @Test
  public void testCreateQcMasksUsesShouldCreateQcMaskConfiguration() throws Exception {
    ChannelSohQcPlugin plugin = ChannelSohQcPlugin
        .create(ChannelSohPluginConfiguration.builder(false, Duration.ofMillis(37)).build());

    List<QcMask> qcMasks = plugin
        .createQcMasks(acquiredChannelSohs, Collections.emptyList(), new UUID( 0L , 0L ))
        .collect(Collectors.toList());
    assertEquals(0, qcMasks.size());
  }

  /**
   * Creates a ChannelSohQcPlugin with configured mergeThreshold smaller than the duration between
   * the {@link TestData) new and existing masks, then verifies the plugin does not merge the new
   * mask with the existing masks.
   */
  @Test
  public void testCreateQcMasksUsesMergeThresholdConfiguration() throws Exception {

    // TestData masks are separated by 25ms so should not be merged
    ChannelSohQcPlugin plugin = ChannelSohQcPlugin
        .create(ChannelSohPluginConfiguration.builder(true, Duration.ofMillis(5)).build());

    final Collection<QcMask> existingMasks = Arrays
        .asList(testData.qcMaskChan1AfterGap, testData.qcMaskChan1, testData.qcMaskChan1NoOverlap,
            testData.qcMaskChan1DiffType, testData.qcMaskChan2);

    List<QcMask> qcMasks = plugin
        .createQcMasks(testData.overlapWaveformQcChannelSohStatus, existingMasks, new UUID( 0L , 0L ))
        .collect(Collectors.toList());

    // Expect a single new mask.  Verify none of the existing masks exist in the plugin's output
    assertEquals(1, qcMasks.size());
    final QcMask newMask = qcMasks.get(0);
    assertTrue(existingMasks.stream().map(QcMask::getId).noneMatch(newMask.getId()::equals));
    assertEquals(1, newMask.getQcMaskVersions().size());

    // New QcMask should match the testData.qcMaskNewOverlap mask
    TestData.verifySingleMask(qcMasks, testData.qcMaskNewOverlap, existingMasks);
  }

  /**
   * Creates sample {@link WaveformQcChannelSohStatus} for testing {@link ChannelSohQcPlugin}
   *
   * @param startingStatus initial status value
   * @param numStatuses num status values to generate
   * @param processingChannelId status for this processingChannel
   * @param qcMaskType mask type
   * @param sohSubtype mask subtype
   * @return a WaveformQcChannelSohStatus built using the provided inputs
   */
  private static WaveformQcChannelSohStatus createMockWaveformQcChannelSohStatus(
      boolean startingStatus, int numStatuses, UUID processingChannelId,
      QcMaskType qcMaskType, ChannelSohSubtype sohSubtype) {

    boolean currentStatus = startingStatus;

    WaveformQcChannelSohStatus.Builder builder = WaveformQcChannelSohStatus
        .builder(processingChannelId, qcMaskType, sohSubtype,
            Instant.ofEpochSecond(0), Instant.ofEpochSecond(statusLength), currentStatus,
            Duration.ofSeconds(5));

    currentStatus = !currentStatus;
    for (int i = 1; i < numStatuses; i++) {
      builder.addStatusChange(Instant.ofEpochSecond(i * statusLength),
          Instant.ofEpochSecond((i + 1) * statusLength), currentStatus);
      currentStatus = !currentStatus;
    }

    return builder.build();
  }

  @Test
  public void testCreateNullSohExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception
        .expectMessage("ChannelSohQcPlugin createQcMasks cannot accept null acquiredChannelSohs");
    plugin.createQcMasks(null, Collections.emptyList(), new UUID( 0L , 0L ));
  }

  @Test
  public void testCreateNullQcMasksExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelSohQcPlugin createQcMasks cannot accept null existing QcMasks");
    plugin.createQcMasks(acquiredChannelSohs, null, new UUID( 0L , 0L ));
  }
}

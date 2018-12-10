package gms.core.waveformqc.waveformsignalqc.plugin;

import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformSpike3PtInterpreter;
import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformSpike3PtQcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.BDDMockito.given;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class WaveformSpike3PtQcPluginTests {
    private static ChannelSegment channelSegment;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    private WaveformSpike3PtQcPluginConfiguration mockWaveformSpike3PtQcPluginConfiguration;

    @Mock
    private WaveformSpike3PtInterpreter mockWaveformSpike3PtInterpreter;

    public static Waveform createWaveform(Instant start, Instant end, double samplesPerSec) {
        if (0 != Duration.between(start, end).getNano()) {
            throw new IllegalArgumentException(
                    "Test can't create waveform where the sample rate does not evenly divide the duration");
        }

        int numSamples = (int) (Duration.between(start, end).getSeconds() * samplesPerSec) + 1;
        double[] values = new double[numSamples];
        Arrays.fill(values, 1.0);

        return Waveform.create(start, end, samplesPerSec, numSamples, values);
    }

    @Before
    public void setUp() throws Exception {
        final Instant start = Instant.EPOCH;
        final Instant end = start.plusSeconds(1000);

        final Waveform waveform = createWaveform(start, start.plusSeconds(200), 40.0);

        /* Include spikes in the first waveform */
        waveform.getValues()[15] = -1.0;

        final Waveform waveform1 = createWaveform(start.plusSeconds(350), start.plusSeconds(400), 40.0);

        /* Include spikes in the second waveform */
        waveform1.getValues()[15] = -1.0;

        final Waveform waveform2 = createWaveform(start.plusSeconds(400), start.plusSeconds(600), 40.0);
        final Waveform waveform3 = createWaveform(start.plusSeconds(610), start.plusSeconds(800), 40.0);
        final Waveform waveform4 = createWaveform(start.plusSeconds(800), end, 40.0);

        channelSegment = ChannelSegment.create(
                UUID.randomUUID(),
                "segmentName",
                ChannelSegment.ChannelSegmentType.RAW,
                start, end, new TreeSet<>(List.of(waveform, waveform1, waveform2, waveform3, waveform4)),
                new CreationInfo("test", Instant.now(), new SoftwareComponentInfo("test", "test")));

        List<WaveformSpike3PtQcMask> discoveredQcMasks = List.of(
            WaveformSpike3PtQcMask.create(channelSegment.getProcessingChannelId(),
                channelSegment.getId(), Instant.ofEpochSecond(0).plusNanos(25000000L)),
            WaveformSpike3PtQcMask.create(channelSegment.getProcessingChannelId(),
                channelSegment.getId(), Instant.ofEpochSecond(0).plusSeconds(350).plusNanos(25000000L)));

      given(mockWaveformSpike3PtQcPluginConfiguration.createParameters())
          .willReturn(WaveformSpike3PtQcPluginParameters.create(0.5,
              4.0, 9, 9));
      given(mockWaveformSpike3PtInterpreter.createWaveformSpike3PtQcMasks(channelSegment,
          .5, 9, 9,
          4.0)).willReturn(discoveredQcMasks);
    }

    @Test
    public void testCreateNullConfigurationExpectNullPointerException() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("WaveformSpike3PtQcPlugin create cannot accept null pluginConfiguration");
        WaveformSpike3PtQcPlugin.create(null, mockWaveformSpike3PtInterpreter);
    }

    @Test
    public void testCreateNullInterpreterExpectNullPointerException() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("WaveformSpike3PtQcPlugin create cannot accept null spike algorithm");
        WaveformSpike3PtQcPlugin.create(mockWaveformSpike3PtQcPluginConfiguration, null);
    }

    /**
     * Make sure the plugin creates masks.  Don't check full details of the mask since that is done
     * in algorithm testing.
     */
    @Test
    public void testQcMaskCreation() {
        WaveformSpike3PtQcPlugin plugin = WaveformSpike3PtQcPlugin.create(mockWaveformSpike3PtQcPluginConfiguration,
            mockWaveformSpike3PtInterpreter);

        UUID creationInfoId = UUID.randomUUID();
        List<QcMask> qcMasks = plugin.createQcMasks(List.of(channelSegment), List.of(), creationInfoId)
                .collect(Collectors.toList());

        assertEquals(2, qcMasks.size());
    }

    /**
     * The plugin takes into account passed in existing QcMasks. In the test below, the plugin removes
     * ALL non-Spike QcMasks. The plugin removes ALL non-Spike, rejected, and duplicated Spike QcMasks.
     * Don't check full details since that is covered in algorithm testing.
     */
    @Test
    public void testCreateQcMasksWithExistingNonSpikeQcMasks() {

        // Create other non-SPIKE masks and provide to plugin
        List<QcMask> existingQcMasks = List.of(
                createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.LONG_GAP,
                       channelSegment.getProcessingChannelId(), channelSegment.getId(),
                        Instant.ofEpochSecond(100), Instant.ofEpochSecond(200)),

                createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.REPAIRABLE_GAP,
                        channelSegment.getProcessingChannelId(), channelSegment.getId(),
                        Instant.ofEpochSecond(400), Instant.ofEpochSecond(600))
        );

        WaveformSpike3PtQcPlugin plugin = WaveformSpike3PtQcPlugin.create(mockWaveformSpike3PtQcPluginConfiguration,
            mockWaveformSpike3PtInterpreter);

        UUID creationInfoId = UUID.randomUUID();
        List<QcMask> spikeMasks = plugin.createQcMasks(List.of(channelSegment), existingQcMasks, creationInfoId)
                .collect(Collectors.toList());

        // Expect two new, passed in two existing non-spike QcMasks
        assertEquals(2, spikeMasks.size());
    }

    /**
     * The plugin takes into account passed in existing QcMasks. In the test below, the plugin removes
     * ALL rejected QcMasks. The plugin removes ALL non-Spike, rejected, and duplicated Spike QcMasks.
     * Don't check full details since that is covered in algorithm testing.
     */
    @Test
    public void testCreateQcMasksWithExistingRejectedQcMasks() {


        // Create rejected QcMasks and provide to plugin
        QcMask rejectQcMask1 = createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.LONG_GAP,
                channelSegment.getProcessingChannelId(), channelSegment.getId(),
                Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));

        rejectQcMask1.reject("Test Reject Mask", List.of(channelSegment.getProcessingChannelId()),UUID.randomUUID());

        QcMask rejectQcMask2 = createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.SPIKE,
                channelSegment.getProcessingChannelId(), channelSegment.getId(),
                Instant.ofEpochSecond(400), Instant.ofEpochSecond(400));

        rejectQcMask2.reject("Test Reject Mask", List.of(channelSegment.getProcessingChannelId()),UUID.randomUUID());

        List<QcMask> existingQcMasks = List.of(rejectQcMask1, rejectQcMask2);


        WaveformSpike3PtQcPlugin plugin = WaveformSpike3PtQcPlugin.create(mockWaveformSpike3PtQcPluginConfiguration,
            mockWaveformSpike3PtInterpreter);

        UUID creationInfoId = UUID.randomUUID();
        List<QcMask> spikeMasks = plugin.createQcMasks(List.of(channelSegment), existingQcMasks, creationInfoId)
                .collect(Collectors.toList());

        // Expect two new, passed in two existing rejected QcMasks
        assertEquals(2, spikeMasks.size());
    }

    /**
     * The plugin takes into account passed in existing QcMasks. In the test below, the plugin contains
     * a duplicate QcMask and removes it. Don't check full details since that is covered in algorithm
     * testing.
     */
    @Test
    public void testCreateQcMasksWithExistingDuplicatedQcMask() {


        // Create duplicated QcMasks and provide to plugin
        List<QcMask> existingQcMasks = List.of(
                createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.SPIKE,
                        channelSegment.getProcessingChannelId(), channelSegment.getId(),
                        Instant.ofEpochSecond(0).plusNanos(25000000L), Instant.ofEpochSecond(0).plusNanos(25000000L))
        );

        WaveformSpike3PtQcPlugin plugin = WaveformSpike3PtQcPlugin.create(mockWaveformSpike3PtQcPluginConfiguration,
            mockWaveformSpike3PtInterpreter);

        UUID creationInfoId = UUID.randomUUID();
        List<QcMask> spikeMasks = plugin.createQcMasks(List.of(channelSegment), existingQcMasks, creationInfoId)
                .collect(Collectors.toList());

        // Expect one new, passed in one duplicated QcMask
        assertEquals(1, spikeMasks.size());
    }

    private static UUID generateDifferentUuid(UUID uuid) {
        UUID other;
        do {
            other = UUID.randomUUID();
        } while (other.equals(uuid));

        return other;
    }


    private QcMask createQcMask(QcMaskCategory category, QcMaskType type, UUID processingChannelId,
                                UUID channelSegmentId,
                                Instant start, Instant end) {

        return QcMask
                .create(processingChannelId, List.of(), List.of(channelSegmentId),
                        category, type, "Test Mask", start, end, UUID.randomUUID());
    }

    private WaveformSpike3PtQcMask createWavformSpike3PtQcMask(UUID channelId, UUID channelSegmentId, Instant spikeTime) {
        return WaveformSpike3PtQcMask.create(channelId, channelSegmentId, spikeTime);
    }
}

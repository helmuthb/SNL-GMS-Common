package gms.core.waveformqc.waveformqccontrol.objects.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus.Builder;
import gms.core.waveformqc.waveformqccontrol.objects.InvokeInputData;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects.CreationInfoDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects.SoftwareComponentInfoDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.QcMaskDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.QcMaskVersionDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.QcMaskVersionReferenceDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.ChannelSegmentDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.WaveformDto;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class InvokeInputDataDtoTests {

  private ChannelSegment channelSegment;
  private WaveformQcChannelSohStatus waveformQcChannelSohStatus;
  private QcMask qcMask;

  @Before
  public void setUp() throws Exception {
    final double sampleRate = 100.0;
    final int sampleCount = 5;
    final Instant start = Instant.parse("1996-01-02T03:04:00Z");
    final Instant middle = start.plusMillis(50);
    final Instant end = middle.plusMillis(50);

    final Waveform waveform1 = Waveform.create(
        start, middle, sampleRate, sampleCount, new double[]{1.1, 2.2, 3.3, 4.4, 5.5});

    final Waveform waveform2 = Waveform.create(
        middle, end, sampleRate, sampleCount, new double[]{6.6, 7.7, 8.8, 9.9, 10.10});

    final SortedSet<Waveform> waveforms = new TreeSet<Waveform>() {{
      add(waveform1);
      add(waveform2);
    }};

    channelSegment = ChannelSegment.create(
        UUID.randomUUID(),
        "segmentName",
        ChannelSegment.ChannelSegmentType.RAW,
        start, end, waveforms,
        new CreationInfo("test", Instant.now(), new SoftwareComponentInfo("test", "test")));

    final Instant startTime = Instant.now();
    final Instant endTime = startTime.plusSeconds(10);
    final Duration threshold = Duration.ofSeconds(5);
    Builder builder = WaveformQcChannelSohStatus
        .builder(UUID.randomUUID(), AcquiredChannelSohType.CLIPPED, startTime, endTime,
            true, threshold);

    waveformQcChannelSohStatus = builder.build();

    List<UUID> channelSegmentIdList = Arrays
        .asList(UUID.randomUUID(), UUID.randomUUID());

    qcMask = QcMask
        .create(UUID.randomUUID(), Collections.emptyList(), channelSegmentIdList,
            QcMaskCategory.STATION_SOH, QcMaskType.STATION_SECURITY, "Rationale",
            Instant.parse("2007-12-03T10:15:30.00Z"),
            Instant.parse("2007-12-03T11:15:30.00Z"), UUID.randomUUID());
  }

  @Test
  public void testConvertToFromInvokeInputDataViaMixin() throws Exception {
    ObjectMapper objMapper = new ObjectMapper();
    objMapper.findAndRegisterModules();

    objMapper.registerModules(WaveformQcChannelSohStatusJacksonUtility.getModule());

    objMapper.addMixIn(QcMask.class, QcMaskDto.class);
    objMapper.addMixIn(QcMaskVersion.class, QcMaskVersionDto.class);
    objMapper.addMixIn(QcMaskVersionReference.class, QcMaskVersionReferenceDto.class);

    objMapper.addMixIn(ChannelSegment.class, ChannelSegmentDto.class);
    objMapper.addMixIn(InvokeInputData.class, InvokeInputDataDto.class);
    objMapper.addMixIn(Waveform.class, WaveformDto.class);
    objMapper.addMixIn(CreationInfo.class, CreationInfoDto.class);
    objMapper.addMixIn(SoftwareComponentInfo.class, SoftwareComponentInfoDto.class);

    InvokeInputData expectedInvokeInputData = InvokeInputData.create(
        Collections.singleton(channelSegment),
        Collections.singleton(qcMask),
        Collections.singleton(waveformQcChannelSohStatus));

    InvokeInputData convertedInvokeInputData = objMapper.readValue(
        objMapper.writeValueAsString(expectedInvokeInputData), InvokeInputData.class);

    Assert.assertEquals(expectedInvokeInputData, convertedInvokeInputData);
  }
}

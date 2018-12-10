package gms.core.waveformqc.waveformqccontrol.objects.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.Converter;
import gms.core.waveformqc.waveformqccontrol.objects.ChannelSohSubtype;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus.Builder;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import org.junit.Test;

public class WaveformQcChannelSohStatusJacksonUtilityTests {

  /**
   * Confirm the Jackson StdConverter from {@link WaveformQcChannelSohStatus} to {@link
   * WaveformQcChannelSohStatusDto} is returned.  Do not need to test the actual conversion as it is
   * a pass through to {@link WaveformQcChannelSohStatusDtoConverter}
   */
  @Test
  public void testGetConverter() throws Exception {
    Converter<WaveformQcChannelSohStatus, WaveformQcChannelSohStatusDto> converter = WaveformQcChannelSohStatusJacksonUtility
        .getConverter();
    assertNotNull(converter);
  }

  /**
   * Confirm the Jackson StdConverter from {@link WaveformQcChannelSohStatusDto } to {@link
   * WaveformQcChannelSohStatus} is returned.  Do not need to test the actual conversion as it is a
   * pass through to {@link WaveformQcChannelSohStatusDtoConverter}
   */
  @Test
  public void testGetDeconverter() throws Exception {
    Converter<WaveformQcChannelSohStatusDto, WaveformQcChannelSohStatus> deconverter = WaveformQcChannelSohStatusJacksonUtility
        .getDeconverter();
    assertNotNull(deconverter);
  }

  /**
   * Confirm the Jackson Module with registered {@link WaveformQcChannelSohStatus} serializer and
   * deserializer processes the WaveformQcChannelSohStatus.
   */
  @Test
  public void testGetModule() throws Exception {
    Module module = WaveformQcChannelSohStatusJacksonUtility.getModule();
    assertNotNull(module);

    ObjectMapper objMapper = new ObjectMapper();
    objMapper.findAndRegisterModules();
    objMapper.registerModule(module);

    final UUID processingChannelId = UUID.randomUUID();
    final QcMaskType qcMaskType = QcMaskType.SENSOR_PROBLEM;
    final ChannelSohSubtype channelSohSubtype = ChannelSohSubtype.CLIPPED;
    final Instant[] statusStartTimes = new Instant[]{Instant.now(),
        Instant.now().plusSeconds(10), Instant.now().plusSeconds(20),
        Instant.now().plusSeconds(30)};
    final Instant[] statusEndTimes = Arrays.stream(statusStartTimes)
        .map(i -> i.plusSeconds(10)).toArray(Instant[]::new);

    final boolean[] statusValues = new boolean[]{true, false, true, false};

    final Duration threshold = Duration.ofSeconds(5);

    Builder builder = WaveformQcChannelSohStatus
        .builder(processingChannelId, qcMaskType, channelSohSubtype, statusStartTimes[0],
            statusEndTimes[0], statusValues[0], threshold);

    for (int i = 1; i < statusStartTimes.length; ++i) {
      builder.addStatusChange(statusStartTimes[i], statusEndTimes[i], statusValues[i]);
    }

    WaveformQcChannelSohStatus expected = builder.build();
    final String json = objMapper.writeValueAsString(expected);
    final WaveformQcChannelSohStatus deserialized = objMapper
        .readValue(json, WaveformQcChannelSohStatus.class);

    assertEquals(expected, deserialized);
  }
}

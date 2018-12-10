package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects.CreationInfoDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects.SoftwareComponentInfoDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CommonJacksonMixinsTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private ObjectMapper objectMapper;

  @Before
  public void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();

    objectMapper
        .addMixIn(CreationInfo.class, CreationInfoDto.class)
        .addMixIn(SoftwareComponentInfo.class, SoftwareComponentInfoDto.class);
  }

  private <T> void runTest(T expected, Class<T> type) throws Exception {
    WaveformsJacksonMixins.register(objectMapper);
    assertEquals(expected, objectMapper.readValue(objectMapper.writeValueAsString(expected), type));
  }

  @Test
  public void testRegisterWaveformsMixinsRegistersAcquiredChannelSohAnalog() throws Exception {
    runTest(TestFixtures.channelSohAnalog, AcquiredChannelSohAnalog.class);
  }

  @Test
  public void testRegisterWaveformsMixinsRegistersAcquiredChannelSohBoolean() throws Exception {
    runTest(TestFixtures.channelSohBoolean, AcquiredChannelSohBoolean.class);
  }

  @Test
  public void testRegisterWaveformsMixinsRegistersWaveform() throws Exception {
    runTest(TestFixtures.waveform1, Waveform.class);
  }

  @Test
  public void testRegisterWaveformsMixinsRegistersChannelSegment() throws Exception {
    runTest(TestFixtures.channelSegment, ChannelSegment.class);
  }

  @Test
  public void testRegisterWaveformsMixinsRegistersRawStationDataFrame() throws Exception {
    runTest(TestFixtures.rawStationDataFrame, RawStationDataFrame.class);
  }

  @Test
  public void testRegisterWaveformsMixinsNullObjectMapperExpectNullPointerException() {

    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "CommonJacksonMixins.register requires non-null objectMapper");
    WaveformsJacksonMixins.register(null);
  }
}

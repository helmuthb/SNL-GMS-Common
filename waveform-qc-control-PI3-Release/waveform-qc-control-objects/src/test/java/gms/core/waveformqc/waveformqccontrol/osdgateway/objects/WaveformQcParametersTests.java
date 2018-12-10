package gms.core.waveformqc.waveformqccontrol.osdgateway.objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.core.waveformqc.waveformqccontrol.objects.PluginVersion;
import gms.core.waveformqc.waveformqccontrol.objects.RegistrationInfo;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcParameters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the {@link WaveformQcParameters} interface and equals functionality. Created by jrhipp
 * on 9/28/17.
 */
public class WaveformQcParametersTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private final UUID processingChannelId = UUID.randomUUID();
  private List<RegistrationInfo> pluginRegistrationInfo = new ArrayList<>();

  @Test(expected = NullPointerException.class)
  public void testFromNullTypeProcessingChannelIdExpectIllegalArgumentException() throws Exception {
    WaveformQcParameters.create(null, pluginRegistrationInfo);
  }

  @Test(expected = NullPointerException.class)
  public void testFromNullTypePluginRegistrationInfoExpectIllegalArgumentException()
      throws Exception {
    WaveformQcParameters.create(processingChannelId, null);
  }

  @Test
  public void testProcessingChannelIdExpectEqualsIdValue() throws Exception {
    final UUID id1 = UUID
        .fromString("b38ae749-2833-4197-a8cb-4609ddd4342f");
    final UUID id2 = UUID
        .fromString("b38ae749-2833-4197-a8cb-4609ddd4342f");
    final UUID id3 = UUID
        .fromString("b38ae749-2833-4197-a8cb-4609ddd4342f");

    WaveformQcParameters waveformQcParameters1 = WaveformQcParameters
        .create(id1, pluginRegistrationInfo);
    WaveformQcParameters waveformQcParameters2 = WaveformQcParameters
        .create(id2, pluginRegistrationInfo);
    WaveformQcParameters waveformQcParameters3 = WaveformQcParameters
        .create(id3, pluginRegistrationInfo);

    assertEquals(waveformQcParameters1.getProcessingChannelId(),
        waveformQcParameters2.getProcessingChannelId());
    assertEquals(waveformQcParameters2.getProcessingChannelId(),
        waveformQcParameters3.getProcessingChannelId());
  }

  @Test
  public void testProcessingChannelIdExpectNotEqualsIdValue() throws Exception {
    final UUID id1 = UUID
        .fromString("04e7d88d-13ef-4e06-ab63-f81c6a170784");
    final UUID id2 = UUID
        .fromString("f66fbfc7-98a1-4e11-826b-968d80ef36eb");

    WaveformQcParameters waveformQcParameters1 = WaveformQcParameters
        .create(id1, pluginRegistrationInfo);
    WaveformQcParameters waveformQcParameters2 = WaveformQcParameters
        .create(id2, pluginRegistrationInfo);

    assertNotEquals(waveformQcParameters1.getProcessingChannelId(),
        waveformQcParameters2.getProcessingChannelId());
  }

  @Test
  public void testPluginRegistrationInfoExpectEqualsValue() throws Exception {

    List<RegistrationInfo> pluginRegistrationInfo1 = new ArrayList<>();
    List<RegistrationInfo> pluginRegistrationInfo2 = new ArrayList<>();
    RegistrationInfo registrationInfo1 = RegistrationInfo
        .from("Test1", PluginVersion.from(1, 2, 3));
    RegistrationInfo registrationInfo2 = RegistrationInfo
        .from("Test2", PluginVersion.from(1, 2, 3));
    pluginRegistrationInfo1.add(registrationInfo1);
    pluginRegistrationInfo1.add(registrationInfo2);
    pluginRegistrationInfo2.add(registrationInfo1);
    pluginRegistrationInfo2.add(registrationInfo2);

    WaveformQcParameters waveformQcParameters1 = WaveformQcParameters
        .create(processingChannelId, pluginRegistrationInfo1);
    WaveformQcParameters waveformQcParameters2 = WaveformQcParameters
        .create(processingChannelId, pluginRegistrationInfo2);

    assertEquals(waveformQcParameters1.getWaveformQcPlugins(),
        waveformQcParameters2.getWaveformQcPlugins());
  }

  @Test
  public void testPluginRegistrationInfoExpectNotEqualsValue() throws Exception {

    List<RegistrationInfo> pluginRegistrationInfo1 = new ArrayList<>();
    List<RegistrationInfo> pluginRegistrationInfo2 = new ArrayList<>();
    RegistrationInfo registrationInfo1 = RegistrationInfo
        .from("Test1", PluginVersion.from(1, 2, 3));
    RegistrationInfo registrationInfo2 = RegistrationInfo
        .from("Test2", PluginVersion.from(1, 2, 4));
    pluginRegistrationInfo1.add(registrationInfo1);
    pluginRegistrationInfo1.add(registrationInfo2);
    pluginRegistrationInfo2.add(registrationInfo1);

    WaveformQcParameters waveformQcParameters1 = WaveformQcParameters
        .create(processingChannelId, pluginRegistrationInfo1);
    WaveformQcParameters waveformQcParameters2 = WaveformQcParameters
        .create(processingChannelId, pluginRegistrationInfo2);

    assertNotEquals(waveformQcParameters1.getWaveformQcPlugins(),
        waveformQcParameters2.getWaveformQcPlugins());
  }

  @Test
  public void testExpectEqualsValue() throws Exception {

    List<RegistrationInfo> pluginRegistrationInfo1 = new ArrayList<>();
    List<RegistrationInfo> pluginRegistrationInfo2 = new ArrayList<>();
    RegistrationInfo registrationInfo1 = RegistrationInfo
        .from("Test1", PluginVersion.from(1, 2, 3));
    RegistrationInfo registrationInfo2 = RegistrationInfo
        .from("Test2", PluginVersion.from(1, 2, 4));
    pluginRegistrationInfo1.add(registrationInfo1);
    pluginRegistrationInfo1.add(registrationInfo2);
    pluginRegistrationInfo2.add(registrationInfo1);
    pluginRegistrationInfo2.add(registrationInfo2);

    WaveformQcParameters waveformQcParameters1 = WaveformQcParameters
        .create(processingChannelId, pluginRegistrationInfo1);
    WaveformQcParameters waveformQcParameters2 = WaveformQcParameters
        .create(processingChannelId, pluginRegistrationInfo2);

    assertEquals(waveformQcParameters1, waveformQcParameters2);
  }

  @Test
  public void testExpectNotEqualsValue() throws Exception {

    List<RegistrationInfo> pluginRegistrationInfo1 = new ArrayList<>();
    List<RegistrationInfo> pluginRegistrationInfo2 = new ArrayList<>();
    RegistrationInfo registrationInfo1 = RegistrationInfo
        .from("Test1", PluginVersion.from(1, 2, 3));
    RegistrationInfo registrationInfo2 = RegistrationInfo
        .from("Test2", PluginVersion.from(1, 2, 4));
    RegistrationInfo registrationInfo3 = RegistrationInfo
        .from("Test3", PluginVersion.from(1, 2, 4));
    pluginRegistrationInfo1.add(registrationInfo1);
    pluginRegistrationInfo1.add(registrationInfo2);
    pluginRegistrationInfo2.add(registrationInfo1);
    pluginRegistrationInfo2.add(registrationInfo3);

    WaveformQcParameters waveformQcParameters1 = WaveformQcParameters
        .create(processingChannelId, pluginRegistrationInfo1);
    WaveformQcParameters waveformQcParameters2 = WaveformQcParameters
        .create(processingChannelId, pluginRegistrationInfo2);

    assertNotEquals(waveformQcParameters1, waveformQcParameters2);
  }
}

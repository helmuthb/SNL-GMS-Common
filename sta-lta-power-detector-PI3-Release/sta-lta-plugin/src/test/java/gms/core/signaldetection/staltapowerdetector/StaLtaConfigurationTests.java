package gms.core.signaldetection.staltapowerdetector;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import gms.core.signaldetection.signaldetectorcontrol.plugin.PluginConfiguration;
import gms.core.signaldetection.staltapowerdetector.Algorithm.AlgorithmType;
import gms.core.signaldetection.staltapowerdetector.Algorithm.WaveformTransformation;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class StaLtaConfigurationTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private PluginConfiguration pluginConfiguration;
  private StaLtaParameters staLtaParameters;

  @Before
  public void setUp() throws Exception {
    staLtaParameters = StaLtaParameters
        .create(AlgorithmType.STANDARD, WaveformTransformation.RECTIFIED,
            Duration.ofMillis(2500), Duration.ofSeconds(5), Duration.ofSeconds(30),
            Duration.ofSeconds(25), 3.0, 2.0, 1.2, 1.3, Duration.ofSeconds(1));

    System.out.println("staLtaParameters = " + staLtaParameters);

    Map<String, Object> defaultParams = new HashMap<>();
    defaultParams.put("algorithmType", AlgorithmType.STANDARD.toString());
    defaultParams.put("waveformTransformation", WaveformTransformation.RECTIFIED.toString());
    defaultParams.put("staLead", Duration.ofMillis(2500).toString());
    defaultParams.put("staLength", Duration.ofSeconds(5).toString());
    defaultParams.put("ltaLead", Duration.ofSeconds(30).toString());
    defaultParams.put("ltaLength", Duration.ofSeconds(25).toString());
    defaultParams.put("triggerThreshold", 3.0);
    defaultParams.put("detriggerThreshold", 2.0);
    defaultParams.put("interpolateGapsSampleRateTolerance", 1.2);
    defaultParams.put("mergeWaveformsSampleRateTolerance", 1.3);
    defaultParams.put("mergeWaveformsMinLength", Duration.ofSeconds(1).toString());

    pluginConfiguration = PluginConfiguration.from(Map.of("default", defaultParams));
  }

  @Test
  public void testFrom() throws Exception {
    StaLtaConfiguration configuration = StaLtaConfiguration.from(pluginConfiguration);
    assertNotNull(configuration);
  }

  @Test
  public void testCreateParameters() throws Exception {
    StaLtaConfiguration config = StaLtaConfiguration.from(pluginConfiguration);
    StaLtaParameters params = config.createParameters(UUID.randomUUID());

    assertNotNull(params);
    assertEquals(staLtaParameters, params);
  }

  @Test
  public void testCreateParametersNullUuidExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception
        .expectMessage("StaLtaConfiguration cannot create StaLtaParameters for null channelId");
    StaLtaConfiguration.from(pluginConfiguration).createParameters(null);
  }

  @Test
  public void testFromNullPluginConfigurationExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("StaLtaConfiguration cannot be created from null PluginConfiguration");
    StaLtaConfiguration.from(null);
  }

  @Test
  public void testFromPluginConfigNoDefaultParamsExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "StaLtaConfiguration.from requires PluginConfiguration with a configuration for key \"default\"");
    StaLtaConfiguration.from(PluginConfiguration.from(Map.of()));
  }

  @Test
  public void testFromPluginConfigDefaultParamsNotMapTypeExpectIllegalArgumentException()
      throws Exception {

    exception.expect(IllegalArgumentException.class);
    exception
        .expectMessage(
            "StaLtaConfiguration.from requires a map(string, object) containing StaLtaParameters for key \"default\"");
    StaLtaConfiguration.from(PluginConfiguration.from(Map.of("default", "string")));
  }

  // TODO: once there is a Configuration pattern add more detailed parsing tests
}

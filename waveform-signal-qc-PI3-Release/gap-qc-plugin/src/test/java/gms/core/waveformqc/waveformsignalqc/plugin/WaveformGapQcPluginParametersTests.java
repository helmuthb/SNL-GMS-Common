package gms.core.waveformqc.waveformsignalqc.plugin;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WaveformGapQcPluginParametersTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testCreate() throws Exception {
    WaveformGapQcPluginParameters params = WaveformGapQcPluginParameters.create(1);
    assertEquals(1, params.getMinLongGapLengthInSamples());

    params = WaveformGapQcPluginParameters.create(100);
    assertEquals(100, params.getMinLongGapLengthInSamples());
  }

  @Test
  public void testCreateNotPositiveLongGapLengthExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "WaveformGapQcPluginParameters requires a positive minLongGapLengthInSamples");
    WaveformGapQcPluginParameters.create(0);
  }
}

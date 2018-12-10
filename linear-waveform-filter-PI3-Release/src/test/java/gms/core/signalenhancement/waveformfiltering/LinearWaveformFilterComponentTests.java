package gms.core.signalenhancement.waveformfiltering;

import static org.junit.Assert.assertEquals;

import gms.core.signalenhancement.waveformfiltering.objects.PluginVersion;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class LinearWaveformFilterComponentTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private LinearWaveformFilterComponent component = new LinearWaveformFilterComponent();

  @Test
  public void testGetName() throws Exception {
    assertEquals("linearWaveformFilterPlugin",
        new LinearWaveformFilterComponent().getName());
  }

  @Test
  public void testGetVersion() throws Exception {
    assertEquals(PluginVersion.from(1, 0, 0),
        new LinearWaveformFilterComponent().getVersion());
  }

  @Test
  public void testGenerateNullChannelSegmentExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    component.filter(null, FilterTestData.FIR_FILTER_DEF);
  }

  @Test
  public void testGenerateNullFilterDefinitionExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    component.filter(FilterTestData.CHANNEL_SEGMENT, null);
  }

}

package gms.core.signalenhancement.waveformfiltering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class LinearWaveformFilterPluginTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private LinearWaveformFilterPlugin plugin = new LinearWaveformFilterPlugin();

  @Test
  public void testGenerateNullChannelSegmentExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    plugin.filter(null, FilterTestData.FIR_FILTER_DEF);
  }

  @Test
  public void testGenerateNullFilterDefinitionExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    plugin.filter(FilterTestData.CHANNEL_SEGMENT, null);
  }

  @Test
  public void testFirFilter() throws Exception {

    Collection<Waveform> waveforms =
        plugin.filter(FilterTestData.CHANNEL_SEGMENT, FilterTestData.FIR_FILTER_DEF);

    assertNotNull(waveforms);
    assertEquals(1, waveforms.size());

    Waveform inputWaveform = FilterTestData.CHANNEL_SEGMENT.getWaveforms().first();
    Waveform outputWaveform = waveforms.iterator().next();

    assertEquals(inputWaveform.getStartTime(), outputWaveform.getStartTime());
    assertEquals(inputWaveform.getEndTime(), outputWaveform.getEndTime());
    assertEquals(inputWaveform.getSampleRate(), outputWaveform.getSampleRate(), Double.MIN_NORMAL);
    assertEquals(inputWaveform.getSampleCount(), outputWaveform.getSampleCount());
    assertTrue(Arrays.equals(FilterTestData.FORWARD_COEFFS, outputWaveform.getValues()));
  }

  @Test
  public void testFirFilterMerges() throws Exception {

    Collection<Waveform> filteredWaveforms =
        plugin.filter(FilterTestData.CHANNEL_SEGMENT2, FilterTestData.FIR_FILTER_DEF);

    assertNotNull(filteredWaveforms);
    assertEquals(1, filteredWaveforms.size());

    Waveform inputWaveform1 = FilterTestData.CHANNEL_SEGMENT2.getWaveforms().first();
    Waveform inputWaveform2 = FilterTestData.CHANNEL_SEGMENT2.getWaveforms().last();
    Waveform outputWaveform = filteredWaveforms.iterator().next();

    assertEquals(inputWaveform1.getStartTime(), outputWaveform.getStartTime());
    assertEquals(inputWaveform2.getEndTime(), outputWaveform.getEndTime());
    assertEquals(inputWaveform1.getSampleRate(), outputWaveform.getSampleRate(), Double.MIN_NORMAL);
    assertEquals(inputWaveform1.getSampleCount() + inputWaveform2.getSampleCount(),
        outputWaveform.getSampleCount());

    // Expected output: FORWARD_COEFFS repeated twice
    final double[] expectedOutput = new double[FilterTestData.FORWARD_COEFFS.length * 2];
    System.arraycopy(FilterTestData.FORWARD_COEFFS, 0, expectedOutput, 0,
        FilterTestData.FORWARD_COEFFS.length);
    System.arraycopy(FilterTestData.FORWARD_COEFFS, 0, expectedOutput,
        FilterTestData.FORWARD_COEFFS.length, FilterTestData.FORWARD_COEFFS.length);

    assertTrue(Arrays.equals(expectedOutput, outputWaveform.getValues()));
  }
}

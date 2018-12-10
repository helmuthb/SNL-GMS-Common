package gms.shared.utilities.signalprocessing.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterCausality;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterPassBandType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FilterTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private double[] forwardCoeffs = new double[]{5.5, 4.4, 3.3, 2.2, 1.1, -6.6};

  private final FilterDefinition firFilterDefinition = FilterDefinition
      .createFir("libTest", "libTestDesc", FilterType.FIR_HAMMING, FilterPassBandType.BAND_PASS,
          1.0, 3.0, 4, FilterSource.USER, FilterCausality.CAUSAL, false, 20.0, 1.0,
          forwardCoeffs, 3.0);

  private final FilterDefinition iirFilterDefinition = FilterDefinition
      .from("libTest", "libTestDesc", FilterType.IIR_BUTTERWORTH, FilterPassBandType.BAND_PASS,
          1.0, 3.0, 4, FilterSource.USER, FilterCausality.CAUSAL, false, 20.0, 1.0,
          new double[]{1.0, 3.0}, forwardCoeffs, 3.0);

  private final Waveform dummyWaveform = Waveform
      .withInferredEndTime(Instant.EPOCH, 20.0, 5, new double[5]);

  @Test
  public void testFilter() throws Exception {

    // Impulse at sample 0 -> output will be coefficients in forward order
    final double[] impulse = new double[forwardCoeffs.length];
    Arrays.fill(impulse, 0.0);
    impulse[0] = 1.0;

    final Waveform inputWaveform = Waveform
        .withInferredEndTime(Instant.EPOCH, 20.0, impulse.length, impulse);

    Waveform outputWaveform = Filter.filter(inputWaveform, firFilterDefinition);
    assertNotNull(outputWaveform);
    assertEquals(inputWaveform.getStartTime(), outputWaveform.getStartTime());
    assertEquals(inputWaveform.getEndTime(), outputWaveform.getEndTime());
    assertEquals(inputWaveform.getSampleRate(), outputWaveform.getSampleRate(), Double.MIN_NORMAL);
    assertEquals(inputWaveform.getSampleCount(), outputWaveform.getSampleCount());
    assertTrue(Arrays.equals(forwardCoeffs, outputWaveform.getValues()));
  }

  @Test
  public void testSampleRateAboveToleranceExpectIllegalArgumentException() throws Exception {
    final double sampleRate =
        firFilterDefinition.getSampleRate() + firFilterDefinition.getSampleRateTolerance() + 1;

    verifySampleRateException(
        Waveform.withInferredEndTime(Instant.EPOCH, sampleRate, 20, new double[20]));
  }

  @Test
  public void testSampleRateBelowToleranceExpectIllegalArgumentException() throws Exception {
    final double sampleRate =
        firFilterDefinition.getSampleRate() - firFilterDefinition.getSampleRateTolerance() - 1;

    verifySampleRateException(
        Waveform.withInferredEndTime(Instant.EPOCH, sampleRate, 20, new double[20]));
  }

  private void verifySampleRateException(Waveform inputWaveform) {
    final double min =
        firFilterDefinition.getSampleRate() - firFilterDefinition.getSampleRateTolerance();
    final double max =
        firFilterDefinition.getSampleRate() + firFilterDefinition.getSampleRateTolerance();

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "Filter requires input waveform with sampleRate in [" + min + ", " + max + "]");
    Filter.filter(inputWaveform, firFilterDefinition);
  }

  @Test
  public void testFilterIirExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Only FIR filtering implemented");
    Filter.filter(dummyWaveform, iirFilterDefinition);
  }

  @Test
  public void testFilterNullInputWaveformExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Filter requires non-null waveform");
    Filter.filter(null, firFilterDefinition);
  }

  @Test
  public void testFilterNullFilterDefinitionExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Filter requires non-null filterDefinition");
    Filter.filter(dummyWaveform, null);
  }
}

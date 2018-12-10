package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FilterDefinitionTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private static final String name = "Detection filter";
  private static final String description = "Detection low pass filter";
  private static final FilterType type = FilterType.FIR_HAMMING;
  private static final FilterPassBandType passBandType = FilterPassBandType.LOW_PASS;
  private static final double low = 0.0;
  private static final double high = 5.0;
  private static final int order = 1;
  private static final FilterSource source = FilterSource.SYSTEM;
  private static final FilterCausality causality = FilterCausality.CAUSAL;
  private static final boolean isZeroPhase = true;
  private static final double sampleRate = 40.0;
  private static final double sampleRateTolerance = 3.14;
  private static final double[] aCoeffs = new double[]{6.7, 7.8};
  private static final double[] bCoeffs = new double[]{3.4, 4.5};
  private static final double groupDelay = 1.5;

  @Test
  public void testCreateFir() {
    FilterDefinition filter = FilterDefinition
        .createFir(name, description, type, passBandType, low, high, order, source, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, bCoeffs, groupDelay);

    assertEquals(name, filter.getName());
    assertEquals(description, filter.getDescription());
    assertEquals(type, filter.getFilterType());
    assertEquals(passBandType, filter.getFilterPassBandType());
    assertEquals(low, filter.getLowFrequencyHz(), Double.MIN_NORMAL);
    assertEquals(high, filter.getHighFrequencyHz(), Double.MIN_NORMAL);
    assertEquals(order, filter.getOrder());
    assertEquals(source, filter.getFilterSource());
    assertEquals(causality, filter.getFilterCausality());
    assertEquals(isZeroPhase, filter.isZeroPhase());
    assertEquals(sampleRate, filter.getSampleRate(), Double.MIN_NORMAL);
    assertEquals(sampleRateTolerance, filter.getSampleRateTolerance(), Double.MIN_NORMAL);
    assertTrue(Arrays.equals(bCoeffs, filter.getBCoefficients()));
    assertEquals(groupDelay, filter.getGroupDelaySecs(), Double.MIN_NORMAL);

    assertTrue(Arrays.equals(new double[]{1.0}, filter.getACoefficients()));
  }

  @Test
  public void testFrom() {
    FilterDefinition filter = FilterDefinition
        .from(name, description, type, passBandType, low, high, order, source, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay);

    assertEquals(name, filter.getName());
    assertEquals(description, filter.getDescription());
    assertEquals(type, filter.getFilterType());
    assertEquals(passBandType, filter.getFilterPassBandType());
    assertEquals(low, filter.getLowFrequencyHz(), Double.MIN_NORMAL);
    assertEquals(high, filter.getHighFrequencyHz(), Double.MIN_NORMAL);
    assertEquals(order, filter.getOrder());
    assertEquals(source, filter.getFilterSource());
    assertEquals(causality, filter.getFilterCausality());
    assertEquals(isZeroPhase, filter.isZeroPhase());
    assertEquals(sampleRate, filter.getSampleRate(), Double.MIN_NORMAL);
    assertEquals(sampleRateTolerance, filter.getSampleRateTolerance(), Double.MIN_NORMAL);
    assertTrue(Arrays.equals(aCoeffs, filter.getACoefficients()));
    assertTrue(Arrays.equals(bCoeffs, filter.getBCoefficients()));
    assertEquals(groupDelay, filter.getGroupDelaySecs(), Double.MIN_NORMAL);
  }

  @Test
  public void testFromLowFreqGreaterHighExpectIllegalArgException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("FilterDefinition requires low frequency < high frequency");
    FilterDefinition
        .from(name, description, type, passBandType, 2.0, 2.0 - Double.MIN_NORMAL, order, source,
            causality, isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay);
  }

  @Test
  public void testFromEqualLowHighFreqExpectIllegalArgException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("FilterDefinition requires low frequency < high frequency");
    FilterDefinition
        .from(name, description, type, passBandType, 3.89, 3.89, order, source, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay);
  }

  @Test
  public void testFromNegativeLowFreqExpectIllegalArgException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("FilterDefinition requires low frequency >= 0.0");
    FilterDefinition
        .from(name, description, type, passBandType, -99.99, high, order, source, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay);
  }

  @Test
  public void testFromZeroOrderExpectIllegalArgException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("FilterDefinition requires order > 0");
    FilterDefinition
        .from(name, description, type, passBandType, low, high, 0, source, causality, isZeroPhase,
            sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay);
  }

  @Test
  public void testFromNegativeOrderExpectIllegalArgException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("FilterDefinition requires order > 0");
    FilterDefinition
        .from(name, description, type, passBandType, low, high, -1, source, causality, isZeroPhase,
            sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay);
  }

  @Test
  public void testFromSampleRateNegativeExpectIllegalArgException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("FilterDefinition requires sampleRate >= 0");
    FilterDefinition
        .from(name, description, type, passBandType, low, high, order, source, causality,
            isZeroPhase, -1, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay);
  }

  @Test
  public void testFromSampleRateToleranceNegativeExpectIllegalArgException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("FilterDefinition requires sampleRateTolerance >= 0");
    FilterDefinition
        .from(name, description, type, passBandType, low, high, order, source, causality,
            isZeroPhase, sampleRate, -1.0, aCoeffs, bCoeffs, groupDelay);
  }

  @Test
  public void testFromEmptyACoefficientsExpectIllegalArgException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("FilterDefinition requires at least 1 aCoefficient");
    FilterDefinition
        .from(name, description, type, passBandType, low, high, order, source, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, new double[0], bCoeffs, groupDelay);
  }

  @Test
  public void testFromEmptyBCoefficientsExpectIllegalArgException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("FilterDefinition requires at least 1 bCoefficient");
    FilterDefinition
        .from(name, description, type, passBandType, low, high, order, source, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, new double[0], groupDelay);
  }

  @Test
  public void testFromNullNameExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("FilterDefinition requires non-null name");
    FilterDefinition
        .from(null, description, type, passBandType, low, high, order, source, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay);
  }

  @Test
  public void testFromNullDescriptionExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("FilterDefinition requires non-null description");
    FilterDefinition
        .from(name, null, type, passBandType, low, high, order, source, causality, isZeroPhase,
            sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay);
  }

  @Test
  public void testFromNullTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("FilterDefinition requires non-null filterType");
    FilterDefinition
        .from(name, description, null, passBandType, low, high, order, source, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay);
  }

  @Test
  public void testFromNullPassBandTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("FilterDefinition requires non-null filterPassBandType");
    FilterDefinition
        .from(name, description, type, null, low, high, order, source, causality, isZeroPhase,
            sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay);
  }

  @Test
  public void testFromNullSourceExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("FilterDefinition requires non-null filterSource");
    FilterDefinition
        .from(name, description, type, passBandType, low, high, order, null, causality, isZeroPhase,
            sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay);
  }

  @Test
  public void testFromNullCausalityExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("FilterDefinition requires non-null filterCausality");
    FilterDefinition
        .from(name, description, type, passBandType, low, high, order, source, null, isZeroPhase,
            sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay);
  }

  @Test
  public void testFromNullACoefficientsTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("FilterDefinition requires non-null aCoefficients");
    FilterDefinition
        .from(name, description, type, passBandType, low, high, order, source, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, null, bCoeffs, groupDelay);
  }

  @Test
  public void testFromNullBCoefficientsTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("FilterDefinition requires non-null bCoefficients");
    FilterDefinition
        .from(name, description, type, passBandType, low, high, order, source, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, null, groupDelay);
  }

  @Test
  public void testEquals() {
    FilterDefinition a = FilterDefinition
        .from(name, description, type, passBandType, low, high, order, source, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay);
    FilterDefinition b = FilterDefinition
        .from(name, description, type, passBandType, low, high, order, source, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay);

    assertTrue(a.equals(b));
    assertTrue(b.equals(a));

    assertFalse(a.equals(FilterDefinition
        .from("other name", description, type, passBandType, low, high, order, source, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay)));
    assertFalse(a.equals(FilterDefinition
        .from(name, "other description", type, passBandType, low, high, order, source, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay)));
    assertFalse(a.equals(FilterDefinition
        .from(name, description, FilterType.IIR_BUTTERWORTH, passBandType, low, high, order, source,
            causality, isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay)));
    assertFalse(a.equals(FilterDefinition
        .from(name, description, type, FilterPassBandType.BAND_PASS, low, high, order, source,
            causality, isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay)));
    assertFalse(a.equals(FilterDefinition
        .from(name, description, type, passBandType, 2.5, high, order, source, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay)));
    assertFalse(a.equals(FilterDefinition
        .from(name, description, type, passBandType, low, 6.6, order, source, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay)));
    assertFalse(a.equals(FilterDefinition
        .from(name, description, type, passBandType, low, high, 99, source, causality, isZeroPhase,
            sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay)));
    assertFalse(a.equals(FilterDefinition
        .from(name, description, type, passBandType, low, high, order, FilterSource.USER, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay)));
    assertFalse(a.equals(FilterDefinition
        .from(name, description, type, passBandType, low, high, order, source,
            FilterCausality.NON_CAUSAL, isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, bCoeffs,
            groupDelay)));
    assertFalse(a.equals(FilterDefinition
        .from(name, description, type, passBandType, low, high, order, source, causality, false,
            sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, groupDelay)));
    assertFalse(a.equals(FilterDefinition
        .from(name, description, type, passBandType, low, high, order, source, causality, isZeroPhase, 20.0,
            sampleRateTolerance, aCoeffs, bCoeffs, groupDelay)));
    assertFalse(a.equals(FilterDefinition
        .from(name, description, type, passBandType, low, high, order, source, causality,
            isZeroPhase, sampleRate, 1.2, aCoeffs, bCoeffs, groupDelay)));
    assertFalse(a.equals(FilterDefinition
        .from(name, description, type, passBandType, low, high, order, source, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, new double[]{9, 8}, bCoeffs, groupDelay)));
    assertFalse(a.equals(FilterDefinition
        .from(name, description, type, passBandType, low, high, order, source, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, new double[]{9, 8}, groupDelay)));
    assertFalse(a.equals(FilterDefinition
        .from(name, description, type, passBandType, low, high, order, source, causality,
            isZeroPhase, sampleRate, sampleRateTolerance, aCoeffs, bCoeffs, -1)));
  }
}

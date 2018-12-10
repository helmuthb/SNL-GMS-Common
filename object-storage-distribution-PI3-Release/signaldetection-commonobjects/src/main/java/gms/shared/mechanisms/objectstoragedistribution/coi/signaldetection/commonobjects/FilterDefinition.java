package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import java.util.Arrays;
import java.util.Objects;

/**
 * Describes an IIR or FIR filter.
 */
public class FilterDefinition {

  private final String name;
  private final String description;
  private final FilterType filterType;
  private final FilterPassBandType filterPassBandType;
  private final double lowFrequencyHz;
  private final double highFrequencyHz;
  private final int order;
  private final FilterSource filterSource;
  private final FilterCausality filterCausality;
  private final boolean zeroPhase;
  private final double sampleRate;
  private final double sampleRateTolerance;
  private final double[] aCoefficients;
  private final double[] bCoefficients;
  private final double groupDelaySecs;

  private FilterDefinition(String name, String description, FilterType filterType,
      FilterPassBandType filterPassBandType, double lowFrequencyHz, double highFrequencyHz,
      int order, FilterSource filterSource, FilterCausality filterCausality, boolean zeroPhase,
      double sampleRate, double sampleRateTolerance, double[] aCoefficients, double[] bCoefficients,
      double groupDelaySecs) {

    this.name = name;
    this.description = description;
    this.filterType = filterType;
    this.filterPassBandType = filterPassBandType;
    this.lowFrequencyHz = lowFrequencyHz;
    this.highFrequencyHz = highFrequencyHz;
    this.order = order;
    this.filterSource = filterSource;
    this.filterCausality = filterCausality;
    this.zeroPhase = zeroPhase;
    this.sampleRate = sampleRate;
    this.sampleRateTolerance = sampleRateTolerance;
    this.aCoefficients = aCoefficients;
    this.bCoefficients = bCoefficients;
    this.groupDelaySecs = groupDelaySecs;
  }

  /**
   * Reconstruct a {@link FilterDefinition}. The definition must meet the following constraints:
   * 1. lowFrequencyHz < highFrequencyHz
   * 2. lowFrequencyHz >= 0.0
   * 3. highFrequencyHz >= 0.0
   * 4. order > 0
   * 5. sampleRate >= 0.0
   * 6. sampleRateTolerance >= 0.0
   * 7. length(aCoefficients) >= 1
   * 8. length(bCoefficients) >= 1
   *
   * @param name filter name, not null
   * @param description filter description, not null
   * @param filterType {@link FilterType}, not null
   * @param filterPassBandType {@link FilterPassBandType}, not null
   * @param lowFrequencyHz filter low frequency in Hz
   * @param highFrequencyHz filter high frequency in Hz
   * @param order filter order
   * @param filterSource {@link FilterSource}, not null
   * @param filterCausality {@link FilterCausality}, not null
   * @param zeroPhase whether this is a zero phase filter
   * @param sampleRate filter sample rate in samples per sec
   * @param sampleRateTolerance filter sample rate tolerance in samples per sec
   * @param aCoefficients filter aCoefficients (feedback coefficients)
   * @param bCoefficients filter bCoefficients (feedforward coefficients)
   * @param groupDelaySecs filter group delay in seconds
   * @return {@link FilterDefinition}, not null
   * @throws NullPointerException if name, description, filterType, filterPassBandType,
   * filterSource, filterCausality, aCoefficients, or bCoefficients are null
   * @throws IllegalArgumentException if the parameters break any of the constraints
   */
  public static FilterDefinition from(String name, String description, FilterType filterType,
      FilterPassBandType filterPassBandType, double lowFrequencyHz, double highFrequencyHz,
      int order, FilterSource filterSource, FilterCausality filterCausality, boolean zeroPhase,
      double sampleRate, double sampleRateTolerance, double[] aCoefficients, double[] bCoefficients,
      double groupDelaySecs) {

    Objects.requireNonNull(name, "FilterDefinition requires non-null name");
    Objects.requireNonNull(description, "FilterDefinition requires non-null description");
    Objects.requireNonNull(filterType, "FilterDefinition requires non-null filterType");
    Objects.requireNonNull(filterPassBandType,
        "FilterDefinition requires non-null filterPassBandType");
    Objects.requireNonNull(filterSource, "FilterDefinition requires non-null filterSource");
    Objects.requireNonNull(filterCausality, "FilterDefinition requires non-null filterCausality");
    Objects.requireNonNull(aCoefficients, "FilterDefinition requires non-null aCoefficients");
    Objects.requireNonNull(bCoefficients, "FilterDefinition requires non-null bCoefficients");

    if (lowFrequencyHz >= highFrequencyHz) {
      throw new IllegalArgumentException(
          "FilterDefinition requires low frequency < high frequency");
    }

    if (lowFrequencyHz < 0.0) {
      throw new IllegalArgumentException("FilterDefinition requires low frequency >= 0.0");
    }

    if (highFrequencyHz < 0.0) {
      throw new IllegalArgumentException("FilterDefinition requires high frequency >= 0.0");
    }

    if (order <= 0) {
      throw new IllegalArgumentException("FilterDefinition requires order > 0");
    }

    if (sampleRate < 0.0) {
      throw new IllegalArgumentException("FilterDefinition requires sampleRate >= 0");
    }

    if (sampleRateTolerance < 0.0) {
      throw new IllegalArgumentException("FilterDefinition requires sampleRateTolerance >= 0");
    }

    if (aCoefficients.length < 1) {
      throw new IllegalArgumentException("FilterDefinition requires at least 1 aCoefficient");
    }

    if (bCoefficients.length < 1) {
      throw new IllegalArgumentException("FilterDefinition requires at least 1 bCoefficient");
    }

    return new FilterDefinition(name, description, filterType, filterPassBandType, lowFrequencyHz,
        highFrequencyHz, order, filterSource, filterCausality, zeroPhase, sampleRate,
        sampleRateTolerance, aCoefficients, bCoefficients, groupDelaySecs);
  }

  /**
   * Create an FIR filter definition.  The definition must meet the following constraints:
   * 1. lowFrequencyHz < highFrequencyHz
   * 2. lowFrequencyHz >= 0.0
   * 3. highFrequencyHz >= 0.0
   * 4. order > 0
   * 5. sampleRate >= 0.0
   * 6. sampleRateTolerance >= 0.0
   * 7. length(bCoefficients) >= 1
   *
   * @param name filter name, not null
   * @param description filter description, not null
   * @param filterType {@link FilterType}, not null
   * @param filterPassBandType {@link FilterPassBandType}, not null
   * @param lowFrequencyHz filter low frequency in Hz
   * @param highFrequencyHz filter high frequency in Hz
   * @param order filter order
   * @param filterSource {@link FilterSource}, not null
   * @param filterCausality {@link FilterCausality}, not null
   * @param zeroPhase whether this is a zero phase filter
   * @param sampleRate filter sample rate in samples per sec
   * @param sampleRateTolerance filter sample rate tolerance in samples per sec
   * @param bCoefficients filter bCoefficients
   * @param groupDelaySecs filter group delay in seconds
   * @return {@link FilterDefinition}, not null
   * @throws NullPointerException if name, description, filterType, filterPassBandType,
   * filterSource, filterCausality, aCoefficients, or bCoefficients are null
   * @throws IllegalArgumentException if the parameters break any of the constraints
   */
  public static FilterDefinition createFir(String name, String description, FilterType filterType,
      FilterPassBandType filterPassBandType, double lowFrequencyHz, double highFrequencyHz,
      int order, FilterSource filterSource, FilterCausality filterCausality, boolean zeroPhase,
      double sampleRate, double sampleRateTolerance, double[] bCoefficients,
      double groupDelaySecs) {

    // Parameter validation occurs in the from() operation

    final double[] firACoeffs = new double[]{1.0};
    return FilterDefinition.from(name, description, filterType, filterPassBandType, lowFrequencyHz,
        highFrequencyHz, order, filterSource, filterCausality, zeroPhase, sampleRate,
        sampleRateTolerance, firACoeffs, bCoefficients, groupDelaySecs);
  }

  /**
   * Obtains the filter name
   *
   * @return String, not null
   */
  public String getName() {
    return name;
  }

  /**
   * Obtains the filter description
   *
   * @return String, not null
   */
  public String getDescription() {
    return description;
  }

  /**
   * Obtains the {@link FilterType}
   *
   * @return FilterType, not null
   */
  public FilterType getFilterType() {
    return filterType;
  }

  /**
   * Obtains the {@link FilterPassBandType}
   *
   * @return FilterPassBandType, not null
   */
  public FilterPassBandType getFilterPassBandType() {
    return filterPassBandType;
  }

  /**
   * Obtains the filter's low frequency
   *
   * @return double, >= 0.0
   */
  public double getLowFrequencyHz() {
    return lowFrequencyHz;
  }

  /**
   * Obtains the filter's high frequency
   *
   * @return double, >= 0.0
   */
  public double getHighFrequencyHz() {
    return highFrequencyHz;
  }

  /**
   * Obtains the filter's order
   *
   * @return integer, >= 1
   */
  public int getOrder() {
    return order;
  }

  /**
   * Obtains the filter's {@link FilterSource}
   *
   * @return FilterSource, not null
   */
  public FilterSource getFilterSource() {
    return filterSource;
  }

  /**
   * Obtains the filter's {@link FilterCausality}
   *
   * @return FilterCausality, not null
   */
  public FilterCausality getFilterCausality() {
    return filterCausality;
  }

  /**
   * Obtains whether the filter is a zero phase filter
   *
   * @return true if this is a zero phase filter
   */
  public boolean isZeroPhase() {
    return zeroPhase;
  }

  /**
   * Obtains the filter's sample rate in samples per second
   *
   * @return double, >= 0.0
   */
  public double getSampleRate() {
    return sampleRate;
  }

  /**
   * Obtains the filter's sample rate tolerance in samples per second
   *
   * @return double, >= 0.0
   */
  public double getSampleRateTolerance() {
    return sampleRateTolerance;
  }

  /**
   * Obtains the filter'a aCoefficients (i.e. the feedback coefficients).
   *
   * @return double array, not null
   * @implNote returns a defensive copy of the aCoefficients (updating the returned array does not
   * affect this Object's aCoefficients)
   */
  public double[] getACoefficients() {
    return aCoefficients.clone();
  }

  /**
   * Obtains the filter'a bCoefficients (i.e. the feedforward coefficients).
   *
   * @return double array, not null
   * @implNote returns a defensive copy of the bCoefficients (updating the returned array does not
   * affect this Object's bCoefficients)
   */
  public double[] getBCoefficients() {
    return bCoefficients;
  }

  /**
   * Obtains the group delay in seconds
   *
   * @return double
   */
  public double getGroupDelaySecs() {
    return groupDelaySecs;
  }

  @Override
  public String toString() {
    return "FilterDefinition{" +
        "name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", filterType=" + filterType +
        ", filterPassBandType=" + filterPassBandType +
        ", lowFrequencyHz=" + lowFrequencyHz +
        ", highFrequencyHz=" + highFrequencyHz +
        ", order=" + order +
        ", filterSource=" + filterSource +
        ", filterCausality=" + filterCausality +
        ", zeroPhase=" + zeroPhase +
        ", sampleRate=" + sampleRate +
        ", sampleRateTolerance=" + sampleRateTolerance +
        ", aCoefficients=" + Arrays.toString(aCoefficients) +
        ", bCoefficients=" + Arrays.toString(bCoefficients) +
        ", groupDelaySecs=" + groupDelaySecs +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    FilterDefinition that = (FilterDefinition) o;

    return Double.compare(that.lowFrequencyHz, lowFrequencyHz) == 0
        && Double.compare(that.highFrequencyHz, highFrequencyHz) == 0 && order == that.order
        && zeroPhase == that.zeroPhase
        && Double.compare(that.sampleRate, sampleRate) == 0
        && Double.compare(that.sampleRateTolerance, sampleRateTolerance) == 0
        && Double.compare(that.groupDelaySecs, groupDelaySecs) == 0 && name.equals(that.name)
        && description.equals(that.description) && filterType == that.filterType
        && filterPassBandType == that.filterPassBandType && filterSource == that.filterSource
        && filterCausality == that.filterCausality && Arrays
        .equals(aCoefficients, that.aCoefficients) && Arrays
        .equals(bCoefficients, that.bCoefficients);
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = name.hashCode();
    result = 31 * result + description.hashCode();
    result = 31 * result + filterType.hashCode();
    result = 31 * result + filterPassBandType.hashCode();
    temp = Double.doubleToLongBits(lowFrequencyHz);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(highFrequencyHz);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + order;
    result = 31 * result + filterSource.hashCode();
    result = 31 * result + filterCausality.hashCode();
    result = 31 * result + (zeroPhase ? 1 : 0);
    temp = Double.doubleToLongBits(sampleRate);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(sampleRateTolerance);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + Arrays.hashCode(aCoefficients);
    result = 31 * result + Arrays.hashCode(bCoefficients);
    temp = Double.doubleToLongBits(groupDelaySecs);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }
}

package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Data class that represents a Waveform which is more generally known as a timeseries.
 */
public final class Waveform extends Timeseries {

  private final double[] values;

  /**
   * Creates a waveform that omits samples.  Infers the sample count by the time range and sample
   * rate.
   *
   * @param startTime The time at which the Waveform beings.
   * @param endTime The time at which the Waveform ends.
   * @param sampleRate The sample rate (a measurement of data points per unit time)
   * @throws NullPointerException if any arg is null
   */
  public static Waveform withoutValues(Instant startTime, Instant endTime, double sampleRate) {
    return new Waveform(startTime, endTime, sampleRate, 0, new double[]{});
  }

  /**
   * Creates a waveform with default creation info. End time is computed using the start time,
   * sample rate, and sample count.
   *
   * @param startTime The time at which the Waveform beings.
   * @param sampleRate The sample rate (a measurement of how many data points there are per unit
   * time)
   * @param sampleCount How many total samples there are in this Waveform.
   * @param values The data points of this Waveform.
   * @throws NullPointerException if any arg is null
   */
  public static Waveform withInferredEndTime(Instant startTime, double sampleRate,
      long sampleCount, double[] values) {

    return new Waveform(startTime, getEndTime(startTime, sampleRate, sampleCount),
        sampleRate, sampleCount, values);
  }

  /**
   * Creates a Waveform.
   *
   * @param startTime The time at which the Waveform beings.
   * @param endTime The time at which the Waveform ends.
   * @param sampleRate The sample rate (a measurement of how many data points there are per unit
   * time)
   * @param sampleCount How many total samples there are in this Waveform.
   * @param values The data points of this Waveform.
   * @throws NullPointerException if any arg is null
   */
  public static Waveform create(Instant startTime, Instant endTime, double sampleRate,
      long sampleCount, double[] values) {
    return new Waveform(startTime, endTime, sampleRate, sampleCount, values);
  }

  /**
   * Creates a Waveform.
   *
   * @param startTime The time at which the Waveform beings.
   * @param endTime The time at which the Waveform ends.
   * @param sampleRate The sample rate (a measurement of how many data points there are per unit
   * time)
   * @param sampleCount How many total samples there are in this Waveform.
   * @param values The data points of this Waveform.
   * @throws NullPointerException if any arg is null
   */
  private Waveform(Instant startTime, Instant endTime, double sampleRate, long sampleCount,
      double[] values) {

    super(startTime, endTime, sampleRate, sampleCount);
    this.values = Objects.requireNonNull(values);
  }

  public double[] getValues() {
    return values;
  }

  public double getFirstSample() { return values[0]; }

  public double getLastSample() { return values[values.length - 1]; }

  /**
   * Expands this waveform data points into time/value pairs using the start time and sample rate.
   *
   * @return A list of ImmutablePairs, containing a time and value.
   */
  public List<ImmutablePair<Instant, Double>> asTimedPairs() {

    List<ImmutablePair<Instant, Double>> timePairs = new ArrayList<>();
    double[] values = this.getValues();
    long nanosecondsBetweenSamples = (long) (1.0 / this.getSampleRate() * 1E+9);
    Instant start = this.getStartTime();

    for (int s = 0; s < values.length; s++) {
      Instant sampleTime = start.plusNanos(s * nanosecondsBetweenSamples);
      timePairs.add(new ImmutablePair<>(sampleTime, values[s]));
    }

    return timePairs;
  }

  /**
   * Windows this waveform to be within the specified time bounds.
   *
   * @param start the start of the time bound
   * @param end the end of the time bound
   * @return A few possibilities: 1.) A new Waveform that contains the narrowed (windowed) set of
   * data from this Waveform. It will have updated start/end times and sample count.  Note that the
   * start/end times of the new Waveform may not equal the requested start/end times, as they
   * reflect where data actually begins and ends. 2.) This exact Waveform if this ones' start/end
   * times are equal to requested range. 3.) A new Waveform with empty points (sampleRate=0,
   * sampleCount=0, values=[]) if the requested range is completely outside the range of this
   * Waveform.
   * @throws NullPointerException if start or end are null
   * @throws IllegalArgumentException if start is not before end.
   */
  public Waveform window(Instant start, Instant end)
      throws NullPointerException, IllegalArgumentException {
    Validate.notNull(start);
    Validate.notNull(end);
    Validate.isTrue(start.isBefore(end));

    // Already exactly windowed?  Great, just return this!
    if (this.getStartTime().equals(start) && this.getEndTime().equals(end)) {
      return this;
    }

    // If Waveform doesn't have samples, return new waveform with the new time range.
    if (this.getValues().length == 0) {
      return Waveform.withoutValues(start, end, this.getSampleRate());
    }

    List<ImmutablePair<Instant, Double>> timedPairs = this.asTimedPairs().stream()
        .filter(p -> isInRange(p.getLeft(), start, end))
        .collect(Collectors.toList());

    Instant newStart = timedPairs.isEmpty() ? start : timedPairs.get(0).getLeft();
    Instant newEnd = timedPairs.isEmpty() ? end : timedPairs.get(timedPairs.size() - 1).getLeft();
    double[] newValues = timedPairs.stream().mapToDouble(ImmutablePair::getRight).toArray();

    return new Waveform(newStart, newEnd, this.getSampleRate(), newValues.length, newValues);
  }

  /**
   * Returns true if time is between [start, end], inclusive.
   *
   * @param time the time to test against [start, end].
   * @param start the start of the range
   * @param end the end of the range
   * @return true if time is in [start, end] (including being equal to either start or end), false
   * otherwise.
   */
  private static boolean isInRange(Instant time, Instant start, Instant end) {
    return (time.isBefore(end) || time.equals(end)) &&
        (time.isAfter(start) || time.equals(start));
  }

  /**
   * Given the input parameters calculate the expected end time.  Truncate the nanoseconds to
   * prevent minor time shifts.
   *
   * @param startTime Start time for a waveform.
   * @param sampleRate Sample rate as samples/second.
   * @param sampleCount Number of samples in the waveform.
   * @return The end time as an Instant object.
   * @throws NullPointerException If the startTime input parameter is null.
   */
  private static Instant getEndTime(Instant startTime, double sampleRate, long sampleCount)
      throws NullPointerException {
    Validate.notNull(startTime);
    long microsToEnd = (long) ((1.0 / sampleRate) * 1E+6 * (sampleCount - 1));
    Duration duration = Duration.of(microsToEnd, ChronoUnit.MICROS);
    return startTime.plus(duration);
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof Waveform)) {
      return false;
    }
    Waveform that = (Waveform) o;

    return Arrays.equals(values, that.values) &&
        super.equals(that);
  }

  @Override
  public final int hashCode() {
    int result = super.hashCode();
    result = 31 * result + Arrays.hashCode(values);
    return result;
  }

}

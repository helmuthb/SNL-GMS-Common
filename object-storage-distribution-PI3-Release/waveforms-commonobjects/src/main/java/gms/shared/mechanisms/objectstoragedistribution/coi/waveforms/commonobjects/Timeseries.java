package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Class which represents a timeseries.
 */
public abstract class Timeseries implements Comparable<Timeseries> {

  private final Instant startTime;
  private final Instant endTime;
  private final double sampleRate;
  private final long sampleCount;
  private static final long NANOSECOND = 1000000000;

  /**
   * Creates a Timeseries.
   *
   * @param startTime The time at which the Timeseries beings.
   * @param endTime The time at which the Timeseries ends.
   * @param sampleRate The sample rate (a measurement of how many data points there are per unit
   * time)
   * @param sampleCount How many total samples there are in this Timeseries.
   * @throws NullPointerException if any arg is null
   */
  public Timeseries(Instant startTime, Instant endTime, double sampleRate, long sampleCount) {

    this.startTime = Objects.requireNonNull(startTime);
    this.endTime = Objects.requireNonNull(endTime);
    this.sampleRate = sampleRate;
    this.sampleCount = sampleCount;
  }

  /**
   * Creates a waveform without samples.  Infers the sample count through the duration
   * of the waveform and the sample rate.
   *
   * @param startTime The time at which the Timeseries beings.
   * @param endTime The time at which the Timeseries ends.
   * @param sampleRate The sample rate (a measurement of how many data points there are per second)
   * @throws NullPointerException if any arg is null
   */
  public Timeseries(Instant startTime, Instant endTime, double sampleRate) {
    this.startTime = Objects.requireNonNull(startTime);
    this.endTime = Objects.requireNonNull(endTime);
    this.sampleRate = sampleRate;
    long durationNanos = Duration.between(startTime, endTime).toNanos();
    this.sampleCount = (long) (durationNanos * sampleRate / NANOSECOND) + 1;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  /**
   * Obtains the Timeseries sampleRate in units of samples per sec
   * @return sample rate in samples per second
   */
  public double getSampleRate() {
    return sampleRate;
  }

  public long getSampleCount() {
    return sampleCount;
  }

  /**
   * Obtains the sampling time for index i in this timeseries
   *
   * @param i find the sampling time for the sample with this index
   * @return {@link Instant} i's sampling time, not null
   */
  public Instant timeForSample(long i) {

    if (i < 0 || i >= this.getSampleCount()) {
      throw new IllegalArgumentException(
          "timeForSample requires sample index within the inclusive range [0, getSampleCount() - 1]");
    }

    //  (1 / samplesPerSec) = secondsPerSample
    //  secondsPerSample * numSamples * 1e9 nanos / sec = nanosOffsetToSample
    // => nanosOffsetToSample = ((sampleNum * 1e9) / samplesPerSec)
    return getStartTime()
        .plusNanos((long) ((i * NANOSECOND) / getSampleRate()));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Timeseries) || (o.getClass() != getClass())) {
      return false;
    }
    Timeseries that = (Timeseries) o;

    return Double.compare(that.sampleRate, sampleRate) == 0 &&
        sampleCount == that.sampleCount &&
        Objects.equals(getStartTime(), that.getStartTime()) &&
        Objects.equals(getEndTime(), that.getEndTime());

  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = startTime != null ? startTime.hashCode() : 0;
    result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
    temp = Double.doubleToLongBits(sampleRate);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (int) (sampleCount ^ (sampleCount >>> 32));

    return result;
  }

  @Override
  public int compareTo(Timeseries ts) {
    return getStartTime().compareTo(ts.getStartTime());
  }

}

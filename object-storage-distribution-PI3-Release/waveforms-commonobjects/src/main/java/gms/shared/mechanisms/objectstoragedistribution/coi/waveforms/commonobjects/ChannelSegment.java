package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Represents a segment of waveform data from a ProcessingChannel.
 */
public final class ChannelSegment implements Comparable<ChannelSegment> {

  private final UUID id;
  private final UUID processingChannelId;
  private final String name;
  private final ChannelSegmentType segmentType;
  private final Instant startTime;
  private final Instant endTime;
  private final SortedSet<Waveform> waveforms;
  private final CreationInfo creationInfo;


  public enum ChannelSegmentType {
    ACQUIRED, RAW, DETECTION_BEAM, FK_BEAM, FILTER
  }

  /**
   * Creates a ChannelSegment anew.
   *
   * @param processingChannelId the id of the processing channel the segment is from.
   * @param name Name of the ChannelSegment.
   * @param type The type of the ChannelSegment, e.g. ChannelSegmentType.RAW.
   * @param start Start time for the ChannelSegment.
   * @param end End time for the ChannelSegment.
   * @param wfs The Waveforms representing the data of the ChannelSegment.
   * @param creationInfo metadata about when this object was created and by what/whom.
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static ChannelSegment create(UUID processingChannelId, String name,
      ChannelSegmentType type, Instant start, Instant end, SortedSet<Waveform> wfs,
      CreationInfo creationInfo) {

    return new ChannelSegment(UUID.randomUUID(), processingChannelId, name,
        type, start, end, wfs, creationInfo);
  }

  /**
   * Creates a ChannelSegment from all params.
   *
   * @param id the identifier for this segment
   * @param processingChannelId the id of the processing channel the segment is from.
   * @param name Name of the ChannelSegment.
   * @param type The type of the ChannelSegment, e.g. ChannelSegmentType.RAW.
   * @param start Start time for the ChannelSegment.
   * @param end End time for the ChannelSegment.
   * @param wfs The Waveforms representing the data of the ChannelSegment.
   * @param creationInfo metadata about when this object was created and by what/whom.
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static ChannelSegment from(UUID id, UUID processingChannelId, String name,
      ChannelSegmentType type, Instant start, Instant end, SortedSet<Waveform> wfs,
      CreationInfo creationInfo) {

    return new ChannelSegment(id, processingChannelId, name,
        type, start, end, wfs, creationInfo);
  }

  /**
   * Creates a ChannelSegment.
   *
   * @param id The UUID assigned to this object.
   * @param processingChannelId the id of the processing channel the segment is from.
   * @param name Name of the ChannelSegment.
   * @param type The type of the ChannelSegment, e.g. ChannelSegmentType.RAW.
   * @param start Start time for the ChannelSegment.
   * @param end End time for the ChannelSegment.
   * @param wfs The Waveforms representing the data of the ChannelSegment.
   * @param creationInfo metadata about when this object was created and by what/whom.
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  private ChannelSegment(UUID id, UUID processingChannelId, String name,
      ChannelSegmentType type, Instant start, Instant end,
      SortedSet<Waveform> wfs,
      CreationInfo creationInfo) {

    Validate.isTrue(end.isAfter(start));
    Validate.notBlank(name);
    this.id = Objects.requireNonNull(id);
    this.processingChannelId = Objects.requireNonNull(processingChannelId);
    this.name = Objects.requireNonNull(name);
    this.segmentType = Objects.requireNonNull(type);
    this.startTime = Objects.requireNonNull(start);
    this.endTime = Objects.requireNonNull(end);
    this.waveforms = Objects.requireNonNull(wfs);
    this.creationInfo = Objects.requireNonNull(creationInfo);
  }

  public UUID getId() {
    return id;
  }

  public UUID getProcessingChannelId() {
    return this.processingChannelId;
  }

  public String getName() {
    return name;
  }

  public ChannelSegmentType getSegmentType() {
    return segmentType;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public SortedSet<Waveform> getWaveforms() {
    return waveforms;
  }

  public CreationInfo getCreationInfo() {
    return creationInfo;
  }

  /**
   * Gets all of the waveform points as pairs of (Instant, Double) for a ChannelSegment by combining
   * all of the Waveforms (flattening).
   *
   * @return flattened list of ImmutablePair<Instant, Double> representing all of the waveform
   * points.
   */
  public static List<ImmutablePair<Instant, Double>> allWaveformPoints(ChannelSegment cs) {
    return cs.getWaveforms().stream()
        .map(Waveform::asTimedPairs)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  /**
   * Given a Collection of ChannelSegment, returns List of all Waveform's in those segments.
   *
   * @param segments the ChannelSegment's to get waveforms from
   * @return List of the Waveforms contained in the given ChannelSegment's.
   */
  public static List<Waveform> allWaveforms(Collection<ChannelSegment> segments) {
    if (segments == null) {
      return new ArrayList<>();
    }

    return segments.stream()
        .map(ChannelSegment::getWaveforms)
        .flatMap(Set::stream)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

  }

  /**
   * Check for the possible insertion of interpolated waveforms between any sample gaps that may
   * exist in this ChannelSegments {@link Waveform}s. A gap is interpolatable if the sample spacing
   * between two adjacent {@link Waveform}s differs by more than 1.5 samples where the sample rate
   * is defined as the mean of the two adjacent waveforms. Also, the sample rates of the two
   * adjacent {@link Waveform}s must be within the input tolerance for the interpolation to occur.
   *
   * The method returns a new ChannelSegment even if no interpolatable waveforms were inserted. In
   * that case the returned ChannelSegment is a copy of the original with a new internal
   * ChannelSegment id.
   *
   * @param sampleRateTolerance The acceptable sample rate tolerance between two adjacent {@link
   * Waveform}s.
   * @param maximumGapSampleCountLimit The size of the gap beyond which interpolation is not
   * performed. The gap limit is defined as a number of sample periods using the mean sample rate
   * between two adjacent {@link Waveform}s.
   * @return A new ChannelSegment containing 0 to the number of input {@link Waveform}s - 1
   * interpolated waveforms between the initial waveforms defining this ChannelSegment on input.
   */
  public ChannelSegment interpolateWaveformGap(double sampleRateTolerance,
      double maximumGapSampleCountLimit) {

    // return this if ChannelSegment waveform set is empty.
    if (getWaveforms().isEmpty()) {
      return this;
    }

    // create a new sorted set for the new channel segment and iterate over all waveforms in this
    // channel segment. Add the first waveform to the new set to begin
    TreeSet<Waveform> newWaveformSet = new TreeSet<>();
    Iterator<Waveform> waveformIterator = getWaveforms().iterator();
    Waveform previousWaveform = waveformIterator.next();
    newWaveformSet.add(previousWaveform);
    while (waveformIterator.hasNext()) {

      // get the next waveform after the previous and see if the sample rates are comparable.
      Waveform nextWaveform = waveformIterator.next();
      if (Math.abs(previousWaveform.getSampleRate() - nextWaveform.getSampleRate()) <=
          sampleRateTolerance) {

        // get the fractional gap sample count (fractional sample period between the two waveforms)
        // and check to see if an interpolated waveform is required (gap is equal to or larger than
        // 1.5 samples but smaller than the maximumGapLimit).
        double fractionalGapSampleCount = getFractionalSampleCount(previousWaveform, nextWaveform);
        if (fractionalGapSampleCount >= 1.5 && fractionalGapSampleCount <
            maximumGapSampleCountLimit) {

          // make a new Waveform that has a sample rate as close to the mean of the original adjacent
          // waveforms as possible. The start time of the new waveform will be equal to the end time
          // of the previous waveform plus 1 sample period (the sample period of the new waveform).
          // The end time will be the start time of the next waveform minus 1 sample period). The
          // number of samples will be the rounded value of the fractionalGapSampleCount - 1. Since
          // the fractionalGapSampleCount is always 1.5 or larger the rounded value - 1 will always
          // contain at least one sample. The new sample count will be 1 for 1.5 <= fraction < 2.5,
          // 2 for 2.5 <= fraction < 3.5, and so on.
          long newSampleCount = Math.round(fractionalGapSampleCount) - 1;

          // make a double array of newSampleCount points and fill with the linearly interpolated values
          // from the last sample of the previousWaveform to the first sample of the nextWaveform
          double[] samples = new double[(int) newSampleCount];
          double startSample = previousWaveform.getLastSample();
          double delSample = (nextWaveform.getFirstSample() - startSample) / (newSampleCount + 1);
          for (int i = 1; i <= newSampleCount; ++i) {
            samples[i - 1] = delSample * i + startSample;
          }

          // create the new waveform as the previous and add to the waveform set.
          double gapTimeWidth = getDurationSeconds(previousWaveform.getEndTime(),
              nextWaveform.getStartTime());
          double sampleRate = (double) (samples.length + 1) / gapTimeWidth;
          long samplePeriod = (long) (1000000000 / sampleRate);

          previousWaveform = Waveform.create(previousWaveform.getEndTime().plusNanos(samplePeriod),
              nextWaveform.getStartTime().minusNanos(samplePeriod), sampleRate, samples.length,
              samples);
          newWaveformSet.add(previousWaveform);
        }
      }

      // add the next waveform, update the previousWaveform and continue
      newWaveformSet.add(nextWaveform);
      previousWaveform = nextWaveform;
    }

    // create and return the new channel segment
    return ChannelSegment.create(this.processingChannelId, this.name, this.segmentType,
        this.startTime, this.endTime, newWaveformSet, this.creationInfo);
  }

  /**
   * Check for the possible merger of existing adjacent {@link Waveform}s where the sample period of
   * the gap between the two {@link Waveform}s is larger than the input minimum gap limit but less
   * than 1.5 samples. If these conditions are met the two adjacent waveforms are merged into a
   * single waveform. Also, the sample rates of the two adjacent {@link Waveform}s must be within
   * the input tolerance for the merger to occur.
   *
   * @param sampleRateTolerance The acceptable sample rate tolerance between two adjacent {@link
   * Waveform}s.
   * @param minimumGapSampleCountLimit The size of the gap within which a merger is not performed.
   * The gap limit is defined as a number of sample periods using the mean sample rate between two
   * adjacent {@link Waveform}s.
   * @return A new ChannelSegment containing 0 to the number of input {@link Waveform}s -1 merged
   * waveforms on return.
   */
  public ChannelSegment mergeWaveforms(double sampleRateTolerance,
      double minimumGapSampleCountLimit) {

    // return this if ChannelSegment waveform set is empty.
    if (getWaveforms().isEmpty()) {
      return this;
    }

    // create a new sorted set for the new channel segment and iterate over all waveforms in this
    // channel segment. Add the first waveform to the new set to begin
    TreeSet<Waveform> newWaveformSet = new TreeSet<>();
    Iterator<Waveform> waveformIterator = getWaveforms().iterator();
    Waveform previousWaveform = waveformIterator.next();
    newWaveformSet.add(previousWaveform);
    while (waveformIterator.hasNext()) {

      // get the next waveform after the previous and see if the sample rates are comparable.
      Waveform nextWaveform = waveformIterator.next();
      if (Math.abs(previousWaveform.getSampleRate() - nextWaveform.getSampleRate())
          <= sampleRateTolerance) {

        // get the fractional gap sample count (fractional sample period between the two waveforms)
        // and check to see if a merged waveform is required (gap is less than 1.5 samples and
        // larger than the minimumGapLimit).
        double fractionalGapSampleCount = getFractionalSampleCount(previousWaveform, nextWaveform);
        if (fractionalGapSampleCount < 1.5 &&
            fractionalGapSampleCount > minimumGapSampleCountLimit) {

          // make a new Waveform that is a merger of the previousWaveform and the next Waveform
          double[] samples = ArrayUtils
              .addAll(previousWaveform.getValues(), nextWaveform.getValues());
          double sampleRate = (double) (samples.length - 1) /
              getDurationSeconds(previousWaveform.getStartTime(), nextWaveform.getEndTime());
          nextWaveform = Waveform.create(previousWaveform.getStartTime(), nextWaveform.getEndTime(),
              sampleRate, samples.length, samples);

          // remove the previous waveform (which is now merged) and continue
          newWaveformSet.pollLast();
        }
      }

      // add the next waveform, update the previousWaveform and continue
      newWaveformSet.add(nextWaveform);
      previousWaveform = nextWaveform;
    }

    // create and return new channel segment
    return ChannelSegment.create(this.processingChannelId, this.name, this.segmentType,
        this.startTime, this.endTime, newWaveformSet, this.creationInfo);
  }

  /**
   * Returns the fractional sample count in a gap between the two input {@link Waveform}s as a
   * Duration.
   *
   * @param previousWaveform The earlier {@link Waveform}
   * @param nextWaveform The later {@link Waveform}
   * @return The fractional sample count as a Duration.
   */
  private static double getFractionalSampleCount(Waveform previousWaveform, Waveform nextWaveform) {
    double meanSampleRate = (nextWaveform.getSampleRate() + previousWaveform.getSampleRate()) / 2.0;
    return meanSampleRate * getDurationSeconds(previousWaveform.getEndTime(),
        nextWaveform.getStartTime());
  }

  /**
   * Returns the time difference of a gap between two adjacent {@link Waveform}s
   *
   * @param startTime The start time of the gap.
   * @param endTime The end time of the gap.
   * @return The gap time difference in seconds.
   */
  private static double getDurationSeconds(Instant startTime, Instant endTime) {
    return getDurationSeconds(Duration.between(startTime, endTime));
  }

  /**
   * Converts the input Duration to (double) seconds and returns the result.
   *
   * @param duration The input Duration to be converted to (double) seconds.
   * @return The input Duration in (double) seconds.
   */
  private static double getDurationSeconds(Duration duration) {
    return (double) duration.getNano() / 1000000000 + duration.getSeconds();
  }

  /**
   * Compares the state of this object against another.
   *
   * @param otherSegment the object to compare against
   * @return true if this object and the provided one have the same state, i.e. their values are
   * equal except for entity ID.  False otherwise.
   */
  public boolean hasSameState(ChannelSegment otherSegment) {
    return otherSegment != null &&
        Objects.equals(this.getProcessingChannelId(), otherSegment.getProcessingChannelId()) &&
        Objects.equals(this.getName(), otherSegment.getName()) &&
        Objects.equals(this.getSegmentType(), otherSegment.getSegmentType()) &&
        Objects.equals(this.getStartTime(), otherSegment.getStartTime()) &&
        Objects.equals(this.getEndTime(), otherSegment.getEndTime()) &&
        Objects.equals(this.getWaveforms(), otherSegment.getWaveforms()) &&
        Objects.equals(this.getCreationInfo(), otherSegment.getCreationInfo());
  }

  @Override
  public final boolean equals(Object other) {
    if (other == null || !(other instanceof ChannelSegment)) {
      return false;
    }

    ChannelSegment otherSegment = (ChannelSegment) other;
    return Objects.equals(this.getId(), otherSegment.getId()) &&
        hasSameState(otherSegment);
  }

  @Override
  public final int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (processingChannelId != null ? processingChannelId.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (segmentType != null ? segmentType.hashCode() : 0);
    result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
    result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
    result = 31 * result + (waveforms != null ? waveforms.hashCode() : 0);
    result = 31 * result + (creationInfo != null ? creationInfo.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ChannelSegment{" +
        "id=" + id +
        ", processingChannelId=" + processingChannelId +
        ", name='" + name + '\'' +
        ", segmentType=" + segmentType +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", waveforms=" + waveforms +
        ", creationInfo=" + creationInfo +
        '}';
  }

  @Override
  public int compareTo(ChannelSegment cs) {
    return getStartTime().compareTo(cs.getStartTime());
  }
}

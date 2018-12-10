package gms.dataacquisition.stationreceiver.cd11.common;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.w3c.dom.ranges.RangeException;


class GapList {

  private long min;
  private long max;
  private final TreeSet<Gap> gapsList = new TreeSet<>();
  private final String JSON_MAX = "max";
  private final String JSON_MIN = "min";
  private final String JSON_GAPS = "gaps";
  private final String JSON_GAP_START = "start";
  private final String JSON_GAP_END = "end";
  private final String JSON_GAP_MODTIME = "modifiedTime";

  // The following is the largest sequence number available
  // It looks weird for -1 to be the largest number but
  // when it's converted to an unsigned 64 bit value it
  // is as follows 0x FFFF FFFF FFFF FFFF.
  static public final long LAST_SEQUENCE_NUMBER = -1;

  private class Gap implements Comparable<Gap> {

    long start;
    long end;
    Instant modifiedTime;

    Gap(long start, long end) {
      this(start, end, Instant.now());
    }

    Gap(long start, long end, Instant modifiedTime) {
      this.start = start;
      this.end = end;
      this.modifiedTime = modifiedTime;
    }

    boolean contains(long value) {
      return ((Long.compareUnsigned(value, this.start) >= 0) && (
          Long.compareUnsigned(value, this.end) <= 0));
    }

    @Override
    public int compareTo(Gap gap) {
      // Check for overlapping gaps.
      if ((Long.compareUnsigned(this.start, gap.end) <= 0) &&
          (Long.compareUnsigned(this.end, gap.start) >= 0)) {

        // Check for equality (the only valid overlap condition).
        if ((Long.compareUnsigned(this.start, gap.start) == 0) &&
            (Long.compareUnsigned(this.end, gap.end) == 0)) {
          return 0;
        } else {
          // Invalid overlap condition.
          throw new RangeException(RangeException.BAD_BOUNDARYPOINTS_ERR, "Range pairs overlap.");
        }
      } else { // No overlap.
        return (Long.compareUnsigned(this.start, gap.end) > 0) ? 1 : -1;
      }
    }

    @Override
    public boolean equals(Object obj) {
      // Used to identify gaps that contain the search value.
      if (obj instanceof Long) {
        long searchValue = (Long) obj;

        // Return true if the gap contains this search value.
        return ((Long.compareUnsigned(searchValue, this.start) >= 0) &&
            (Long.compareUnsigned(searchValue, this.end) <= 0));
      }

      // Used to test if two gaps identify the exact same range.
      else if (obj instanceof Gap) {
        Gap gap = (Gap) obj;

        // Return true if the two gaps represent the same range.
        return ((Long.compareUnsigned(this.start, gap.start) == 0) &&
            (Long.compareUnsigned(this.end, gap.end) == 0));
      }

      // Other object types cannot be compared, so return false.
      else {
        return false;
      }
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.start, this.end);
    }
  }

  /**
   * Constructs the object, and sets the initial min and max range values.
   *
   * @param min initial minimum range value
   * @param max initial maximum range value
   */
  GapList(long min, long max) {
    Validate.isTrue(
        Long.compareUnsigned(min, max) <= 0,
        "Minimum value must be less than or equal to the maximum value.");

    // Set the min and max values.
    this.min = min;
    this.max = max;

    // Create the first gap.
    this.gapsList.add(new Gap(this.min, this.max));
  }

  /**
   * Constructs the object, and loads the JSON gap state.
   *
   * @param json JSON gap state
   */
  GapList(String json) {
    // Calculate index positions.
    int minIndex = json.indexOf(":", json.indexOf(JSON_MIN)) + 1;
    int maxIndex = json.indexOf(":", json.indexOf(JSON_MAX)) + 1;
    int gapsIndex = json.indexOf("[", json.indexOf(JSON_GAPS)) + 1;

    // Load the gap list's min and max range values.
    this.min = Long.parseUnsignedLong(json.substring(minIndex, json.indexOf(",", minIndex)).trim());
    this.max = Long.parseUnsignedLong(json.substring(maxIndex, json.indexOf(",", maxIndex)).trim());

    // Recreate all of the gaps.
    String[] strGaps = json
        .substring(gapsIndex, json.indexOf("]", gapsIndex))
        .split("}\\s*,");
    for (String gap : strGaps) {
      // Clean up the gap string.
      gap = gap
          .replace("\n", "")
          .replace("{", "")
          .replace("}", "")
          .trim();

      // Parse the start.
      int startMinIndex = gap.indexOf(":", gap.indexOf(JSON_GAP_START)) + 1;
      int startMaxIndex = gap.indexOf(",", gap.indexOf(JSON_GAP_START));
      if (startMaxIndex == -1) {
        startMaxIndex = gap.length() - 1;
      }
      String strStart = gap.substring(startMinIndex, startMaxIndex).trim();

      // Parse the end.
      int endMinIndex = gap.indexOf(":", gap.indexOf(JSON_GAP_END)) + 1;
      int endMaxIndex = gap.indexOf(",", gap.indexOf(JSON_GAP_END));
      if (endMaxIndex == -1) {
        endMaxIndex = gap.length() - 1;
      }
      String strEnd = gap.substring(endMinIndex, endMaxIndex).trim();

      // Parse the modified time.
      int mtimeMinIndex = gap.indexOf("\"", gap.indexOf(JSON_GAP_MODTIME));
      int mtimeMaxIndex;
      if (mtimeMinIndex == -1) {
        mtimeMinIndex = gap.indexOf("'", gap.indexOf(JSON_GAP_MODTIME)) + 1;
        mtimeMaxIndex = gap.indexOf("'", mtimeMinIndex);
      } else {
        mtimeMinIndex++;
        mtimeMaxIndex = gap.indexOf("\"", mtimeMinIndex);
      }
      String strModifiedTime = gap.substring(mtimeMinIndex, mtimeMaxIndex).trim();

      // Add the gap.
      this.gapsList.add(new Gap(
          Long.parseUnsignedLong(strStart),
          Long.parseUnsignedLong(strEnd),
          Instant.parse(strModifiedTime)));
    }
  }

  /**
   * Sets both the min and max range values.
   *
   * @param newMin new minimum range value
   * @param newMax
   */
  void setMinMax(long newMin, long newMax) {
    Validate.isTrue(Long.compareUnsigned(min, max) <= 0,
        "Minimum value must be less than or equal to the maximum value.");

    if (Long.compareUnsigned(newMin, this.min) > 0 &&
        Long.compareUnsigned(newMax, this.max) > 0) {
      // Order of operations matters!
      increaseMax(newMax);
      increaseMin(newMin);
    } else if (Long.compareUnsigned(newMin, this.min) < 0 &&
        Long.compareUnsigned(newMax, this.max) < 0) {
      // Order of operations matters!
      increaseMin(newMin);
      increaseMax(newMax);
    } else {
      // Order of operations doesn't matter in the other two cases.
      increaseMax(newMax);
      increaseMin(newMin);
    }
  }

  synchronized void increaseMin(long newMin) {
    Validate.isTrue(Long.compareUnsigned(newMin, this.min) >= 0,
        "Minimum value may only be increased.");
    Validate.isTrue(Long.compareUnsigned(newMin, this.max) <= 0,
        "Minimum value can not be increased above the current maximum value.");

    // Check that the value has changed.
    if (Long.compareUnsigned(newMin, this.min) == 0) {
      return;
    }

    // Set the new maximum.
    this.min = newMin;

    // Check whether any gaps exist.
    if (this.gapsList.size() == 0) {
      return;
    }

    // Retrieve all gaps that fall or span below the new minimum.
    //this.gapsList.stream()
    //    .filter(gap -> gap.start < this.min)
    //    .collect(Collectors.toList())
    //    .forEach(gap -> {
    //      if (gap.end < this.min) {
    //        this.gapsList.remove(gap);
    //      } else {
    //        gap.start = this.min;
    //      }
    //    });

    List<Gap> removeList = new ArrayList<>();
    Gap adjustedFirstGap = null;
    for (Gap gap : this.gapsList) {
      if (Long.compareUnsigned(gap.end, this.min) < 0) {
        removeList.add(gap);
      } else if (Long.compareUnsigned(gap.start, this.min) < 0) {
        removeList.add(gap);
        adjustedFirstGap = new Gap(this.min, gap.end);
      } else {
        break;
      }
    }
    removeList.forEach(this.gapsList::remove);
    if (adjustedFirstGap != null) {
      this.gapsList.add(adjustedFirstGap);
    }
  }

  /**
   * Increases the maximum range value.
   *
   * @param newMax new maximum range value
   */
  synchronized void increaseMax(long newMax) {
    Validate.isTrue(Long.compareUnsigned(newMax, this.max) >= 0,
        "Maximum value was decreased, rather than increased.");

    // Check that the value has changed.
    if (Long.compareUnsigned(newMax, this.max) == 0) {
      return;
    }

    // Set the new maximum.
    long oldMax = this.max;
    this.max = newMax;

    // Check whether any gaps exist.
    if (this.gapsList.size() == 0) {
      return;
    }

    // Retrieve the last gap in the set.
    Gap lastGap = this.gapsList.last();

    // Check if the last gap touches the old maximum.
    if (Long.compareUnsigned(lastGap.end, oldMax) == 0) {
      // Extend the last gap to reach the new maximum.
      lastGap.end = this.max;
    } else {
      // Add a new gap to the end of the gapsList.
      this.gapsList.add(new Gap(oldMax + 1, this.max));
    }
  }

  /**
   * Decreases the minimum range value.
   *
   * @param newMin new minimum range value
   */
  synchronized void decreaseMin(long newMin) {
    Validate.isTrue(Long.compareUnsigned(newMin, this.min) <= 0,
        "Minimum value was increased, rather than decreased.");

    // Check that the value has changed.
    if (Long.compareUnsigned(newMin, this.min) == 0) {
      return;
    }

    // Set the new minimum.
    long oldMin = this.min;
    this.min = newMin;

    // Check whether any gaps exist.
    if (this.gapsList.size() == 0) {
      return;
    }

    // Retrieve the first gap in the set.
    Gap firstGap = this.gapsList.first();

    // Check if the first gap touches the old minimum.
    if (Long.compareUnsigned(firstGap.start, oldMin) == 0) {
      // Extend the first gap to reach the new minimum.
      firstGap.start = this.min;
    } else {
      // Add a new gap to the beginning of the gap-list.
      this.gapsList.add(new Gap(this.min, oldMin - 1));
    }
  }

  /**
   * Decreases the maximum range value.
   *
   * @param newMax new maximum range value
   */
  synchronized void decreaseMax(long newMax) {
    Validate.isTrue(Long.compareUnsigned(newMax, this.max) <= 0,
        "Maximum value was increased, rather than decreased.");
    Validate.isTrue(Long.compareUnsigned(newMax, this.min) >= 0,
        "Maximum value can not be decreased below the current minimum value.");

    // Check that the value has changed.
    if (Long.compareUnsigned(newMax, this.max) == 0) {
      return;
    }

    // Set the new maximum.
    this.max = newMax;

    // Check whether any gaps exist.
    if (this.gapsList.size() == 0) {
      return;
    }

    // Retrieve all gaps that fall or span above the new maximum.
    //this.gapsList.stream()
    //    .filter(gap -> gap.end > this.max)
    //    .collect(Collectors.toList())
    //    .forEach(gap -> {
    //      if (gap.start > this.max) {
    //        this.gapsList.remove(gap);
    //      } else {
    //        gap.end = this.max;
    //      }
    //    });

    // Retrieve the last gap in the set.
    List<Gap> removeList = new ArrayList<>();
    Gap adjustedLastGap = null;
    Iterator<Gap> itrDesc = this.gapsList.descendingIterator();
    while (itrDesc.hasNext()) {
      Gap gap = itrDesc.next();
      if (Long.compareUnsigned(gap.start, this.max) > 0) {
        removeList.add(gap);
      } else if (Long.compareUnsigned(gap.end, this.max) > 0) {
        removeList.add(gap);
        adjustedLastGap = new Gap(gap.start, this.max);
      } else {
        break;
      }
    }
    removeList.forEach(this.gapsList::remove);
    if (adjustedLastGap != null) {
      this.gapsList.add(adjustedLastGap);
    }
  }

  /**
   * Fills in the gap list with the given value.
   *
   * @param value Value to fill in.
   */
  synchronized void addValue(long value) {
    Validate.isTrue(
        Long.compareUnsigned(value, this.min) >= 0 &&
            Long.compareUnsigned(value, this.max) <= 0,
        "Value is out of range.");

    // Check if there are no gaps.
    if (this.gapsList.size() == 0) {
      return;
    }

    // Check if the value falls within a gap.
    Optional<Gap> result = this.gapsList.stream().filter(x -> x.equals(value)).findFirst();
    // LESS EFFICIENT // Optional<Gap> result = binarySearch(this.gapsList, value);

    // SCENARIO 1: Value does not fall within an existing gap.
    if (!result.isPresent()) {
      return;
    }
    Gap gap = result.get();

    // SCENARIO 2: Check if the gap was simply eliminated.
    if ((Long.compareUnsigned(gap.start, value) == 0) && (Long.compareUnsigned(gap.end, value)
        == 0)) {
      // Remove the gap from the gapsList.
      this.gapsList.remove(gap);
    }

    // SCENARIO 3: Check if the gap's lower limit needs to be incremented.
    else if (Long.compareUnsigned(gap.start, value) == 0) {
      gap.start++;

      // Update the gap's "modified" time.
      gap.modifiedTime = Instant.now();
    }

    // SCENARIO 4: Check if the gap's upper limit needs to be decremented.
    else if (Long.compareUnsigned(gap.end, value) == 0) {
      gap.end--;

      // Update the gap's "modified" time.
      gap.modifiedTime = Instant.now();
    }

    // SCENARIO 5: Check if the gap needs to be split into two gaps.
    else {
      Instant now = Instant.now();

      // Store the current end value of the gap.
      long oldEnd = gap.end;

      // Modify the existing gap to span the range of the lower split.
      gap.end = value - 1;

      // Update the existing gap's "modified" time.
      gap.modifiedTime = now;

      // Add a new gap to span the range of the upper split.
      this.gapsList.add(new Gap(value + 1, oldEnd, now));
    }
  }

  /**
   * Returns the minimum range value.
   *
   * @return minimum range value
   */
  synchronized long getMin() {
    return this.min;
  }

  /**
   * Returns the maximum range value.
   *
   * @return maximum range value
   */
  synchronized long getMax() {
    return this.max;
  }

  /**
   * Returns the total number of gaps in the gap list.
   *
   * @return total number of gaps
   */
  synchronized int getTotalGaps() {
    return this.gapsList.size();
  }

  /**
   * Removes gaps that were last modified before the specified expiration.
   *
   * @param expiration expiration time
   */
  synchronized void removeGapsModifiedBefore(Instant expiration) {
    this.gapsList.removeIf(x -> x.modifiedTime.isBefore(expiration));
  }

  /**
   * List of gaps. NOTE: start and end positions are inclusive of the gap.
   *
   * @return List of gaps.
   */
  ArrayList<ImmutablePair<Long, Long>> getGaps() {
    return getGaps(false, false);
  }

  /**
   * List of gaps.
   *
   * @param exclusiveStart Gap start position will be exclusive to the gap.
   * @param exclusiveEnd Gap end position will be exclusive to the gap.
   * @return List of gaps.
   */
  synchronized ArrayList<ImmutablePair<Long, Long>> getGaps(
      boolean exclusiveStart, boolean exclusiveEnd) {
    ArrayList<ImmutablePair<Long, Long>> gapRanges = new ArrayList<>();
    for (Gap gap : this.gapsList) {
      // Check whether the exclusiveStart / exclusiveEnd can be accommodated.
      Validate.isTrue(!exclusiveStart || gap.start > 0,
          "Exclusive start cannot represent a gap starting at point 0.");
      Validate.isTrue(!exclusiveEnd || Long.compareUnsigned(gap.end, -1) < 0,
          "Exclusive start cannot represent a gap starting at point 0.");

      ImmutablePair<Long, Long> gr = new ImmutablePair<>(
          (exclusiveStart) ? gap.start - 1 : gap.start,
          (exclusiveEnd) ? gap.end + 1 : gap.end);
      gapRanges.add(gr);
    }
    return gapRanges;
  }

  private static Optional<Gap> binarySearch(SortedSet<Gap> gaps, long value) {
    if (gaps.isEmpty()) {
      return Optional.empty();
    }

    // NOTE: This operation may be very inefficient (memory usage and copy time).
    List<Gap> gapsList = new ArrayList<>(gaps);

    int low = 0, high = gaps.size() - 1;
    while (low <= high) {
      // get the middle element
      int index = (low + high) / 2;
      Gap middleGap = gapsList.get(index);

      // check if the middle element matches the search. if so, return it.
      if (middleGap.contains(value)) {
        // Value was found.
        return Optional.of(middleGap);
      }

      // if gap is higher than value, search lower half
      if (Long.compareUnsigned(middleGap.start, value) > 0) {
        high = index - 1;
      } else { // gap is lower than value, search upper half.
        low = index + 1;
      }
    }

    // Value was not found.
    return Optional.empty();
  }

  /**
   * Returns a JSON representation of the current gap list (i.e. the "gap state").
   *
   * @return JSON gap state
   */
  String toJson() {
    StringBuilder sb = new StringBuilder("{\n  ");
    sb.append(JSON_MIN).append(": ").append(Long.toUnsignedString(this.min)).append(",\n  ");
    sb.append(JSON_MAX).append(": ").append(Long.toUnsignedString(this.max)).append(",\n  ");
    sb.append(JSON_GAPS).append(": [\n");
    Iterator<Gap> itr = this.gapsList.iterator();
    for (int i = 0; itr.hasNext(); i++) {
      Gap gap = itr.next();
      if (i > 0) {
        sb.append(",\n");
      }
      sb.append("    { ");
      sb.append(JSON_GAP_START).append(": ").append(Long.toUnsignedString(gap.start)).append(", ");
      sb.append(JSON_GAP_END).append(": ").append(Long.toUnsignedString(gap.end)).append(", ");
      sb.append(JSON_GAP_MODTIME).append(": \"").append(gap.modifiedTime.toString()).append("\" ");
      sb.append("}");
    }
    sb.append("\n  ]\n}");
    return sb.toString();
  }
}

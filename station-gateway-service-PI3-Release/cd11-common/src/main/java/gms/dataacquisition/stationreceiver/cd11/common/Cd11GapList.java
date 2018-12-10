package gms.dataacquisition.stationreceiver.cd11.common;

import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11AcknackFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11CommandResponseFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11DataFrame;
import java.time.Instant;
import java.util.ArrayList;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simplified gap-list for use by CD 1.1 components.
 */
public class Cd11GapList {

  private static Logger logger = LoggerFactory.getLogger(Cd11GapList.class);
  private final String JSON_STARTING_SEQUENCE_NUMBER = "startingSequenceNumber";
  private final String JSON_GAP_LIST = "gapList";

  private GapList gapList;
  private Long startingSequenceNumber = null;

  /**
   * Constructor.
   */
  public Cd11GapList() {
    this(null);
  }

  /**
   * Constructor that loads the CD 1.1 gap state.
   *
   * @param json CD 1.1 gap state
   */
  public Cd11GapList(String json) {
    this.gapList = new GapList(0, 0);

    // Check if we need to load gap state from a JSON object.
    if (json != null) {
      // Parse the starting sequence number.
      int idxStart = json.indexOf(":", json.indexOf(JSON_STARTING_SEQUENCE_NUMBER)) + 1;
      int idxEnd = json.indexOf(",", idxStart);
      String strStartSeqNum = json.substring(idxStart, idxEnd).trim();
      this.startingSequenceNumber = (strStartSeqNum.equals("null")) ?
          null : Long.parseUnsignedLong(strStartSeqNum);

      // Parse the gap list JSON object.
      idxStart = json.indexOf(":", json.indexOf(JSON_GAP_LIST)) + 1;
      idxEnd = json.lastIndexOf("}", json.lastIndexOf("}") - 1) + 1;
      String jsonGapList = json.substring(idxStart, idxEnd).trim();
      this.gapList = new GapList(jsonGapList);
    }
  }

  /**
   * Updates the gap state based on the contents of the CD 1.1 Acknack frame.
   *
   * @param acknackFrame CD 1.1 Acknack frame
   */
  public void update(Cd11AcknackFrame acknackFrame) {
    // Ignore invalid input.
    if (Long.compareUnsigned(acknackFrame.lowestSeqNum, acknackFrame.highestSeqNum) > 0) {
      logger.error("Acknack frame contains a lowestSeqNum that is larger than the highestSeqNum.");
      return;
    }

    // Check for a reset.
    if (Long.compareUnsigned(acknackFrame.highestSeqNum, this.gapList.getMin()) < 0) {
      this.gapList = new GapList(acknackFrame.lowestSeqNum, acknackFrame.highestSeqNum);
      this.startingSequenceNumber = null;
    } else {
      // Update the gap range.
      try {
        this.gapList.setMinMax(acknackFrame.lowestSeqNum, acknackFrame.highestSeqNum);
      } catch (Exception e) {
        // Leave the min/max as it was before.
        logger.error("Acknack frame provided invalid sequence range; frame will be ignored: ", e);
        return;
      }
    }

    // Check if the gap range has fallen below the starting-sequence-number (only happens with non-compliant data providers).
    if (startingSequenceNumber != null &&
        Long.compareUnsigned(this.getHighestSequenceNumber(), startingSequenceNumber) < 0) {
      startingSequenceNumber = null;
    }

    // Add invalid sequence numbers indicated by the Data Provider to the gap list (so that we stop requesting them).
    if (acknackFrame.gapRanges.length > 0 && acknackFrame.gapRanges.length % 2 == 0) {
      long currentMinimum = this.gapList.getMin();

      for (int i = 0; i < acknackFrame.gapRanges.length; i += 2) {
        long gapRangeStartInclusive = acknackFrame.gapRanges[i];
        long gapRangeEndExclusive = acknackFrame.gapRanges[i + 1];

        // Skip gap ranges that fall below the current minimum.
        if (Long.compareUnsigned((gapRangeEndExclusive - 1), currentMinimum) < 0) {
          continue;
        }

        for (long j = gapRangeStartInclusive; Long.compareUnsigned(j, gapRangeEndExclusive) < 0;
            j++) {
          // Skip individual gaps that fall below the current minimum.
          if (Long.compareUnsigned(j, currentMinimum) < 0) {
            continue;
          }

          this.processSequenceNumber(j, false);
        }
      }
    }
  }

  /**
   * Adds a sequence number to the gap list.
   *
   * @param cd11DataFrame CD 1.1 Data frame
   */
  public void addSequenceNumber(Cd11DataFrame cd11DataFrame) {
    this.addSequenceNumber(cd11DataFrame.getFrameHeader().sequenceNumber);
  }

  /**
   * Adds a sequence number to the gap list.
   *
   * @param cd11CommandResponseFrame CD 1.1 Command Response frame
   */
  public void addSequenceNumber(Cd11CommandResponseFrame cd11CommandResponseFrame) {
    this.addSequenceNumber(cd11CommandResponseFrame.getFrameHeader().sequenceNumber);
  }

  /**
   * Adds a sequence number to the gap list.
   *
   * @param value sequence number
   */
  public void addSequenceNumber(long value) {
    this.processSequenceNumber(value, true);

    // Set the starting-sequence-number, if it has not yet been set.
    if (startingSequenceNumber == null) {
      startingSequenceNumber = value;
    }
  }

  private void processSequenceNumber(long value, boolean isDataOrCommandFrameSequenceNumber) {
    // Do not allow non-data frames to dramatically change the gap list.
    if (!isDataOrCommandFrameSequenceNumber &&
        (startingSequenceNumber == null ||
            Long.compareUnsigned(value, 1) < 0 ||
            Long.compareUnsigned(value, getLowestSequenceNumber()) < 0 ||
            Long.compareUnsigned(value, (this.gapList.getMax() + 20)) > 0)) {
      return;
    }

    // Check if this sequence number falls outside of the current min/max range.
    if (Long.compareUnsigned(value, this.gapList.getMax()) > 0) {
      // Increase the maximum.
      this.gapList.increaseMax(value);
    } else if (Long.compareUnsigned(value, this.gapList.getMin()) < 0) {
      // Decrease the minimum.
      this.gapList.decreaseMin(value);
    }

    // Add the new value.
    try {
      this.gapList.addValue(value);
    } catch (Exception e) {
      logger.warn(String.format("Ignoring invalid sequence number: %d", value),
          ExceptionUtils.getStackTrace(e));
    }
  }

  /**
   * Returns the highest sequence number.
   *
   * @return highest sequence number
   */
  public long getHighestSequenceNumber() {
    return (this.startingSequenceNumber == null && this.gapList.getMax() == 0) ?
        -1 :
        this.gapList.getMax();
  }

  /**
   * Returns the lowest sequence number.
   *
   * @return lowest sequence number
   */
  public long getLowestSequenceNumber() {
    return (this.startingSequenceNumber == null ||
        Long.compareUnsigned(this.startingSequenceNumber, this.gapList.getMin()) < 0) ?
        this.gapList.getMin() : this.startingSequenceNumber;
  }

  /**
   * Returns an array of gap ranges, as required to produce a CD 1.1 Acknack frame.
   *
   * @return array of gap ranges
   */
  public long[] getGaps() {
    // Check if the starting sequence number has been set.
    if (startingSequenceNumber == null) {
      return new long[]{};
    }

    ArrayList<ImmutablePair<Long, Long>> gaps = this.gapList.getGaps(false, true);

    // Filter out gaps that touch the lowestSequenceNumber() or the max of the range.
    for (int i = gaps.size() - 1; i >= 0; i--) {
      long gapStart = gaps.get(i).getLeft();
      long gapEnd = gaps.get(i).getRight();
      if ((Long.compareUnsigned(gapStart, this.getLowestSequenceNumber()) <= 0) ||
          (Long.compareUnsigned(gapEnd, this.gapList.getMax()) > 0)) {
        gaps.remove(i);
      }
    }

    // Convert to long array.
    long[] cd11Gaps = new long[gaps.size() * 2];
    int i = 0;
    for (ImmutablePair<Long, Long> gap : gaps) {
      cd11Gaps[i] = gap.getLeft();
      cd11Gaps[i + 1] = gap.getRight();
      i += 2;
    }

    return cd11Gaps;
  }

  /**
   * Removes gaps that have not changed in the specified number of days.
   *
   * @param days days
   */
  public void removeExpiredGaps(int days) {
    Validate.isTrue(days > 0);
    this.gapList.removeGapsModifiedBefore(Instant.now().minusSeconds(60*60*24*days));
  }

  /**
   * Returns a JSON string representing the CD 1.1 Gap State.
   *
   * @return JSON representation of the CD 1.1 gap state
   */
  public String toJson() {
    StringBuilder sb = new StringBuilder();
    sb.append("{\n  ");
    sb.append(JSON_STARTING_SEQUENCE_NUMBER).append(": ")
        .append((this.startingSequenceNumber == null) ?
            "null" : Long.toUnsignedString(this.startingSequenceNumber))
        .append(",\n  ");
    sb.append(JSON_GAP_LIST).append(": ")
        .append(this.gapList.toJson().replace("\n", "\n  "))
        .append("\n");
    sb.append("}");
    return sb.toString();
  }
}

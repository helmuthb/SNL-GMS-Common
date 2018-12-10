package gms.dataacquisition.cssloader.data;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.ChannelSegmentType;
import java.util.Map;

/**
 * Class with an enum for the 'segtype' field of CSS and a mapping from 'segtype' to
 * ChannelSegment.ChannelSegmentType.
 */
public class SegmentType {

  /**
     * Enumeration of the segment types (flatfilereaders.segtype) for CSS flatfilereaders entries.
   */
  public enum CssSegtype {
    o, v, s, d;
  }

  private static final Map<CssSegtype, ChannelSegmentType> fromCss = Map.of(
      CssSegtype.o, ChannelSegment.ChannelSegmentType.ACQUIRED,
      CssSegtype.v, ChannelSegment.ChannelSegmentType.FK_BEAM);  // TODO: is this value right?
  // TODO: uncomment and add enum values to OSD project
  //.put(CssSegtype.s, ChannelSegment.ChannelSegmentType.SEGMENTED)
  //.put(CssSegtype.d, ChannelSegment.ChannelSegmentType.DUPLICATED)

  public static ChannelSegment.ChannelSegmentType segmentTypeFor(CssSegtype cssSegType) {
    return fromCss.get(cssSegType);
  }
}

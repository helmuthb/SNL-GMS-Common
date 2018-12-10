package gms.dataacquisition.cssloader.data;


import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Ridiculously simple class that groups a Set of ChannelSegment and a Set of
 * AcquiredChannelSohBoolean. Both are immutable - using Collections.unmodifiableSet. The only
 * purpose of the class is to group these two things (both can come from a Wfdisc row) for a
 * convenient return type. Created by jwvicke on 10/19/17.
 */
public class SegmentAndSohBatch {

  public final Set<ChannelSegment> segments;
  public final Set<AcquiredChannelSohBoolean> sohs;

  public SegmentAndSohBatch(Set<ChannelSegment> segments, Set<AcquiredChannelSohBoolean> sohs) {
    this.segments = Collections.unmodifiableSet(Objects.requireNonNull(segments));
    this.sohs = Collections.unmodifiableSet(Objects.requireNonNull(sohs));
  }
}

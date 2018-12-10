package gms.core.signalenhancement.waveformfiltering;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.utilities.signalprocessing.filter.Filter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps algorithm logic from {@link Filter} to filter waveforms
 */
public class LinearWaveformFilterPlugin {

  private static final Logger logger = LoggerFactory
      .getLogger(LinearWaveformFilterPlugin.class);

  /**
   * Uses the wrapped {@link LinearWaveformFilterPlugin} to generate a sequence of filtered
   * waveforms
   *
   * @param channelSegment {@link ChannelSegment} containing waveforms to filter, not null
   * @param filterDefinition {@link FilterDefinition}, containing filter parameters, not null
   * @return New filtered waveforms
   * @throws NullPointerException if channelSegment or filterDefinition is null
   */
  public Collection<Waveform> filter(ChannelSegment channelSegment,
      FilterDefinition filterDefinition) {

    Objects.requireNonNull(channelSegment,
        "LinearWaveformFilterPlugin cannot filter with null channelSegment");
    Objects.requireNonNull(filterDefinition,
        "LinearWaveformFilterPlugin cannot filter with null filterDefinition");

    ArrayList<Waveform> filteredWaveforms = new ArrayList<Waveform>();
    for (Waveform waveform : channelSegment.mergeWaveforms(1.0e-7, 0.5).getWaveforms()) {
      // If one waveform errors out, then it will be skipped and keep processing
      try {
        filteredWaveforms.add(Filter.filter(waveform, filterDefinition));
      } catch (Exception e) {
        logger.error("Filtering failed with error: " + e.getMessage());
        logger.warn("Skipping waveform {" + waveform + "} and continuing to next waveform");
      }
    }

    return filteredWaveforms;
  }
}

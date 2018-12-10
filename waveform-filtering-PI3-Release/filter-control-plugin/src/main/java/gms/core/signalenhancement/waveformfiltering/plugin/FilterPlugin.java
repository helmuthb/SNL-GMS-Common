package gms.core.signalenhancement.waveformfiltering.plugin;

import gms.core.signalenhancement.waveformfiltering.objects.PluginVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.Collection;

public interface FilterPlugin {


  /**
   * returns the name of the plugin, which combined with the version gives us identification for
   * this plugin.
   *
   * @return The name of the plugin.
   */
  String getName();

  /**
   * returns the version of the plugin, which combined with the name gives us identification for
   * this plugin.
   *
   * @return The version of the plugin.
   */
  PluginVersion getVersion();

  /**
   * Initialize this plugin using the provided {@link PluginConfiguration}. Used to configure how
   * the plugin should run.
   *
   * @param configuration Plugin configuration.
   */
  void initialize(PluginConfiguration configuration);

  /**
   * Generates a sequence of filtered waveforms
   *
   * @param channelSegment {@link ChannelSegment} containing waveforms to filter, not null
   * @param filterDefinition {@link FilterDefinition}, containing filter parameters, not null
   * @return New filtered waveforms
   */
  Collection<Waveform> filter(ChannelSegment channelSegment, FilterDefinition filterDefinition);

}

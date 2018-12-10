package gms.core.signaldetection.signaldetectorcontrol.plugin;

import gms.core.signaldetection.signaldetectorcontrol.objects.PluginVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.time.Instant;
import java.util.Collection;

public interface SignalDetectorPlugin {


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

  // TODO: Signal Detection is configurable by: region, station, channel, time of day, time of year.
  // Consider providing a Network as part of PluginConfiguration so the plugin can determine the
  // correct parameters for a ChannelSegment.
  /**
   * Initialize this plugin using the provided {@link PluginConfiguration}. Used to configure how
   * the plugin should run.
   *
   * @param configuration Plugin configuration.
   */
  void initialize(PluginConfiguration configuration);

  /**
   * Detects signal arrival times on the input {@link ChannelSegment}.  May use metadata from
   * the {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel}
   * to influence processing.
   * @param channelSegment detect signal arrivals in this ChannelSegment, not null
   * @return Collection of {@link Instant} signal arrival times, not null
   */
  Collection<Instant> detectSignals(ChannelSegment channelSegment);
}

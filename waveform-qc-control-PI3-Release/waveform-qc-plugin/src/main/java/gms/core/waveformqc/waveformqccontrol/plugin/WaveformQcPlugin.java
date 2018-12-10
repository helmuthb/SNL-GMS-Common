package gms.core.waveformqc.waveformqccontrol.plugin;

import gms.core.waveformqc.waveformqccontrol.objects.PluginVersion;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Plugin interface for creating waveform quality control masks
 */
public interface WaveformQcPlugin {

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
   * Generates a sequence of QC masks
   *
   * @param channelSegments The collection of waveforms to be masked
   * @param waveformQcChannelSohStatuses The channel SOH data for the waveform data
   * @param qcMasks Previously created {@link QcMask}s that can affect processing.
   */
  Stream<QcMask> generateQcMasks(Collection<ChannelSegment> channelSegments,
      Collection<WaveformQcChannelSohStatus> waveformQcChannelSohStatuses,
      Collection<QcMask> qcMasks,
      UUID creationInfoId); //NEW
}

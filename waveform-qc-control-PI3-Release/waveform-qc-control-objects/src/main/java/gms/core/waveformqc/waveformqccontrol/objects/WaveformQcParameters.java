package gms.core.waveformqc.waveformqccontrol.objects;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Waveform Quality Control Plugin Configuration data object. This object is used to control which
 * plugins are used for a specific processing channel requested by the WaveformQcControl object.
 */
public class WaveformQcParameters {

  private final UUID processingChannelId;
  private final List<RegistrationInfo> waveformQcPlugins;

  private WaveformQcParameters(UUID processingChannelId,
      List<RegistrationInfo> waveformQcPlugins) {
    this.processingChannelId = processingChannelId;
    this.waveformQcPlugins = waveformQcPlugins;
  }

  /**
   * Factory method to create new WaveformQcParameters objects.
   *
   * @param processingChannelId The processing channel id for this configuration (not null).
   * @param waveformQcPlugins The list of plugin {@link RegistrationInfo} objects defined by this
   * configuration (not null).
   */
  public static WaveformQcParameters create(UUID processingChannelId,
      List<RegistrationInfo> waveformQcPlugins) {
    Objects.requireNonNull(processingChannelId,
        "Error Creating WaveformQcParameters, Processing Channel Id cannot be null");
    Objects.requireNonNull(waveformQcPlugins,
        "Error Creating WaveformQcParameters, WaveformQc Plugin List cannot be null");

    return new WaveformQcParameters(processingChannelId, waveformQcPlugins);
  }

  public UUID getProcessingChannelId() {
    return processingChannelId;
  }

  /**
   * Return a stream of {@link RegistrationInfo} objects (Plugin identities) defined by this
   * configuration.
   *
   * @return A stream of {@link RegistrationInfo} objects.
   */
  public Stream<RegistrationInfo> waveformQcPlugins() {
    return waveformQcPlugins.stream();
  }

  /**
   * Return the list of {@link RegistrationInfo} objects (Plugin Identities) defined by this
   * configuration.
   *
   * @return A list of {@link RegistrationInfo} objects.
   */
  public List<RegistrationInfo> getWaveformQcPlugins() {
    return this.waveformQcPlugins;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof WaveformQcParameters)) {
      return false;
    }

    WaveformQcParameters that = (WaveformQcParameters) o;

    return (processingChannelId != null ? processingChannelId.equals(that.processingChannelId)
        : that.processingChannelId == null) && (waveformQcPlugins != null ? waveformQcPlugins
        .equals(that.waveformQcPlugins) : that.waveformQcPlugins == null);
  }

  @Override
  public int hashCode() {
    int result = processingChannelId != null ? processingChannelId.hashCode() : 0;
    result = 31 * result + (waveformQcPlugins != null ? waveformQcPlugins.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "WaveformQcParameters{" +
        "processingChannelId=" + processingChannelId +
        ", waveformQcPlugins=" + waveformQcPlugins +
        '}';
  }
}

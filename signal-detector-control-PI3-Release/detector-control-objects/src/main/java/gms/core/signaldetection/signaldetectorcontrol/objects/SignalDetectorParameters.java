package gms.core.signaldetection.signaldetectorcontrol.objects;


import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Signal Detector Control Plugin Configuration data object. This object is used to control which
 * plugins are used for a specific processing channel requested by the SignalDetectorControl object.
 */
public class SignalDetectorParameters {
  private final UUID processingChannelId;
  private final List<RegistrationInfo> signalDetectorPlugins;

  private SignalDetectorParameters(UUID processingChannelId,
      List<RegistrationInfo> signalDetectorPlugins) {
    this.processingChannelId = processingChannelId;
    this.signalDetectorPlugins = signalDetectorPlugins;
  }

  /**
   * Factory method to create new SignalDetectorParameters objects.
   *
   * @param processingChannelId The processing channel id for this configuration (not null).
   * @param signalDetectorPlugins The list of plugin {@link RegistrationInfo} objects defined by this
   * configuration (not null).
   */
  public static SignalDetectorParameters create(UUID processingChannelId,
      List<RegistrationInfo> signalDetectorPlugins) {
    Objects.requireNonNull(processingChannelId,
        "Error Creating SignalDetectorParameters, Processing Channel Id cannot be null");
    Objects.requireNonNull(signalDetectorPlugins,
        "Error Creating SignalDetectorParameters, Signal Detector Plugin List cannot be null");

    return new SignalDetectorParameters(processingChannelId, signalDetectorPlugins);
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
  public Stream<RegistrationInfo> signalDetectorPlugins() {
    return signalDetectorPlugins.stream();
  }

  /**
   * Return the list of {@link RegistrationInfo} objects (Plugin Identities) defined by this
   * configuration.
   *
   * @return A list of {@link RegistrationInfo} objects.
   */
  public List<RegistrationInfo> getSignalDetectorPlugins() {
    return this.signalDetectorPlugins;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SignalDetectorParameters)) {
      return false;
    }

    SignalDetectorParameters that = (SignalDetectorParameters) o;

    if (!getProcessingChannelId().equals(that.getProcessingChannelId())) {
      return false;
    }
    return getSignalDetectorPlugins().equals(that.getSignalDetectorPlugins());
  }

  @Override
  public int hashCode() {
    int result = getProcessingChannelId().hashCode();
    result = 31 * result + getSignalDetectorPlugins().hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "SignalDetectorParameters{" +
        "processingChannelId=" + processingChannelId +
        ", signalDetectorPlugins=" + signalDetectorPlugins +
        '}';
  }
}
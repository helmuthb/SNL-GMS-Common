package gms.core.signaldetection.signaldetectorcontrol.objects;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SignalDetectorConfiguration {

  public SignalDetectorConfiguration() {
  }

  public Optional<SignalDetectorParameters> createParameters(UUID processingChannelId) {
    RegistrationInfo staLtaPowerDetectorPlugin = RegistrationInfo
        .from("staLtaPowerDetectorPlugin", PluginVersion.from(1, 0, 0));

    //TODO: Add more signal detector plugins here

    SignalDetectorParameters parameters = SignalDetectorParameters.create(processingChannelId,
        List.of(staLtaPowerDetectorPlugin));

    return Optional.of(parameters);
  }
}
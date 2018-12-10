package gms.core.waveformqc.waveformqccontrol.objects;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WaveformQcConfiguration {

  public WaveformQcConfiguration() {
  }

  public Optional<WaveformQcParameters> createParameters(UUID processingChannelId) {
    RegistrationInfo channelSohRegistrationInfo = RegistrationInfo
        .from("channelSohQcPlugin", PluginVersion.from(1, 0, 0));

    RegistrationInfo waveformGapQcPlugin = RegistrationInfo
        .from("waveformGapQcPlugin", PluginVersion.from(1, 0, 0));

    RegistrationInfo waveformRepeatedAmplitudeQcPlugin = RegistrationInfo
        .from("waveformRepeatedAmplitudeQcPlugin", PluginVersion.from(1, 0, 0));

    RegistrationInfo waveformSpike3PtQcPlugin = RegistrationInfo
            .from("waveformSpike3PtQcPlugin", PluginVersion.from(1, 0, 0));

    WaveformQcParameters parameters = WaveformQcParameters.create(processingChannelId,
        List.of(channelSohRegistrationInfo, waveformGapQcPlugin,
            waveformRepeatedAmplitudeQcPlugin, waveformSpike3PtQcPlugin));

    return Optional.of(parameters);
  }
}

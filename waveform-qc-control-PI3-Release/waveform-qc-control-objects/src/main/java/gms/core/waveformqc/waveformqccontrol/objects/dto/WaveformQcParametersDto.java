package gms.core.waveformqc.waveformqccontrol.objects.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.core.waveformqc.waveformqccontrol.objects.RegistrationInfo;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcParameters;
import java.util.List;
import java.util.UUID;


public class WaveformQcParametersDto {

  @JsonCreator
  static WaveformQcParameters create(
      @JsonProperty("processingChannelId") UUID processingChannelId,
      @JsonProperty("waveformQcPlugins") List<RegistrationInfo> waveformQcPlugins) {
    return WaveformQcParameters.create(processingChannelId, waveformQcPlugins);
  }
}

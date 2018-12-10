package gms.core.waveformqc.waveformqccontrol.objects.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.core.waveformqc.waveformqccontrol.objects.PluginVersion;
import gms.core.waveformqc.waveformqccontrol.objects.RegistrationInfo;

public interface RegistrationInfoDto {

  @JsonCreator
  static RegistrationInfo from(
      @JsonProperty("name") String name,
      @JsonProperty("version") PluginVersion version) {
    return RegistrationInfo.from(name, version);
  }
}

package gms.core.waveformqc.waveformqccontrol.objects.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.core.waveformqc.waveformqccontrol.objects.PluginVersion;

public interface PluginVersionDto {

  @JsonCreator
  static PluginVersion from(
      @JsonProperty("major") Integer major,
      @JsonProperty("minor") Integer minor,
      @JsonProperty("patch") Integer patch) {
    return PluginVersion.from(major, minor, patch);
  }
}

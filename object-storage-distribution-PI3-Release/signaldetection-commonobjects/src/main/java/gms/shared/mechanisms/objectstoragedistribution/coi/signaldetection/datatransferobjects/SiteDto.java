package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import java.util.List;
import java.util.UUID;

public interface SiteDto {

  @JsonCreator
  static Site from(
      @JsonProperty("id") UUID id,
      @JsonProperty("name") String name,
      @JsonProperty("latitude") double latitude,
      @JsonProperty("longitude") double longitude,
      @JsonProperty("elevation") double elevation,
      @JsonProperty("channels") List<Channel> channels) {
    return Site.from(id, name, latitude, longitude, elevation, channels);
  }

}


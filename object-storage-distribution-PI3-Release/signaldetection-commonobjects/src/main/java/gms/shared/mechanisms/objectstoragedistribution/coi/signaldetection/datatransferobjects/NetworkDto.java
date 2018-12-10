package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Network;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkOrganization;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkRegion;
import java.util.List;
import java.util.UUID;

public interface NetworkDto {

  @JsonCreator
  static Network from(
      @JsonProperty("id") UUID id,
      @JsonProperty("name") String name,
      @JsonProperty("organization") NetworkOrganization organization,
      @JsonProperty("region") NetworkRegion region,
      @JsonProperty("stations") List<Station> stations) {
    return Network.from(id, name, organization, region, stations);
  }

}


package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Network;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkOrganization;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkRegion;
import java.util.List;
import java.util.UUID;

public interface StationDto {

  @JsonCreator
  static Station from(
      @JsonProperty("id") UUID id,
      @JsonProperty("name") String name,
      @JsonProperty("latitude") double latitude,
      @JsonProperty("longitude") double longitude,
      @JsonProperty("elevation") double elevation,
      @JsonProperty("sites") List<Site> sites) {
    return Station.from(id, name, latitude, longitude, elevation, sites);
  }

}


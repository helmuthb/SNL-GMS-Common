package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.converters;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Network;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetwork;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.Validate;

/**
 * Converts ReferenceNetwork -> Network.
 */
public class NetworkConverter {

  /**
   * Converts a ReferenceNetwork to a Network without any stations populated.
   * @param n the reference network
   * @return a Network
   */
  public static Network withoutStations(ReferenceNetwork n) {
    Validate.notNull(n);

    return Network.from(n.getVersionId(), n.getName(), n.getOrganization(),
        n.getRegion(), new ArrayList<>());
  }

  /**
   * Converts a ReferenceNetwork to a Network with any stations populated.
   * @param n the reference network
   * @param stations the stations
   * @return a Network
   */
  public static Network withStations(ReferenceNetwork n, List<Station> stations) {
    Validate.notNull(n);

    return Network.from(n.getVersionId(), n.getName(), n.getOrganization(),
        n.getRegion(), stations);
  }

}

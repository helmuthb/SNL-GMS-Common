package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.converters;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.Validate;

/**
 * Converts ReferenceStation -> Station.
 */
public class StationConverter {

  /**
   * Converts a ReferenceStation to a Station, without any Site's populated.
   * @param s the reference station
   * @return a Station
   */
  public static Station withoutSites(ReferenceStation s) {
    Validate.notNull(s);

    return Station.from(s.getVersionId(), s.getName(), s.getLatitude(),
        s.getLongitude(), s.getElevation(),
        new ArrayList<>());
  }

  /**
   * Converts a ReferenceStation to a Station, with Site's populated.
   * @param s the reference station
   * @param sites the sites
   * @return a Station
   */
  public static Station withSites(ReferenceStation s, List<Site> sites) {
    Validate.notNull(s);

    return Station.from(s.getVersionId(), s.getName(), s.getLatitude(),
        s.getLongitude(), s.getElevation(), sites);
  }

}

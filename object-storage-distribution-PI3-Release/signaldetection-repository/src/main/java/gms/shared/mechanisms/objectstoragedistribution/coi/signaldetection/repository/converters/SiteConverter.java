package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.converters;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSite;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.Validate;

/**
 * Converts ReferenceSite -> Site.
 */
public class SiteConverter {

  /**
   * Converts a ReferenceSite to a Site, without any Channel's populated.
   * @param s the reference site
   * @return a Site
   */
  public static Site withoutChannels(ReferenceSite s) {
    Validate.notNull(s);

    return Site.from(s.getVersionId(), s.getName(), s.getLatitude(),
        s.getLongitude(), s.getElevation(), new ArrayList<>());
  }

  /**
   * Converts a ReferenceSite to a Site, with Channel's populated.
   * @param s the reference site
   * @param channels the channels
   * @return a Site
   */
  public static Site withChannels(ReferenceSite s, List<Channel> channels) {
    Validate.notNull(s);

    return Site.from(s.getVersionId(), s.getName(), s.getLatitude(),
        s.getLongitude(), s.getElevation(), channels);
  }

}

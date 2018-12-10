package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.converters;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Calibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Response;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import org.apache.commons.lang3.Validate;

/**
 * Converts ReferenceChannel -> Channel.
 */
public class ChannelConverter {

  /**
   * Converts a ReferenceChannel to a Channel.
   * @param refChan the reference channel
   * @return a Channel
   */
  public static Channel from(ReferenceChannel refChan, Response response,
      Calibration calibration) {

    Validate.notNull(refChan);
    Validate.notNull(response);
    Validate.notNull(calibration);

    return Channel.from(refChan.getVersionId(), refChan.getName(), refChan.getType(),
        refChan.getDataType(), refChan.getLatitude(), refChan.getLongitude(),
        refChan.getElevation(), refChan.getDepth(), refChan.getVerticalAngle(),
        refChan.getHorizontalAngle(), refChan.getNominalSampleRate(),
        response, calibration);
  }
}

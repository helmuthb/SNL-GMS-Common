package gms.dataacquisition.stationreceiver.osdgateway;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;


public interface StationReceiverOsdGatewayInterface {

  /**
   * Stores a Set of ChannelSegment.
   * @param segments the segments to store
   * @throws Exception
   *
   */
  void storeChannelSegments(Collection<ChannelSegment> segments) throws Exception;

  /**
   * Stores a set of Channel State-Of-Health (with analog status).
   *
   * @param sohs the soh's to store
   * @throws Exception
   */
  void storeAnalogChannelStatesOfHealth(Collection<AcquiredChannelSohAnalog> sohs)
  throws Exception;

  /**
   * Stores a set of Channel State-Of-Health (with boolean status).
   *
   * @param sohs the soh's to store
   * @throws Exception
   */
  void storeBooleanChannelStatesOfHealth(Collection<AcquiredChannelSohBoolean> sohs)
  throws Exception;

  /**
   * Stores a set of Channel State-Of-Health (may contain both analog and boolean status).
   *
   * @param sohs the soh's to store
   * @throws Exception
   */
  void storeChannelStatesOfHealth(Collection<AcquiredChannelSoh> sohs) throws Exception;

  /**
   * Stores a raw station data frame.
   *
   * @param frame the frame to store
   * @throws Exception
   */
  void storeRawStationDataFrame(RawStationDataFrame frame) throws Exception;

  /**
   * Looks up a station given it's name (for the present time)
   *
   * @param stationName the name of the station
   * @return the station, or null if none by that name is found (for the present).
   */
  Optional<UUID> getStationId(String stationName) throws Exception;

  /**
   * Finds the id of the channel with the given name that belongs to the given site name.  Time is
   * used to narrow the result to a single configuration that existed at that point in time.
   *
   * @param siteName name of the site
   * @param channelName name of the channel
   * @param time time to pull the definition for
   * @return UUID of the matching channel, or null if none by that name is found (for the present).
   */
  Optional<UUID> getChannelId(String siteName, String channelName, Instant time) throws Exception;

}

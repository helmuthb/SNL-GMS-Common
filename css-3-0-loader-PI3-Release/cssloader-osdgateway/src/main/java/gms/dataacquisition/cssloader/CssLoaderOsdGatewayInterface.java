package gms.dataacquisition.cssloader;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceCalibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetwork;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetworkMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSensor;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSite;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSiteMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStationMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

/**
 * OSD gateway interface for the CSS loader.
 */
public interface CssLoaderOsdGatewayInterface {

  /**
   * Stores a Set of ChannelSegment.
   *
   * @param segments the segments to store
   */
  void storeChannelSegments(Collection<ChannelSegment> segments) throws Exception;

  /**
   * Stores a set of Channel State-Of-Health (with boolean status).
   *
   * @param sohs the soh's to store
   */
  void storeChannelStatesOfHealth(Collection<AcquiredChannelSohBoolean> sohs) throws Exception;

  /**
   * Finds the id of the channel with the given name that belongs to the given site name.  Time is
   * used to narrow the result to a single configuration that existed at that point in time.
   *
   * @param siteName name of the site
   * @param channelName name of the channel
   * @param time time to pull the definition for
   * @return UUID of the matching channel, or null if none exists.
   */
  UUID idForChannel(String siteName, String channelName, Instant time) throws Exception;

  /**
   * Stores a ReferenceNetwork.
   * @param network the network
   */
  void storeNetwork(ReferenceNetwork network) throws Exception;

  /**
   * Stores a ReferenceStation.
   * @param station the station
   */
  void storeStation(ReferenceStation station) throws Exception;

  /**
   * Stores a ReferenceSite.
   * @param site the site
   */
  void storeSite(ReferenceSite site) throws Exception;

  /**
   * Stores a ReferenceChannel.
   * @param channel the channel
   */
  void storeChannel(ReferenceChannel channel) throws Exception;

  /**
   * Stores a calibration
   * @param calibration the calibration
   */
  void storeCalibration(ReferenceCalibration calibration) throws Exception;

  /**
   * Stores a response
   * @param response the response
   */
  void storeResponse(ReferenceResponse response) throws Exception;

  /**
   * Stores a sensor
   * @param sensor the sensor
   */
  void storeSensor(ReferenceSensor sensor) throws Exception;

  /**
   * Stores network memberships
   * @param memberships the memberships
   */
  void storeNetworkMemberships(Collection<ReferenceNetworkMembership> memberships)
      throws Exception;

  /**
   * Stores station memberships
   * @param memberships the memberships
   */
  void storeStationMemberships(Collection<ReferenceStationMembership> memberships)
      throws Exception;

  /**
   * Stores site memberships
   * @param memberships the memberships
   */
  void storeSiteMemberships(Collection<ReferenceSiteMembership> memberships)
      throws Exception;
}

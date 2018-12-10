package gms.dataacquisition.cssloader.osdgateway;

import gms.dataacquisition.cssloader.CssLoaderOsdGatewayInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.ProcessingStationReferenceFactoryInterface;
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
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.StationReferenceRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.StationSohRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepositoryInterface;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.Validate;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;

/**
 * OSD gateway for the CssLoaderOsdGatewayService.
 */
public class CssLoaderOsdGateway implements CssLoaderOsdGatewayInterface {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CssLoaderOsdGateway.class);
  private final WaveformRepositoryInterface waveformPersistence;
  private final StationSohRepositoryInterface sohPersistence;
  private final ProcessingStationReferenceFactoryInterface stationFactory;
  private final StationReferenceRepositoryInterface stationRefRepo;

  /**
   * Creates a CssLoaderOsdGateway.
   *
   * @param waveformPersistence the interface for OSD persistence of Channel Acquisition objects,
   * such as ChannelSegment's.
   * @param sohPersistence the interface for OSD persistence of Station State-Of-Health (SOH)
   * objects.
   * @param stationFactory the interface to find station reference information.
   * @throws NullPointerException if either of the input arguments are null
   */
  public CssLoaderOsdGateway(
      WaveformRepositoryInterface waveformPersistence,
      StationSohRepositoryInterface sohPersistence,
      ProcessingStationReferenceFactoryInterface stationFactory,
      StationReferenceRepositoryInterface stationRefRepo) throws NullPointerException {

    this.waveformPersistence = Objects.requireNonNull(waveformPersistence);
    this.sohPersistence = Objects.requireNonNull(sohPersistence);
    this.stationFactory = Objects.requireNonNull(stationFactory);
    this.stationRefRepo = Objects.requireNonNull(stationRefRepo);
  }

  /**
   * @inheritDoc
   */
  @Override
  public void storeChannelSegments(Collection<ChannelSegment> segments) throws Exception {
    Validate.notNull(segments);
    for (ChannelSegment cs : segments) {
      this.waveformPersistence.storeChannelSegment(cs);
    }
  }

  /**
   * @inheritDoc
   */
  @Override
  public void storeChannelStatesOfHealth(Collection<AcquiredChannelSohBoolean> sohs)
      throws Exception {
    Validate.notNull(sohs);
    for (AcquiredChannelSohBoolean soh : sohs) {
      this.sohPersistence.storeBooleanSoh(soh);
    }
  }

  /**
   * @inheritDoc
   */
  @Override
  public UUID idForChannel(String siteName, String channelName, Instant time)
      throws Exception {
    // pass both 'actual change time' and 'system change time' as the same value.
    Optional<Site> val = this.stationFactory.siteFromName(siteName, time, time);
    if (!val.isPresent()) {
      logger.warn("Could not find site with name " + siteName
          + " and time " + time);
      return null;
    }
    Site site = val.get();
    Optional<Channel> chan = site.getChannels().stream()
        .filter(c -> c.getName().equals(channelName))
        .findFirst();
    if (!chan.isPresent()) {
      logger.warn(String.format(
          "Could not find channel with name %s from station/site %s and time %s",
          channelName, siteName, time));
      return null;
    }
    return chan.get().getId();
  }

  /**
   * @inheritDoc
   */
  @Override
  public void storeNetwork(ReferenceNetwork network) throws Exception {
    Validate.notNull(network);
    this.stationRefRepo.storeReferenceNetwork(network);
  }

  /**
   * @inheritDoc
   */
  @Override
  public void storeStation(ReferenceStation station) throws Exception {
    Validate.notNull(station);
    this.stationRefRepo.storeReferenceStation(station);
  }

  /**
   * @inheritDoc
   */
  @Override
  public void storeSite(ReferenceSite site) throws Exception {
    Validate.notNull(site);
    this.stationRefRepo.storeReferenceSite(site);
  }

  /**
   * @inheritDoc
   */
  @Override
  public void storeChannel(ReferenceChannel channel) throws Exception {
    Validate.notNull(channel);
    this.stationRefRepo.storeReferenceChannel(channel);
  }

  /**
   * @inheritDoc
   */
  @Override
  public void storeCalibration(ReferenceCalibration calibration) throws Exception {
    Validate.notNull(calibration);
    this.stationRefRepo.storeCalibration(calibration);
  }

  /**
   * @inheritDoc
   */
  @Override
  public void storeResponse(ReferenceResponse response) throws Exception {
    Validate.notNull(response);
    this.stationRefRepo.storeResponse(response);
  }

  /**
   * @inheritDoc
   */
  @Override
  public void storeSensor(ReferenceSensor sensor) throws Exception {
    Validate.notNull(sensor);
    this.stationRefRepo.storeSensor(sensor);
  }

  /**
   * @inheritDoc
   */
  @Override
  public void storeNetworkMemberships(Collection<ReferenceNetworkMembership> memberships)
      throws Exception {
    Validate.notNull(memberships);
    for (ReferenceNetworkMembership m : memberships) {
      this.stationRefRepo.storeNetworkMembership(m);
    }
  }

  /**
   * @inheritDoc
   */
  @Override
  public void storeStationMemberships(Collection<ReferenceStationMembership> memberships)
      throws Exception {
    Validate.notNull(memberships);
    for (ReferenceStationMembership m : memberships) {
      this.stationRefRepo.storeStationMembership(m);
    }
  }

  /**
   * @inheritDoc
   */
  @Override
  public void storeSiteMemberships(Collection<ReferenceSiteMembership> memberships)
      throws Exception {
    Validate.notNull(memberships);
    for (ReferenceSiteMembership m : memberships) {
      this.stationRefRepo.storeSiteMembership(m);
    }
  }
}

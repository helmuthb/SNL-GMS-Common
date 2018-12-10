package gms.dataacquisition.stationreceiver.osdgateway.service;

import gms.dataacquisition.stationreceiver.osdgateway.StationReceiverOsdGatewayInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.ProcessingStationReferenceFactoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RawStationDataFrameRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.StationSohRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepositoryInterface;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.LoggerFactory;

/**
 * OSD gateway for the OSD gateway service to use - this is how the service stores things.
 */
public class StationReceiverOsdGateway implements StationReceiverOsdGatewayInterface {

  private static final org.slf4j.Logger logger = LoggerFactory
      .getLogger(StationReceiverOsdGateway.class);

  private final StationSohRepositoryInterface sohPersistence;
  private final WaveformRepositoryInterface waveformPersistence;
  private final RawStationDataFrameRepositoryInterface dataFrameRepository;
  private final ProcessingStationReferenceFactoryInterface referenceFactory;

  /**
   * Creates a StationReceiverOsdGateway.
   *
   * @param sohPersistence the interface to store State-Of-Health
   * @param waveformPersistence the interface to store waveforms and channel segment's
   * @param dataFrameRepository the interface to store raw station data frames
   * @param referenceFactory the interface to retrieve station reference information
   */
  public StationReceiverOsdGateway(StationSohRepositoryInterface sohPersistence,
      WaveformRepositoryInterface waveformPersistence,
      RawStationDataFrameRepositoryInterface dataFrameRepository,
      ProcessingStationReferenceFactoryInterface referenceFactory) {
    this.sohPersistence = Objects.requireNonNull(sohPersistence);
    this.waveformPersistence = Objects.requireNonNull(waveformPersistence);
    this.dataFrameRepository = Objects.requireNonNull(dataFrameRepository);
    this.referenceFactory = Objects.requireNonNull(referenceFactory);
    ;
  }

  /**
   * Stores a set of channel segments.
   *
   * @param segments the segments to store
   * @throws Exception
   */
  @Override
  public void storeChannelSegments(Collection<ChannelSegment> segments) throws Exception {
    for (ChannelSegment cs : segments) {
      this.waveformPersistence.storeChannelSegment(cs);
    }
  }

  /**
   * Stores a set of analog SOH.
   *
   * @param sohs the soh's to store
   * @throws Exception
   */
  @Override
  public void storeAnalogChannelStatesOfHealth(Collection<AcquiredChannelSohAnalog> sohs)
    throws Exception {
    for (AcquiredChannelSohAnalog soh : sohs) {
      this.sohPersistence.storeAnalogSoh(soh);
    }
  }

  /**
   * Stores a set of boolean SOH
   *
   * @param sohs the soh's to store
   * @throws Exception
   */
  @Override
  public void storeBooleanChannelStatesOfHealth(Collection<AcquiredChannelSohBoolean> sohs)
    throws Exception {
    for (AcquiredChannelSohBoolean soh : sohs) {
      this.sohPersistence.storeBooleanSoh(soh);
    }
  }

  /**
   * Stores a set of SOH objects which could be analog or boolean.
   *
   * @param sohs the soh's to store
   * @throws Exception
   */
  @Override
  public void storeChannelStatesOfHealth(Collection<AcquiredChannelSoh> sohs)
    throws Exception {
    for (AcquiredChannelSoh soh : sohs) {
      if (soh instanceof AcquiredChannelSohBoolean) {
        this.sohPersistence.storeBooleanSoh((AcquiredChannelSohBoolean) soh);
      } else if (soh instanceof AcquiredChannelSohAnalog) {
        this.sohPersistence.storeAnalogSoh((AcquiredChannelSohAnalog) soh);
      }
    }
  }

  /**
   * Stores a RawStationDataFrame.
   *
   * @param frame the frame to store
   * @throws Exception
   */
  @Override
  public void storeRawStationDataFrame(RawStationDataFrame frame) throws Exception {
    this.dataFrameRepository.storeRawStationDataFrame(frame);
  }

  /**
   * Retrieves a station by it's name, for the current system time.
   *
   * @param stationName the name of the station
   * @return a Station, or null if none with that name is found (at presetn)
   */
  @Override
  public Optional<UUID> getStationId(String stationName) {
    Optional<Station> sta = this.referenceFactory.stationFromName(stationName);
    return sta.map(x -> x.getId());
//    return sta.isPresent() ? Optional.of(sta.get().getId()) : Optional.empty();
  }

  @Override
  public Optional<UUID> getChannelId(String siteName, String channelName, Instant time) {
    // pass both 'actual change time' and 'system change time' as the same value.
    Optional<Site> val = this.referenceFactory.siteFromName(siteName, time, time);
    if (!val.isPresent()) {
      logger.warn("Could not find site with name " + siteName
          + " and time " + time);
      return Optional.empty();
    }
    Site site = val.get();
    Optional<Channel> chan = site.getChannels().stream()
        .filter(c -> c.getName().equals(channelName))
        .findFirst();
    if (!chan.isPresent()) {
      logger.warn(String.format(
          "Could not find channel with name %s from station/site %s and time %s",
          channelName, siteName, time));
      return Optional.empty();
    }
    return chan.map(x -> x.getId());
  }


}

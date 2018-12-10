package gms.core.signalenhancement.waveformfiltering.osdgateway.gateway;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepositoryInterface;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link OsdGateway}
 */
public class OsdGateway {

  private static final Logger logger = LoggerFactory.getLogger(OsdGateway.class);

  private final WaveformRepositoryInterface waveformRepository;

  private OsdGateway(WaveformRepositoryInterface waveformRepository) {
    this.waveformRepository = waveformRepository;
  }

  /**
   * Obtains a new {@link OsdGateway} that uses the provided {@link WaveformRepositoryInterface}
   *
   * @param waveformRepository a WaveformRepositoryInterface, not null
   * @return an {@link OsdGateway}, not null
   */
  public static OsdGateway create(WaveformRepositoryInterface waveformRepository) {
    Objects.requireNonNull(waveformRepository,
        "OsdGateway requires a non-null WaveformRepositoryInterface");

    return new OsdGateway(waveformRepository);
  }

  /**
   * Storage operation. Stores all {@link ChannelSegment}s and {@link CreationInformation}s at the
   * appropriate visibility.
   *
   * @param channelSegments Channel Segments with derived channels to store in the osd
   */
  public void store(Collection<ChannelSegment> channelSegments,
      StorageVisibility storageVisibility) {

    Objects.requireNonNull(channelSegments, "Channel Segments cannot be null");
    Objects.requireNonNull(storageVisibility, "Storage Visibility cannot be null");

    channelSegments.forEach(
        cs -> logger.info("Storing ChannelSegment with {} waveforms", cs.getWaveforms().size()));

    for (ChannelSegment channelSegment : channelSegments) {
      try {
        waveformRepository.storeChannelSegment(channelSegment);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection<ChannelSegment> loadInvokeInputData(Collection<UUID> channelIds,
      Instant startTime,
      Instant endTime) {

    Objects.requireNonNull(channelIds, "Error loading InvokeInputData, channelIds cannot be null");
    Objects.requireNonNull(startTime, "Error loading InvokeInputData, Start Time cannot be null");
    Objects.requireNonNull(endTime, "Error loading InvokeInputData, End Time cannot be null");

    Set<ChannelSegment> set = new HashSet<>();
    for (UUID id : channelIds) {
      Optional<ChannelSegment> channelSegment;
      try {
        channelSegment = waveformRepository
            .retrieveChannelSegment(id, startTime, endTime, true);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      channelSegment.ifPresent(set::add);
    }
    return set;
  }
}

package gms.core.signaldetection.signaldetectorcontrol.osdgateway.gateway;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.SignalDetectionRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepositoryInterface;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of {@link OsdGateway}
 */
public class OsdGateway {

  private final WaveformRepositoryInterface waveformRepository;
  private final SignalDetectionRepository signalDetectionRepository;

  private OsdGateway(WaveformRepositoryInterface waveformRepository,
      SignalDetectionRepository signalDetectionRepository) {
    this.waveformRepository = waveformRepository;
    this.signalDetectionRepository = signalDetectionRepository;
  }

  /**
   * Obtains a new {@link OsdGateway} that uses the provided {@link WaveformRepositoryInterface} and
   * {@link SignalDetectionRepository}
   *
   * @param waveformRepository a WaveformRepositoryInterface, not null
   * @param signalDetectionRepository a SignalDetectionRepository, not null
   * @return an {@link OsdGateway}, not null
   */
  public static OsdGateway create(WaveformRepositoryInterface waveformRepository,
      SignalDetectionRepository signalDetectionRepository) {
    Objects.requireNonNull(waveformRepository,
        "OsdGateway requires a non-null WaveformRepository");
    Objects.requireNonNull(signalDetectionRepository,
        "OsdGateway requires a non-null SignalDetectionRepository");

    return new OsdGateway(waveformRepository, signalDetectionRepository);
  }

  /**
   * Storage operation. Stores all {@link SignalDetection}s and {@link CreationInformation}s at the
   * appropriate visibility.
   *
   * @param signalDetections Signal Detections to store in the osd
   */
  public void store(Collection<SignalDetection> signalDetections,
      StorageVisibility storageVisibility) {

    Objects.requireNonNull(signalDetections, "Signal Detections cannot be null");
    Objects.requireNonNull(storageVisibility, "Storage Visibility cannot be null");

    signalDetections.forEach(signalDetectionRepository::store);
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

    Set<ChannelSegment> channelSegments = new HashSet<>();

    for (UUID id : channelIds) {

      Optional<ChannelSegment> channelSegment;
      try {
        channelSegment = waveformRepository
            .retrieveChannelSegment(id, startTime, endTime, true);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      channelSegment.ifPresent(channelSegments::add);
    }

    return channelSegments;
  }
}

package gms.core.waveformqc.waveformqccontrol.osdgateway.gateway;

import gms.core.waveformqc.waveformqccontrol.objects.InvokeInputData;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus.Builder;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.ProvenanceRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.QcMaskRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.StationSohRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepositoryInterface;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link OsdGateway}
 */
public class OsdGateway {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(OsdGateway.class);

  private final WaveformRepositoryInterface waveformRepository;
  private final StationSohRepositoryInterface stationSohRepository;
  private final QcMaskRepository qcMaskRepository;
  private final ProvenanceRepository provenanceRepository;

  public OsdGateway(WaveformRepositoryInterface waveformRepository,
      StationSohRepositoryInterface stationSohRepository, QcMaskRepository qcMaskRepository,
      ProvenanceRepository provenanceRepository) {

    Objects.requireNonNull(waveformRepository,
        "OsdGateway requires a non-null WaveformRepositoryInterface");
    Objects.requireNonNull(stationSohRepository,
        "OsdGateway requires a non-null StationSohPersistenceInterface");
    Objects.requireNonNull(qcMaskRepository,
        "OsdGateway requires a non-null QcMaskRepository");
    Objects.requireNonNull(provenanceRepository,
        "OsdGateway requires a non-null ProvenanceRepository");

    this.waveformRepository = waveformRepository;
    this.stationSohRepository = stationSohRepository;
    this.qcMaskRepository = qcMaskRepository;
    this.provenanceRepository = provenanceRepository;
  }

  /**
   * Storage operation. Stores all {@link QcMask}s and {@link CreationInformation}s
   * at the appropriate visibility.
   *
   * @param qcMasks masks to store in the osd
   * @param storageVisibility Visibility of the stored masks (i.e. Public or Private)
   */
  public void store(List<QcMask> qcMasks, List<CreationInformation> creationInfos,
      StorageVisibility storageVisibility) {

    // Where does QcMaskRepositoryJpa class get initialized? In this class' constructor?
    Objects.requireNonNull(qcMasks, "QC Masks cannot be null");
    Objects.requireNonNull(storageVisibility, "Storage Visibility cannot be null");
    Objects.requireNonNull(creationInfos, "Creation Information cannot be null");

    for (QcMask qcMask : qcMasks) {
      qcMaskRepository.store(qcMask);
      //Do we want to immediately return upon the FIRST store failure?
    }

    for (CreationInformation creationInfo : creationInfos) {
      provenanceRepository.store(creationInfo);
    }
  }

  /**
   * {@inheritDoc}
   */
  public InvokeInputData loadInvokeInputData(Set<UUID> processingChannelIds,
      Instant startTime, Instant endTime) {

    Objects.requireNonNull(processingChannelIds,
        "Error loading InvokeInputData, Processing Channel Ids cannot be null");
    Objects.requireNonNull(startTime,
        "Error loading InvokeInputData, Start Time cannot be null");
    Objects.requireNonNull(endTime,
        "Error loading InvokeInputData, End Time cannot be null");

    // TODO: need better exception handling; need standard approach to repository exceptions.
    Function<UUID, Optional<ChannelSegment>> channelSegmentQueryFunction = id -> {
      try {
        return waveformRepository.retrieveChannelSegment(id, startTime, endTime, true);
      } catch (Exception e) {
        logger.info("Trapping WaveformRepository.retrieveChannelSegment exception", e);
        return Optional.empty();
      }
    };

    // TODO: need better exception handling; need standard approach to repository exceptions.
    Function<UUID, List<AcquiredChannelSohBoolean>> sohQueryFunction = id -> {
      try {
        return stationSohRepository.retrieveBooleanSohByProcessingChannelAndTimeRange(id, startTime, endTime);
      } catch (Exception e) {
        logger.info("Trapping stationSohRepository.retrieveChannelSegment exception", e);
        return List.of();
      }
    };

    Function<UUID, Stream<QcMask>> qcMaskQueryFunction = id ->
        qcMaskRepository.findCurrentByProcessingChannelIdAndTimeRange(id, startTime, endTime)
            .stream();

    Set<ChannelSegment> channelSegments = processingChannelIds.stream()
        .map(channelSegmentQueryFunction)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());

    Map<UUID, List<AcquiredChannelSohBoolean>> sohResults = processingChannelIds.stream()
        .collect(Collectors.toMap(Function.identity(), sohQueryFunction));

    Set<QcMask> qcMaskResults = processingChannelIds.stream()
        .flatMap(qcMaskQueryFunction)
        .collect(Collectors.toSet());

    Set<WaveformQcChannelSohStatus> gatewaySohs = sohResults.entrySet().stream()
        .flatMap(e -> buildGatewayObjectsFromCoi(e.getKey(), e.getValue()).stream())
        .collect(Collectors.toSet());

    return InvokeInputData.create(channelSegments, qcMaskResults, gatewaySohs);
  }

  /**
   * Convenience method for creating a list of {@link WaveformQcChannelSohStatus} from a list of
   * {@link AcquiredChannelSohBoolean} and the specified {@link Channel}
   * id.
   *
   * @param processingChannelId Processing Channel Id affiliated with the SOH data.
   * @param acquiredChannelSohBooleans COI SOH data
   * @return Gateway objects representing the SOH data.
   */
  private List<WaveformQcChannelSohStatus> buildGatewayObjectsFromCoi(
      UUID processingChannelId,
      List<AcquiredChannelSohBoolean> acquiredChannelSohBooleans) {

    Map<AcquiredChannelSohType, List<AcquiredChannelSohBoolean>> sohsByType = acquiredChannelSohBooleans
        .stream()
        .collect(Collectors.groupingBy(AcquiredChannelSohBoolean::getType));

    return sohsByType.entrySet().stream()
        .map(e -> buildGatewayObjectFromCoi(processingChannelId, e.getKey(), e.getValue()))
        .collect(Collectors.toList());
  }

  /**
   * Convenience method for creating a {@link WaveformQcChannelSohStatus} from {@link
   * AcquiredChannelSohBoolean}s and the specified {@link AcquiredChannelSohType} and {@link
   * Channel} id.
   *
   * @param processingChannelId Processing Channel Id affiliated with the SOH data.
   * @param type Type of the SOH data
   * @param acquiredChannelSohBooleans COI SOH data
   * @return Gateway objects representing the SOH data.
   */
  private WaveformQcChannelSohStatus buildGatewayObjectFromCoi(UUID processingChannelId,
      AcquiredChannelSohType type, List<AcquiredChannelSohBoolean> acquiredChannelSohBooleans) {

    Iterator<AcquiredChannelSohBoolean> sohIterator = acquiredChannelSohBooleans.iterator();
    AcquiredChannelSohBoolean soh = sohIterator.next();

    // TODO: make Duration a parameter?
    Builder builder = WaveformQcChannelSohStatus
        .builder(processingChannelId, type, soh.getStartTime(), soh.getEndTime(),
            soh.getStatus(), Duration.ofMillis(1000));

    while (sohIterator.hasNext()) {
      soh = sohIterator.next();
      builder.addStatusChange(soh.getStartTime(), soh.getEndTime(), soh.getStatus());
    }

    return builder.build();
  }
}

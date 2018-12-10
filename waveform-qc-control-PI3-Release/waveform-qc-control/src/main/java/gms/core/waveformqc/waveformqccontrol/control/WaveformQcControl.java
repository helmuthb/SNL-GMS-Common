package gms.core.waveformqc.waveformqccontrol.control;

import gms.core.waveformqc.waveformqccontrol.objects.RegistrationInfo;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcConfiguration;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcParameters;
import gms.core.waveformqc.waveformqccontrol.osdgateway.client.InvokeInputDataMap;
import gms.core.waveformqc.waveformqccontrol.osdgateway.client.OsdGatewayClient;
import gms.core.waveformqc.waveformqccontrol.plugin.WaveformQcPlugin;
import gms.core.waveformqc.waveformqccontrol.plugin.WaveformQcPluginRegistry;
import gms.core.waveformqc.waveformqccontrol.util.ExceptionSupplier;
import gms.core.waveformqc.waveformqccontrol.util.Validator;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class following our Control pattern for performing processing in the GMS. This class in
 * particular is responsible for handling quality control on waveforms acquired by the GMS.
 */
public class WaveformQcControl {

  private static final Logger logger = LoggerFactory.getLogger(WaveformQcControl.class);

  private final WaveformQcPluginRegistry registry;
  private final OsdGatewayClient osdGatewayAccess;
  private WaveformQcConfiguration configuration;

  /**
   * Constructs a WaveformQcControl with provided plugin registry and OSD gateway access library.
   *
   * @param registry plugin registry, not null
   * @param osdGatewayAccessLibrary osd gateway access library, not null
   */
  public WaveformQcControl(WaveformQcPluginRegistry registry,
      OsdGatewayClient osdGatewayAccessLibrary) {

    Objects.requireNonNull(registry,
        "WaveformQcControl requires a non-null WaveformQcPluginRegistry");
    Objects.requireNonNull(osdGatewayAccessLibrary,
        "WaveformQcControl requires a non-null OsdGatewayClient");

    this.registry = registry;
    this.osdGatewayAccess = osdGatewayAccessLibrary;
    this.configuration = null;
  }

  /**
   * Initialization method used to set configuration for control class and its bound plugins.
   */
  public void initialize() {
    logger.info("Loading configuration for control class");
    this.configuration = osdGatewayAccess.loadConfiguration();

    logger.info("Loading configuration for plugins");
    registry.entrySet().forEach(e -> e.getPlugin()
        .initialize(osdGatewayAccess.loadPluginConfiguration(e.getRegistration())));
  }

  /**
   * Execute Waveform qc processing using the provided {@link ExecuteCommand}
   *
   * @param command object describing the qc processing request, not null
   * @return stream of new or updated {@link QcMask}, not null
   */
  public List<QcMask> execute(ExecuteCommand command) {
    Objects.requireNonNull(command, "WaveformQcControl cannot execute a null command");

    Objects.requireNonNull(configuration,
        "WaveformQcControl cannot execute with a null ServiceConfiguration");

    logger.info("Executing Waveform QC for: {}", command);

    InvokeInputDataMap invokeInputData = osdGatewayAccess.loadInvokeInputData(
        command.getProcessingChannelIds(), command.getStartTime(), command.getEndTime());

    logger.info("Loaded Invoke Input Data: {}", invokeInputData);

    Set<UUID> processingChannelIds = command.getProcessingChannelIds();
    ProcessingContext processingContext = command.getProcessingContext();

    List<Pair<Stream<QcMask>, CreationInformation>> qcMasksWithProvenance = processingChannelIds
        .stream()
        .flatMap(pc -> execute(pc, invokeInputData, processingContext))
        .collect(Collectors.toList());

    List<QcMask> qcMasks = qcMasksWithProvenance.stream()
        .flatMap(Pair::getKey).collect(Collectors.toList());

    List<CreationInformation> creationInfos = qcMasksWithProvenance.stream()
        .map(Pair::getValue)
        .collect(Collectors.toList());

    StorageVisibility storageVisibility = command.getProcessingContext().getStorageVisibility();

    osdGatewayAccess.store(qcMasks, creationInfos, storageVisibility);

    return qcMasks;
  }

  /**
   * Convenience method for executing Waveform QC on a single ProcessingChannel Id.
   *
   * @param processingChannelId Processing Channel Id to perform Waveform QC on
   * @param invokeInputData Data map holding necessary execution information by Processing Channel
   * Id.
   * @return A Stream containing all masks created by all plugins for this ProcessingChannel Id.
   */
  private Stream<Pair<Stream<QcMask>, CreationInformation>> execute(
      UUID processingChannelId,
      InvokeInputDataMap invokeInputData,
      ProcessingContext processingContext) {

    WaveformQcParameters qcParameters = configuration
        .createParameters(processingChannelId).orElseThrow(ExceptionSupplier.illegalState(
            "Cannot execute Waveform QC. Parameters not found for processing channel"
                + processingChannelId.toString()));

    List<RegistrationInfo> registrationInfos = qcParameters.getWaveformQcPlugins();

    Map<RegistrationInfo, Optional<WaveformQcPlugin>> potentialPlugins = registrationInfos.stream()
        .collect(Collectors.toMap(Function.identity(), registry::lookup));

    List<RegistrationInfo> missingPlugins = potentialPlugins.entrySet().stream()
        .filter(e -> !e.getValue().isPresent())
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());

    Validator.requireTrue(List::isEmpty, missingPlugins,
        ExceptionSupplier.illegalState(
            "Cannot execute Waveform QC. No plugins found for: " + missingPlugins.toString()));

    Set<ChannelSegment> channelSegments = invokeInputData
        .getChannelSegments(processingChannelId).orElseGet(Collections::emptySet);

    Set<WaveformQcChannelSohStatus> sohStatuses =
        invokeInputData.getWaveformQcChannelSohStatuses(processingChannelId)
            .orElseGet(Collections::emptySet);

    Set<QcMask> existingQcMasks = invokeInputData.getQcMasks(processingChannelId)
        .orElseGet(Collections::emptySet);

    return potentialPlugins.values().stream()
        .map(Optional::get)
        .map(p -> execute(p, channelSegments, sohStatuses, existingQcMasks, processingContext));
  }

  /**
   * Base execute method that creates {@link CreationInformation} based on the plugin and processing
   * context, then invokes the plugin to create {@link QcMask}s given the input data and the
   * creation information.
   *
   * @param plugin Plugin used to create QcMasks.
   * @param channelSegments Segments of Channel data provided as input to the plugin.
   * @param sohStatuses State of health status information provided as input to the plugin.
   * @param existingQcMasks Previously stored qc masks provided as iinput to the plugin.
   * @param processingContext Context in which we are running qc processing.
   * @return A Stream of all created QcMasks, and the CreationInformation provenance for each new
   * mask version created.
   */
  private static Pair<Stream<QcMask>, CreationInformation> execute(WaveformQcPlugin plugin,
      Set<ChannelSegment> channelSegments, Set<WaveformQcChannelSohStatus> sohStatuses,
      Set<QcMask> existingQcMasks, ProcessingContext processingContext) {
    logger.info("Running plugin:{}...", plugin);

    CreationInformation creationInformation = createCreationInformation(plugin, processingContext);
    Stream<QcMask> qcMasks = plugin.generateQcMasks(channelSegments, sohStatuses, existingQcMasks,
        creationInformation.getId());

    return Pair.of(qcMasks, creationInformation);
  }

  /**
   * Convenience method for creating {@link CreationInformation} from a {@link WaveformQcPlugin} and
   * a {@link ProcessingContext}
   *
   * @param plugin Plugin used to create the QcMask.
   * @param processingContext Context in which we are processing the data.
   * @return CreationInformation representing how an object was created.
   */
  private static CreationInformation createCreationInformation(WaveformQcPlugin plugin,
      ProcessingContext processingContext) {
    return CreationInformation
        .create(processingContext.getAnalystActionReference(),
            processingContext.getProcessingStepReference(),
            new SoftwareComponentInfo(plugin.getName(), plugin.getVersion().toString()));
  }
}

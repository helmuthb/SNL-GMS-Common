package gms.core.signaldetection.signaldetectorcontrol.control;

import gms.core.signaldetection.signaldetectorcontrol.objects.RegistrationInfo;
import gms.core.signaldetection.signaldetectorcontrol.objects.SignalDetectorConfiguration;
import gms.core.signaldetection.signaldetectorcontrol.objects.SignalDetectorParameters;
import gms.core.signaldetection.signaldetectorcontrol.osdgateway.client.OsdGatewayClient;
import gms.core.signaldetection.signaldetectorcontrol.plugin.SignalDetectorControlPluginRegistry;
import gms.core.signaldetection.signaldetectorcontrol.plugin.SignalDetectorPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignalDetectorControl {

  private static final Logger logger = LoggerFactory.getLogger(SignalDetectorControl.class);

  private final SignalDetectorControlPluginRegistry registry;
  private final OsdGatewayClient osdGatewayAccess;
  private SignalDetectorConfiguration configuration;

  private boolean initialized;

  private SignalDetectorControl(
      SignalDetectorControlPluginRegistry registry,
      OsdGatewayClient osdGatewayAccess) {
    this.registry = registry;
    this.osdGatewayAccess = osdGatewayAccess;
    this.initialized = false;
  }

  /**
   * Initialization method used to set configuration for control class and its bound plugins.
   */
  public void initialize() {
    //TODO: Initialization steps
    logger.info("Loading configuration for control class");
    this.configuration = osdGatewayAccess.loadConfiguration();

    logger.info("Loading configuration for plugins");
    registry.entrySet().forEach(e -> e.getPlugin()
        .initialize(osdGatewayAccess.loadPluginConfiguration(e.getRegistration())));

    initialized = true;
  }

  /**
   * Factory method for creating a SignalDetectorControl
   *
   * @param registry plugin registry, not null
   * @param osdGatewayAccessLibrary osd gateway access library, not null
   * @return a new SignalDetectorControl object
   */
  public static SignalDetectorControl create(SignalDetectorControlPluginRegistry registry,
      OsdGatewayClient osdGatewayAccessLibrary) {

    Objects
        .requireNonNull(registry, "Error creating SignalDetectorControl: registry cannot be null");
    Objects.requireNonNull(osdGatewayAccessLibrary,
        "Error creating SignalDetectorControl: osdGatewayAccessLibrary cannot be null");

    return new SignalDetectorControl(registry, osdGatewayAccessLibrary);
  }

  /**
   * Execute signal detection using the provided {@link ExecuteClaimCheckCommand}
   *
   * @param command object describing the filter processing request, not null
   * @return list of {@link UUID} to generated {@link ChannelSegment}, not null
   */
  public Collection<UUID> execute(ExecuteClaimCheckCommand command) {
    if (!initialized) {
      throw new IllegalStateException("SignalDetectorControl must be initialized before execution");
    }

    Objects.requireNonNull(command, "SignalDetectorControl cannot execute a null ClaimCheck");

    //TODO: What to do if there is no channel segment?
    Set<ChannelSegment> channelSegments = osdGatewayAccess
        .loadChannelSegments(List.of(command.getStationId()), command.getStartTime(),
            command.getEndTime());

    logger.info("SignalDetectorControl ClaimCheck execution processing {} ChannelSegments",
        channelSegments.size());

    return channelSegments.stream()
        .map(cs -> execute(cs, command.getProcessingContext(), cs.getProcessingChannelId()))
        .flatMap(Collection::stream).map(SignalDetection::getId).collect(Collectors.toList());
  }

  /**
   * Execute signal detection using the provided {@link ExecuteStreamingCommand}
   *
   * @param command object describing the qc processing request, not null
   * @return list of generated {@link ChannelSegment}, not null
   */
  public Collection<SignalDetection> execute(ExecuteStreamingCommand command) {
    if (!initialized) {
      throw new IllegalStateException("SignalDetectorControl must be initialized before execution");
    }

    Objects.requireNonNull(command, "SignalDetectorControl cannot execute a null StreamingCommand");

    return execute(command.getChannelSegment(), command.getProcessingContext(),
        command.getChannelSegment().getProcessingChannelId());
  }

  /**
   * Creates a list of signal detections given a channel segment, time range, processing context and
   * processing channel id. Signal detections are created by calling plugins configured for use with
   * the processing channel.
   *
   * @param channelSegment channel segment to process
   * @param processingContext processing context
   * @param processingChannelId processingChannelId to process
   * @return list of SignalDetections
   */
  private Collection<SignalDetection> execute(ChannelSegment channelSegment,
      ProcessingContext processingContext, UUID processingChannelId) {

    logger.info("Performing signal detection on ChannelSegment {}", channelSegment);

    List<SignalDetectorPlugin> plugins = getPlugins(processingChannelId);

    // create creationInformation for each filter plugin
    List<CreationInformation> creationInformationList = new ArrayList<>();
    Set<SignalDetection> signalDetectionSet = new HashSet<>();

    plugins.stream().forEach(plugin -> {

      logger.info("SignalDetectionControl invoking plugin {} {} for Channel {}", plugin.getName(),
          plugin.getVersion(), processingChannelId);

      final CreationInformation creationInformation = createCreationInformation(plugin,
          processingContext);

      creationInformationList.add(creationInformation);

      Collection<SignalDetection> newDetections = plugin.detectSignals(channelSegment).stream()
          .map(arrivalTime -> SignalDetection
              .create("Organization", processingChannelId, "phase",
                  List.of(FeatureMeasurement
                      .create(FeatureMeasurementType.ARRIVAL_TIME, arrivalTime.toEpochMilli(),
                          creationInformation.getId())), creationInformation.getId())).collect(
              Collectors.toSet());

      newDetections.stream().forEach(sd -> logger.info("Created new SignalDetection {}", sd));

      signalDetectionSet.addAll(newDetections);
    });

    osdGatewayAccess.store(signalDetectionSet, creationInformationList,
        processingContext.getStorageVisibility());

    return signalDetectionSet;
  }

  /**
   * Returns a list of detector plugins used for a channel.
   *
   * @param channelId {@link UUID} holding the id to a processing channel
   * @return List of plugins that were found
   */
  private List<SignalDetectorPlugin> getPlugins(
      UUID channelId) {
    Optional<SignalDetectorParameters> signalDetectorParameters = configuration
        .createParameters(channelId);

    if(!signalDetectorParameters.isPresent()) {
      throw new IllegalStateException("No SignalDetectorParameters for channel " + channelId);
    }

    Map<RegistrationInfo, Optional<SignalDetectorPlugin>> potentialPlugins =
        signalDetectorParameters.get().signalDetectorPlugins()
            .collect(Collectors.toMap(Function.identity(), registry::lookup));

    // get list of missingPlugins and throw an error if there are any
    List<RegistrationInfo> missingPlugins = potentialPlugins.entrySet().stream()
        .filter(e -> !e.getValue().isPresent()).map(Map.Entry::getKey)
        .collect(Collectors.toList());

    if (!missingPlugins.isEmpty()) {
      throw new IllegalStateException(
          "Cannot execute Signal Detection. Missing plugins found for: " +
              missingPlugins.toString());
    }

    return potentialPlugins.values().stream().map(Optional::get).collect(Collectors.toList());
  }

  /**
   * Convenience method for creating {@link CreationInformation} from a {@link SignalDetectorPlugin}
   * and a {@link ProcessingContext}. If the processing context is an empty Optional then the
   * analyst action and processing step reference are both set to empty optionals.
   *
   * @param plugin Plugin used to for signal detection.
   * @param processingContext Context in which we are processing the data.
   * @return CreationInformation representing how an object was created.
   */
  private static CreationInformation createCreationInformation(SignalDetectorPlugin plugin,
      ProcessingContext processingContext) {
    return CreationInformation.create(processingContext.getAnalystActionReference(),
        processingContext.getProcessingStepReference(),
        new SoftwareComponentInfo(plugin.getName(), plugin.getVersion().toString()));
  }
}
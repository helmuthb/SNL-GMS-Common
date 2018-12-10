package gms.core.signalenhancement.waveformfiltering.control;

import gms.core.signalenhancement.waveformfiltering.objects.FilterConfiguration;
import gms.core.signalenhancement.waveformfiltering.objects.FilterParameters;
import gms.core.signalenhancement.waveformfiltering.objects.RegistrationInfo;
import gms.core.signalenhancement.waveformfiltering.objects.StreamingFilterPluginParameters;
import gms.core.signalenhancement.waveformfiltering.osdgateway.client.OsdGatewayClient;
import gms.core.signalenhancement.waveformfiltering.plugin.FilterControlPluginRegistry;
import gms.core.signalenhancement.waveformfiltering.plugin.FilterPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.ChannelSegmentType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterControl {

  private static final Logger logger = LoggerFactory.getLogger(FilterControl.class);

  private final FilterControlPluginRegistry registry;
  private final OsdGatewayClient osdGatewayAccess;
  private FilterConfiguration configuration;

  private boolean initialized;

  /**
   * Factory method for creating a FilterControl
   *
   * @param registry plugin registry, not null
   * @param osdGatewayAccessLibrary osd gateway access library, not null
   * @return a new FilterControl object
   */
  public static FilterControl create(FilterControlPluginRegistry registry,
      OsdGatewayClient osdGatewayAccessLibrary) {

    Objects.requireNonNull(registry, "Error creating FilterControl: registry cannot be null");
    Objects.requireNonNull(osdGatewayAccessLibrary,
        "Error creating FilterControl: osdGatewayAccessLibrary cannot be null");

    return new FilterControl(registry, osdGatewayAccessLibrary);
  }

  /**
   * Constructs a FilterControl with provided plugin registry and OSD gateway access library.
   *
   * @param registry plugin registry, not null
   * @param osdGatewayAccessLibrary osd gateway access library, not null
   */
  private FilterControl(FilterControlPluginRegistry registry,
      OsdGatewayClient osdGatewayAccessLibrary) {

    this.registry = registry;
    this.osdGatewayAccess = osdGatewayAccessLibrary;
    this.configuration = null;
    this.initialized = false;
  }

  /**
   * Initialization method used to set configuration for control class and its bound plugins.
   */
  public void initialize() {
    // TODO: is separate initialize() called by client necessary? Depends on SystemControl pattern.

    logger.info("Loading configuration for control class");
    configuration = osdGatewayAccess.loadConfiguration();

    logger.info("Loading configuration for plugins");
    registry.entrySet().forEach(e -> e.getPlugin()
        .initialize(osdGatewayAccess.loadPluginConfiguration(e.getRegistration())));

    initialized = true;
  }

  /**
   * Execute Waveform qc processing using the provided {@link ExecuteClaimCheckCommand}
   *
   * @param command object describing the filter processing request, not null
   * @return list of {@link UUID} to generated {@link ChannelSegment}, not null
   */
  public List<UUID> execute(ExecuteClaimCheckCommand command) {

    if (!initialized) {
      throw new IllegalStateException("FilterControl must be initialized before execution");
    }

    Objects.requireNonNull(command, "FilterControl cannot execute a null ClaimCheck");

    logger.info("FilterControl executing claim check command for {} between {} and {}",
        command.getInputToOutputChannelIds().keySet(), command.getStartTime(),
        command.getEndTime());

    // Load the input channel segments
    Set<ChannelSegment> channelSegments = osdGatewayAccess
        .loadChannelSegments(command.getInputToOutputChannelIds().keySet(), command.getStartTime(),
            command.getEndTime());

    logger.info("Loaded {} channelSegments", channelSegments.size());
    channelSegments.forEach(cs -> logger
        .info("ChannelSegment with id {} has {} waveforms", cs.getId(), cs.getWaveforms().size()));

    // get filter parameters and validate
    Optional<FilterParameters> filterParameters = configuration
        .createParameters(command.getChannelProcessingStepId());
    Validate.isTrue(filterParameters.isPresent(), "Filter configuration must define parameters");

    // get map of filter definitions for each filter plugin
    Map<RegistrationInfo, List<FilterDefinition>> requestedPluginFilters = filterParameters.get()
        .getInfoListMap();

    // get map of plugins for each define RegistrationInfo in the parameters
    Map<RegistrationInfo, Optional<FilterPlugin>> potentialPlugins = requestedPluginFilters.keySet()
        .stream().collect(Collectors.toMap(Function.identity(), registry::lookup));

    // get list of missingPlugins and throw an error if there are any
    List<RegistrationInfo> missingPlugins = potentialPlugins.entrySet().stream()
        .filter(e -> !e.getValue().isPresent()).map(Map.Entry::getKey)
        .collect(Collectors.toList());

    if (!missingPlugins.isEmpty()) {
      throw new IllegalStateException(
          "Cannot execute Waveform Filtering. Missing plugins found for: " +
              missingPlugins.toString());
    }

    // create creationInformation for each filter plugin
    List<CreationInformation> creationInformationList = new ArrayList<>();
    potentialPlugins.keySet().forEach(
        ri -> creationInformationList.add(createCreationInformation(potentialPlugins.get(ri).get(),
            command.getProcessingContext())));

    // for each channelSegment cs stream registrationInfo ri stream filterDefinition fd execute(plugin(ri),
    // fd, cs, command.getInputToOutputChannelIds().get(cs.getProcessingChannelId())
    // accumulate new channel segments in newChannelSegments list
    List<ChannelSegment> newChannelSegments = new ArrayList<>();
    channelSegments.forEach(cs -> potentialPlugins.keySet()
        .forEach(ri -> requestedPluginFilters.get(ri)
            .forEach(fd -> newChannelSegments.add(execute(potentialPlugins.get(ri).get(), fd, cs,
                command.getInputToOutputChannelIds().get(cs.getProcessingChannelId()))))));

    // get list of new channelSegmentIds
    List<UUID> newChannelSegmentIds = newChannelSegments.stream().
        map(ChannelSegment::getProcessingChannelId).collect(Collectors.toList());

    // store new channel segments
    osdGatewayAccess.store(newChannelSegments, creationInformationList,
        command.getProcessingContext().getStorageVisibility());
    logger.info("Storing {} filtered channelSegments", channelSegments.size());

    // return new channel segment ids
    logger.info("Returning UUIDs to filtered channel segments");
    return newChannelSegmentIds;
  }

  /**
   * Execute Waveform qc processing using the provided {@link ExecuteStreamingCommand}
   *
   * @param command object describing the qc processing request, not null
   * @return list of generated {@link ChannelSegment}, not null
   */
  public List<ChannelSegment> execute(ExecuteStreamingCommand command) {
    if (!initialized) {
      throw new IllegalStateException("FilterControl must be initialized before execution");
    }

    logger.info("FilterControl executing streaming command: {}", command);

    Objects.requireNonNull(command, "FilterControl cannot execute a null StreamingCommand");

    logger.info(
        "FilterControl executing streaming command for channel segments {} with filter definition {}",
        command.getInputChannelSegmentToOutputChannelIds().keySet(),
        command.getFilterDefinition().getName());

    // get parameters, filter definition, and plugin
    StreamingFilterPluginParameters params = configuration.createStreamingFilterPluginParameters();
    logger.info("Loaded StreamingFilterPluginParameters: {}", params);

    FilterDefinition filterDefinition = command.getFilterDefinition();
    RegistrationInfo registrationInfo = params.lookupPlugin(filterDefinition.getFilterType());
    Optional<FilterPlugin> filterPlugin = registry.lookup(registrationInfo);

    // validate filterPlugin exists
    if (!filterPlugin.isPresent()) {
      throw new IllegalStateException("Cannot execute streaming filter plugin. Plugin not found: " +
          registrationInfo.toString());
    }

    // build new filtered channel segments from input segments
    List<ChannelSegment> newChannelSegments = new ArrayList<>();
    command.getInputChannelSegmentToOutputChannelIds().keySet()
        .forEach(cs -> newChannelSegments.add(execute(filterPlugin.get(), filterDefinition, cs,
            command.getInputChannelSegmentToOutputChannelIds().get(cs))));

    // Store computed ChannelSegments and return the filtered list
    osdGatewayAccess.store(newChannelSegments,
        List.of(FilterControl
            .createCreationInformation(filterPlugin.get(), command.getProcessingContext())),
        StorageVisibility.PRIVATE);
    logger.info("Storing {} filtered channelSegments", newChannelSegments.size());

    logger.info("Returning filtered channel segments");
    return newChannelSegments;
  }

  /**
   * Creates a new filtered {@link ChannelSegment} from the input {@link ChannelSegment} using the
   * supplied {@link FilterPlugin} and {@link FilterDefinition}. The new ChannelSegment is defined
   * using the a new output channel id that is not the same as the channel id referenced by the
   * input {@link ChannelSegment}.
   *
   * @param plugin The plugin filter algorithm.
   * @param filterDefinition The filter definition object.
   * @param inputChannelSegment The input ChannelSegment supplying the waveforms to be filtered.
   * @param outputChannelId The Channel id for the new ChannelSegment.
   * @return The new ChannelSegment
   */
  private ChannelSegment execute(FilterPlugin plugin, FilterDefinition filterDefinition,
      ChannelSegment inputChannelSegment, UUID outputChannelId) {

    Collection<Waveform> newWaveforms = plugin.filter(inputChannelSegment, filterDefinition);

    logger.info("Filtered ChannelSegment {} output {} waveforms", inputChannelSegment.getId(), newWaveforms.size());

    // The TreeSet seems to be an unnecessary performance hit. ChannelSegment waveforms should be a simple
    // list. The caller can sort these, IF DESIRED, when necessary.

    return ChannelSegment.create(outputChannelId,
        inputChannelSegment.getName() + "/" + filterDefinition.getName(), ChannelSegmentType.FILTER,
        inputChannelSegment.getStartTime(), inputChannelSegment.getEndTime(),
        new TreeSet<>(newWaveforms), new CreationInfo("FilterControl",
            new SoftwareComponentInfo(plugin.getName(), plugin.getVersion().toString())));
  }

  /**
   * Convenience method for creating {@link CreationInformation} from a {@link FilterPlugin} and
   * a {@link ProcessingContext}. If the processing context is an empty Optional then the analyst
   * action and processing step reference are both set to empty optionals.
   *
   * @param plugin Plugin used to create the QcMask.
   * @param processingContext Context in which we are processing the data.
   * @return CreationInformation representing how an object was created.
   */
  private static CreationInformation createCreationInformation(FilterPlugin plugin,
      ProcessingContext processingContext) {
    return CreationInformation.create(processingContext.getAnalystActionReference(),
        processingContext.getProcessingStepReference(),
        new SoftwareComponentInfo(plugin.getName(), plugin.getVersion().toString()));
  }
}

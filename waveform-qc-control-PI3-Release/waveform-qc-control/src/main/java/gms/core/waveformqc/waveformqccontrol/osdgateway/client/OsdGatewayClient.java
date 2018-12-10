package gms.core.waveformqc.waveformqccontrol.osdgateway.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import gms.core.waveformqc.waveformqccontrol.objects.InvokeInputData;
import gms.core.waveformqc.waveformqccontrol.objects.RegistrationInfo;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcConfiguration;
import gms.core.waveformqc.waveformqccontrol.objects.dto.InvokeInputDataRequestDto;
import gms.core.waveformqc.waveformqccontrol.objects.dto.StoreQcMasksDto;
import gms.core.waveformqc.waveformqccontrol.plugin.PluginConfiguration;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Access library for the Waveform Qc Control class' OSD Gateway.
 *
 * This is a placeholder implementation used to show the initial pattern for control class
 * interfaces to the OSD.
 */
public class OsdGatewayClient {

  private static final Logger logger = LoggerFactory
      .getLogger(OsdGatewayClient.class);

  private String url;

  /**
   * Construct the gateway client that is used to make service calls to the
   * OSD Gateway service
   */
  public OsdGatewayClient(String host, int port, String baseUri) {
    this.url = "http://" + host + ":" + Integer.toString(port) + baseUri;
  }

  public WaveformQcConfiguration loadConfiguration() {
    return new WaveformQcConfiguration();
  }

  public PluginConfiguration loadPluginConfiguration(RegistrationInfo registrationInfo) {

    // TODO: replace with real configuration
    InputStream pluginConfigInputStream;
    switch (registrationInfo.getName()) {
      case "channelSohQcPlugin":
        pluginConfigInputStream = getClass().getClassLoader().getResourceAsStream(
            "gms/core/waveformqc/waveformqccontrol/osdgateway/client/channel_soh_qc_plugin_config.yaml");
        break;
      case "waveformGapQcPlugin":
        pluginConfigInputStream = getClass().getClassLoader().getResourceAsStream(
            "gms/core/waveformqc/waveformqccontrol/osdgateway/client/waveform_gap_qc_plugin_config.yaml");
        break;
      case "waveformRepeatedAmplitudeQcPlugin":
        pluginConfigInputStream = getClass().getClassLoader().getResourceAsStream(
            "gms/core/waveformqc/waveformqccontrol/osdgateway/client/waveform_repeated_amplitude_qc_plugin_config.yaml");
        break;
      case "waveformSpike3PtQcPlugin":
        pluginConfigInputStream = getClass().getClassLoader().getResourceAsStream(
            "gms/core/waveformqc/waveformqccontrol/osdgateway/client/waveform_spike_3pt_qc_plugin_config.yaml");
        break;
      default:
        throw new IllegalArgumentException(
            "Can't find plugin configuration for " + registrationInfo);
    }

    return PluginConfiguration.from(new Yaml().load(pluginConfigInputStream));
  }

  /**
   * Performs a POST on the Waveform Qc Control OSD Gateway to retrieve the data required for an
   * invoke operation in Waveform Qc Control.  The processing data is grouped as a {@link
   * InvokeInputData}
   *
   * @param processingChannelIds load input data for this set of {@link UUID} to {@link Channel}
   * @param startTime load input data inclusively beginning at this time, not null
   * @param endTime load input data inclusively ending at this time, not null
   * @return an InvokeInputData with all available processing inputs, not null
   * @throws NullPointerException if processingChannelIds, startTime, or endTime are null
   * @throws IllegalArgumentException if endTime is before startTime
   */
  public InvokeInputDataMap loadInvokeInputData(Set<UUID> processingChannelIds,
      Instant startTime,
      Instant endTime) {

    Objects.requireNonNull(processingChannelIds,
        "Cannot invoke loadInvokeInputData with null Processing Channel identities");
    Objects.requireNonNull(startTime, "Cannot invoke loadInvokeInputData with null endTime");
    Objects.requireNonNull(startTime, "Cannot invoke loadInvokeInputData with null startTime");

    if (endTime.isBefore(startTime)) {
      throw new IllegalArgumentException(
          "Cannot invoke loadInvokeInputData with endTime before startTime");
    }

    logger.info(
        "OsdGatewayClient received request to loadInvokeInputData for {} between {} and {}",
        processingChannelIds, startTime, endTime);

    try {
      HttpResponse<InvokeInputData> response = Unirest
          .post(url + "/invoke-input-data")
          .header("accept", "application/json")
          .header("Content-Type", "application/json")
          .body(new InvokeInputDataRequestDto(processingChannelIds, startTime, endTime))
          .asObject(InvokeInputData.class);

      InvokeInputData invokeInputData = response.getBody();

      logger.info("Invoke input data received from OSD Gateway: {}", invokeInputData);

      return InvokeInputDataMap.create(
          invokeInputData.channelSegments().collect(Collectors.toSet()),
          invokeInputData.qcMasks().collect(Collectors.toSet()),
          invokeInputData.waveformQcChannelSohStatuses().collect(Collectors.toSet()));
    } catch (UnirestException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Qc Mask Control OSD Gateway access library operation to store QcMasks. This
   * invokes an operation in the OSD Gateway service over HTTP.
   *
   * @param qcMasks a collection of QcMasks, not null
   * @param storageVisibility has the {@link StorageVisibility} and associated context for this
   * store, not null
   */
  public void store(List<QcMask> qcMasks, List<CreationInformation> creationInfos,
      StorageVisibility storageVisibility) {
    logger.info("OsdGatewayClient received request to store {}", qcMasks,
        storageVisibility);

    Objects.requireNonNull(qcMasks, "OsdGatewayClient store requires non-null qcMasks");
    Objects.requireNonNull(creationInfos,
        "OsdGatewayClient store requires non-null creationInformations");
    Objects.requireNonNull(storageVisibility,
        "OsdGatewayClient store requires non-null StorageVisibility");

    StoreQcMasksDto storeQcMasksDto = new StoreQcMasksDto(qcMasks, creationInfos,
        storageVisibility);

    try {
      HttpResponse<JsonNode> response =
          Unirest
              .post(url + "/store")
              .header("Accept", "text/plain")
              .header("Content-Type", "application/json")
              .body(storeQcMasksDto)
              .asJson();
    } catch (UnirestException e) {
      throw new RuntimeException(e);
    }
  }

}

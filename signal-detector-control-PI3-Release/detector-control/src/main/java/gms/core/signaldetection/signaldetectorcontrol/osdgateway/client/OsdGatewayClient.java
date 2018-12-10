package gms.core.signaldetection.signaldetectorcontrol.osdgateway.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import gms.core.signaldetection.signaldetectorcontrol.http.ContentType;
import gms.core.signaldetection.signaldetectorcontrol.http.ObjectSerialization;
import gms.core.signaldetection.signaldetectorcontrol.objects.RegistrationInfo;
import gms.core.signaldetection.signaldetectorcontrol.objects.SignalDetectorConfiguration;
import gms.core.signaldetection.signaldetectorcontrol.objects.dto.InvokeInputDataRequestDto;
import gms.core.signaldetection.signaldetectorcontrol.objects.dto.StoreSignalDetectionsDto;
import gms.core.signaldetection.signaldetectorcontrol.plugin.PluginConfiguration;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class OsdGatewayClient {

  private static final Logger logger = LoggerFactory.getLogger(OsdGatewayClient.class);

  private final String baseGatewayServiceUrl;

  /**
   * Construct the gateway client that is used to make service calls to the
   * OSD Gateway service
   */
  private OsdGatewayClient(String host, int port, String basePath) {
    this.baseGatewayServiceUrl = "http://" + host + ":" + Integer.toString(port) + basePath;
  }

  /**
   * Obtains a new {@link OsdGatewayClient} configured to access the service at the provided
   * {@link HttpClientConfiguration}
   *
   * @param httpClientConfiguration {@link HttpClientConfiguration} describing how to access the OSD
   * service, not null
   * @return OsdGatewayClient, not null
   * @throws NullPointerException if httpClientConfiguration is null
   */
  public static OsdGatewayClient create(HttpClientConfiguration httpClientConfiguration) {
    Objects.requireNonNull(httpClientConfiguration,
        "Cannot create OsdGatewayClient with null httpClientConfiguration");

    return new OsdGatewayClient(httpClientConfiguration.getHost(),
        httpClientConfiguration.getPort(), httpClientConfiguration.getBasePath());
  }

  public SignalDetectorConfiguration loadConfiguration() {
    return new SignalDetectorConfiguration();
  }

  /**
   * Obtains the {@link PluginConfiguration} for the plugin with the provided {@link
   * RegistrationInfo}
   *
   * @param registrationInfo {@link RegistrationInfo}, not null
   * @return {@link PluginConfiguration}, not null
   * @throws NullPointerException if registrationInfo is null
   */
  public PluginConfiguration loadPluginConfiguration(RegistrationInfo registrationInfo) {
    Objects.requireNonNull(registrationInfo,
        "loadPluginConfiguration require non-null RegistrationInfo");

    InputStream pluginConfigInputStream;

    // TODO: check version number as well?

    switch (registrationInfo.getName()) {
      case "staLtaPowerDetectorPlugin":
        pluginConfigInputStream = getClass().getClassLoader().getResourceAsStream(
            //TODO: Will the StaLtaPowerDetectorPlugin use a config.yaml file.
            "gms/core/signaldetection/signaldetectorcontrol/osdgateway/client/sta_lta_power_detector_plugin_config.yaml");
        break;
      default:
        throw new IllegalArgumentException(
            "Can't find plugin configuration for " + registrationInfo);
    }

    return PluginConfiguration.from(new Yaml().load(pluginConfigInputStream));
  }

  /**
   * Performs a POST on the Signal Detector Control OSD Gateway to retrieve the data required for an
   * invoke operation in Signal Detector Control.
   *
   * @param channelIds load input data for these {@link UUID} to {@link Channel}s
   * @param startTime load input data inclusively beginning at this time, not null
   * @param endTime load input data inclusively ending at this time, not null
   * @return Set of available channel segments for processing
   * @throws IllegalStateException if the osd-gateway service responds with a failure; if the
   * osd-gateway-service responds with an unanticipated Content-Type
   */
  public Set<ChannelSegment> loadChannelSegments(Collection<UUID> channelIds, Instant startTime,
      Instant endTime) {

    Objects.requireNonNull(channelIds,
        "Cannot invoke loadChannelSegments with null channelIds");
    Objects.requireNonNull(startTime, "Cannot invoke loadChannelSegments with null startTime");
    Objects.requireNonNull(endTime, "Cannot invoke loadChannelSegments with null endTime");

    if (endTime.isBefore(startTime)) {
      throw new IllegalArgumentException(
          "Cannot invoke loadChannelSegments with endTime before startTime");
    }

    Set<ChannelSegment> channelSegments = new HashSet<>();
    try {
      // json object mapper serializes the request body; response is binary MessagePack
      Unirest.setObjectMapper(ObjectSerialization.getJsonClientObjectMapper());
      HttpResponse<InputStream> response = Unirest
          .post(baseGatewayServiceUrl + "/invoke-input-data")
          .header("Accept", "application/msgpack")
          .header("Content-Type", "application/json")
          .body(new InvokeInputDataRequestDto(channelIds, startTime, endTime))
          .asBinary();

      final List<String> contentTypes = response.getHeaders().get("Content-Type");

      // Request succeeded: parse raw response from byte[] to Set<ChannelSegment>
      if (HttpStatus.OK_200 == response.getStatus()) {

        // Check the provided Content-Type is acceptable
        if (!contentTypes.contains(ContentType.APPLICATION_MSGPACK.toString())) {
          StringBuilder types = new StringBuilder();
          contentTypes.forEach(t -> {
            types.append(t);
            types.append(" ");
          });
          throw new IllegalStateException(
              "Expected Content-Type: application/msgpack but server provided " + types.toString());
        }

        // Deserialize body from MessagePack
        try (InputStream body = response.getRawBody()) {
          // Type erasure requires deserializing to an intermediary ChannelSegment[] and creating
          // the Set<ChannelSegment> from that array
          channelSegments.addAll(Arrays.asList(
              ObjectSerialization.readMessagePack(body.readAllBytes(), ChannelSegment[].class)));
        } catch (IOException e) {
          logger.error("Could not deserialize channelSegments from message pack response", e);
          throw new RuntimeException(e);
        }

        logger.info("Invoke loadChannelSegments received from OSD Gateway: {}", channelSegments);

        return channelSegments;
      }

      // Response failure: throw an exception with the body message
      else if (HttpStatus.INTERNAL_SERVER_ERROR_500 == response.getStatus()) {
        String message = "";
        if (contentTypes != null && contentTypes.contains(ContentType.TEXT_PLAIN.toString())) {
          message = new BufferedReader(new InputStreamReader(response.getBody())).lines()
              .collect(Collectors.joining("\n"));
        }
        throw new IllegalStateException(message);
      }
    } catch (UnirestException e) {
      throw new RuntimeException(e);
    }

    return channelSegments;
  }

  /**
   * Signal Detector Control OSD Gateway access library operation to store {@link SignalDetection}s.
   * This invokes an operation in the OSD Gateway service over HTTP.
   *
   * @param signalDetections collection of SignalDetections, not null
   * @param creationInfos List of creation infos
   * @param storageVisibility has the {@link StorageVisibility} and associated context for this
   * store, not null
   * @return wheter the store was successful
   */
  public boolean store(Set<SignalDetection> signalDetections,
      List<CreationInformation> creationInfos,
      StorageVisibility storageVisibility) {
    Objects.requireNonNull(signalDetections,
        "OsdGatewayClient store requires non-null signalDetections");
    Objects.requireNonNull(creationInfos,
        "OsdGatewayClient store requires non-null creationInformations");
    Objects.requireNonNull(storageVisibility,
        "OsdGatewayClient store requires non-null storageVisibility");

    // TODO: store CreationInformation after settling CreationInformation vs. CreationInfo
    // TODO: handle private vs public StorageVisibility

    logger.info("Store signal detections to OSD Gateway: {}", signalDetections);

    boolean result = false;

    try {
      logger.info("Posting to {} Content-Type: {} with Dto: {}", (baseGatewayServiceUrl + "/store"),
          "application/msgpack", new StoreSignalDetectionsDto(signalDetections, storageVisibility));

      HttpResponse<String> response = Unirest
          .post(baseGatewayServiceUrl + "/store")
          .header("Content-Type", "application/msgpack")
          .header("Accept", "application/json")
          .body(ObjectSerialization
              .writeMessagePack(new StoreSignalDetectionsDto(signalDetections, storageVisibility)))
          .asString();

      logger.info("OSD Gateway responded with {} (200 = success)", response.getStatus());

      if (HttpStatus.OK_200 == response.getStatus()) {
        result = true;
      } else {
        logger.error("Store signal detection failed with response code {} and response body {}",
            response.getStatus(), response.getBody());
      }

    } catch (UnirestException e) {
      throw new RuntimeException(e);
    }

    return result;
  }
}
package gms.core.signalenhancement.waveformfiltering.osdgateway.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import gms.core.signalenhancement.waveformfiltering.control.FilterControl;
import gms.core.signalenhancement.waveformfiltering.http.ObjectSerialization;
import gms.core.signalenhancement.waveformfiltering.objects.FilterConfiguration;
import gms.core.signalenhancement.waveformfiltering.objects.RegistrationInfo;
import gms.core.signalenhancement.waveformfiltering.objects.dto.InvokeInputDataRequestDto;
import gms.core.signalenhancement.waveformfiltering.objects.dto.StoreChannelSegmentsDto;
import gms.core.signalenhancement.waveformfiltering.plugin.PluginConfiguration;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access library for the Filter Control class' OSD Gateway.
 *
 * This is a placeholder implementation used to show the initial pattern for control class
 * interfaces to the OSD.
 */
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

  /**
   * Obtains {@link FilterConfiguration} for {@link FilterControl}
   *
   * @return {@link FilterConfiguration}, not null
   */
  public FilterConfiguration loadConfiguration() {
    return new FilterConfiguration();
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

    return PluginConfiguration.from(Map.of());
  }

  /**
   * Performs a POST on the Filter Control OSD Gateway to retrieve the data required for an
   * invoke operation in Filter Control.
   *
   * @param channelIds load input data for these {@link UUID} to {@link Channel}s
   * @param startTime load input data inclusively beginning at this time, not null
   * @param endTime load input data inclusively ending at this time, not null
   * @return Set of available channel segments for processing
   */
  public Set<ChannelSegment> loadChannelSegments(Collection<UUID> channelIds, Instant startTime,
      Instant endTime) {

    Objects.requireNonNull(channelIds,
        "Cannot invoke loadChannelSegments with null channelIds");
    Objects.requireNonNull(startTime, "Cannot invoke loadChannelSegments with null endTime");
    Objects.requireNonNull(endTime, "Cannot invoke loadChannelSegments with null endTime");

    if (endTime.isBefore(startTime)) {
      throw new IllegalArgumentException(
          "Cannot invoke loadChannelSegments with endTime before startTime");
    }

    try {
      // json object mapper serializes the request body; response is binary MessagePack
      Unirest.setObjectMapper(ObjectSerialization.getJsonClientObjectMapper());
      HttpResponse<InputStream> response = Unirest
          .post(baseGatewayServiceUrl + "/invoke-input-data")
          .header("Accept", "application/msgpack")
          .header("Content-Type", "application/json")
          .body(new InvokeInputDataRequestDto(channelIds, startTime, endTime))
          .asBinary();

      // Parse raw response from byte[] to Set<ChannelSegment>
      Set<ChannelSegment> channelSegments;
      try (InputStream body = response.getRawBody()) {
        // Type erasure requires deserializing to an intermediary ChannelSegment[] and creating
        // the Set<ChannelSegment> from that array
        channelSegments = new HashSet<>(Arrays.asList(
            ObjectSerialization.readMessagePack(body.readAllBytes(), ChannelSegment[].class)));
      } catch (IOException e) {
        logger.error("Could not deserialize channelSegments from message pack response", e);
        throw new RuntimeException(e);
      }

      logger.info("Invoke loadChannelSegments received from OSD Gateway: {}", channelSegments);

      return channelSegments;
    } catch (UnirestException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Filter Control OSD Gateway access library operation to store filtered waveforms. This
   * invokes an operation in the OSD Gateway service over HTTP.
   *
   * @param channelSegments collection of channel segments, not null
   * @param creationInfos List of creation infos
   * @param storageVisibility has the {@link StorageVisibility} and associated context for this
   * store, not null
   */
  public void store(List<ChannelSegment> channelSegments, List<CreationInformation> creationInfos,
      StorageVisibility storageVisibility) {
    Objects.requireNonNull(channelSegments,
        "OsdGatewayClient store requires non-null channelSegments");
    Objects.requireNonNull(creationInfos,
        "OsdGatewayClient store requires non-null creationInformations");
    Objects.requireNonNull(storageVisibility,
        "OsdGatewayClient store requires non-null StorageVisibility");

    // TODO: store CreationInformation after settling CreationInformation vs. CreationInfo
    // TODO: handle private vs public StorageVisibility

    logger.info("Store channel segments to OSD Gateway: {}", channelSegments);

    try {
      logger.info("Posting to {} Content-Type: {} with Dto: {}", (baseGatewayServiceUrl + "/store"),
          "application/msgpack", new StoreChannelSegmentsDto(channelSegments, storageVisibility));

      HttpResponse<InputStream> response = Unirest
          .post(baseGatewayServiceUrl + "/store")
          .header("Content-Type", "application/msgpack")
          .header("Accept", "application/json")
          .body(ObjectSerialization
              .writeMessagePack(new StoreChannelSegmentsDto(channelSegments, storageVisibility)))
          .asBinary();

      logger.info("OSD Gateway responded with {} (200 = success)", response.getStatus());

      // TODO: handle OSD Gateway failures / non-200 responses?

    } catch (UnirestException e) {
      throw new RuntimeException(e);
    }
  }
}

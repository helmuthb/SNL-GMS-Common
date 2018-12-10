package gms.dataacquisition.cssloader.accesslibrary;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.body.RawBody;
import gms.dataacquisition.cssloader.CssLoaderOsdGatewayInterface;
import gms.dataacquisition.cssloader.Endpoints;
import gms.dataacquisition.cssloader.serialization.SerializationUtility;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.DataExistsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.StorageUnavailableException;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceCalibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceDigitizer;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceDigitizerMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetwork;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetworkMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSensor;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSite;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSiteMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStationMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;

import java.time.Instant;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Performs the service calls to the OSD Gateway Service. Abstracts the cssloader-client from the
 * details of how it accesses the OSD (such that it's deployed as a gateway service).
 */
public class CssLoaderOsdGatewayAccessLibrary implements CssLoaderOsdGatewayInterface {

  private static final Logger logger = LoggerFactory.getLogger(
      CssLoaderOsdGatewayAccessLibrary.class);
  private static final int DEFAULT_CONNECTION_TIMEOUT = 10000;
  private final String storeChannelSegmentsUrl;
  private final String storeChannelStatesOfHealthUrl;
  private final String idForChannelUrl;
  private final String storeChannelUrl;
  private final String storeCalibrationUrl;
  private final String storeResponseUrl;
  private final String storeSensorUrl;
  private final String storeNetworkUrl;
  private final String storeSiteUrl;
  private final String storeStationUrl;
  private final String storeNetworkMembershipsUrl;
  private final String storeStationMembershipsUrl;
  private final String storeSiteMembershipsUrl;

  // Required for JSON serialization.
  static {

    Unirest.setObjectMapper(new com.mashape.unirest.http.ObjectMapper() {

      public <T> T readValue(String s, Class<T> aClass) {
        try {
          return SerializationUtility.objectMapper.readValue(s, aClass);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      public String writeValue(Object o) {
        try {
          return SerializationUtility.objectMapper.writeValueAsString(o);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
    // Set socket data timeout to 5 minutes, for large waveform uploads.
    Unirest.setTimeouts(DEFAULT_CONNECTION_TIMEOUT, 300000);
  }

  public CssLoaderOsdGatewayAccessLibrary(String host, short port) {
    this.idForChannelUrl = Endpoints.retreiveChannelIdUrl(port, host);
    this.storeChannelSegmentsUrl = Endpoints.storeChannelSegmentsUrl(port, host);
    this.storeChannelStatesOfHealthUrl = Endpoints.storeSohBooleanUrl(port, host);
    this.storeChannelUrl = Endpoints.storeChannelUrl(port, host);
    this.storeCalibrationUrl = Endpoints.storeCalibrationUrl(port, host);
    this.storeResponseUrl = Endpoints.storeResponseUrl(port, host);
    this.storeSensorUrl = Endpoints.storeSensorUrl(port, host);
    this.storeSiteUrl = Endpoints.storeSiteUrl(port, host);
    this.storeStationUrl = Endpoints.storeStationUrl(port, host);
    this.storeNetworkUrl = Endpoints.storeNetworkUrl(port, host);
    this.storeNetworkMembershipsUrl = Endpoints.storeNetworkMembershipsUrl(port, host);
    this.storeStationMembershipsUrl = Endpoints.storeStationMembershipsUrl(port, host);
    this.storeSiteMembershipsUrl = Endpoints.storeSiteMembershipsUrl(port, host);
  }

  /**
   * Sends a channel segment batch to the OSD Gateway Service.
   *
   * @param channelSegmentBatch data to be sent
   */
  @Override
  public void storeChannelSegments(Collection<ChannelSegment> channelSegmentBatch) throws Exception {
    Validate.notNull(channelSegmentBatch);
    postMsgPack(channelSegmentBatch, this.storeChannelSegmentsUrl);
  }

  /**
   * Sends a batch of AcquiredChannelSohBoolean to the OSD Gateway Service.
   *
   * @param sohs data to be sent
   */
  @Override
  public void storeChannelStatesOfHealth(Collection<AcquiredChannelSohBoolean> sohs) throws Exception {
    Validate.notNull(sohs);
    postJson(sohs, this.storeChannelStatesOfHealthUrl);
  }

  /**
   * Retrieve a Channel ID from the OSD Gateway Service.
   *
   * @param siteName name of site
   * @param channelName name of channel
   * @param time moment in time as Instant
   * @return Channel ID as UUID
   */
  @Override
  public UUID idForChannel(String siteName, String channelName, Instant time) {
    try {
      return getJson(
          this.idForChannelUrl
              + "?site-name=" + siteName + "&channel-name="
              + channelName + "&time=" + time.toString(), UUID.class);
    } catch (Exception ex) {
      logger.error("idForChannel error", ex);
      return null;
    }
  }

  /**
   * Sends a network to the OSD Gateway Service.
   *
   * @param network data to be sent
   */
  @Override
  public void storeNetwork(ReferenceNetwork network) throws Exception {
    Validate.notNull(network);
    postJson(network, this.storeNetworkUrl);
  }

  /**
   * Sends a station to the OSD Gateway Service.
   *
   * @param station data to be sent
   */
  @Override
  public void storeStation(ReferenceStation station) throws Exception {
    Validate.notNull(station);
    postJson(station, this.storeStationUrl);
  }

  /**
   * Sends a site to the OSD Gateway Service.
   *
   * @param site data to be sent
   */
  @Override
  public void storeSite(ReferenceSite site) throws Exception {
    Validate.notNull(site);
    postJson(site, this.storeSiteUrl);
  }

  /**
   * Sends a channel to the OSD Gateway Service.
   *
   * @param channel data to be sent
   */
  @Override
  public void storeChannel(ReferenceChannel channel) throws Exception {
    Validate.notNull(channel);
    postJson(channel, this.storeChannelUrl);
  }

  /**
   * Sends a calibration to the OSD Gateway Service.
   *
   * @param calibration data to be sent
   */
  @Override
  public void storeCalibration(ReferenceCalibration calibration) throws Exception {
    Validate.notNull(calibration);
    postJson(calibration, this.storeCalibrationUrl);
  }

  /**
   * Sends a response to the OSD Gateway Service.
   *
   * @param response data to be sent
   */
  @Override
  public void storeResponse(ReferenceResponse response) throws Exception {
    Validate.notNull(response);
    postJson(response, this.storeResponseUrl);
  }

  /**
   * Sends a sensor to the OSD Gateway Service.
   *
   * @param sensor data to be sent
   */
  @Override
  public void storeSensor(ReferenceSensor sensor) throws Exception {
    Validate.notNull(sensor);
    postJson(sensor, this.storeSensorUrl);
  }

  /**
   * Sends a set of network memberships to the OSD Gateway Service.
   *
   * @param memberships data to be sent
   */
  @Override
  public void storeNetworkMemberships(Collection<ReferenceNetworkMembership> memberships)
      throws Exception {
    Validate.notNull(memberships);
    postJson(memberships, this.storeNetworkMembershipsUrl);
  }

  /**
   * Sends a set of station memberships to the OSD Gateway Service.
   *
   * @param memberships data to be sent
   */
  @Override
  public void storeStationMemberships(Collection<ReferenceStationMembership> memberships)
      throws Exception {
    Validate.notNull(memberships);
    postJson(memberships, this.storeStationMembershipsUrl);
  }

  /**
   * Sends a set of site memberships to the OSD Gateway Service.
   *
   * @param memberships data to be sent
   */
  @Override
  public void storeSiteMemberships(Collection<ReferenceSiteMembership> memberships) throws Exception {
    Validate.notNull(memberships);
    postJson(memberships, this.storeSiteMembershipsUrl);
  }

  /**
   * Sends the data to the OSD Gateway Service, via an HTTP post with JSON.
   *
   * @param obj          data to be sent
   * @param url          endpoint
   * @throws Exception if for instance, the host cannot be reached
   */
  private static void postJson(Object obj, String url) throws Exception {
    HttpResponse<String> response =  Unirest.post(url)
        .header("Accept", "application/json")
        .header("Content-Type", "application/json")
        .header("Connection", "close")
        .body(obj)
        .asString();
    handleResponse(obj, response);
  }

  /**
   * Sends the data to the OSD Gateway Service, via an HTTP post with msgpack.
   *
   * @param obj          data to be sent
   * @param url          endpoint
   * @throws Exception if for instance, the host cannot be reached
   */
  private static void postMsgPack(Object obj, String url) throws Exception {
    HttpResponse <String> response = Unirest.post(url)
        .header("Accept", "application/json")
        .header("Content-Type", "application/msgpack")
        .header("Connection", "close")
        .body(SerializationUtility.msgPackMapper.writeValueAsBytes(obj))
        .asString();
    handleResponse(obj, response);
  }

  /**
   * Sends the data to the OSD Gateway Service, via an HTTP post with msgpack.
   *
   * @param url endpoint
   * @param responseType type of the response object
   * @param <T> deserialized response
   * @return An object containing the OSD Gateway Service's response.
   * @throws Exception if for instance, the host cannot be reached
   */
  private static <T> T getJson(String url, Class<T> responseType) throws Exception {
    HttpResponse<T> response = Unirest.get(url)
        .header("Accept", "application/json")
        .asObject(responseType);
    return response.getBody();
  }

  private static void handleResponse(Object postedObj, HttpResponse<String> response) throws Exception {
    int statusCode = response.getStatus();
    if (statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE) {
      throw new StorageUnavailableException();
    }
    else if (statusCode == HttpStatus.SC_CONFLICT) {
      throw new DataExistsException("Conflict in storing data: " + postedObj);
    }
    // 400's and 500's are errors, except 'conflict', which is not considered an error.
    else if (statusCode >= 400 && statusCode <= 599) {
      throw new Exception(String.format("Error response from server (code %d): %s",
          statusCode, response.getBody()));
    }
  }
}

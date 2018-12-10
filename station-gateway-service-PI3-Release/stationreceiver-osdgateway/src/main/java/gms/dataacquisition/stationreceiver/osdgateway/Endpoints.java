package gms.dataacquisition.stationreceiver.osdgateway;

/**
 * Defines the endpoints and URL's of the service for both the service and client to know about.
 */
public class Endpoints {

  public static final String
      BASE = "/station-receiver/",
      GET_STATION_ID_BY_NAME = BASE + "station-id-by-name",
      STORE_ANALOG_SOHS = BASE + "acquired-channel-soh-analog-batch/store",
      STORE_BOOLEAN_SOHS = BASE + "acquired-channel-soh-boolean-batch/store",
      STORE_CHANNEL_SEGMENTS = BASE + "channel-segment-batch/store",
      STORE_RAW_STATION_DATA_FRAME = BASE + "raw-station-data-frame/store",
      GET_CHANNEL_ID_BY_NAME_AND_SITE = BASE + "channel";

  /**
   * Gets base URL for the service, given a host and port.
   *
   * @param host the host name
   * @param port the port number
   * @return string URL of the base of the service
   */
  public static String baseUrl(String host, int port) {
    return "http://" + host + ":" + port;
  }

  /**
   * Gets URL for the 'retrieve station by name' endpoint, given a host and port.
   *
   * @param port the port number
   * @return string URL for storing analog SOH
   */
  public static String getStationIdByNameUrl(String host, int port) {
    return baseUrl(host, port) + GET_STATION_ID_BY_NAME;
  }

  /**
   * Gets URL for the 'retrieve channel' endpoint, given a port and host.
   *
   * @param port the port number
   * @param host the host
   * @return string URL with query parameters for finding channel
   */
  public static String getChannelIdByNameUrl(String host, int port) {
    return baseUrl(host, port) + GET_CHANNEL_ID_BY_NAME_AND_SITE;
  }


  /**
   * Gets URL for the 'store analog SOH' endpoint, given a host and port.
   *
   * @param port the port number
   * @return string URL for storing analog SOH
   */
  public static String storeSohAnalogUrl(String host, int port) {
    return baseUrl(host, port) + STORE_ANALOG_SOHS;
  }

  /**
   * Gets URL for the 'store boolean SOH' endpoint, given a host and port.
   *
   * @param port the port number
   * @return string URL for storing boolean SOH
   */
  public static String storeSohBooleanUrl(String host, int port) {
    return baseUrl(host, port) + STORE_BOOLEAN_SOHS;
  }

  /**
   * Gets URL for the 'store channel segments' endpoint, given a host and port.
   *
   * @param port the port number
   * @return string URL for storing channel segments
   */
  public static String storeChannelSegmentsUrl(String host, int port) {
    return baseUrl(host, port) + STORE_CHANNEL_SEGMENTS;
  }

  /**
   * Gets URL for the 'store raw station data frame' endpoint, given a host and port.
   *
   * @param port the port number
   * @return string URL for storing raw station data frames
   */
  public static String storeRawStationDataFrameUrl(String host, int port) {
    return baseUrl(host, port) + STORE_RAW_STATION_DATA_FRAME;
  }

}

package gms.dataacquisition.cssloader;

/**
 * Defines the endpoints and URL's of the service for both the service and client to know about.
 */
public class Endpoints {

  public static final String BASE = "/css-loader/";
  public static final String STORE_CHANNEL_SEGMENTS = BASE + "channel-segment/store";
  public static final String STORE_BOOLEAN_SOHS = BASE + "acquired-channel-soh-boolean/store";
  public static final String RETRIEVE_CHANNEL_ID = BASE + "channel";
  public static final String STORE_NETWORK = BASE + "network/store";
  public static final String STORE_STATION = BASE + "station/store";
  public static final String STORE_SITE = BASE + "site/store";
  public static final String STORE_CHANNEL = BASE + "channel/store";
  public static final String STORE_CALIBRATION = BASE + "calibration/store";
  public static final String STORE_RESPONSE = BASE + "response/store";
  public static final String STORE_SENSOR = BASE + "sensor/store";
  public static final String STORE_NETWORK_MEMBERSHIPS = BASE + "network-memberships/store";
  public static final String STORE_STATION_MEMBERSHIPS = BASE + "station-memberships/store";
  public static final String STORE_SITE_MEMBERSHIPS = BASE + "site-memberships/store";

  /**
   * Gets base URL for the service, given a port and host.
   *
   * @param port the port number
   * @return string URL of the base of the service
   */
  public static String baseUrl(int port, String host) {
    return "http://" + host + ":" + port;
  }

  /**
   * Gets URL for the 'store boolean SOH' endpoint, given a port and host.
   *
   * @param port the port number
   * @param host the host
   * @return string URL for storing boolean SOH
   */
  public static String storeSohBooleanUrl(int port, String host) {
    return baseUrl(port, host) + STORE_BOOLEAN_SOHS;
  }

  /**
   * Gets URL for the 'store channel segments' endpoint, given a port and host.
   *
   * @param port the port number
   * @param host the host
   * @return string URL for storing channel segments
   */
  public static String storeChannelSegmentsUrl(int port, String host) {
    return baseUrl(port, host) + STORE_CHANNEL_SEGMENTS;
  }

  /**
   * Gets URL for the 'retrieve channel' endpoint, given a port and host.
   *
   * @param port the port number
   * @param host the host
   * @return string URL with query parameters for finding channel
   */
  public static String retreiveChannelIdUrl(int port, String host) {
    return baseUrl(port, host) + RETRIEVE_CHANNEL_ID;
  }

  /**
   * Gets URL for the 'store network' endpoint, given a port and host.
   *
   * @param port the port number
   * @param host the host
   * @return string URL
   */
  public static String storeNetworkUrl(int port, String host) {
    return baseUrl(port, host) + STORE_NETWORK;
  }

  /**
   * Gets URL for the 'store station' endpoint, given a port and host.
   *
   * @param port the port number
   * @param host the host
   * @return string URL
   */
  public static String storeStationUrl(int port, String host) {
    return baseUrl(port, host) + STORE_STATION;
  }

  /**
   * Gets URL for the 'store site' endpoint, given a port and host.
   *
   * @param port the port number
   * @param host the host
   * @return string URL
   */
  public static String storeSiteUrl(int port, String host) {
    return baseUrl(port, host) + STORE_SITE;
  }

  /**
   * Gets URL for the 'store channel' endpoint, given a port and host.
   *
   * @param port the port number
   * @param host the host
   * @return string URL
   */
  public static String storeChannelUrl(int port, String host) {
    return baseUrl(port, host) + STORE_CHANNEL;
  }

  /**
   * Gets URL for the 'store calibration' endpoint, given a port and host.
   *
   * @param port the port number
   * @param host the host
   * @return string URL
   */
  public static String storeCalibrationUrl(int port, String host) {
    return baseUrl(port, host) + STORE_CALIBRATION;
  }

  /**
   * Gets URL for the 'store response' endpoint, given a port and host.
   *
   * @param port the port number
   * @param host the host
   * @return string URL
   */
  public static String storeResponseUrl(int port, String host) {
    return baseUrl(port, host) + STORE_RESPONSE;
  }

  /**
   * Gets URL for the 'store sensor' endpoint, given a port and host.
   *
   * @param port the port number
   * @param host the host
   * @return string URL
   */
  public static String storeSensorUrl(int port, String host) {
    return baseUrl(port, host) + STORE_SENSOR;
  }

  /**
   * Gets URL for the 'store network memberships' endpoint, given a port and host.
   *
   * @param port the port number
   * @param host the host
   * @return string URL
   */
  public static String storeNetworkMembershipsUrl(int port, String host) {
    return baseUrl(port, host) + STORE_NETWORK_MEMBERSHIPS;
  }

  /**
   * Gets URL for the 'store station memberships' endpoint, given a port and host.
   *
   * @param port the port number
   * @param host the host
   * @return string URL
   */
  public static String storeStationMembershipsUrl(int port, String host) {
    return baseUrl(port, host) + STORE_STATION_MEMBERSHIPS;
  }

  /**
   * Gets URL for the 'store site memberships' endpoint, given a port and host.
   *
   * @param port the port number
   * @param host the host
   * @return string URL
   */
  public static String storeSiteMembershipsUrl(int port, String host) {
    return baseUrl(port, host) + STORE_SITE_MEMBERSHIPS;
  }
}

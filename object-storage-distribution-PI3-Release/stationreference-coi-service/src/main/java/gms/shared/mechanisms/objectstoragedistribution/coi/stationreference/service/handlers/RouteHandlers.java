package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.handlers;

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
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.StationReferenceRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.utility.FilterUtility;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains handler functions for the service routes.
 */
public class RouteHandlers {

  private static Logger logger = LoggerFactory.getLogger(RouteHandlers.class);

  /**
   * Handles a request to retrieve networks by id via
   * {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceNetwork}
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing networks; may be an empty list
   */
  public static List<ReferenceNetwork> retrieveNetworksById(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    String idParam = request.params(":id");
    logger.info("networks by id endpoint hit with id: " + idParam);
    UUID id = UUID.fromString(idParam);
    List<ReferenceNetwork> nets = repo.retrieveNetworksByEntityId(id);
    return filterByTimesIfRequired(request, response, nets,
        ReferenceNetwork::getActualChangeTime, ReferenceNetwork::getEntityId);
  }

  /**
   * Handles a request to retrieve networks by name via
   * {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceNetwork}
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing networks; may be an empty list
   */
  public static List<ReferenceNetwork> retrieveNetworksByName(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    String name = request.params(":name");
    logger.info("networks by name endpoint hit with name: " + name);
    List<ReferenceNetwork> nets = repo.retrieveNetworksByName(name);
    return filterByTimesIfRequired(request, response, nets,
        ReferenceNetwork::getActualChangeTime, ReferenceNetwork::getEntityId);
  }

  /**
   * Handles a request to retrieve networks via {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceNetwork}
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing networks; may be an empty list
   */
  public static List<ReferenceNetwork> retrieveNetworks(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    logger.info("retrieveNetworks endpoint hit");
    List<ReferenceNetwork> networks = new ArrayList<>();
    String stationName = request.queryParams("station-name");

    if (stationName == null) {
      networks = repo.retrieveNetworks();
    }
    else {
      List<ReferenceStation> stationsByName = repo.retrieveStationsByName(stationName);
      if (stationsByName.isEmpty()) {
        logger.warn("Retrieve networks by station: "
            + "could not find any stations by name " + stationName);
        return List.of();
      }
      UUID staId = stationsByName.get(0).getEntityId();
      List<ReferenceNetworkMembership> networkMemberships
          = repo.retrieveNetworkMembershipsByStationId(staId);
      List<UUID> networkIds = networkMemberships.stream()
          .map(ReferenceNetworkMembership::getNetworkId)
          .distinct()
          .collect(Collectors.toList());
      for (UUID netId : networkIds) {
        networks.addAll(repo.retrieveNetworksByEntityId(netId));
      }
    }
    return filterByTimesIfRequired(request, response,
        networks, ReferenceNetwork::getActualChangeTime, ReferenceNetwork::getEntityId);
  }

  /**
   * Handles a request to retrieve stations by id via
   * {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceStation}
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing stations; may be an empty list
   */
  public static List<ReferenceStation> retrieveStationsById(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    String idParam = request.params(":id");
    logger.info("stations by id endpoint hit with id: " + idParam);
    UUID id = UUID.fromString(idParam);
    List<ReferenceStation> stations = repo.retrieveStationsByEntityId(id);
    return filterByTimesIfRequired(request, response, stations,
        ReferenceStation::getActualChangeTime, ReferenceStation::getEntityId);
  }

  /**
   * Handles a request to retrieve stations by name via
   * {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceStation}
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing stations; may be an empty list
   */
  public static List<ReferenceStation> retrieveStationsByName(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    String name = request.params(":name");
    logger.info("stations by name endpoint hit with name: " + name);
    List<ReferenceStation> stas = repo.retrieveStationsByName(name);
    return filterByTimesIfRequired(request, response, stas,
        ReferenceStation::getActualChangeTime, ReferenceStation::getEntityId);
  }

  /**
   * Handles a request to retrieve stations via {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceStation}
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing stations; may be an empty list
   */
  public static List<ReferenceStation> retrieveStations(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    logger.info("retrieveStations endpoint hit");
    List<ReferenceStation> stations = new ArrayList<>();
    String netName = request.queryParams("network-name");
    String siteName = request.queryParams("site-name");

    if (netName == null && siteName == null) {
      stations = repo.retrieveStations();
    }
    else {
      Set<UUID> stationIdsFromNetwork = new HashSet<>();
      Set<UUID> stationIdsFromSite = new HashSet<>();
      if (netName != null) {
        Optional<UUID> netId = repo.retrieveNetworksByName(netName)
            .stream()
            .map(ReferenceNetwork::getEntityId)
            .findAny();
        if (!netId.isPresent()) {
          logger.warn("Could not find any networks by name " + netName);
        }
        else {
          stationIdsFromNetwork = repo.retrieveNetworkMembershipsByNetworkId(netId.get())
              .stream()
              .map(ReferenceNetworkMembership::getStationId)
              .distinct()
              .collect(Collectors.toSet());
        }
      }
      if (siteName != null) {
        Optional<UUID> siteId = repo.retrieveSitesByName(siteName)
            .stream()
            .map(ReferenceSite::getEntityId)
            .findAny();
        if (!siteId.isPresent()) {
          logger.warn("Could not find stations by site " + siteName);
        }
        else {
          stationIdsFromSite = repo.retrieveStationMembershipsBySiteId(siteId.get())
              .stream()
              .map(ReferenceStationMembership::getStationId)
              .distinct()
              .collect(Collectors.toSet());
        }
      }
      Set<UUID> stationIdsToInclude = new HashSet<>(stationIdsFromNetwork);
      if (netName != null && siteName != null) {
        stationIdsToInclude.retainAll(stationIdsFromSite);
      }
      else {
        stationIdsToInclude.addAll(stationIdsFromSite);
      }
      for (UUID staId : stationIdsToInclude) {
        stations.addAll(repo.retrieveStationsByEntityId(staId));
      }
    }
    return filterByTimesIfRequired(request, response,
        stations, ReferenceStation::getActualChangeTime, ReferenceStation::getEntityId);
  }

  /**
   *
   * Handles a request to retrieve Sites by id via
   * {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceSite}
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing sites; may be an empty list
   */
  public static List<ReferenceSite> retrieveSitesById(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    String idParam = request.params(":id");
    logger.info("sites by id endpoint hit with id: " + idParam);
    UUID id = UUID.fromString(idParam);
    List<ReferenceSite> sites = repo.retrieveSitesByEntityId(id);
    return filterByTimesIfRequired(request, response, sites,
        ReferenceSite::getActualChangeTime, ReferenceSite::getEntityId);
  }

  /**
   *
   * Handles a request to retrieve Sites by name via
   * {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceSite}
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing sites; may be an empty list
   */
  public static List<ReferenceSite> retrieveSitesByName(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    String name = request.params(":name");
    logger.info("sites by name endpoint hit with name: " + name);
    List<ReferenceSite> sites = repo.retrieveSitesByName(name);
    return filterByTimesIfRequired(request, response, sites,
        ReferenceSite::getActualChangeTime, ReferenceSite::getEntityId);
  }

  /**
   *
   * Handles a request to retrieve Sites via {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceSite}
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing sites; may be an empty list
   */
  public static List<ReferenceSite> retrieveSites(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    logger.info("retrieveSites endpoint hit");

    List<ReferenceSite> sites = new ArrayList<>();

    String staName = request.queryParams("station-name");
    String channelIdParam = request.queryParams("channel-id");

    if (staName == null && channelIdParam == null) {
      sites = repo.retrieveSites();
    }
    else {
      Set<UUID> siteIdsFromStation = new HashSet<>();
      Set<UUID> siteIdsFromChannel = new HashSet<>();
      if (staName != null) {
        Optional<UUID> stationsId = repo.retrieveStationsByName(staName)
            .stream()
            .map(ReferenceStation::getEntityId)
            .findAny();
        if (!stationsId.isPresent()) {
          logger.warn("Retrieve sites by station: "
              + "could not find any stations by name " + staName);
        }
        else {
          siteIdsFromStation = repo.retrieveStationMembershipsByStationId(stationsId.get())
              .stream()
              .map(ReferenceStationMembership::getSiteId)
              .distinct()
              .collect(Collectors.toSet());
        }
      }
      if (channelIdParam != null) {
        UUID channelId = UUID.fromString(channelIdParam);
        siteIdsFromChannel = repo.retrieveSiteMembershipsByChannelId(channelId)
            .stream()
            .map(ReferenceSiteMembership::getSiteId)
            .distinct()
            .collect(Collectors.toSet());
      }
      // take the intersection of the two sets of site id's - these are the id's to query for.
      Set<UUID> siteIdsToInclude = new HashSet<>(siteIdsFromStation);
      if (staName != null && channelIdParam != null) {
        siteIdsToInclude.retainAll(siteIdsFromChannel);
      }
      else {
        siteIdsToInclude.addAll(siteIdsFromChannel);
      }
      for (UUID siteId : siteIdsToInclude) {
        sites.addAll(repo.retrieveSitesByEntityId(siteId));
      }
    }
    return filterByTimesIfRequired(request, response,
      sites, ReferenceSite::getActualChangeTime, ReferenceSite::getEntityId);
  }

  /**
   * Handles a request to retrieve channels by id via
   * {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceChannel}.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing channels; may be an empty list
   */
  public static List<ReferenceChannel> retrieveChannelsById(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    String idParam = request.params(":id");
    logger.info("channels by id endpoint hit with id: " + idParam);
    UUID id = UUID.fromString(idParam);
    List<ReferenceChannel> chans = repo.retrieveChannelsByEntityId(id);
    return filterByTimesIfRequired(request, response, chans,
        ReferenceChannel::getActualTime, ReferenceChannel::getEntityId);
  }

  /**
   * Handles a request to retrieve channels by name via
   * {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceChannel}.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing channels; may be an empty list
   */
  public static List<ReferenceChannel> retrieveChannelsByName(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    String name = request.params(":name");
    logger.info("channels by name endpoint hit with name: " + name);
    List<ReferenceChannel> chans = repo.retrieveChannelsByName(name);
    return filterByTimesIfRequired(request, response, chans,
        ReferenceChannel::getActualTime, ReferenceChannel::getEntityId);
  }

  /**
   * Handles a request to retrieve channels via {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceChannel}.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing channels; may be an empty list
   */
  public static List<ReferenceChannel> retrieveChannels(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    logger.info("retrieveChannels endpoint hit");
    List<ReferenceChannel> channels = new ArrayList<>();
    String siteName = request.queryParams("site-name");
    String digitizerIdParam = request.queryParams("digitizer-id");

    if (siteName == null && digitizerIdParam == null) {
      channels = repo.retrieveChannels();
    }
    else {
      Set<UUID> channelIdsFromSites = new HashSet<>();
      Set<UUID> channelIdsFromDigitizers = new HashSet<>();
      if (siteName != null) {
        Optional<UUID> siteId = repo.retrieveSitesByName(siteName)
            .stream()
            .map(ReferenceSite::getEntityId)
            .findAny();
        if (!siteId.isPresent()) {
          logger.error("Could not find site with name " + siteName);
        }
        else {
          channelIdsFromSites = repo.retrieveSiteMembershipsBySiteId(siteId.get())
              .stream()
              .map(ReferenceSiteMembership::getChannelId)
              .distinct()
              .collect(Collectors.toSet());
        }
      }
      if (digitizerIdParam != null) {
        UUID digitizerId = UUID.fromString(digitizerIdParam);
        channelIdsFromDigitizers = repo.retrieveDigitizerMembershipsByDigitizerId(digitizerId)
            .stream()
            .map(ReferenceDigitizerMembership::getChannelId)
            .distinct()
            .collect(Collectors.toSet());
      }
      // take the intersection of the two sets of site id's - these are the id's to query for.
      Set<UUID> channelIdsToInclude = new HashSet<>(channelIdsFromSites);
      if (siteName != null && digitizerIdParam != null) {
        channelIdsToInclude.retainAll(channelIdsFromDigitizers);
      }
      else {
        channelIdsToInclude.addAll(channelIdsFromDigitizers);
      }
      for (UUID chanId : channelIdsToInclude) {
        channels.addAll(repo.retrieveChannelsByEntityId(chanId));
      }
    }
    return filterByTimesIfRequired(request, response,
        channels, ReferenceChannel::getActualTime, ReferenceChannel::getEntityId);
  }

  /**
   * Handles a request to retrieve digitizers by id via
   * {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceDigitizer}
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing digitizers; may be an empty list
   */
  public static List<ReferenceDigitizer> retrieveDigitizersById(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    String idParam = request.params(":id");
    logger.info("digitizers by id endpoint hit with id: " + idParam);
    UUID id = UUID.fromString(idParam);
    List<ReferenceDigitizer> digis = repo.retrieveDigitizersByEntityId(id);
    return filterByTimesIfRequired(request, response, digis,
        ReferenceDigitizer::getActualChangeTime, ReferenceDigitizer::getEntityId);
  }

  /**
   * Handles a request to retrieve digitizers by name via
   * {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceDigitizer}
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing digitizers; may be an empty list
   */
  public static List<ReferenceDigitizer> retrieveDigitizersByName(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    String name = request.params(":name");

    logger.info("digitizers by name endpoint hit with name: " + name);

    List<ReferenceDigitizer> digis = repo.retrieveDigitizersByName(name);
    return filterByTimesIfRequired(request, response, digis,
        ReferenceDigitizer::getActualChangeTime, ReferenceDigitizer::getEntityId);
  }

  /**
   * Handles a request to retrieve digitizers via {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceDigitizer}
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing digitizers; may be an empty list
   */
  public static List<ReferenceDigitizer> retrieveDigitizers(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    logger.info("retrieveDigitizers endpoint hit");
    List<ReferenceDigitizer> digitizers = new ArrayList<>();
    String channelIdParam = request.queryParams("channel-id");

    if (channelIdParam == null) {
      digitizers = repo.retrieveDigitizers();
    }
    else {
      UUID channelId = UUID.fromString(channelIdParam);
      List<ReferenceDigitizerMembership> digitizerMemberships
          = repo.retrieveDigitizerMembershipsByChannelId(channelId);
      List<UUID> digiIds = digitizerMemberships.stream()
          .map(ReferenceDigitizerMembership::getDigitizerId)
          .distinct()
          .collect(Collectors.toList());
      for (UUID digiId : digiIds) {
        digitizers.addAll(repo.retrieveDigitizersByEntityId(digiId));
      }
    }
    return filterByTimesIfRequired(request, response,
        digitizers, ReferenceDigitizer::getActualChangeTime, ReferenceDigitizer::getEntityId);
  }

  /**
   * Handles a request to retrieve calibrations via {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceCalibration}
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing calibrations; may be an empty list
   */
  public static List<ReferenceCalibration> retrieveCalibrations(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    String idParam = request.queryParams("channel-id");
    logger.info(
        "retrieveCalibrations endpoint hit with channel id: " + idParam);
    List<ReferenceCalibration> calibrations;

    if (idParam != null) {
      UUID chanId = UUID.fromString(idParam);
      calibrations = repo.retrieveCalibrationsByChannelId(chanId);
    }
    else {
      calibrations = repo.retrieveCalibrations();
    }
    return filterByTimesIfRequired(request, response,
        calibrations, ReferenceCalibration::getActualTime, ReferenceCalibration::getChannelId);
  }

  /**
   * Handles a request to retrieve sensors via {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceSensor}
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing sensors; may be an empty list
   */
  public static List<ReferenceSensor> retrieveSensors(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    String idParam = request.queryParams("channel-id");
    logger.info(
        "retrieveSensors endpoint hit with channel id: " + idParam);
    List<ReferenceSensor> sensors;

    if (idParam != null) {
      UUID chanId = UUID.fromString(idParam);
      sensors = repo.retrieveSensorsByChannelId(chanId);
    }
    else {
      sensors = repo.retrieveSensors();
    }
    return filterByTimesIfRequired(request, response,
       sensors, ReferenceSensor::getActualTime, ReferenceSensor::getChannelId);
  }

  /**
   * Handles a request to retrieve responses via {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceResponse}
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing responses; may be an empty list
   */
  public static List<ReferenceResponse> retrieveResponses(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    String idParam = request.queryParams("channel-id");
    logger.info(
        "retrieveResponses endpoint hit with channel id: " + idParam);
    List<ReferenceResponse> responses;

    if (idParam != null) {
      UUID chanId = UUID.fromString(idParam);
      responses = repo.retrieveResponsesByChannelId(chanId);
    }
    else {
      responses = repo.retrieveResponses();
    }
    return filterByTimesIfRequired(request, response,
        responses, ReferenceResponse::getActualTime, ReferenceResponse::getChannelId);
  }

  /**
   * Handles a request to retrieve network memberships via {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceNetworkMembership}
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing network memberships; may be an empty list
   */
  public static List<ReferenceNetworkMembership> retrieveNetworkMemberships(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    // Parse and validate id params, if present.
    String networkIdParam = request.queryParams("network-id");
    String stationIdParam = request.queryParams("station-id");
    UUID networkId = null, stationId = null;
    if (networkIdParam != null) {
      networkId = UUID.fromString(networkIdParam);
    }
    if (stationIdParam != null) {
      stationId = UUID.fromString(stationIdParam);
    }

    List<ReferenceNetworkMembership> networkMembers;
    // four cases of id params
    // Case one: neither id param provided
    if (networkId == null && stationId == null) {
      networkMembers = repo.retrieveNetworkMemberships();
    }
    // Case two: both id params provided
    else if (networkId != null && stationId != null) {
      networkMembers = repo.retrieveNetworkMembershipsByNetworkAndStationId(
          networkId, stationId);
    }
    // Case three: only network-id provided
    else if (networkId != null) {
      networkMembers = repo.retrieveNetworkMembershipsByNetworkId(networkId);
    }
    // Case four: only station-id provided
    else {
      networkMembers = repo.retrieveNetworkMembershipsByStationId(stationId);
    }

    return filterByTimesIfRequired(request, response,
        networkMembers, ReferenceNetworkMembership::getActualChangeTime);
  }

  /**
   * Handles a request to retrieve station memberships via {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceStationMembership}
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing station memberships; may be an empty list
   */
  public static List<ReferenceStationMembership> retrieveStationMemberships(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    // Parse and validate id params, if present.
    String stationIdParam = request.queryParams("station-id");
    String siteIdParam = request.queryParams("site-id");
    UUID stationId = null, siteId = null;
    if (stationIdParam != null) {
      stationId = UUID.fromString(stationIdParam);
    }
    if (siteIdParam != null) {
      siteId = UUID.fromString(siteIdParam);
    }

    List<ReferenceStationMembership> stationMembers;
    // four cases of id params
    // Case one: neither id param provided
    if (stationId == null && siteId == null) {
      stationMembers = repo.retrieveStationMemberships();
    }
    // Case two: both id params provided
    else if (stationId != null && siteId != null) {
      stationMembers = repo.retrieveStationMembershipsByStationAndSiteId(
          stationId, siteId);
    }
    // Case three: only station-id provided
    else if (stationId != null) {
      stationMembers = repo.retrieveStationMembershipsByStationId(stationId);
    }
    // Case four: only site-id provided
    else {
      stationMembers = repo.retrieveStationMembershipsBySiteId(siteId);
    }

    return filterByTimesIfRequired(request, response,
        stationMembers, ReferenceStationMembership::getActualChangeTime);
  }

  /**
   * Handles a request to retrieve site memberships via {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceSiteMembership}
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing site memberships; may be an empty list
   */
  public static List<ReferenceSiteMembership> retrieveSiteMemberships(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    // Parse and validate id params, if present.
    String siteIdParam = request.queryParams("site-id");
    String channelIdParam = request.queryParams("channel-id");
    UUID siteId = null, channelId = null;
    if (siteIdParam != null) {
      siteId = UUID.fromString(siteIdParam);
    }
    if (channelIdParam != null) {
      channelId = UUID.fromString(channelIdParam);
    }

    List<ReferenceSiteMembership> siteMembers;
    // four cases of id params
    // Case one: neither id param provided
    if (siteId == null && channelId == null) {
      siteMembers = repo.retrieveSiteMemberships();
    }
    // Case two: both id params provided
    else if (siteId != null && channelId != null) {
      siteMembers = repo.retrieveSiteMembershipsBySiteAndChannelId(
          siteId, channelId);
    }
    // Case three: only site-id provided
    else if (siteId != null) {
      siteMembers = repo.retrieveSiteMembershipsBySiteId(siteId);
    }
    // Case four: only channel-id provided
    else {
      siteMembers = repo.retrieveSiteMembershipsByChannelId(channelId);
    }

    return filterByTimesIfRequired(request, response,
        siteMembers, ReferenceSiteMembership::getActualChangeTime);
  }

  /**
   * Handles a request to retrieve digitizer memberships via {@link StationReferenceRepositoryInterface}.
   *
   * Returns a JSON body with a list of {@link ReferenceDigitizerMembership}
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param repo the interface to the repository
   * @return return HTTP response with body containing digitizer memberships; may be an empty list
   */
  public static List<ReferenceDigitizerMembership> retrieveDigitizerMemberships(spark.Request request,
      spark.Response response, StationReferenceRepositoryInterface repo) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(repo);

    // Parse and validate id params, if present.
    String channelIdParam = request.queryParams("channel-id");
    String digitizerIdParam = request.queryParams("digitizer-id");
    UUID channelId = null, digitizerId = null;
    if (channelIdParam != null) {
      channelId = UUID.fromString(channelIdParam);
    }
    if (digitizerIdParam != null) {
      digitizerId = UUID.fromString(digitizerIdParam);
    }

    List<ReferenceDigitizerMembership> digitizerMembers;
    // four cases of id params
    // Case one: neither id param provided
    if (channelId == null && digitizerId == null) {
      digitizerMembers = repo.retrieveDigitizerMemberships();
    }
    // Case two: both id params provided
    else if (channelId != null && digitizerId != null) {
      digitizerMembers = repo.retrieveDigitizerMembershipsByDigitizerAndChannelId(
          digitizerId, channelId);
    }
    // Case three: only channel-id provided
    else if (channelId != null) {
      digitizerMembers = repo.retrieveDigitizerMembershipsByChannelId(channelId);
    }
    // Case four: only digitizer-id provided
    else {
      digitizerMembers = repo.retrieveDigitizerMembershipsByDigitizerId(digitizerId);
    }

    return filterByTimesIfRequired(request, response,
        digitizerMembers, ReferenceDigitizerMembership::getActualChangeTime);
  }

  ///////////////////////////////////////////////////////////////////
  /**
   * State of health operation to determine if the stationreference-coi-service is running.  Returns
   * a message with the current time in plaintext.
   *
   * @return Response code 200 with a plaintext string containing the current time
   */
  public static String alive(
      spark.Request request,
      spark.Response response) {

    response.status(HttpStatus.OK_200);
    return "alive at " + Instant.now()
        .toString();
  }

  private static <T> List<T> filterByTimesIfRequired(
      spark.Request request, spark.Response response,
      List<T> elems, Function<T, Instant> timeExtractor) {

    String startTimeParam = request.queryParams("start-time");
    String endTimeParam = request.queryParams("end-time");
    Instant startTime = null, endTime = null;
    // validate time parameters, if present
    if (startTimeParam != null) {
      try {
        startTime = Instant.parse(startTimeParam);
      }
      catch(DateTimeParseException ex) {
        logger.error("Bad format for start-time: " + startTimeParam, ex);
        response.status(400);
        return List.of();
      }
    }
    if (endTimeParam != null) {
      try {
        endTime = Instant.parse(endTimeParam);
      }
      catch(DateTimeParseException ex) {
        logger.error("Bad format for end-time: " + endTimeParam, ex);
        response.status(400);
        return List.of();
      }
    }
    // filter by time parameters, if present.
    if (startTime != null) {
      elems = FilterUtility.filterByStartTime(elems, startTime, timeExtractor);
    }
    if (endTime != null) {
      elems = FilterUtility.filterByEndTime(elems, endTime, timeExtractor);
    }
    return elems;
  }

  private static <T> List<T> filterByTimesIfRequired(
      spark.Request request, spark.Response response,
      List<T> elems, Function<T, Instant> timeExtractor,
      Function<T, UUID> idExtractor) {

    String startTimeParam = request.queryParams("start-time");
    String endTimeParam = request.queryParams("end-time");
    Instant startTime = null, endTime = null;
    // validate time parameters, if present
    if (startTimeParam != null) {
      try {
        startTime = Instant.parse(startTimeParam);
      }
      catch(DateTimeParseException ex) {
        logger.error("Bad format for start-time: " + startTimeParam, ex);
        response.status(400);
        return List.of();
      }
    }
    if (endTimeParam != null) {
      try {
        endTime = Instant.parse(endTimeParam);
      }
      catch(DateTimeParseException ex) {
        logger.error("Bad format for end-time: " + endTimeParam, ex);
        response.status(400);
        return List.of();
      }
    }
    // filter by time parameters, if present.
    if (startTime != null) {
      elems = FilterUtility.filterByStartTime(elems, startTime, timeExtractor, idExtractor);
    }
    if (endTime != null) {
      elems = FilterUtility.filterByEndTime(elems, endTime, timeExtractor);
    }
    return elems;
  }
}


package gms.dataacquisition.stationreceiver.osdgateway.accesslibrary;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.body.RawBody;
import gms.dataacquisition.stationreceiver.osdgateway.Endpoints;
import gms.dataacquisition.stationreceiver.osdgateway.SerializationUtility;
import gms.dataacquisition.stationreceiver.osdgateway.StationReceiverOsdGatewayInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.StorageUnavailableException;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;


public class StationReceiverOsdGatewayAccessLibrary implements StationReceiverOsdGatewayInterface {

    private static final org.slf4j.Logger logger = LoggerFactory
            .getLogger(StationReceiverOsdGatewayAccessLibrary.class);

    private final String fsOutputDirectory;

    private static final int DEFAULT_CONNECTION_TIMEOUT = 10000;
    private final String storeChanSegmentsUrl, storeAnalogSohUrl,
            storeBooleanSohUrl, storeDataFrameUrl, getStationIdUrl, idForChannelUrl;

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

    /**
     * Access library used to query and send data to the OSD.
     *
     * @param host IP Address or Hostname of the OSD.
     * @param port Port number of the OSD gateway.
     */
    public StationReceiverOsdGatewayAccessLibrary(String host, int port) {
        this(host, port, null);
    }

    /**
     * Access library used to query and send data to the OSD, and optionally write raw station data
     * frames to disk.
     *
     * @param host              IP Address or Hostname of the OSD.
     * @param port              Port number of the OSD gateway.
     * @param fsOutputDirectory Optional path to write raw station data frames to, as flat JSON
     *                          files.
     */
    public StationReceiverOsdGatewayAccessLibrary(String host, int port, String fsOutputDirectory) {
        this.fsOutputDirectory = fsOutputDirectory;
        this.storeAnalogSohUrl = Endpoints.storeSohAnalogUrl(host, port);
        this.storeBooleanSohUrl = Endpoints.storeSohBooleanUrl(host, port);
        this.storeChanSegmentsUrl = Endpoints.storeChannelSegmentsUrl(host, port);
        this.storeDataFrameUrl = Endpoints.storeRawStationDataFrameUrl(host, port);
        this.getStationIdUrl = Endpoints.getStationIdByNameUrl(host, port);
        this.idForChannelUrl = Endpoints.getChannelIdByNameUrl(host, port);
    }

    /**
     * Sends a channel segment batch to the OSD Gateway Service.
     *
     * @param channelSegmentBatch data to be sent
     * @throws Exception
     */
    @Override
    public void storeChannelSegments(Collection<ChannelSegment> channelSegmentBatch)
        throws Exception {

        Validate.notNull(channelSegmentBatch);
        List<String> segmentNames = channelSegmentBatch.stream()
          .map(ChannelSegment::getName).collect(Collectors.toList());
        handleResponse(postMsgPack(channelSegmentBatch, this.storeChanSegmentsUrl),
            segmentNames.toString());
    }

    /**
     * Sends a batch of AcquiredChannelSohAnalog to the OSD Gateway Service.
     *
     * @param sohs data to be sent
     * @throws Exception
     */
    @Override
    public void storeAnalogChannelStatesOfHealth(Collection<AcquiredChannelSohAnalog> sohs)
      throws Exception {

      Validate.notNull(sohs);
      handleResponse(postJson(sohs, this.storeAnalogSohUrl), sohs.toString());
    }

    /**
     * Sends a batch of AcquiredChannelSohBoolean to the OSD Gateway Service.
     *
     * @param sohs data to be sent
     * @throws Exception
     */
    @Override
    public void storeBooleanChannelStatesOfHealth(Collection<AcquiredChannelSohBoolean> sohs)
        throws Exception {

        Validate.notNull(sohs);
        handleResponse(postJson(sohs, this.storeBooleanSohUrl), sohs.toString());
    }

    /**
     * Stores a set of SOH objects which may contain both boolean and analog types.
     *
     * @param sohs the soh's to store
     * @throws Exception
     */
    @Override
    public void storeChannelStatesOfHealth(Collection<AcquiredChannelSoh> sohs)
      throws Exception {

      Validate.notNull(sohs);
      Set<AcquiredChannelSohAnalog> analogSohs = sohs.stream()
          .filter(soh -> soh instanceof AcquiredChannelSohAnalog)
          .map(soh -> (AcquiredChannelSohAnalog) soh)
          .collect(Collectors.toSet());
      Set<AcquiredChannelSohBoolean> booleanSohs = sohs.stream()
          .filter(soh -> soh instanceof AcquiredChannelSohBoolean)
          .map(soh -> (AcquiredChannelSohBoolean) soh)
          .collect(Collectors.toSet());

      storeAnalogChannelStatesOfHealth(analogSohs);
      storeBooleanChannelStatesOfHealth(booleanSohs);
    }

    /**
     * Sends a RawStationDataFrame to the OSD gateway service.
     *
     * @param frame the frame to store
     * @throws Exception
     */
    @Override
    public void storeRawStationDataFrame(RawStationDataFrame frame) throws Exception {
        FileWriter fileWriter = null;
        try {
            if (this.fsOutputDirectory != null) {
                // Write to disk.

                String json = SerializationUtility.objectMapper.writeValueAsString(frame);
                String fileName = frame.getStationName() + "-" + System.nanoTime() + ".json";
                File file = new File(this.fsOutputDirectory + fileName);
                fileWriter = new FileWriter(file);
                fileWriter.write(json);
                fileWriter.close();
                fileWriter = null;

                //Now change permissions so we can rsync them
                file.setReadable(true, false);
                file.setWritable(true, false);

                //Add json to manifest file
                File manifest = new File(this.fsOutputDirectory + "manifest.inv");
                boolean successfulManifestCreation = false;
                successfulManifestCreation = manifest.exists() || manifest.createNewFile();

                if (successfulManifestCreation) {
                    FileOutputStream manifestOutputStream = new FileOutputStream(manifest, true);
                    manifestOutputStream.write((fileName + '\n').getBytes());
                    manifest.setReadable(true, false);
                    manifest.setWritable(true, false);
                    manifestOutputStream.close();
                } else {
                    logger.error("Could not find or create manifest.inv");
                }
            }
        } catch (Exception e) {
            logger.error("Raw station data frame could not be written to the output directory.", e);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException ioex) {
                }
            }
        }

      handleResponse(postJson(frame, this.storeDataFrameUrl),
          "frame for station " + frame.getStationName()
              + " starting at " + frame.getPayloadDataStartTime());
    }

    /**
     * Retrieves a Station by it's name.
     *
     * @param stationName the name of the station
     * @return the Station, or null if it cannot be found.
     */
    @Override
    public Optional<UUID> getStationId(String stationName) throws Exception {
      UUID id = getJson(this.getStationIdUrl, Map.of("station-name", stationName), UUID.class);
      return Optional.ofNullable(id);
    }

    /**
     * Retrieve a Channel ID from the OSD Gateway Service.
     *
     * @param siteName    name of site
     * @param channelName name of channel
     * @param time        moment in time as Instant
     * @return Channel ID as UUID, or null if it cannot be found.
     */
    @Override
    public Optional<UUID> getChannelId(String siteName, String channelName, Instant time)
        throws Exception {
      UUID id = getJson(this.idForChannelUrl, Map.of("site-name", siteName,
          "channel-name", channelName, "time", time.toString()), UUID.class);
      return Optional.ofNullable(id);
    }

  /**
   * Sends the data to the OSD Gateway Service, via an HTTP post with JSON.
   *
   * @param obj          data to be sent
   * @param url          endpoint
   * @return An object containing the OSD Gateway Service's response.
   * @throws Exception if for instance, the host cannot be reached
   */
  private static HttpResponse<String> postJson(Object obj, String url) throws Exception {
    return Unirest.post(url)
        .header("Accept", "application/json")
        .header("Content-Type", "application/json")
        .header("Connection", "close")
        .body(obj)
        .asString();
  }

    /**
     * Sends the data to the OSD Gateway Service, via an HTTP post with msgpack.
     *
     * @param obj          data to be sent
     * @param url          endpoint
     * @return An object containing the OSD Gateway Service's response.
     * @throws Exception if for instance, the host cannot be reached
     */
    private static HttpResponse <String> postMsgPack(Object obj, String url) throws Exception {
        RawBody body = Unirest.post(url)
                .header("Accept", "application/json")
                .header("Content-Type", "application/msgpack")
                .header("Connection", "close")
                .body(SerializationUtility.msgPackMapper.writeValueAsBytes(obj));
        return body.asString();
    }

    /**
     * Performs a GET request with query params, expecting a response of type T back.
     *
     * @param url          endpoint
     * @param params       the parmeters of the query
     * @param responseType type of the response object
     * @param <T>          deserialized response
     * @return An object containing the OSD Gateway Service's response.
     * @throws Exception if for instance, the host cannot be reached
     */
    private static <T> T getJson(String url, Map<String, Object> params,
                                 Class<T> responseType) throws Exception {

        HttpResponse<T> response = Unirest.get(url)
                .header("Accept", "application/json")
                .queryString(params)
                .asObject(responseType);
        return response.getBody();
    }

  /**
   * Handles an HTTP response, checking for error codes and throwing exceptions.
   * @param response the http response to handle
   * @throws Exception if the response contains an error status code (client or server)
   */
    private static void handleResponse(HttpResponse<String> response,
        String dataDescription) throws Exception {
      int statusCode = response.getStatus();
      if (statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE) {
        throw new StorageUnavailableException();
      }
      else if (statusCode == HttpStatus.SC_CONFLICT) {
        logger.warn("Conflict in storing data: " + dataDescription);
      }
      // 400's and 500's are errors, except 'conflict', which is not considered an error.
      else if (statusCode >= 400 && statusCode <= 599) {
        throw new Exception(String.format("Error response from server (code %d): %s",
            statusCode, response.getBody()));
      }
    }
}

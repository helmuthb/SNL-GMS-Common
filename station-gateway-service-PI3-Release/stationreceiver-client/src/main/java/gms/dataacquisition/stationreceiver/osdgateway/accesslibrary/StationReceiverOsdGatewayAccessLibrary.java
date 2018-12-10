package gms.dataacquisition.stationreceiver.osdgateway.accesslibrary;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import gms.dataacquisition.stationreceiver.osdgateway.StationReceiverOsdGatewayInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataobject.dataacquisition.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataobject.dataacquisition.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataobject.dataacquisition.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.dto.utility.ObjectSerializationUtility;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;


public class StationReceiverOsdGatewayAccessLibrary implements StationReceiverOsdGatewayInterface {

  private final String baseUrl = "http://localhost:8080/stationreceiver/";

  private final String
      STORE_ANALOG_SOH_URL = baseUrl + "stationSohAnalog/store",
      STORE_BOOLEAN_SOH_URL = baseUrl + "stationSohBoolean/store",
      RETRIEVE_ALL_ANALOG_SOH_URL = baseUrl + "stationSohAnalog/all",
      RETRIEVE_ALL_BOOLEAN_SOH_URL = baseUrl + "stationSohBoolean/all",
      STORE_WAVEFORM_URL = baseUrl + "waveform/store",
      RETRIEVE_WAVEFORM_URL = baseUrl + "waveform/retrieveByStaChanTime";

  static {
    Unirest.setObjectMapper(new ObjectMapper() {

      public <T> T readValue(String s, Class<T> aClass) {
        try {
          return ObjectSerializationUtility.deserialize(s, aClass);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      public String writeValue(Object o) {
        try {
          return ObjectSerializationUtility.serialize(o);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  public boolean storeAcquiredChannelSohAnalog(AcquiredChannelSohAnalog soh) {

    try {
      return postJson(soh, STORE_ANALOG_SOH_URL, Boolean.class);
    } catch (Exception e) {
      return false;
    }
  }

  public boolean storeAcquiredChannelSohBoolean(AcquiredChannelSohBoolean soh) {

    try {
      return postJson(soh, STORE_BOOLEAN_SOH_URL, Boolean.class);
    } catch (Exception e) {
      return false;
    }

  }


  @Override
  public Collection<AcquiredChannelSohAnalog> retrieveAllAnalogSoh() {

    try {
      return getJson(RETRIEVE_ALL_ANALOG_SOH_URL,
          new ArrayList<AcquiredChannelSohAnalog>().getClass());
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public Collection<AcquiredChannelSohBoolean> retrieveAllBooleanSoh() {

    try {
      return getJson(RETRIEVE_ALL_BOOLEAN_SOH_URL,
          new ArrayList<AcquiredChannelSohBoolean>().getClass());
    } catch (Exception e) {
      return null;
    }
  }

  public boolean storeWaveform(Waveform waveform) {

    try {
      return postJson(waveform, STORE_WAVEFORM_URL, Boolean.class);
    } catch (Exception e) {
      return false;
    }

  }

  private static <T> T getWithParams(String url, Class<T> type, Map<String, Object> params)
      throws Exception {
    HttpResponse<T> response = Unirest.get(url)
        .queryString(params)
        .asObject(type);
    return response.getBody();
  }

  private static <T> T postJson(Object obj, String url, Class<T> responseType) throws Exception {
    HttpResponse<T> response = Unirest.post(url)
        .header("accept", "application/json")
        .header("content-type", "application/json")
        .body(obj)
        .asObject(responseType);
    return response.getBody();
  }

  private static <T> T getJson(String url, Class<T> type) throws Exception {
    HttpResponse<T> response = Unirest.get(url)
        .header("accept", "application/json")
        .header("content-type", "application/json")
        .asObject(type);
    return response.getBody();
  }
}

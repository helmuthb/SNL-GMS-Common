package gms.core.waveformqc.waveformqccontrol.osdgateway.gateway;

import static gms.core.waveformqc.waveformqccontrol.osdgateway.Application.gateway;

import gms.core.waveformqc.waveformqccontrol.objects.InvokeInputData;
import gms.core.waveformqc.waveformqccontrol.objects.dto.InvokeInputDataRequestDto;
import gms.core.waveformqc.waveformqccontrol.objects.dto.StoreQcMasksDto;
import gms.core.waveformqc.waveformqccontrol.osdgateway.util.ObjectSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

public class GatewayHandler {

  private static Logger logger = LoggerFactory.getLogger(GatewayHandler.class);

  public static Object fetchInvokeInputData(Request request, Response response) {
    InvokeInputDataRequestDto dataRequestDto = ObjectSerialization.readValue(request.body(),
        InvokeInputDataRequestDto.class);

    logger.info("load invoke request received: {}", dataRequestDto);

    InvokeInputData result = gateway.loadInvokeInputData(dataRequestDto.getProcessingChannelIds(),
        dataRequestDto.getStartTime(), dataRequestDto.getEndTime());
    logger.info("Result: {} ", result);

    return ObjectSerialization.writeValue(result);
  }

  /**
   * QcMask storage request. Accepts a JSON body representing the object to store (a {@link
   * StoreQcMasksDto}).
   *
   * Delegates to the {@link OsdGateway} to perform the store.
   */
  public static Object storeQcMasks(Request request, Response response){
    StoreQcMasksDto storeQcMasksDto = ObjectSerialization
        .readValue(request.body(), StoreQcMasksDto.class);
    logger.info("store request received: {}", storeQcMasksDto);

    gateway.store(storeQcMasksDto.getQcMasks(), storeQcMasksDto.getCreationInfos(),
        storeQcMasksDto.getStorageVisibility());

    return "";
  }


}

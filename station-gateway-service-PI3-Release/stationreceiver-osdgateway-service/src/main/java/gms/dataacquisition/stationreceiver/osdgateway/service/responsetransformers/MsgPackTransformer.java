package gms.dataacquisition.stationreceiver.osdgateway.service.responsetransformers;

import gms.dataacquisition.stationreceiver.osdgateway.SerializationUtility;
import java.io.IOException;
import spark.ResponseTransformer;

/**
 * Created by jwvicke on 11/3/17.
 */
public class MsgPackTransformer implements ResponseTransformer {

  @Override
  public String render(Object model) throws IOException {
    byte[] compressed = SerializationUtility.msgPackMapper.writeValueAsBytes(model);
    return new String(compressed);
  }
}

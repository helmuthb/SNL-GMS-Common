package gms.dataacquisition.cssloader.osdgateway.service.responsetransformers;

import gms.dataacquisition.cssloader.serialization.SerializationUtility;
import spark.ResponseTransformer;

import java.io.IOException;

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

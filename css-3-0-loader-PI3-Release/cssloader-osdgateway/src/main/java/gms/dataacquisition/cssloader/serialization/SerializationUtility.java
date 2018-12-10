package gms.dataacquisition.cssloader.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.SignalDetectionJacksonMixins;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects.ReferenceJacksonMixins;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.WaveformsJacksonMixins;
import org.msgpack.jackson.dataformat.MessagePackFactory;

/**
 * Created by jwvicke on 11/20/17.
 */
public class SerializationUtility {

  public static final ObjectMapper objectMapper = new ObjectMapper();

  public static final ObjectMapper msgPackMapper
      = new ObjectMapper(new MessagePackFactory());

  static {
    setupMapper(objectMapper);
    setupMapper(msgPackMapper);
  }

  private static void setupMapper(ObjectMapper om) {
    WaveformsJacksonMixins.register(om);
    SignalDetectionJacksonMixins.register(om);
    ReferenceJacksonMixins.register(om);
    om.findAndRegisterModules();
  }
}

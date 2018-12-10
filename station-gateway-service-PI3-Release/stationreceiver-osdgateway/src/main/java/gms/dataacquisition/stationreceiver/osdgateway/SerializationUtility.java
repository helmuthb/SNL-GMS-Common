package gms.dataacquisition.stationreceiver.osdgateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.SignalDetectionJacksonMixins;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.WaveformsJacksonMixins;
import org.msgpack.jackson.dataformat.MessagePackFactory;

public class SerializationUtility {

  public static final ObjectMapper objectMapper = new ObjectMapper();

  public static final ObjectMapper msgPackMapper = new ObjectMapper(new MessagePackFactory());

  static {
    WaveformsJacksonMixins.register(objectMapper);
    SignalDetectionJacksonMixins.register(objectMapper);
    WaveformsJacksonMixins.register(msgPackMapper);
    objectMapper.findAndRegisterModules();
    msgPackMapper.findAndRegisterModules();
  }
}

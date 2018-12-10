package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects.ProvenanceJacksonMixins;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;

import java.util.Objects;

/**
 * Utility operations to configure a Jackson {@link ObjectMapper} to correctly serialize and
 * deserialize business objects from the Waveforms domain.
 */
public class WaveformsJacksonMixins {

  /**
   * Registers an {@link ObjectMapper} with mixins for Waveforms domain business objects
   *
   * @param objectMapper register mixins with this ObjectMapper, non null
   */
  public static void register(ObjectMapper objectMapper) {

    Objects.requireNonNull(objectMapper,
        "CommonJacksonMixins.register requires non-null objectMapper");

    objectMapper
        .addMixIn(AcquiredChannelSohAnalog.class, AcquiredChannelSohAnalogDto.class)
        .addMixIn(AcquiredChannelSohBoolean.class, AcquiredChannelSohBooleanDto.class)
        .addMixIn(ChannelSegment.class, ChannelSegmentDto.class)
        .addMixIn(RawStationDataFrame.class, RawStationDataFrameDto.class)
        .addMixIn(Waveform.class, WaveformDto.class);

    // Register mix-ins for CreationInfo
    ProvenanceJacksonMixins.register(objectMapper);
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }
}

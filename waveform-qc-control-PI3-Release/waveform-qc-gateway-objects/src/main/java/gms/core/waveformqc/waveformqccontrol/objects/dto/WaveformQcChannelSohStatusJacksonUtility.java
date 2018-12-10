package gms.core.waveformqc.waveformqccontrol.objects.dto;


import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;

/**
 * Jackson serialization and deserialization utilities for {@link WaveformQcChannelSohStatus}.
 *
 * These utilities support configuring the {@link com.fasterxml.jackson.databind.ObjectMapper} so
 * that WaveformQcChannelSohStatus can be automatically serialized and deserialized using the
 * correct DTO.
 */
public class WaveformQcChannelSohStatusJacksonUtility {

  /**
   * Private default constructor to prevent instantiation of this utility class
   */
  private WaveformQcChannelSohStatusJacksonUtility() {
  }

  /**
   * Obtains a {@link Converter} to translate an {@link WaveformQcChannelSohStatus} to a {@link
   * WaveformQcChannelSohStatusDto}
   *
   * @return converter, not null
   */
  public static Converter<WaveformQcChannelSohStatus, WaveformQcChannelSohStatusDto> getConverter() {
    return new StdConverter<WaveformQcChannelSohStatus, WaveformQcChannelSohStatusDto>() {
      @Override
      public WaveformQcChannelSohStatusDto convert(
          WaveformQcChannelSohStatus waveformQcChannelSohStatus) {
        return WaveformQcChannelSohStatusDtoConverter.toDto(waveformQcChannelSohStatus);
      }
    };
  }

  /**
   * Obtains a {@link Converter} to translate an {@link WaveformQcChannelSohStatusDto} to a {@link
   * WaveformQcChannelSohStatus}
   *
   * @return converter, not null
   */
  public static Converter<WaveformQcChannelSohStatusDto, WaveformQcChannelSohStatus> getDeconverter() {
    return new StdConverter<WaveformQcChannelSohStatusDto, WaveformQcChannelSohStatus>() {
      @Override
      public WaveformQcChannelSohStatus convert(
          WaveformQcChannelSohStatusDto waveformQcChannelSohStatusDto) {
        return WaveformQcChannelSohStatusDtoConverter.fromDto(waveformQcChannelSohStatusDto);
      }
    };
  }

  /**
   * Obtains a {@link Module} with configured {@link WaveformQcChannelSohStatus} serializer and
   * deserializer.
   *
   * @return module, not null
   */
  public static Module getModule() {
    SimpleModule module = new SimpleModule("CustomWaveformQcChannelSohStatusModule");

    module.addSerializer(WaveformQcChannelSohStatus.class,
        new StdDelegatingSerializer(getConverter()));
    module.addDeserializer(WaveformQcChannelSohStatus.class,
        new StdDelegatingDeserializer<>(getDeconverter()));

    return module;
  }
}

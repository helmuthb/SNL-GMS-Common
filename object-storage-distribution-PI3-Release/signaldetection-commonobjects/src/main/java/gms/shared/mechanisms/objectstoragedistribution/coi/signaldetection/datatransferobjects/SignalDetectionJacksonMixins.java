package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects.ProvenanceJacksonMixins;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Network;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Calibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Response;

import java.util.Objects;

/**
 * Utility operations to configure a Jackson {@link ObjectMapper} to correctly serialize and
 * deserialize business objects from the Signal Detection domain.
 */
public class SignalDetectionJacksonMixins {

  /**
   * Registers an {@link ObjectMapper} with mixins for Signal Detection domain objects
   *
   * @param objectMapper register mixins with this ObjectMapper, non null
   */
  public static void register(ObjectMapper objectMapper) {
    Objects.requireNonNull(objectMapper,
        "CommonJacksonMixins.register requires non-null objectMapper");

    // Register QcMask mixins
    objectMapper
        .addMixIn(QcMask.class, QcMaskDto.class)
        .addMixIn(QcMaskVersion.class, QcMaskVersionDto.class)
        .addMixIn(QcMaskVersionReference.class, QcMaskVersionReferenceDto.class);

    // Register Processing Station Reference mixins
    objectMapper
        .addMixIn(Calibration.class, CalibrationDto.class)
        .addMixIn(Channel.class, ChannelDto.class)
        .addMixIn(Site.class, SiteDto.class)
        .addMixIn(Response.class, ResponseDto.class);

    objectMapper
        .addMixIn(Site.class, SiteDto.class)
        .addMixIn(Station.class, StationDto.class)
        .addMixIn(Network.class, NetworkDto.class);

    // Register FilterDefinition
    objectMapper.addMixIn(FilterDefinition.class, FilterDefinitionDto.class);

    // Register detection mixins
    objectMapper.addMixIn(FeatureMeasurement.class, FeatureMeasurementDto.class);
    objectMapper.addMixIn(SignalDetection.class, SignalDetectionDto.class);
    objectMapper.addMixIn(SignalDetectionHypothesis.class, SignalDetectionHypothesisDto.class);

    // Register mix-ins for CreationInfo
    ProvenanceJacksonMixins.register(objectMapper);
  }
}

package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.testUtilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.SignalDetectionJacksonMixins;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * QcMask Test Fixtures
 */
public class TestFixtures {

  public static final QcMask qcMask;
  public static final QcMaskVersion qcMaskVersion;
  public static final QcMaskVersionReference qcMaskVersionReference;

  private static ObjectMapper objectMapper;
  public static final String qcMaskJson;

  static {
    qcMask = QcMask.create(
        UUID.randomUUID(),
        Arrays.asList(
            QcMaskVersionReference.from(UUID.randomUUID(), 3),
            QcMaskVersionReference.from(UUID.randomUUID(), 1)),
        Arrays.asList(UUID.randomUUID(), UUID.randomUUID()),
        QcMaskCategory.WAVEFORM_QUALITY,
        QcMaskType.LONG_GAP,
        "Rationale",
        Instant.now(),
        Instant.now().plusSeconds(2),
        UUID.randomUUID());

    qcMask.addQcMaskVersion(
        Arrays.asList(UUID.randomUUID(), UUID.randomUUID()),
        QcMaskCategory.WAVEFORM_QUALITY,
        QcMaskType.SPIKE,
        "Rationale SPIKE",
        Instant.now().plusSeconds(3),
        Instant.now().plusSeconds(4),
        UUID.randomUUID());

    qcMaskVersion = qcMask.getCurrentQcMaskVersion();

    qcMaskVersionReference = qcMaskVersion.getParentQcMasks().iterator().next();

    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    SignalDetectionJacksonMixins.register(objectMapper);

    qcMaskJson = toJson(qcMask);
  }

  private static String toJson(Object object) {
    String json = "{Initialization error}";
    try {
      json = objectMapper.writeValueAsString(List.of(object));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    return json;
  }
}

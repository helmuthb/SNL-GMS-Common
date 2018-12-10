package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the ability to serialize and deserialize the CreationInfo
 * class using the ObjectSerializationUtility with the registered CreationInfoDto mix-in.
 * Also check that malformed JSON (null fields) cause exceptions on deserialization.
 */
public class CreationInfoDtoTest {

  private final CreationInfo creationInfo = TestFixtures.creationInfo;
  private String creationInfoJson;

  private ObjectMapper objectMapper;

  @Before
  public void setup() throws Exception {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    ProvenanceJacksonMixins.register(objectMapper);

    assertNotNull(creationInfo);
    creationInfoJson = objectMapper.writeValueAsString(creationInfo);
    assertNotNull(creationInfoJson);
    assertTrue(creationInfoJson.length() > 0);
  }

  @Test
  public void deserializeTest() throws Exception {
    CreationInfo deserializedCreationInfo = callDeserializer(creationInfoJson);
    assertNotNull(deserializedCreationInfo);
    assertTrue(deserializedCreationInfo.equals(creationInfo));
  }

  private CreationInfo callDeserializer(String json) throws Exception {
    return objectMapper.readValue(json, CreationInfo.class);
  }
}

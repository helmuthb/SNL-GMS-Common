package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the ability to serialize and deserialize the CreationInfo
 * class using the ObjectSerializationUtility with the registered CreationInfoDto mix-in.
 * Also check that malformed JSON (null fields) cause exceptions on deserialization.
 */
public class InformationSourceDtoTest {

  private final InformationSource info = TestFixtures.informationSource;
  private String infoJson;

  private ObjectMapper objectMapper;

  @Before
  public void setup() throws Exception {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    ProvenanceJacksonMixins.register(objectMapper);

    assertNotNull(info);
    infoJson = objectMapper.writeValueAsString(info);
    assertNotNull(infoJson);
    assertTrue(infoJson.length() > 0);
  }



  @Test
  public void testSerialization() throws Exception {

    String json = objectMapper.writeValueAsString(info);

    assertNotNull(json);
    assertTrue(json.length() > 0);

    InformationSource informationSource = objectMapper
        .readValue(json, InformationSource.class);

    assertNotNull(informationSource);
    assertEquals(info, informationSource);
  }
}

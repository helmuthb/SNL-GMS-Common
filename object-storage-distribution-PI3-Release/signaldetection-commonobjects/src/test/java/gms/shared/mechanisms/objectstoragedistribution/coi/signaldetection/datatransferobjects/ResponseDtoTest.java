package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Response;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ResponseDtoTest {

  private final Response obj = TestFixtures.response;
  private String json;

  @Before
  public void setup() throws Exception {
    assertNotNull(obj);
    json = TestFixtures.objMapper.writeValueAsString(obj);
    assertNotNull(json);
    assertTrue(json.length() > 0);
  }

  /**
   * Tests that a COI object can be serialized and deserialized.
   * @throws Exception
   */
  @Test
  public void deserializeTest() throws Exception {
    Response deserialObj = callDeserializer(json);
    assertNotNull(deserialObj);
    assertEquals(deserialObj, obj);
  }

  private static Response callDeserializer(String json) throws Exception {
    return TestFixtures.objMapper.readValue(json, Response.class);
  }

}

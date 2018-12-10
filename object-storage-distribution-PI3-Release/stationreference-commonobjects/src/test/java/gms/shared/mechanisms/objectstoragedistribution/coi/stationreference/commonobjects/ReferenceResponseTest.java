package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReferenceResponseTest {


  @BeforeClass
  public static void setup() {
  }


  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceResponse.class);
  }

  @Test
  public void testReferenceResponseCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceResponse.class, "create",
        TestFixtures.channel.getEntityId(),
        TestFixtures.responseType,
        TestFixtures.responseData,
        TestFixtures.responseUnits,
        TestFixtures.actualTime,
        TestFixtures.source,
        TestFixtures.comment);
  }

  @Test
  public void testReferenceResponseFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceResponse.class, "from",
        TestFixtures.responseId,
        TestFixtures.channel.getEntityId(),
        TestFixtures.responseType,
        TestFixtures.responseData,
        TestFixtures.responseUnits,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source,
        TestFixtures.comment);
  }


  /**
   * Test that arguments are saved correctly.
   * @throws Exception
   */
  @Test
  public void testReferenceResponseCreate() {
    ReferenceResponse response = ReferenceResponse.create(
        TestFixtures.channel.getEntityId(),
        TestFixtures.responseType,
        TestFixtures.responseData,
        TestFixtures.responseUnits,
        TestFixtures.actualTime,
        TestFixtures.source,
        TestFixtures.comment);
    assertNotEquals(TestFixtures.responseId, response.getId());
    assertEquals(TestFixtures.channel.getEntityId(), response.getChannelId());
    assertEquals(TestFixtures.responseType, response.getResponseType());
    assertEquals(TestFixtures.responseData, response.getResponseData());
    assertEquals(TestFixtures.responseUnits, response.getUnits());
    assertEquals(TestFixtures.actualTime, response.getActualTime());
    assertNotEquals(TestFixtures.systemTime, response.getSystemTime());
    assertEquals(TestFixtures.source, response.getInformationSource());
    assertEquals(TestFixtures.comment, response.getComment());
  }


  /**
   * Test that arguments are saved correctly.
   * @throws Exception
   */
  @Test
  public void testReferenceResponseFrom() {
    ReferenceResponse response = ReferenceResponse.from(
        TestFixtures.responseId,
        TestFixtures.channel.getEntityId(),
        TestFixtures.responseType,
        TestFixtures.responseData,
        TestFixtures.responseUnits,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source,
        TestFixtures.comment);
    assertEquals(TestFixtures.responseId, response.getId());
    assertEquals(TestFixtures.channel.getEntityId(), response.getChannelId());
    assertEquals(TestFixtures.responseType, response.getResponseType());
    assertEquals(TestFixtures.responseData, response.getResponseData());
    assertEquals(TestFixtures.responseUnits, response.getUnits());
    assertEquals(TestFixtures.actualTime, response.getActualTime());
    assertEquals(TestFixtures.systemTime, response.getSystemTime());
    assertEquals(TestFixtures.source, response.getInformationSource());
    assertEquals(TestFixtures.comment, response.getComment());
  }

}

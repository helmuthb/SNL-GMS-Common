package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Calibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * Tests the {@link SignalDetectionJacksonMixins}
 */
public class CommonJacksonMixinsTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private ObjectMapper objectMapper;

  @Before
  public void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    SignalDetectionJacksonMixins.register(objectMapper);
  }

  private <T> void runTest(T expected, Class<T> type) throws Exception {
    assertEquals(expected, objectMapper.readValue(objectMapper.writeValueAsString(expected), type));
  }

  @Test
  public void testRegisterSignalDetectionMixinsRegistersQcMaskVersionReference() throws Exception {
    runTest(TestFixtures.qcMaskVersionReference, QcMaskVersionReference.class);
  }

  @Test
  public void testRegisterSignalDetectionMixinsRegistersQcMaskVersion() throws Exception {
    runTest(TestFixtures.qcMaskVersion, QcMaskVersion.class);
  }

  @Test
  public void testRegisterSignalDetectionMixinsRegistersQcMask() throws Exception {
    runTest(TestFixtures.qcMask, QcMask.class);
  }

  @Test
  public void testRegisterSignalDetectionMixinsRegistersProcessingCalibration() throws Exception {
    runTest(TestFixtures.processingCalibration1, Calibration.class);
  }

  @Test
  public void testRegisterSignalDetectionMixinsRegistersProcessingChannel() throws Exception {
    runTest(TestFixtures.processingChannel01, Channel.class);
  }

  @Test
  public void testRegisterSignalDetectionMixinsRegistersProcessingSite() throws Exception {
    runTest(TestFixtures.processingSite, Site.class);
  }

  @Test
  public void testRegisterSignalDetectionMixinsNullObjectMapperExpectNullPointerException() {

    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "CommonJacksonMixins.register requires non-null objectMapper");
    SignalDetectionJacksonMixins.register(null);
  }
}

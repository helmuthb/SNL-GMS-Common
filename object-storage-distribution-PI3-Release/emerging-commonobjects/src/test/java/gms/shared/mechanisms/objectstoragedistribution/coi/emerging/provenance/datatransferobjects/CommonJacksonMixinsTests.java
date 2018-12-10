package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.AnalystActionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingStepReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.datatransferobjects.AnalystActionReferenceDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.datatransferobjects.ProcessingStepReferenceDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CommonJacksonMixinsTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private ObjectMapper objectMapper;

  @Before
  public void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();

    objectMapper
        .addMixIn(AnalystActionReference.class, AnalystActionReferenceDto.class)
        .addMixIn(ProcessingStepReference.class, ProcessingStepReferenceDto.class);
  }

  private <T> void runTest(T expected, Class<T> type) throws Exception {
    ProvenanceJacksonMixins.register(objectMapper);
    assertEquals(expected, objectMapper.readValue(objectMapper.writeValueAsString(expected), type));
  }

  @Test
  public void testRegisterProvenanceMixinsRegistersSoftwareComponentInfo() throws Exception {
    runTest(TestFixtures.softwareComponentInfo, SoftwareComponentInfo.class);
  }

  @Test
  public void testRegisterProvenanceMixinsRegistersCreationInfo() throws Exception {
    runTest(TestFixtures.creationInfo, CreationInfo.class);
  }

  @Test
  public void testRegisterProvenanceMixinsRegistersCreationInformation() throws Exception {
    runTest(TestFixtures.creationInformation, CreationInformation.class);
  }

  @Test
  public void testRegisterProvenanceMixinsNullObjectMapperExpectNullPointerException() {

    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "CommonJacksonMixins.register requires non-null objectMapper");
    ProvenanceJacksonMixins.register(null);
  }
}

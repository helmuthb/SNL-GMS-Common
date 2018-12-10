package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.time.Instant;
import java.util.UUID;
import org.junit.Test;

public class InformationSourceTest {

  @Test
  public void testParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        InformationSource.class, "create", "abc", Instant.now(), "xyz");
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(InformationSource.class);
  }
}

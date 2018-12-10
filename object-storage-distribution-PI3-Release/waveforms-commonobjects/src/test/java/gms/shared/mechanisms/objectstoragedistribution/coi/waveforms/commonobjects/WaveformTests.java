package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.time.Instant;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * Tests {@link Waveform} creation and usage semantics Created by trsault on 8/25/17.
 */
public class WaveformTests {

  private final Instant startTime = Instant.EPOCH;
  private final Instant endTime = Instant.now();
  private final double[] values = new double[]{0};

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(Waveform.class, false);
  }

  @Test
  public void createOperationValidationTest() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        Waveform.class, "create",
        startTime, endTime, 0, 0, values);
  }
}

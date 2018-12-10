package gms.core.waveformqc.waveformsignalqc.algorithm;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.DoubleStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RootMeanSquareTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testRmsDc() throws Exception {
    final double dcValue = 17.0;
    final double[] dcInput = new double[20];
    Arrays.fill(dcInput, dcValue);

    assertEquals(dcValue, RootMeanSquare.rms(dcInput), 1e-100);
  }

  @Test
  public void testRms() throws Exception {
    final double[] input = DoubleStream.iterate(1.2, d -> d + 1.001).limit(15).toArray();
    final double expectedRms = Math.sqrt(Arrays.stream(input).map(d -> d * d).sum() / input.length);

    assertEquals(expectedRms, RootMeanSquare.rms(input), 1e-100);
  }

  @Test
  public void testRmsNegatives() throws Exception {
    final double[] input = DoubleStream.iterate(-1.2, d -> d - 1.001).limit(15).toArray();
    final double expectedRms = Math.sqrt(Arrays.stream(input).map(d -> d * d).sum() / input.length);

    assertEquals(expectedRms, RootMeanSquare.rms(input), 1e-100);
  }

  @Test
  public void testRmsNullInputExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("RootMeanSquare requires non-null input signal");
    RootMeanSquare.rms(null);
  }

  @Test
  public void testRmsEmptyInputExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("RootMeanSquare requires non-empty input signal");
    RootMeanSquare.rms(new double[]{});
  }
}

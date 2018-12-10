package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TimeseriesTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testSampleCount() {
    Timeseries t = new Timeseries(Instant.EPOCH, Instant.EPOCH.plusSeconds(100), 2) {
    };
    assertEquals(201, t.getSampleCount());

    Timeseries t2 = new Timeseries(Instant.EPOCH, Instant.EPOCH.plusSeconds(100), .5) {
    };
    assertEquals(51, t2.getSampleCount());
  }

  @Test
  public void testTimeForSample() {
    Instant start = Instant.EPOCH;
    Instant end = Instant.EPOCH.plusSeconds(100);

    Timeseries t = new Timeseries(start, end, 1) {
    };

    assertEquals(Instant.EPOCH, t.timeForSample(0));
    assertEquals(Instant.EPOCH.plusSeconds(5), t.timeForSample(5));
    assertEquals(Instant.EPOCH.plusSeconds(100), t.timeForSample(100));
  }

  @Test
  public void testTimeForSampleBeyondRangeExpectIllegalArgumentException() {
    Timeseries t = new Timeseries(Instant.EPOCH, Instant.EPOCH.plusSeconds(100), 1) {
    };

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "timeForSample requires sample index within the inclusive range [0, getSampleCount() - 1]");
    t.timeForSample(t.getSampleCount());
  }

  @Test
  public void testTimeForSampleBeforeRangeExpectIllegalArgumentException() {
    Timeseries t = new Timeseries(Instant.EPOCH, Instant.EPOCH.plusSeconds(100), 1) {
    };

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "timeForSample requires sample index within the inclusive range [0, getSampleCount() - 1]");
    t.timeForSample(-1);
  }
}

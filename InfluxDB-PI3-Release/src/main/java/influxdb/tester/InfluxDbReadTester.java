package influxdb.tester;

import data.Constants;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import influxdb.repository.WaveformRepositoryInfluxDb;

import java.time.Instant;

/**
 * Created by jwvicke on 11/16/17.
 */
public class InfluxDbReadTester {
  private static final WaveformRepositoryInfluxDb waveformPersistence = new WaveformRepositoryInfluxDb();

  public static void run() {
    Instant start = Instant.EPOCH;
    Instant end = start.plusSeconds(50000000);
    for (int size : Constants.TEST_SIZES) {
      doTest("TEST_" + size, start, end);
    }
  }

  private static void doTest(String seriesName, Instant startTime, Instant endTime) {
    long t1 = System.currentTimeMillis();
    Waveform wf = waveformPersistence.retrieveWaveformByTime(seriesName, startTime, endTime);
    long t2 = System.currentTimeMillis();
    if ( wf == null ) {
      System.out.println("No waveform returned!");
    }
    else {
      System.out.println("Influx read performance test: read " + wf.getValues().length
              + " samples in " + (t2 - t1) + " milliseconds");
    }
  }
}

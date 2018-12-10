package influxdb.tester;

import data.Constants;
import data.WaveformGenerators;
import influxdb.repository.WaveformRepositoryInfluxDb;

import java.util.UUID;

/**
 * Created by jwvicke on 11/16/17.
 */
public class InfluxDbWriteTester {
  private static final WaveformRepositoryInfluxDb waveformPersistence = new WaveformRepositoryInfluxDb();

  public static void run() {
    for (int size : Constants.TEST_SIZES) {
      doTest("TEST_" + size, size);
    }
    //WaveformRepositoryInfluxDb.flush();
  }

  public static long doTest(String seriesName, int size) {
      return waveformPersistence.storeChannelSegmentWaveform(
              WaveformGenerators.buildWaveform(size),
              seriesName, UUID.randomUUID());
  }
}

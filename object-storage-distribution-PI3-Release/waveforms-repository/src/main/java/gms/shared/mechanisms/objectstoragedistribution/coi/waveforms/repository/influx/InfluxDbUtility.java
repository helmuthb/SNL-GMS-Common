package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.influx;

import java.util.*;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.influxdb.dto.QueryResult;
import org.slf4j.LoggerFactory;

/**
 * Define a class which provides utility methods for InfluxDB.
 */
public class InfluxDbUtility {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(InfluxDbUtility.class);

  // Define the names used to store waveform data.
  public static final String RAW_MEASUREMENT_NAME = "RawWaveform";
  public static final String VALUE_TAG = "value";
  public static final String TIME_TAG = "time";
  public static final String ID_TAG = "id";
  public static final String SAMPLE_RATE_TAG = "sampleRate";


  /**
   * Generate a collection of Waveform objects given a list of waveform points. If the sample rate
   * changes over the list of points, then a new waveform is generated.
   *
   * @param points A list of WaveformPoint objects.
   * @return A Waveform object or null.
   */
  public static List<Waveform> buildWaveformFromPoints(List<WaveformPoint> points) {

    Map<Double, List<WaveformPoint>> pointsBySampleRate = points.stream()
        .collect(Collectors.groupingBy(p -> p.sampleRate));

    List<Waveform> waveforms = new ArrayList<>();

    for (Map.Entry<Double, List<WaveformPoint>> e : pointsBySampleRate.entrySet()) {
      List<WaveformPoint> wfPoints = e.getValue();
      Collections.sort(wfPoints);  // sort by start time
      double sampleRate = e.getKey();
      Instant start = wfPoints.get(0).time;
      Instant end = wfPoints.get(wfPoints.size() - 1).time;
      double[] values = wfPoints.stream().mapToDouble(p -> p.value).toArray();
      waveforms.add(Waveform.create(start, end, sampleRate, values.length, values));
    }
    return waveforms;
  }

  /**
   * Creates a query string to select based on processingChannelId and a time range.
   *
   * @param processingChannelId the id
   * @param start the start time of the range
   * @param end the end time of the range
   * @return a query string for the timeseries database
   */
  public static String byChannelIdAndTimeQuery(UUID processingChannelId, Instant start,
      Instant end) {
    Validate.notNull(processingChannelId);
    Validate.notNull(start);
    Validate.notNull(end);

    return String.format("SELECT * FROM %s WHERE (%s >= '%s' AND %s <= '%s') AND %s = '%s';",
        RAW_MEASUREMENT_NAME, TIME_TAG, start, TIME_TAG, end, ID_TAG, processingChannelId);
  }

  /**
   * Build a query string to retrieve the timeseries metadata associated with an id.
   *
   * @param id The UUID used to store the timeseries.
   * @return A SQL like string.
   */
  public static String querySampleRateById(UUID id, Instant start, Instant end)
      throws NullPointerException {
    Validate.notNull(id);
    return String.format("SELECT %s FROM %s WHERE (%s >= '%s' AND %s <= '%s') AND %s = '%s';",
        SAMPLE_RATE_TAG, RAW_MEASUREMENT_NAME, TIME_TAG, start, TIME_TAG, end, ID_TAG,
        id.toString());
  }

  /**
   * Creates a point given a time/value pair, a sample rate, and an ID.
   *
   * @param point A point represented as a time and value.
   * @param id The UUID to associate with the series.
   */
  private static Point toPoint(ImmutablePair<Instant, Double> point, double sampleRate, UUID id) {

    return Point.measurement(RAW_MEASUREMENT_NAME)
        .time(getEpochMicroseconds(point.getLeft()), TimeUnit.MICROSECONDS)
        .addField(VALUE_TAG, point.getRight())
        .addField(SAMPLE_RATE_TAG, sampleRate)
        .tag(ID_TAG, id.toString())
        .build();
  }

  /**
   * Save a series of waveform point and tag with the unique channel segment UUID.
   *
   * @param influx The connection manager for InfluxDB.
   * @param points A collection of points represented as a time and value.
   * @param id The ChannelSegment UUID used to tag the series.
   */
  public static void writeWaveformPoints(InfluxDB influx,
      Collection<ImmutablePair<Instant, Double>> points,
      double sampleRate, UUID id) throws NullPointerException {
    Validate.notNull(influx);
    Validate.notNull(points);
    Validate.notNull(id);

    long startTime = System.currentTimeMillis();

    points.stream()
        .map(p -> toPoint(p, sampleRate, id))
        .forEach(influx::write);
    influx.flush();

    logger.debug("Wrote " + points.size() + " points to Influx in "
        + (System.currentTimeMillis() - startTime) + " milliseconds.");

  }

  /**
   * Parses an InfluxDB 'Series' object into a Waveform.
   *
   * @param series create a timeseries from this series
   * @return Waveform object with data from the input series
   * @throws NullPointerException If the input parameter is null.
   */
  public static Waveform parseAsWaveform(QueryResult.Series series, Instant endTime)
      throws NullPointerException {
    Validate.notNull(series);
    long sTime = System.currentTimeMillis();
    List<String> columns = series.getColumns();
    List<List<Object>> valuesList = series.getValues();
    boolean isMetadata = false;

    double[] values = new double[valuesList.size()];
    Instant first = null; // Track the date and time of the first point in the series
    int count = 0;    // Track the count of points retrieved
    Double sampleRate = null;

    if (columns.indexOf(VALUE_TAG) == -1) {
      isMetadata = true;
    }

    // Loop over all the data points in the series.
    for (List valList : valuesList) {
      Instant time = Instant.parse((String) (valList.get(columns.indexOf(TIME_TAG))));

      // Save the first time in the series.
      if (first == null) {
        first = time;
      }
      // Save the first sample rate in the series.
      if (sampleRate == null) {
        sampleRate = (Double) (valList.get(columns.indexOf(SAMPLE_RATE_TAG)));
      }

      // Verify whether sampled data 'values' were queried.
      if (!isMetadata) {
        double val = (double) valList.get(columns.indexOf(VALUE_TAG));
        values[count++] = val;
      } else {
        values = new double[0];
        break;
      }

    }
    logger.info("Processed " + valuesList.size() + " points from Influx in "
        + (System.currentTimeMillis() - sTime) + " milliseconds.");

    return isMetadata ? Waveform.withoutValues(first, endTime, sampleRate)
                      : Waveform.create(first, endTime, sampleRate, count, values);
  }


  /**
   * Given an Instant, returns the number of microsecond since the unix epoch began (jan 1, 1970)
   *
   * @param instant the instant of time
   * @return epoch microseconds
   */
  private static long getEpochMicroseconds(Instant instant) {
    return (long) (instant.getEpochSecond() * 1E+6) + (instant.getNano() / 1000);
  }


}

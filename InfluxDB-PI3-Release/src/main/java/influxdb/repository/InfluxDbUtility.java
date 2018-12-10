package influxdb.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Define a class which provides utility methods for InfluxDB.
 */
public class InfluxDbUtility {

  private static final Logger logger = LoggerFactory.getLogger(InfluxDbUtility.class);

  // Define the names used to store waveform data.
  private static final String VALUE_TAG = "value";
  private static final String TIME_TAG = "time";
  private static final String SEGMENT_TAG = "segmentID";
  private static final String SAMPLE_RATE_TAG = "sampleRate";

  /**
   * Parses an InfluxDB 'Series' object into a Waveform.
   *
   * @param series create a timeseries from this series
   * @return Waveform object with data from the input series
   * @throws NullPointerException If the input parameter is null.
   */
  public static Waveform parseAsWaveform(QueryResult.Series series) throws NullPointerException {
    Validate.notNull(series);
    List<String> columns = series.getColumns();
    List<List<Object>> valuesList = series.getValues();

    double[] values = new double[valuesList.size()];
    Instant first = null; // Track the date and time of the first point in the series
    int count = 0;    // Track the count of points retrieved
    Double sampleRate = null;

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

      double val = (double) valList.get(columns.indexOf(VALUE_TAG));
      values[count++] = val;
    }

    return new Waveform(first, sampleRate, count, values);
  }

  /**
   * Generate a query string given the input parameters.
   *
   * @param startTime The start time of the waveform of interest.
   * @param endTime The end time of the waveform of interest.
   * @param seriesName the name of the series
   * @return A string representing the InfluxDB query.
   * @throws NullPointerException If given null parameters.
   * @throws IllegalArgumentException If given empty parameters.
   */
  public static String bySeriesAndTimeQueryString(Instant startTime, Instant endTime, String seriesName)
          throws NullPointerException, IllegalArgumentException {

    Validate.notNull(startTime);
    Validate.notNull(endTime);
    Validate.notEmpty(seriesName);

    String start = startTime.toString();
    String end = endTime.toString();
    return String.format("SELECT * FROM %s WHERE (%s >= '%s' AND %s <= '%s');",
            seriesName, TIME_TAG, start, TIME_TAG, end);
  }

  /**
   * Create a new point with the appropriate fields.
   *
   * @param point The point and time for the new point.
   * @param sampleRate The sample rate used when collecting the points.
   * @param seriesName the name of the series
   * @param id The UUID associated with the channel segment.
   * @return An Influx point.
   */
  private static Point influxPoint(ImmutablePair<Instant, Double> point,
                                        double sampleRate, String seriesName, UUID id) {
    return Point.measurement(seriesName)
            .time(getEpochMicroseconds(point.getLeft()), TimeUnit.MICROSECONDS)
            .addField(VALUE_TAG, point.getRight())
            .addField(SAMPLE_RATE_TAG, sampleRate)
            .addField(SEGMENT_TAG, id.toString())
            .build();
  }

  /**
   * Save a series of waveform point for the indicated site and channel.
   *
   * @param influx The connection manager for InfluxDB.
   * @param points A collection of points represented as a time and value.
   * @param seriesName the name of the series
   * @param id The UUID of the channel segment which delivered this waveform.
   */
  public static long writeWaveformPoints(InfluxDB influx,
                                         Collection<ImmutablePair<Instant, Double>> points, double sampleRate,
                                         String seriesName, UUID id)
          throws NullPointerException, IllegalArgumentException {

    Validate.notNull(points);
    Validate.notEmpty(seriesName);
    Validate.notNull(id);

    long startTime = System.currentTimeMillis();

    for (ImmutablePair<Instant, Double> point : points) {
      influx.write(influxPoint(point, sampleRate, seriesName, id));
    }
    influx.flush();

    long totalTime = (System.currentTimeMillis() - startTime);
    System.out.println(String.format("InfluxDbUtility: Wrote %d points to Influx in %d milliseconds.",
            points.size(), totalTime));
    return totalTime;
  }

  private static long getEpochMicroseconds(Instant instant) {
    return (long) (instant.getEpochSecond() * 1E+6) + (instant.getNano() / 1000);
  }


}

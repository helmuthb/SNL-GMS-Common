package influxdb.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import org.apache.commons.lang3.Validate;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Store Waveform COI objects in the appropriate database.
 */
public class WaveformRepositoryInfluxDb {

  /**
   * Default constructor.
   */
  public WaveformRepositoryInfluxDb() {}

  /**
   * Retrieve a collection from waveforms from the database.
   *
   * @param seriesName name of the influx series
   * @param startTime Starting time for the time-series.
   * @param endTime Ending time for the time-series.
   * @return A Waveform or NULL if none found.
   * @throws NullPointerException If any parameters are null.
   * @throws IllegalArgumentException If any string parameters are empty.
   * @throws IllegalStateException If unable to acquire connection to the database.
   */
  public Waveform retrieveWaveformByTime(String seriesName, Instant startTime, Instant endTime)
    throws NullPointerException, IllegalArgumentException {

    Validate.notEmpty(seriesName);
    Validate.notNull(startTime);
    Validate.notNull(endTime);

    // Get a connection to InfluxDB.
    InfluxDB influxDB = TimeseriesDependencies.getConnection();

    if (influxDB == null) {
      throw new IllegalStateException("Could not get connection to database");
    }

    String queryString = InfluxDbUtility.bySeriesAndTimeQueryString(startTime, endTime, seriesName);
    Query query = new Query(queryString, TimeseriesDependencies.getDatabaseName());

    // Issue the query to InfluxDB.
    QueryResult results = influxDB.query(query);

    // Process the waveform results. If there are no results an empty collection will be
    // returned.
    List<Waveform> waveforms = results.getResults().stream()
        .filter(r -> Objects.nonNull(r))
        .map(r -> r.getSeries())  // get each List<Series> in the result set
        .filter(r -> Objects.nonNull(r))
        .flatMap(
            List::stream)    // flatten each all from the List<Series> into one Stream<Series>
        .map(
            InfluxDbUtility::parseAsWaveform) // map the parseAsWaveform function over each Series
        .collect(Collectors.toList());  // return the result as a List

    return (waveforms != null && waveforms.size() > 0) ? waveforms.get(0) : null;
  }

  /**
   * Store a waveform associated with a channel segment in the raw continuous timeseries
   * associated with the site and channel names.
   *
   * @param waveform The Waveform object.
   * @param seriesName the name of the series
   * @param channelSegmentId The UUID obtained from the storing from the ChannelSegment metadata.
   * @return True if successful, otherwise false.
   * @throws NullPointerException
   */
  public long storeChannelSegmentWaveform(Waveform waveform, String seriesName, UUID channelSegmentId)
      throws NullPointerException {

    Validate.notEmpty(seriesName);
    Validate.notNull(waveform);
    Validate.notNull(channelSegmentId);

    // Get connection.  If it returns null, return false.
    InfluxDB influxDB = TimeseriesDependencies.getConnection();

    // Write the data points.
    return InfluxDbUtility.writeWaveformPoints(
            influxDB, waveform.asTimedPairs(), waveform.getSampleRate(),
            seriesName, channelSegmentId);
  }

  public static void flush() {
    TimeseriesDependencies.getConnection().flush();
  }

}

package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.influx;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;
import org.slf4j.LoggerFactory;

/**
 * Process waveform results in chunks rather then waiting for one potentially large response. TODO:
 * Consider revisions to improve performance.
 */
public class WaveformResults {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(WaveformResults.class);
  private List<QueryResult> resultList = null;
  private List<WaveformPoint> points = null;
  private String errorString = null;


  /**
   * Constructor
   */
  public WaveformResults() {
    this.resultList = new LinkedList<>();
    this.points = new ArrayList<>();
  }


  /**
   * Add a result chunk to the list of chunks to process.
   *
   * @param queryResult A QueryResult object.
   */
  public void addResult(QueryResult queryResult) {
    logger.debug("Added result chunk to list...");
    resultList.add(queryResult);
    logger.debug(queryResult.toString());
  }

  /**
   * Indicate if an error been detected.
   *
   * @return Boolean
   */
  public boolean isError() {
    return errorString != null;
  }

  /**
   * Get the error string.
   *
   * @return A string if there was an error, otherwise null.
   */
  public String getError() {
    return errorString;
  }


  /**
   * Convert the results into a list of Waveform objects.  A new waveform is generated whenever the
   * sample rate changes.
   *
   * @return A Waveform object.
   */
  public List<Waveform> getWaveforms() {
    logger.debug("Returning waveform with " + points.size() + " points.");
    return InfluxDbUtility.buildWaveformFromPoints(points);
  }

  /**
   * Process all results.  The results are presented as chunks and stored as waveform points. TODO:
   * Can this be reworked so it doesn't spin waiting for chunks?
   */

  public void saveWaveformResults() {

    QueryResult queryResult;

    logger.debug("Processing next chunk...");
    LinkedList<QueryResult> resultLinkedList = (LinkedList<QueryResult>)resultList;
    while((queryResult = resultLinkedList.poll()) != null) {

      logger.debug("Query results: " + queryResult.toString());

      // Check for the done indicator.
      if (queryResult.getError() != null && queryResult.getError().trim()
          .equalsIgnoreCase("DONE")) {
        logger.debug("Done detected: " + queryResult.toString());
        return;
      }

      // If there are result, then loop over all.
      if (queryResult.getResults() != null) {
        for (Result result : queryResult.getResults()) {
          if (result != null) {

            // Each results may have one or more series.
            if (result.getSeries() != null) {
              for (Series series : result.getSeries()) {
                if (series != null) {
                  List<String> columns = series.getColumns();
                  List<List<Object>> valuesList = series.getValues();
                  logger.debug(
                      "Processing series with " + valuesList.size()
                          + " points.");

                  // Loop over all the data points in the series.
                  for (List valList : valuesList) {
                    points.add(new WaveformPoint(
                        (double) (valList.get(columns.indexOf(InfluxDbUtility.VALUE_TAG))),
                        Instant.parse(
                            (String) (valList.get(columns.indexOf(InfluxDbUtility.TIME_TAG)))),
                        UUID.fromString(
                            (String) valList.get(columns.indexOf(InfluxDbUtility.ID_TAG))),
                        (double) (valList
                            .get(columns.indexOf(InfluxDbUtility.SAMPLE_RATE_TAG)))));
                  } // end for each point
                } // end if series == null
              } // end for each series
            } else {
              logger.warn("No series found!");
            }
          } else {
            logger.warn("No results found.");
          }

        } // end for each result
      } else {
        logger.warn("No query results found.");
      }
    } // end while loop
  }
}

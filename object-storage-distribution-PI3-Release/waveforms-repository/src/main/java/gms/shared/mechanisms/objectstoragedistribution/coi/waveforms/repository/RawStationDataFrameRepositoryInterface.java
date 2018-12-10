package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import java.time.Instant;
import java.util.List;
import javax.persistence.EntityExistsException;
import javax.persistence.QueryTimeoutException;

/**
 * A repository interface for storing and retrieving raw station data frames.
 */
public interface RawStationDataFrameRepositoryInterface {

  /**
   * Stores a frame.
   * @param frame the frame to store
   */
  void storeRawStationDataFrame(RawStationDataFrame frame) throws Exception;

  /**
   * Retrieves all frames that have any data in the specified time range.
   * @param start the start of the time range
   * @param end the end of the time range
   * @return list of frames that start in that time range; may be empty
   */
  List<RawStationDataFrame> retrieveAll(Instant start, Instant end) throws Exception;

  /**
   * Retrieves all frames that have any data in the specified time range
   * and which have the given stationName.
   * @param stationName the name of the station to query by
   * @param start the start of the time range
   * @param end the end of the time range
   * @return list of frames that start in that time range with the given station name;
   * may be empty
   */
  List<RawStationDataFrame> retrieveByStationName(String stationName, Instant start, Instant end) throws Exception;
}

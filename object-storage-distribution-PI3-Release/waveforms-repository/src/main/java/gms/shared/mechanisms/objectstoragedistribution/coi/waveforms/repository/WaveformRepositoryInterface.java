package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface for storing and retrieving waveform COI objects.
 */
public interface WaveformRepositoryInterface {

  /**
   * Get a collection from Waveform objects from the database.
   *
   * @param processingChannelId the id of the processing channel the waveform is for.
   * @param startTime Starting time for the time-series.
   * @param endTime Ending time for the time-series.
   * @return A list of Waveform objects.  The list may be empty.
   */
  List<Waveform> retrieveWaveformsByTime(UUID processingChannelId, Instant startTime,
      Instant endTime, boolean includeWaveformValues) throws Exception;

  /**
   * Store a waveform associated with a ChannelSegment.
   *
   * @param waveform The Waveform object.
   * @param processingChannelId id of the processing channel the data is for
   */
  void storeWaveform(Waveform waveform, UUID processingChannelId) throws Exception;

  /**
   * Store a ChannelSegment to the relational and timeseries database.
   *
   * @param segment The channelSegment object.
   */
  void storeChannelSegment(ChannelSegment segment) throws Exception;

  /**
   * Retrieve a collection of ChannelSegment id of the ProcessingChannel and a time range.
   *
   * @param processingChannelId id of the processing channel to retrieve segments for
   * @param rangeStart - the start of the range to query for - inclusive
   * @param rangeEnd - the end of the range to query for - inclusive
   * @return A List of ChannelSegment's, ordered by start time.  The list may be empty.  The query
   * will return all ChannelSegment's that contain data within the time range specified, which means
   * there may be segment's returned with data outside of [rangeStart, rangeEnd].
   */
  List<ChannelSegment> segmentsForProcessingChannel(UUID processingChannelId, Instant rangeStart,
      Instant rangeEnd, boolean includeWaveformValues) throws Exception;

  /**
   * Retrieve a ChannelSegment id of the ProcessingChannel and a time range.  Returns an empty
   * {@link Optional} if no data matches the query parameters.
   *
   * @param processingChannelId id of the processing channel to retrieve segments for
   * @param rangeStart - the start of the range to query for - inclusive
   * @param rangeEnd - the end of the range to query for - inclusive
   * @param includeWaveformValues - return waveform sample or just metadata.
   * @return An {@link Optional} ChannelSegment.
   */
  Optional<ChannelSegment> retrieveChannelSegment(UUID processingChannelId, Instant rangeStart,
      Instant rangeEnd, boolean includeWaveformValues) throws Exception;
}

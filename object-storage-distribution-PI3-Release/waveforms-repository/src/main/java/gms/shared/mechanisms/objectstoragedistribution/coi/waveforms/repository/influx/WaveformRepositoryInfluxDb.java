package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.influx;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.DataExistsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.StorageUnavailableException;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RepositoryExceptionUtils;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects.ChannelSegmentDao;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import org.apache.commons.lang3.Validate;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBIOException;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.slf4j.LoggerFactory;

/**
 * Store Waveform COI objects in the appropriate database.
 */
public class WaveformRepositoryInfluxDb implements WaveformRepositoryInterface {

  private static final org.slf4j.Logger logger = LoggerFactory
      .getLogger(WaveformRepositoryInfluxDb.class);

  private static final String UNIT_NAME = "waveforms";

  private final EntityManagerFactory entityManagerFactory;

  /**
   * Default constructor.
   */
  public WaveformRepositoryInfluxDb() {
    this(Persistence.createEntityManagerFactory(UNIT_NAME));
  }

  public WaveformRepositoryInfluxDb(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  @Override
  public List<Waveform> retrieveWaveformsByTime(UUID processingChannelId, Instant startTime,
      Instant endTime, boolean includeWaveformValues) throws Exception {
    Validate.notNull(processingChannelId);
    Validate.notNull(startTime);
    Validate.notNull(endTime);

    final InfluxDB influxDb;
    try {
      // Get a connection to InfluxDB.
      influxDb = TimeseriesDependencies.getConnection();
    } catch (InfluxDBIOException e) {
      throw new StorageUnavailableException(e);
    }

    return includeWaveformValues ? getWaveformsWithValues(influxDb, processingChannelId,
        startTime,
        endTime)
        : getWaveformsWithoutValues(influxDb, processingChannelId, startTime, endTime);

  }

  private static List<Waveform> getWaveformsWithValues(InfluxDB influxDB, UUID processingChannelId,
      Instant startTime, Instant endTime) {

    String queryString = InfluxDbUtility
        .byChannelIdAndTimeQuery(processingChannelId, startTime, endTime);

    WaveformResults results = new WaveformResults();

    Query query = new Query(queryString, TimeseriesDependencies.getDatabaseName());

    logger.info("Querying for waveforms using query string:{}", queryString);

    try {
      final BlockingQueue<QueryResult> queue = new LinkedBlockingQueue<>();

      influxDB.query(query, 2, queue::add);

      QueryResult result;
      while ((result = queue.poll(1, TimeUnit.SECONDS)) != null) {

        // Process the results.
        results.addResult(result);
      }
    } catch (InterruptedException e) {
      logger.error("Error reading from influx", e);
      return new ArrayList<>();
    }
    results.saveWaveformResults();
    // If an error was generated, throw an exception.
    if (results.isError()) {
      throw new RuntimeException(results.getError());
    }
    return results.getWaveforms();
  }

  /**
   * @inheritDoc
   */
  @Override
  public void storeWaveform(Waveform waveform, UUID processingChannelId) throws Exception {
    Validate.notNull(waveform);
    Validate.notNull(processingChannelId);

    // Get connection.  If that throws an exception, influx can't be reached.
    final InfluxDB influxDB;
    try {
      influxDB = TimeseriesDependencies.getConnection();
    } catch (Exception ex) {
      throw new StorageUnavailableException(ex);
    }

    // Write the data points.
    InfluxDbUtility.writeWaveformPoints(influxDB, waveform.asTimedPairs(),
        waveform.getSampleRate(), processingChannelId);
  }

  /**
   * Stores a ChannelSegment into the OSD.  Note that this involves both the relational and
   * time-series store (they are linked with an id reference).
   *
   * @param segment The channelSegment object.
   */
  @Override
  public void storeChannelSegment(ChannelSegment segment) throws Exception {
    Validate.notNull(segment);
    // store the channel segment
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    try {
      if (segmentExists(entityManager, segment)) {
        throw new DataExistsException(
            "Attempt to store this segment, already persisted: " + segment);
      }
      ChannelSegmentDao segmentDao = new ChannelSegmentDao(segment);
      entityManager.getTransaction().begin();
      entityManager.persist(segmentDao);
      entityManager.getTransaction().commit();
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      entityManager.close();
    }
    // Get the UUID assigned to this entry.
    UUID processingChannelId = segment.getProcessingChannelId();
    // Store the waveforms associated with the segment in the timeseries database.
    for (Waveform wf : segment.getWaveforms()) {
      this.storeWaveform(wf, processingChannelId);
    }
  }

  /**
   * @inheritDoc
   */
  @Override
  public List<ChannelSegment> segmentsForProcessingChannel(UUID processingChannelId,
      Instant rangeStart,
      Instant rangeEnd, boolean includeWaveformValues) throws Exception {
    Validate.notNull(processingChannelId);
    Validate.notNull(rangeStart);
    Validate.notNull(rangeEnd);

    EntityManager entityManager = this.entityManagerFactory.createEntityManager();

    try {

      TypedQuery<ChannelSegmentDao> query = entityManager
          .createQuery("select cs from ChannelSegmentDao cs where cs.processingChannelId = ?1 "
                  + "and cs.endTime >= ?2 and cs.startTime <= ?3",
              ChannelSegmentDao.class);

      List<ChannelSegmentDao> segmentDaos = query.setParameter(1, processingChannelId)
          .setParameter(2, rangeStart)
          .setParameter(3, rangeEnd)
          .getResultList();

      return createChannelSegments(segmentDaos, rangeStart,
          rangeEnd, includeWaveformValues);
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      entityManager.close();
    }
  }

  /**
   * Retrieves a {@link ChannelSegment}. If Parameters provided results in many Channel Segments,
   * All the waveforms will be placed into a single {@link SortedSet} and a single Channel Segment
   * Will be created, using the passed in parameters for starttime, endtime, processingChannelId
   * ChannelSegmentType, and channelName.
   *
   * @param processingChannelId - UUID processing channel id
   * @param rangeStart - Instant
   * @param rangeEnd - Instant
   * @param includeWaveformValues - if true will include waveform samples, if false just meta data.
   * @return ChannelSegment
   */
  @Override
  public Optional<ChannelSegment> retrieveChannelSegment(UUID processingChannelId,
      Instant rangeStart,
      Instant rangeEnd, boolean includeWaveformValues) throws Exception {

    //TODO: query based on channel segment type
    //TODO: output type is only raw when the type being quiered on is ACQUIRED
    List<ChannelSegment> channelSegments = segmentsForProcessingChannel(processingChannelId,
        rangeStart, rangeEnd,
        includeWaveformValues);

    ChannelSegment channelSegment = null;

    if (!channelSegments.isEmpty()) {
      SortedSet<Waveform> waveforms = channelSegments.stream()
          .map(ChannelSegment::getWaveforms)
          .flatMap(Set::stream)
          .map(w -> w.window(rangeStart, rangeEnd))
          .collect(Collectors.toCollection(TreeSet::new));

      channelSegment = ChannelSegment.create(processingChannelId,
          channelSegments.get(0).getName(), channelSegments.get(0).getSegmentType(),
          rangeStart, rangeEnd, waveforms,
          new CreationInfo("waveforms-repository", Instant.now(),
              new SoftwareComponentInfo("waveforms-repository", "0.0.1")));
    }

    return Optional.ofNullable(channelSegment);
  }

  /**
   * Creates ChannelSegments with their associated waveforms retrieved from the OSD.
   *
   * @param segmentDaos The collection of ChannelSegmentDao objects to retrieve waveform data.
   */
  private List<ChannelSegment> createChannelSegments(List<ChannelSegmentDao> segmentDaos,
      Instant rangeStart, Instant rangeEnd,
      boolean includeWaveformValues) throws Exception {

    List<ChannelSegment> segments = new ArrayList<>();

    for (ChannelSegmentDao segmentDao : segmentDaos) {
      if (segmentDao != null) {
        UUID processingChannelId = segmentDao.getProcessingChannelId();
        Instant start = segmentDao.getStartTime().isBefore(rangeStart) ?
            rangeStart : segmentDao.getStartTime();
        Instant end = segmentDao.getEndTime().isAfter(rangeEnd) ?
            rangeEnd : segmentDao.getEndTime();
        SortedSet<Waveform> waveforms = new TreeSet<>(
            this.retrieveWaveformsByTime(processingChannelId, start, end, includeWaveformValues));

        // Build the ChannelSegment.
        ChannelSegment segment = ChannelSegment.from(
            segmentDao.getId(), segmentDao.getProcessingChannelId(), segmentDao.getName(),
            segmentDao.getSegmentType(), segmentDao.getStartTime(),
            segmentDao.getEndTime(),
            waveforms, CreationInfo.DEFAULT);
        segments.add(segment);
      }
    }
    Collections.sort(segments);  // sorts by Comparator in ChannelSegment (by start time)
    return segments;
  }

  private static List<Waveform> getWaveformsWithoutValues(InfluxDB influx, UUID processingChannelId,
      Instant startTime, Instant endTime) throws Exception {

    String queryString = InfluxDbUtility
        .querySampleRateById(processingChannelId, startTime, endTime);
    Query query = new Query(queryString, TimeseriesDependencies.getDatabaseName());
    QueryResult queryResult = influx.query(query);
    if (queryResult != null) {
      List<Result> results = queryResult.getResults();
      if (results != null) {
        List<Waveform> waveforms = results.stream()
            .flatMap(r -> r.getSeries().stream())
            .map((s -> InfluxDbUtility.parseAsWaveform(s, endTime)))
            .collect(Collectors.toList());
        if (waveforms != null) {
          return waveforms;
        }
      }
    }
    logger.debug("getWaveformsWithoutValues: no results, returning empty list");
    return new ArrayList<>();
  }

  private static boolean segmentExists(EntityManager em, ChannelSegment cs) {
    return !em
        .createQuery("SELECT cs.id FROM " + ChannelSegmentDao.class.getSimpleName()
            + " cs where cs.processingChannelId = :chan_id"
            + " and cs.segmentType = :segType and cs.startTime = :start")
        .setParameter("chan_id", cs.getProcessingChannelId())
        .setParameter("segType", cs.getSegmentType())
        .setParameter("start", cs.getStartTime())
        .setMaxResults(1)
        .getResultList()
        .isEmpty();
  }

}

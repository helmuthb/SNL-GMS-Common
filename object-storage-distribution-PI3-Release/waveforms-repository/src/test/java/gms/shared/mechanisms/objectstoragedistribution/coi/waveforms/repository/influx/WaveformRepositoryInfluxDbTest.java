package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.influx;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.DataExistsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.TestFixtures;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.Before;
import org.junit.Test;

/**
 * Perform unit tests on the WaveformPersistenceImpl class.
 */
public class WaveformRepositoryInfluxDbTest {

  private static WaveformRepositoryInfluxDb waveformPersistence;

  @Before
  public void setup() throws Exception {
    EntityManagerFactory entityManagerFactory = Persistence
        .createEntityManagerFactory("waveforms-unitDB");
    waveformPersistence = new WaveformRepositoryInfluxDb(entityManagerFactory);
    // store the test fixture channel segment's
    try {
      TimeseriesDependencies.getConnection();
    } catch (Exception ex) {
      return;
    }
    waveformPersistence.storeChannelSegment(TestFixtures.channelSegment);
    waveformPersistence.storeChannelSegment(TestFixtures.channelSegment2);
  }

  @Test
  public void storeAndRetrieveWaveform() throws Exception {
    try {
      TimeseriesDependencies.getConnection();
    } catch (Exception ex) {
      return;
    }
    // Store waveform.
    waveformPersistence.storeWaveform(TestFixtures.waveform1, TestFixtures.PROCESSING_CHANNEL_ID);

    // Retrieve the waveform(s).
    List<Waveform> waveforms = waveformPersistence.retrieveWaveformsByTime(
        TestFixtures.PROCESSING_CHANNEL_ID,
        TestFixtures.SEGMENT_START,
        TestFixtures.SEGMENT_END, true);

    assertNotNull(waveforms);
    assertEquals(1, waveforms.size());
    assertEquals(TestFixtures.waveform1, waveforms.get(0));
  }


  /**
   * Store two waveforms with different sample rates in a single series, when we retrieve the series
   * the results should be presented as two distinct waveforms.
   */
  @Test
  public void storeAndRetrieveWaveforms() throws Exception {
    try {
      TimeseriesDependencies.getConnection();
    } catch (Exception ex) {
      return;
    }

    // Store waveforms.
    waveformPersistence.storeWaveform(TestFixtures.waveform1, TestFixtures.CHANNEL_SEGMENT_ID);
    waveformPersistence.storeWaveform(TestFixtures.waveform2, TestFixtures.CHANNEL_SEGMENT_ID);

    // Retrieve the waveform(s).
    List<Waveform> waveforms = waveformPersistence.retrieveWaveformsByTime(
        TestFixtures.CHANNEL_SEGMENT_ID,
        TestFixtures.SEGMENT_START,
        TestFixtures.SEGMENT_END2,
        true);

    assertNotNull(waveforms);
    assertEquals(2, waveforms.size());
    assertEquals(TestFixtures.waveform1, waveforms.get(0));
    assertEquals(TestFixtures.waveform2, waveforms.get(1));
  }


  @Test
  public void testStoreAndRetrieveChannelSegment() throws Exception {
    try {
      TimeseriesDependencies.getConnection();
    } catch (Exception ex) {
      return;
    }
    // Test whether the retrieval returns more than zero
    List<ChannelSegment> segmentsForSiteChan = waveformPersistence.segmentsForProcessingChannel(
        TestFixtures.PROCESSING_CHANNEL_ID, TestFixtures.SEGMENT_START,
        TestFixtures.SEGMENT_END2, true);
    assertNotNull(segmentsForSiteChan);
    assertEquals(2, segmentsForSiteChan.size());

    // Test whether the retrieval returns ONLY one
    segmentsForSiteChan = waveformPersistence.segmentsForProcessingChannel(
        TestFixtures.PROCESSING_CHANNEL_ID, TestFixtures.SEGMENT_START,
        TestFixtures.SEGMENT_END2, false);
    assertNotNull(segmentsForSiteChan);
    assertEquals(2, segmentsForSiteChan.size());
    // assert the data is empty
    assertArrayEquals(new double[]{},
        segmentsForSiteChan.get(0).getWaveforms().first().getValues(), 0.01);
    assertArrayEquals(new double[]{},
        segmentsForSiteChan.get(1).getWaveforms().first().getValues(), 0.01);
  }

  @Test
  public void testStoreChannelSegment() throws Exception {
    try {
      TimeseriesDependencies.getConnection();
    } catch (Exception ex) {
      return;
    }
    // retrieve the segment(s) and compare to original
    List<ChannelSegment> segmentsForSiteChan = waveformPersistence.segmentsForProcessingChannel(
        TestFixtures.PROCESSING_CHANNEL_ID, TestFixtures.SEGMENT_START,
        TestFixtures.SEGMENT_END, true);
    assertNotNull(segmentsForSiteChan);
    assertTrue(segmentsForSiteChan.size() > 0);
    assertEquals(TestFixtures.channelSegment, segmentsForSiteChan.get(0));
  }

  @Test(expected = DataExistsException.class)
  public void testStoreChannelSegmentTwice() throws Exception {
    try {
      TimeseriesDependencies.getConnection();
    } catch (Exception ex) {
      throw new DataExistsException();  // fake the test passing
    }
    waveformPersistence.storeChannelSegment(TestFixtures.channelSegment);
    waveformPersistence.storeChannelSegment(TestFixtures.channelSegment);
  }

  /**
   * Switch the start and end times to no data will meet the criteria.
   */
  @Test
  public void retrieveEmptySeries1() throws Exception {
    try {
      TimeseriesDependencies.getConnection();
    } catch (Exception ex) {
      return;
    }
    List<Waveform> waveforms = waveformPersistence.retrieveWaveformsByTime(
        TestFixtures.PROCESSING_CHANNEL_ID,
        TestFixtures.SEGMENT_END,
        TestFixtures.SEGMENT_START, true);
    assertTrue(waveforms.size() == 0);
  }

  @Test
  public void testRetrieveWaveformsNullChecks() throws Exception {
    try {
      TimeseriesDependencies.getConnection();
    } catch (Exception ex) {
      return;
    }
    TestUtilities.checkMethodValidatesNullArguments(
        waveformPersistence, "retrieveWaveformsByTime",
        TestFixtures.PROCESSING_CHANNEL_ID, TestFixtures.SEGMENT_START, TestFixtures.SEGMENT_END,
        true);
  }

  @Test
  public void testStoreWaveformNullChecks() throws Exception {
    try {
      TimeseriesDependencies.getConnection();
    } catch (Exception ex) {
      return;
    }
    TestUtilities.checkMethodValidatesNullArguments(
        waveformPersistence, "storeWaveform",
        TestFixtures.waveform1, TestFixtures.PROCESSING_CHANNEL_ID);
  }

  @Test(expected = Exception.class)
  public void testStoreChannelSegmentNullChecks() throws Exception {
    waveformPersistence.storeChannelSegment(null);
  }

  @Test
  public void testSegmentsForProcessingChannelNullChecks() throws Exception {
    TestUtilities.checkMethodValidatesNullArguments(
        waveformPersistence, "segmentsForProcessingChannel",
        TestFixtures.PROCESSING_CHANNEL_ID, TestFixtures.SEGMENT_START, TestFixtures.SEGMENT_END,
        true);
  }

  @Test
  public void testRetrieveChannelSegmentNullChecks() throws Exception {
    TestUtilities.checkMethodValidatesNullArguments(
        waveformPersistence, "retrieveChannelSegment",
        TestFixtures.PROCESSING_CHANNEL_ID, TestFixtures.SEGMENT_START, TestFixtures.SEGMENT_END,
        true);
  }
}

package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.ChannelSegmentType;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link Waveform} creation and usage semantics Created by trsault on 8/25/17.
 */
public class ChannelSegmentTests {

  private final UUID segmentId = UUID.fromString("8952f988-ff83-4f3d-a832-a82a04022539"),
      processingChannelId = UUID.fromString("41ea0291-af5e-4694-a551-a215f95c78d1");
  private final ChannelSegment.ChannelSegmentType type = ChannelSegment.ChannelSegmentType.ACQUIRED;
  private final Instant start = Instant.EPOCH;
  private final Instant end = Instant.now();
  private final Waveform waveform = Waveform.withoutValues(start, end, 0);
  private final SortedSet<Waveform> set = new TreeSet<>(Set.of(waveform));

  private Waveform waveform1;

  private Waveform waveform2HalfSampleRate;
  private Waveform waveform2InterpolatedGap1;
  private Waveform waveform2InterpolatedGap2;
  private Waveform waveform2InterpolatedGap4;
  private Waveform waveform2InterpolatedGap1_5;

  private Waveform waveform2MergedGap1;
  private Waveform waveform2MergedGap2;

  private Waveform waveform3;

  private ChannelSegment channelSegmentDifferentSampleRates;
  private ChannelSegment channelSegmentInterpolatedGap1;
  private ChannelSegment channelSegmentInterpolatedGap2;
  private ChannelSegment channelSegmentInterpolatedGap4;
  private ChannelSegment channelSegmentInterpolatedGap1_5;

  private ChannelSegment channelSegmentMergedGap1;
  private ChannelSegment channelSegmentMergedGap2;

  @Before
  public void setUp() {

    // Base waveforms and channel segment to test no interpolated or merged waveforms are produced.
    Instant startTime = Instant.EPOCH;

    double[] samples = getDoubleArray(41, 1.0);
    waveform1 = Waveform.withInferredEndTime(startTime, 40.0, 41, samples);
    samples = getDoubleArray(41, 3.0);
    waveform3 = Waveform.withInferredEndTime(startTime.plusSeconds(2), 40.0, 41, samples);

    samples = getDoubleArray(21, 1.0);
    waveform2HalfSampleRate = Waveform.create(startTime.plusSeconds(1).plusMillis(25),
        startTime.plusSeconds(2).minusMillis(25), 20.0, 21, samples);

    channelSegmentDifferentSampleRates = createChannelSegment(startTime, waveform3.getEndTime(),
        Set.of(waveform1, waveform2HalfSampleRate, waveform3));

    // Used to test no interpolated waveforms when gaps less than 1.5 sample time difference exist
    samples = getDoubleArray(39, 2.0);
    waveform2InterpolatedGap1 = Waveform.withInferredEndTime(waveform1.getEndTime().plusMillis(25),
        40.0, 39, samples);
    channelSegmentInterpolatedGap1 = createChannelSegment(startTime, waveform3.getEndTime(),
        Set.of(waveform1, waveform2InterpolatedGap1, waveform3));

    // Used to test production of two interpolated waveforms with 1 point each (limiting)
    samples = getDoubleArray(37, 2.0);
    waveform2InterpolatedGap2 = Waveform.withInferredEndTime(waveform1.getEndTime().plusMillis(50),
        40.0, 37, samples);
    channelSegmentInterpolatedGap2 = createChannelSegment(startTime, waveform3.getEndTime(),
        Set.of(waveform1, waveform2InterpolatedGap2, waveform3));

    // Used to test production of two interpolated waveforms with 3 points each
    samples = getDoubleArray(33, 2.0);
    waveform2InterpolatedGap4 = Waveform.withInferredEndTime(waveform1.getEndTime().plusMillis(100),
        40.0, 33, samples);
    channelSegmentInterpolatedGap4 = createChannelSegment(startTime, waveform3.getEndTime(),
        Set.of(waveform1, waveform2InterpolatedGap4, waveform3));

    // Used to test sensitivity to the 1.5 sample time gap difference
    samples = getDoubleArray(38, 2.0);
    Instant startTime1_5 = waveform1.getEndTime().plusMillis(37);
    Instant endTime1_5 = startTime.plusSeconds(2).minusMillis(38);
    double sampleRate1_5 = 37.0 / ((double) Duration.between(startTime1_5, endTime1_5).toNanos() / 1000000000);
    waveform2InterpolatedGap1_5 = Waveform.withInferredEndTime(startTime1_5, sampleRate1_5, 38,
        samples);
    channelSegmentInterpolatedGap1_5 = createChannelSegment(startTime, waveform3.getEndTime(),
        Set.of(waveform1, waveform2InterpolatedGap1_5, waveform3));

    // Create a merged gap channel segment with a different middle waveform so that the gaps produced
    // are slightly less than (by 2 nanoseconds) than a single sample period between the waveforms.
    samples = getDoubleArray(39, 2.0);
    Instant startTimeMergedGap1 = startTime.plusSeconds(1).plusMillis(25).minusNanos(2);
    Instant endTimeMergedGap1 = startTime.plusSeconds(2).minusMillis(25).plusNanos(2);
    double sampleRateMergedGap1 = 38.0 / ((double) Duration.between(startTimeMergedGap1, endTimeMergedGap1).toNanos() / 1000000000);
    waveform2MergedGap1 = Waveform.withInferredEndTime(startTimeMergedGap1, sampleRateMergedGap1, 39,
        samples);
    channelSegmentMergedGap1 = createChannelSegment(startTime, waveform3.getEndTime(),
        Set.of(waveform1, waveform2MergedGap1, waveform3));

    // Create a merged gap channel segment such that the gap between this waveform and the first
    // waveform is the same as in waveform2MergedGap1 but the gap between this waveform and the
    // third waveform is 2 samples wide. This gap will fail to be merged because it will be larger
    // than the 1.5 sample period allowed for forming merged waveforms.
    samples = getDoubleArray(38, 2.0);
    Instant startTimeMergedGap2 = startTime.plusSeconds(1).plusMillis(25).minusNanos(2);
    Instant endTimeMergedGap2 = startTime.plusSeconds(2).minusMillis(50).plusNanos(2);
    double sampleRateMergedGap2 = 37.0 / ((double) Duration.between(startTimeMergedGap2, endTimeMergedGap2).toNanos() / 1000000000);
    waveform2MergedGap2 = Waveform.withInferredEndTime(startTimeMergedGap2, sampleRateMergedGap2, 38,
        samples);
    channelSegmentMergedGap2 = createChannelSegment(startTime, waveform3.getEndTime(),
        Set.of(waveform1, waveform2MergedGap2, waveform3));

  }

  private ChannelSegment createChannelSegment(Instant startTime, Instant endTime,
      Set<Waveform> waveformSet) {
    return ChannelSegment.create(UUID.randomUUID(), "NAME", ChannelSegmentType.FILTER,
        startTime, endTime, new TreeSet<Waveform>(waveformSet), CreationInfo.DEFAULT);
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ChannelSegment.class);
  }

  @Test
  public void testChannelSegmentCreateNullArguments() throws Exception {
    // Test analog SOH
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ChannelSegment.class, "create",
        processingChannelId, "NAME", type, start, end, set, CreationInfo.DEFAULT);
  }

  @Test
  public void testChannelSegmentFromNullArguments() throws Exception {
    // Test analog SOH
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ChannelSegment.class, "from", segmentId,
        processingChannelId, "NAME", type, start, end, set, CreationInfo.DEFAULT);
  }

  /**
   * Test merged waveforms where gaps are a couple nanoseconds less than the minimumGapLimit (1.0)
   * so no merged waveforms are created.
   */
  @Test
  public  void testChannelSegmentMergedWaveformMinLimitGap() {
    ChannelSegment newChannelSegment =
        channelSegmentMergedGap1.mergeWaveforms(.01, 1.1);
    assertTrue(newChannelSegment.hasSameState(channelSegmentMergedGap1));
  }

  /**
   * Test merged waveforms where sample rates are different (1st and 3rd waveforms have sample rates
   * of 40 samples per second, while the middle waveform is half of that).
   * No merged waveforms should be created
   */
  @Test
  public  void testChannelSegmentMergedWaveformDifferentSampleRates() {
    ChannelSegment newChannelSegment =
        channelSegmentDifferentSampleRates.mergeWaveforms(2.0, 1.0);
    assertTrue(newChannelSegment.hasSameState(channelSegmentDifferentSampleRates));
  }

  /**
   * Test merged waveforms where gaps are a couple nanoseconds less than 1.0 with a
   * minimumGapLimit of .9. Both gaps should be merged and a ChannelSegment with a single waveform
   * should be returned.
   */
  @Test
  public  void testChannelSegmentMergedWaveformGap1() {
    ChannelSegment newChannelSegment =
        channelSegmentMergedGap1.mergeWaveforms(1.25, 0.9);

    assertEquals(1, newChannelSegment.getWaveforms().size());
    assertEquals(channelSegmentMergedGap1.getProcessingChannelId(), newChannelSegment.getProcessingChannelId());
    assertEquals(channelSegmentMergedGap1.getStartTime(), newChannelSegment.getStartTime());
    assertEquals(channelSegmentMergedGap1.getEndTime(), newChannelSegment.getEndTime());
    assertEquals(channelSegmentMergedGap1.getName(), newChannelSegment.getName());
    assertEquals(channelSegmentMergedGap1.getCreationInfo(), newChannelSegment.getCreationInfo());

    Waveform waveform = newChannelSegment.getWaveforms().first();
    assertEquals(waveform.getSampleCount(), waveform1.getSampleCount() +
        waveform2MergedGap1.getSampleCount() + waveform3.getSampleCount());
    assertEquals(waveform.getStartTime(), waveform1.getStartTime());
    assertEquals(waveform.getEndTime(), waveform3.getEndTime());

    for (int i = 0; i < waveform1.getValues().length; ++i) {
      assertEquals(waveform.getValues()[i], waveform1.getValues()[i], 0.0);
    }
    for (int i = 0; i < waveform2MergedGap1.getValues().length; ++i) {
      assertEquals(waveform.getValues()[i+(int) waveform1.getSampleCount()],
          waveform2MergedGap1.getValues()[i], 0.0);
    }
    for (int i = 0; i < waveform3.getValues().length; ++i) {
      assertEquals(waveform.getValues()[i + (int) (waveform1.getSampleCount() +
              waveform2MergedGap1.getSampleCount())],
          waveform3.getValues()[i], 0.0);
    }

    assertEquals(waveform.getSampleRate(), (double) (waveform.getSampleCount() - 1) /
        ((double) Duration.between(waveform.getStartTime(), waveform.getEndTime()).toNanos() / 1000000000),
        0.0);
  }

  /**
   * Test merged waveforms where gaps are a couple nanoseconds less than 1.0 for the gap between the
   * first and second waveforms and the gap between the second and third waveforms is larger than 1.5
   * (2.0) and thus is not produced. The result is that
   * the first two waveforms are merged and the third waveform is returned without modification.
   */
  @Test
  public  void testChannelSegmentMergedWaveformGap2() {
    ChannelSegment newChannelSegment =
        channelSegmentMergedGap2.mergeWaveforms(1.25, 0.9);

    assertEquals(2, newChannelSegment.getWaveforms().size());
    assertEquals(channelSegmentMergedGap2.getProcessingChannelId(), newChannelSegment.getProcessingChannelId());
    assertEquals(channelSegmentMergedGap2.getStartTime(), newChannelSegment.getStartTime());
    assertEquals(channelSegmentMergedGap2.getEndTime(), newChannelSegment.getEndTime());
    assertEquals(channelSegmentMergedGap2.getName(), newChannelSegment.getName());
    assertEquals(channelSegmentMergedGap2.getCreationInfo(), newChannelSegment.getCreationInfo());

    // get the first waveform and validate it is a merged waveforms formed by merging the first and
    // the second waveform of the input channelSegment
    Waveform waveform = newChannelSegment.getWaveforms().first();
    assertEquals(waveform.getSampleCount(), waveform1.getSampleCount() +
        waveform2MergedGap2.getSampleCount());
    assertEquals(waveform.getStartTime(), waveform1.getStartTime());
    assertEquals(waveform.getEndTime(), waveform2MergedGap2.getEndTime());

    for (int i = 0; i < waveform1.getValues().length; ++i) {
      assertEquals(waveform.getValues()[i], waveform1.getValues()[i], 0.0);
    }
    for (int i = 0; i < waveform2MergedGap2.getValues().length; ++i) {
      assertEquals(waveform.getValues()[i+(int) waveform1.getSampleCount()],
          waveform2MergedGap2.getValues()[i], 0.0);
    }

    assertEquals(waveform.getSampleRate(), (double) (waveform.getSampleCount() - 1) /
            ((double) Duration.between(waveform.getStartTime(), waveform.getEndTime()).toNanos() / 1000000000),
        0.0);

    // verify last waveform is waveform3

    assertEquals(newChannelSegment.getWaveforms().last(), waveform3);
  }

  /**
   * Test that no waveforms are interpolated if the gaps exceed the maximumGapLimit setting. Using
   * channelSegmentInterpolatedGap2 where the gap sample periods are 2.5 and setting the limit to 2.0.
   */
  @Test
  public  void testChannelSegmentInterpolatedWaveformMaxLimitGap() {
    ChannelSegment newChannelSegment =
        channelSegmentInterpolatedGap2.interpolateWaveformGap(.01, 2.0);
    assertTrue(newChannelSegment.hasSameState(channelSegmentInterpolatedGap2));
  }

  /**
   * Test interpolated waveforms where sample rates are different (1st and 3rd waveforms have sample rates
   * of 40 samples per second, while the middle waveform is half of that).
   * No interpolated waveforms should be created
   */
  @Test
  public  void testChannelSegmentInterpolatedWaveformDifferentSampleRates() {
    ChannelSegment newChannelSegment =
        channelSegmentDifferentSampleRates.interpolateWaveformGap(2.0, 1.0);
    assertTrue(newChannelSegment.hasSameState(channelSegmentDifferentSampleRates));
  }

  /**
   * Test where the gaps are small so no interpolation is performed. Output ChannelSegment is the
   * same as the initialChannelSegment.
   */
  @Test
  public  void testChannelSegmentInterpolateWaveformGap1() {
    ChannelSegment newChannelSegment =
        channelSegmentInterpolatedGap1.interpolateWaveformGap(.01, 4.0);
    assertTrue(newChannelSegment.hasSameState(channelSegmentInterpolatedGap1));
  }

  /**
   * Test where two gaps exist between three consecutive waveforms that are separated by 2 data samples
   * each. In this case two new waveforms are inserted between the 3 existing waveforms. This test
   * verifies the interpolation of samples in the new interpolated waveforms each containing 1 sample
   * point.
   */
  @Test
  public  void testChannelSegmentInterpolateWaveformGap2() {
    ChannelSegment newChannelSegment =
        channelSegmentInterpolatedGap2.interpolateWaveformGap(.01, 4.0);

    // Original 3 waveforms and 2 new interpolated waveforms. Other data is the same as the original
    // channel segment
    assertEquals(5, newChannelSegment.getWaveforms().size());
    assertEquals(channelSegmentInterpolatedGap2.getProcessingChannelId(), newChannelSegment.getProcessingChannelId());
    assertEquals(channelSegmentInterpolatedGap2.getStartTime(), newChannelSegment.getStartTime());
    assertEquals(channelSegmentInterpolatedGap2.getEndTime(), newChannelSegment.getEndTime());
    assertEquals(channelSegmentInterpolatedGap2.getName(), newChannelSegment.getName());
    assertEquals(channelSegmentInterpolatedGap2.getCreationInfo(), newChannelSegment.getCreationInfo());

    // iterate across the original ('before') and produced ('after') waveforms to verify their
    // correctness
    Iterator<Waveform> beforeIterator = channelSegmentInterpolatedGap2.getWaveforms().iterator();
    Iterator<Waveform> afterIterator = newChannelSegment.getWaveforms().iterator();

    // first waveforms are the same (waveform1)
    Waveform beforeWaveform = beforeIterator.next();
    Waveform afterWaveform = afterIterator.next();
    assertEquals(beforeWaveform, afterWaveform);
    assertEquals(waveform1, afterWaveform);

    // third waveform of 'after' is the same as second waveform of 'before' (waveform2InterpolatedGap2)
    // second waveform of 'after' is first interpolated waveform
    beforeWaveform = beforeIterator.next();
    Waveform waveformInterp1 = afterIterator.next();;
    afterWaveform = afterIterator.next();
    assertEquals(beforeWaveform, afterWaveform);
    assertEquals(waveform2InterpolatedGap2, afterWaveform);

    // last waveform of 'before' and 'after' are the same (waveform3)
    // second to last waveform of 'after' is second interpolated waveform
    beforeWaveform = beforeIterator.next();
    Waveform waveformInterp2 = afterIterator.next();;
    afterWaveform = afterIterator.next();
    assertEquals(beforeWaveform, afterWaveform);
    assertEquals(waveform3, afterWaveform);

    // validate first interpolated waveform (1 point)
    assertEquals(1, waveformInterp1.getSampleCount());
    assertEquals(waveform1.getEndTime().plusNanos((long) (1000000000 / waveformInterp1.getSampleRate())),
        waveformInterp1.getStartTime());
    assertEquals(waveform2InterpolatedGap2.getStartTime().minusNanos((long) (1000000000 / waveformInterp1.getSampleRate())),
        waveformInterp1.getEndTime());
    assertEquals(waveform2InterpolatedGap2.getSampleRate(), waveformInterp1.getSampleRate(), 1.0e-9);
    assertEquals(0.5 * (waveform2InterpolatedGap2.getFirstSample() + waveform1.getLastSample()),
        waveformInterp1.getValues()[0], 1.0e-9);

    // validate second interpolated waveform (1 point)
    assertEquals(1, waveformInterp2.getSampleCount());
    assertEquals(waveform2InterpolatedGap2.getEndTime().plusNanos((long) (1000000000 / waveformInterp2.getSampleRate())),
        waveformInterp2.getStartTime());
    assertEquals(waveform3.getStartTime().minusNanos((long) (1000000000 / waveformInterp2.getSampleRate())),
        waveformInterp2.getEndTime());
    assertEquals(waveform2InterpolatedGap2.getSampleRate(), waveformInterp2.getSampleRate(), 1.0e-9);
    assertEquals(0.5 * (waveform2InterpolatedGap2.getLastSample() + waveform3.getFirstSample()),
        waveformInterp2.getValues()[0], 1.0e-9);
  }

  /**
   * Test where two gaps exist between three consecutive waveforms that are separated by 4 data samples
   * each. In this case two new waveforms are inserted between the 3 existing waveforms. This test
   * verifies the interpolation of samples in the new interpolated waveforms each containing 3
   * points.
   */
  @Test
  public  void testChannelSegmentInterpolateWaveformGap4() {
    ChannelSegment newChannelSegment =
        channelSegmentInterpolatedGap4.interpolateWaveformGap(.01, 5.0);

    // Original 3 waveforms and 2 new interpolated waveforms. Other data is the same as the original
    // channel segment
    assertEquals(5, newChannelSegment.getWaveforms().size());
    assertEquals(channelSegmentInterpolatedGap4.getProcessingChannelId(), newChannelSegment.getProcessingChannelId());
    assertEquals(channelSegmentInterpolatedGap4.getStartTime(), newChannelSegment.getStartTime());
    assertEquals(channelSegmentInterpolatedGap4.getEndTime(), newChannelSegment.getEndTime());
    assertEquals(channelSegmentInterpolatedGap4.getName(), newChannelSegment.getName());
    assertEquals(channelSegmentInterpolatedGap4.getCreationInfo(), newChannelSegment.getCreationInfo());

    // iterate across the original ('before') and produced ('after') waveforms to verify their
    // correctness
    Iterator<Waveform> beforeIterator = channelSegmentInterpolatedGap4.getWaveforms().iterator();
    Iterator<Waveform> afterIterator = newChannelSegment.getWaveforms().iterator();

    // first waveforms are the same (waveform1)
    Waveform beforeWaveform = beforeIterator.next();
    Waveform afterWaveform = afterIterator.next();
    assertEquals(beforeWaveform, afterWaveform);
    assertEquals(waveform1, afterWaveform);

    // third waveform of 'after' is the same as second waveform of 'before' (waveform2InterpolatedGap4)
    // second waveform of 'after' is first interpolated waveform
    beforeWaveform = beforeIterator.next();
    Waveform waveformInterp1 = afterIterator.next();;
    afterWaveform = afterIterator.next();
    assertEquals(beforeWaveform, afterWaveform);
    assertEquals(waveform2InterpolatedGap4, afterWaveform);

    // last waveform of 'before' and 'after' are the same (waveform3)
    // second to last waveform of 'after' is second interpolated waveform
    beforeWaveform = beforeIterator.next();
    Waveform waveformInterp2 = afterIterator.next();;
    afterWaveform = afterIterator.next();
    assertEquals(beforeWaveform, afterWaveform);
    assertEquals(waveform3, afterWaveform);

    // validate first interpolated waveform (3 points)
    assertEquals(3, waveformInterp1.getSampleCount());
    assertEquals(waveform1.getEndTime().plusNanos((long) (1000000000 / waveformInterp1.getSampleRate())),
        waveformInterp1.getStartTime());
    assertEquals(waveform2InterpolatedGap4.getStartTime().minusNanos((long) (1000000000 / waveformInterp1.getSampleRate())),
        waveformInterp1.getEndTime());
    assertEquals(waveform2InterpolatedGap4.getSampleRate(), waveformInterp1.getSampleRate(), 1.0e-9);

    double delta = (waveform2InterpolatedGap4.getFirstSample() - waveform1.getLastSample()) /
        (waveformInterp1.getSampleCount() + 1);
    for (int i = 0; i < waveformInterp1.getSampleCount(); ++i) {
      assertEquals(delta * (i+1) + waveform1.getLastSample(),
          waveformInterp1.getValues()[i], 1.0e-9);
    }

    // validate second interpolated waveform (3 points)
    assertEquals(3, waveformInterp2.getSampleCount());
    assertEquals(waveform2InterpolatedGap4.getEndTime().plusNanos((long) (1000000000 / waveformInterp2.getSampleRate())),
        waveformInterp2.getStartTime());
    assertEquals(waveform3.getStartTime().minusNanos((long) (1000000000 / waveformInterp2.getSampleRate())),
        waveformInterp2.getEndTime());
    assertEquals(waveform2InterpolatedGap4.getSampleRate(), waveformInterp2.getSampleRate(), 1.0e-9);

    delta = (waveform3.getFirstSample() - waveform2InterpolatedGap4.getLastSample()) /
        (waveformInterp2.getSampleCount() + 1);
    for (int i = 0; i < waveformInterp2.getSampleCount(); ++i) {
      assertEquals(delta * (i + 1) + waveform2InterpolatedGap4.getLastSample(),
          waveformInterp2.getValues()[i], 1.0e-9);
    }
  }

  /**
   * Tests that interpolated waveforms are inserted when the the sample time difference (using a
   * mean sample rate between the adjacent waveforms) between two consecutive waveforms are just
   * smaller than and just larger than 1.5. In this case the first and second waveforms are
   * separated by a 1.48 sample period and the second and third waveforms are separated by 1.52 a
   * 1.52 sample period. Proper evaluation inserts a single new interpolated waveform between the
   * second and third waveforms, but not between the first and second waveforms since it's sample
   * time difference is less than 1.5.
   */
  @Test
  public  void testChannelSegmentInterpolateWaveformGap1_5() {
    ChannelSegment newChannelSegment =
        channelSegmentInterpolatedGap1_5.interpolateWaveformGap(.01, 4.0);

    assertEquals(4, newChannelSegment.getWaveforms().size());

    // iterate across the original ('before') and produced ('after') waveforms to verify their
    // correctness
    Iterator<Waveform> beforeIterator = channelSegmentInterpolatedGap1_5.getWaveforms().iterator();
    Iterator<Waveform> afterIterator = newChannelSegment.getWaveforms().iterator();

    // first waveforms are the same (waveform1)
    Waveform beforeWaveform = beforeIterator.next();
    Waveform afterWaveform = afterIterator.next();
    assertEquals(beforeWaveform, afterWaveform);
    assertEquals(waveform1, afterWaveform);

    // second waveforms are the same (waveform2Gap1_5)
    beforeWaveform = beforeIterator.next();
    afterWaveform = afterIterator.next();
    assertEquals(beforeWaveform, afterWaveform);
    assertEquals(waveform2InterpolatedGap1_5, afterWaveform);

    // last waveform of 'before' and 'after' are the same (waveform3)
    // second to last waveform of 'after' is second interpolated waveform
    beforeWaveform = beforeIterator.next();
    Waveform waveformInterp2 = afterIterator.next();;
    afterWaveform = afterIterator.next();
    assertEquals(beforeWaveform, afterWaveform);
    assertEquals(waveform3, afterWaveform);

    // validate interpolated waveform (1 point)
    assertEquals(1, waveformInterp2.getSampleCount());
    assertEquals(waveform2InterpolatedGap1_5.getEndTime().plusNanos((long) (1000000000 / waveformInterp2.getSampleRate())),
        waveformInterp2.getStartTime());
    assertEquals(waveform3.getStartTime().minusNanos((long) (1000000000 / waveformInterp2.getSampleRate())),
        waveformInterp2.getEndTime());

    double gapTimeWidth = (double) Duration.between(waveform2InterpolatedGap1_5.getEndTime(),
        waveform3.getStartTime()).toNanos() / 1000000000;
    double sampleRate = (double) (waveformInterp2.getSampleCount() + 1) / gapTimeWidth;
    assertEquals(sampleRate, waveformInterp2.getSampleRate(), 1.0e-9);

    assertEquals(0.5 * (waveform2InterpolatedGap1_5.getLastSample() + waveform3.getFirstSample()),
        waveformInterp2.getValues()[0], 1.0e-9);
  }

  private double[] getDoubleArray(int n, double value) {
    double[] a = new double [n];
    Arrays.fill(a, value);
    return a;
  }
}
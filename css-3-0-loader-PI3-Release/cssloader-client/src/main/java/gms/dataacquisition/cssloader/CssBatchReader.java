package gms.dataacquisition.cssloader;

import gms.dataacquisition.cssloader.data.SegmentAndSohBatch;
import gms.dataacquisition.cssloader.data.SegmentType;
import gms.dataacquisition.cssreader.data.WfdiscRecord;
import gms.dataacquisition.cssreader.waveformreaders.FlatFileWaveformReader;
import gms.dataacquisition.cssreader.flatfilereaders.FlatFileWfdiscReader;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import org.apache.commons.lang3.Validate;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

/**
 * Reads CSS wfdisc data in batches for efficiency (and not running out of memory).
 */
public class CssBatchReader {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CssBatchReader.class);
  private final int batchSize;
  private final FlatFileWaveformReader waveformReader = new FlatFileWaveformReader();
  private final CssLoaderOsdGatewayInterface osdGatewayInterface;
  private Collection<WfdiscRecord> wfdiscRecords;
  private final Map<String, UUID> staChanNameToChannelId = new HashMap<>();
  private Iterator<WfdiscRecord> wfdiscRecordsIterator;
  private static String creatorName = CssBatchReader.class
      .getName(); //TODO: get this information from somewhere else
  private static final String VERSION = "0.0.2";  //TODO: get this information from somewhere else
  private final String wfdiscFile;

  /**
   * Creates a CssBatchReader
   *
   * @param wfdiscFile the location of the wfdiscFile to read
   * @param batchSize the size of each batch to read at a time, in number of wfdisc rows
   * @param stationList the stations to include (null means include all)
   * @param channelList the channels to include (null means include all)
   * @param timeInstant the start time to include records for
   * @param endtimeInstant the end time to include records for
   * @throws Exception on various problems, such as reading from the specified wfdisc file, parsing
   * errors, etc.
   */
  public CssBatchReader(CssLoaderOsdGatewayInterface osdGatewayInterface,
      String wfdiscFile, int batchSize,
      List<String> stationList, List<String> channelList,
      Instant timeInstant, Instant endtimeInstant)
      throws Exception {

    // Validate.
    Validate.notEmpty(wfdiscFile);
    Validate.isTrue(batchSize > 0, "The batchSize value must be greater than zero.");

    // Set properties.
    this.wfdiscFile = wfdiscFile;
    this.batchSize = batchSize;
    this.osdGatewayInterface = Objects.requireNonNull(osdGatewayInterface);

    // Read the WF Disc file.
    FlatFileWfdiscReader wfdiscReader = new FlatFileWfdiscReader(
        stationList, channelList, timeInstant, endtimeInstant);
    this.wfdiscRecords = wfdiscReader.read(wfdiscFile);

    // Initialize the WF Disc Records iterator.
    this.wfdiscRecordsIterator = this.wfdiscRecords.iterator();
  }

  /**
   * Indicates whether there is more CSS data to retrieve from the WF Disc file.
   *
   * @return true if more data exists, otherwise false
   */
  public boolean nextBatchExists() {
    return wfdiscRecordsIterator.hasNext();
  }

  /**
   * Retrieves the next batch of CSS data.
   *
   * @return list of css data records.
   */
  public SegmentAndSohBatch readNextBatch() throws Exception {
    Set<ChannelSegment> channelSegmentBatch = new HashSet<>();
    Set<AcquiredChannelSohBoolean> sohBatch = new HashSet<>();

    int i = 0;
    while (i++ < this.batchSize && this.wfdiscRecordsIterator.hasNext()) {
      // Retrieve the next WF Disc record.
      WfdiscRecord wdr = this.wfdiscRecordsIterator.next();

      // Read the WF Disc waveform.
      int[] intWaveform = this.waveformReader.readWaveform(wdr, this.wfdiscFile);

      // Convert from int array to double array.
      double[] dblWaveform = Arrays.stream(intWaveform).mapToDouble(x -> x).toArray();

      SortedSet<Waveform> wfs = new TreeSet<>();
      wfs.add(Waveform.create(
          wdr.getTime(),
          wdr.getEndtime(),
          wdr.getSamprate(),
          (long) wdr.getNsamp(),
          dblWaveform));

      // Add the channel segment.
      String site = wdr.getSta(), chan = wdr.getChan();
      Instant time = wdr.getTime();
      String key = keyFor(site, chan);
      UUID processingChannelId = this.staChanNameToChannelId.containsKey(key) ?
          this.staChanNameToChannelId.get(key)
          : this.osdGatewayInterface.idForChannel(site, chan, time);
      if (processingChannelId == null) {
        logger.warn(String.format("Could not find channel for site/chan/time %s/%s/%s",
            site, chan, time));
      } else {
        this.staChanNameToChannelId.put(key, processingChannelId);

        channelSegmentBatch.add(ChannelSegment.create(
            processingChannelId,
            constructSiteChanSegTypeName(wdr),
            getCoiObjectChanSegType(wdr),
            time,
            wdr.getEndtime(),
            wfs,
            creationInfo()));

        // Add boolean SOH if 'clipped' is true.
        if (wdr.getClip()) {
          AcquiredChannelSohBoolean soh = AcquiredChannelSohBoolean.create(
              processingChannelId,
              AcquiredChannelSoh.AcquiredChannelSohType.CLIPPED,
              time,
              wdr.getEndtime(),
              true,   // data is clipped, so status is true.
              creationInfo());
          sohBatch.add(soh);
        }
      }
    }

    return new SegmentAndSohBatch(channelSegmentBatch, sohBatch);
  }

  /**
   * Returns the number of records parsed from the WF Disc file.
   *
   * @return number of parsed WF Disc records
   */
  public int size() {
    return this.wfdiscRecords.size();
  }

  private static String keyFor(String site, String chan) {
    return site + "|" + chan;
  }

  /**
   * Get the channel segment type enumerated in the COI object, instead of the enumeration used in
   * CSS
   *
   * @return ChannelSegmentType The channel segment type enumerated in the COI object
   */
  private static ChannelSegment.ChannelSegmentType getCoiObjectChanSegType(WfdiscRecord wdr) {
    return
        SegmentType.segmentTypeFor(SegmentType.CssSegtype.valueOf(wdr.getSegtype()));
  }

  /**
   * Constructs a string consisting of the site, channel and channel segment type (from the
   * enumeration used in the COI object instead of the CSS enumeration) 
   *
   * @return String Concatenated string consisting of "Site/Channel ChannelSegmentType"
   */
  private static String constructSiteChanSegTypeName(WfdiscRecord wdr) {
    return wdr.getSta() +
        "/" + wdr.getChan() + " " + getCoiObjectChanSegType(wdr);
  }

  private static CreationInfo creationInfo() {
    return new CreationInfo(creatorName, new SoftwareComponentInfo(creatorName, VERSION));
  }
}

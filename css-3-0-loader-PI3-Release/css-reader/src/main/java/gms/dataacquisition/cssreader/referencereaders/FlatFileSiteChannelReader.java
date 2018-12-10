package gms.dataacquisition.cssreader.referencereaders;

import com.github.ffpojo.FFPojoHelper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import gms.dataacquisition.cssreader.data.SiteChannelRecord;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.slf4j.LoggerFactory;

/**
 * Read a site-channel flat file and convert into SiteChannelRecord objects.
 */
public class FlatFileSiteChannelReader {

  private static final org.slf4j.Logger logger = LoggerFactory
      .getLogger(FlatFileSiteChannelReader.class);


  /**
   * Create a reader without any filtering.
   */
  public FlatFileSiteChannelReader() {
  }


  /**
   * Reads the file at the specified path.
   *
   * @param filePath the path to the file to read
   * @return the read site records
   */
  public ListMultimap<Integer, SiteChannelRecord> read(String filePath) throws Exception {
    // Validate file path argument.
    Validate.notEmpty(filePath);
    Path path = Paths.get(filePath);
    if (path == null || Files.exists(path) == false) {
      throw new IllegalArgumentException("Path " + path + " doesn't exist in file system.");
    }

    // read the lines out of the file
    List<String> fileLines = Files.readAllLines(path);
    logger.debug("Site has " + fileLines.size() + " entries in file: " + path);
    if (fileLines.size() <= 0) {
      logger.error("Provided site channel file " + path + " is empty");
      return ArrayListMultimap.create();
    }

    // Convert the file into SiteChannelRecords
    final FFPojoHelper ffpojo = FFPojoHelper.getInstance();
    ListMultimap<Integer, SiteChannelRecord> siteRecords = ArrayListMultimap.create();

    for (int i = 0; i < fileLines.size(); i++) {
      String line = fileLines.get(i);
      try {
        SiteChannelRecord record = toRecord(ffpojo, line);
        siteRecords.put(record.getChanid(), record);
      } catch (Exception ex) {
        logger.error(
            "Encountered error (" + ex.getMessage() + ") at line " + (i + 1) + " of file: " + line);
        ex.printStackTrace();
      }
    }

    logger.debug(siteRecords.size() + " site channel records retrieved.");
    return siteRecords;
  }

  /**
   * Helper method to read a String into a SiteChannelRecord using a FFPojoHelper.
   *
   * @param ffpojo used to parse the input string
   * @param line the line to parse
   * @return the parsed SiteRecord, or null if an error occurs.
   */
  private static SiteChannelRecord toRecord(FFPojoHelper ffpojo, String line) throws Exception {
    SiteChannelRecord record = null;
    if (line.length() == SiteChannelRecord.getRecordLength()) {
      record = ffpojo.createFromText(SiteChannelRecord.class, line);
    } else {
      logger
          .warn("Unable to determine type of site channel record by its length: " + line.length());
    }

    if (record != null) {
      record.validate();
    }
    return record;
  }
}

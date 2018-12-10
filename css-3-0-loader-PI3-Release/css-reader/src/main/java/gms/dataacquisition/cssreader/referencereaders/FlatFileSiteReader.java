package gms.dataacquisition.cssreader.referencereaders;

import com.github.ffpojo.FFPojoHelper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import gms.dataacquisition.cssreader.data.SiteRecord;
import gms.dataacquisition.cssreader.data.SiteRecordCss30;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.slf4j.LoggerFactory;

/**
 * Read a site flat file and convert into SiteRecord objects.
 */
public class FlatFileSiteReader {

  private static final org.slf4j.Logger logger = LoggerFactory
      .getLogger(FlatFileSiteReader.class);


  /**
   * Create a reader without any filtering.
   */
  public FlatFileSiteReader() {
  }


  /**
   * Reads the file at the specified path.
   *
   * @param filePath the path to the file to read
   * @return the read site records
   */
  public ListMultimap<String, SiteRecord> read(String filePath) throws Exception {
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
      logger.error("Provided site file " + path + " is empty");
      return ArrayListMultimap.create();
    }

    // Convert the file into SiteRecords
    final FFPojoHelper ffpojo = FFPojoHelper.getInstance();
    ListMultimap<String, SiteRecord> siteRecords = ArrayListMultimap.create();

    for (int i = 0; i < fileLines.size(); i++) {
      String line = fileLines.get(i);
      try {
        SiteRecord record = toRecord(ffpojo, line);
        siteRecords.put(record.getSta(), record);
      } catch (Exception ex) {
        logger.error(
            "Encountered error (" + ex.getMessage() + ") at line " + (i + 1) + " of file: " + line);
        ex.printStackTrace();
      }
    }

    logger.debug(siteRecords.size() + " site records retrieved.");
    return siteRecords;
  }

  /**
   * Helper method to read a String into a SiteRecord using a FFPojoHelper.
   *
   * @param ffpojo used to parse the input string
   * @param line the line to parse
   * @return the parsed SiteRecord, or null if an error occurs.
   */
  private static SiteRecord toRecord(FFPojoHelper ffpojo, String line) throws Exception {
    SiteRecord record = null;
    if (line.length() == SiteRecordCss30.getRecordLength()) {
      record = ffpojo.createFromText(SiteRecordCss30.class, line);
    } else {
      logger
          .warn("Unable to determine type of site record by its length: " + line.length());
    }

    if (record != null) {
      record.validate();
    }
    return record;
  }
}

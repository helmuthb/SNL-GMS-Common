package gms.dataacquisition.cssreader.referencereaders;

import com.github.ffpojo.FFPojoHelper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import gms.dataacquisition.cssreader.data.SensorRecord;
import gms.dataacquisition.cssreader.data.SensorRecordCss30;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.slf4j.LoggerFactory;

/**
 * Read a site flat file and convert into SensorRecord objects.
 */
public class FlatFileSensorReader {

  private static final org.slf4j.Logger logger = LoggerFactory
      .getLogger(FlatFileSensorReader.class);


  /**
   * Create a reader without any filtering.
   */
  public FlatFileSensorReader() {
  }


  /**
   * Reads the file at the specified path.
   *
   * @param filePath the path to the file to read
   * @return the read site records
   */
  public ListMultimap<Integer, SensorRecord> read(String filePath) throws Exception {
    // Validate file path argument.
    Validate.notEmpty(filePath);
    Path path = Paths.get(filePath);
    if (path == null || Files.exists(path) == false) {
      throw new IllegalArgumentException("Path " + path + " doesn't exist in file system.");
    }

    // read the lines out of the file
    List<String> fileLines = Files.readAllLines(path);
    logger.debug("Sensor has " + fileLines.size() + " entries in file: " + path);
    if (fileLines.size() <= 0) {
      logger.error("Provided sensor file " + path + " is empty");
      return ArrayListMultimap.create();
    }

    // Convert the file into SiteRecords
    final FFPojoHelper ffpojo = FFPojoHelper.getInstance();
    ListMultimap<Integer, SensorRecord> siteRecords = ArrayListMultimap.create();

    for (int i = 0; i < fileLines.size(); i++) {
      String line = fileLines.get(i);
      try {
        SensorRecord record = toRecord(ffpojo, line);
        siteRecords.put(record.getChanid(), record);
      } catch (Exception ex) {
        logger.error(
            "Encountered error (" + ex.getMessage() + ") at line " + (i + 1) + " of file: " + line);
        ex.printStackTrace();
      }
    }

    logger.debug(siteRecords.size() + " sensor records retrieved.");
    return siteRecords;
  }

  /**
   * Helper method to read a String into a SiteRecord using a FFPojoHelper.
   *
   * @param ffpojo used to parse the input string
   * @param line the line to parse
   * @return the parsed SiteRecord, or null if an error occurs.
   */
  private static SensorRecord toRecord(FFPojoHelper ffpojo, String line) throws Exception {
    SensorRecord record = null;
    if (line.length() == SensorRecordCss30.getRecordLength()) {
      record = ffpojo.createFromText(SensorRecordCss30.class, line);
    } else {
      logger
          .warn("Unable to determine type of sensor record by its length: " + line.length());
    }

    if (record != null) {
      record.validate();
    }
    return record;
  }
}

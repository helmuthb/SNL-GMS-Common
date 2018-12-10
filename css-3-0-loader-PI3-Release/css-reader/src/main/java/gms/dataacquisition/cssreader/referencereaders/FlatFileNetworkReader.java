package gms.dataacquisition.cssreader.referencereaders;

import com.github.ffpojo.FFPojoHelper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import gms.dataacquisition.cssreader.data.NetworkRecord;
import gms.dataacquisition.cssreader.data.NetworkRecordCss30;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.slf4j.LoggerFactory;

/**
 * Read a network flat file and convert into ReferenceNetwork objects.
 */
public class FlatFileNetworkReader {

  private static final org.slf4j.Logger logger = LoggerFactory
      .getLogger(FlatFileNetworkReader.class);


  /**
   * Create a reader without any filtering.
   */
  public FlatFileNetworkReader() {
  }


  /**
   * Reads the file at the specified path.
   *
   * @param filePath the path to the file to read
   * @return the read network records
   */
  public ListMultimap<String, NetworkRecord> read(String filePath) throws Exception {
    // Validate file path argument.
    Validate.notEmpty(filePath);
    Path path = Paths.get(filePath);
    if (path == null || Files.exists(path) == false) {
      throw new IllegalArgumentException("Path " + path + " doesn't exist in file system.");
    }

    // read the lines out of the file
    List<String> fileLines = Files.readAllLines(path);
    logger.debug("Network has " + fileLines.size() + " entries in file: " + path);
    if (fileLines.size() <= 0) {
      logger.error("Provided network file " + path + " is empty");
      return ArrayListMultimap.create();
    }

    // convert the file into NetworkRecords
    final FFPojoHelper ffpojo = FFPojoHelper.getInstance();
    ListMultimap<String, NetworkRecord> networkRecords = ArrayListMultimap.create();

    for (int i = 0; i < fileLines.size(); i++) {
      String line = fileLines.get(i);
      try {
        NetworkRecord record = toRecord(ffpojo, line);
        if ( record != null ) {
          networkRecords.put(record.getName(), record);
        }
      } catch (Exception ex) {
        logger.error(
            "Encountered error (" + ex.getMessage() + ") at line " + (i + 1) + " of file: " + line);
        ex.printStackTrace();
      }
    }

    logger.debug(networkRecords.size() + " network records retrieved.");
    return networkRecords;
  }

  /**
   * Helper method to read a String into a NetworkRecord using a FFPojoHelper.
   *
   * @param ffpojo used to parse the input string
   * @param line the line to parse
   * @return the parsed NetworkRecord, or null if an error occurs.
   */
  private static NetworkRecord toRecord(FFPojoHelper ffpojo, String line) throws Exception {
    NetworkRecord record = null;
    if (line.length() == NetworkRecordCss30.getRecordLength()) {
      record = ffpojo.createFromText(NetworkRecordCss30.class, line);
    } else {
      logger
          .warn("Unable to determine type of network record by its length: " + line.length());
    }

    if (record != null) {
      record.validate();
    }
    return record;
  }
}

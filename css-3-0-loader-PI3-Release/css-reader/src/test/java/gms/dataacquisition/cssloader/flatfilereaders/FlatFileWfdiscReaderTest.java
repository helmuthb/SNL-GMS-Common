package gms.dataacquisition.cssloader.flatfilereaders;

import gms.dataacquisition.cssreader.flatfilereaders.WfdiscReaderInterface;
import gms.dataacquisition.cssreader.flatfilereaders.FlatFileWfdiscReader;
import gms.dataacquisition.cssreader.data.WfdiscRecord;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Tests the FlatFileWfdiscReader.
 */
public class FlatFileWfdiscReaderTest {

  WfdiscReaderInterface reader = new FlatFileWfdiscReader();

  private final String TEST_DATA_DIR = "src/test/resources/css/WFS4/";
  private final String WF_DISC_TEST_FILE = TEST_DATA_DIR + "wfdisc_gms_s4.txt";

  private final int WF_DISC_TEST_FILE_ROWS = 76;

  @Test
  public void testReadOnSampleFile() throws Exception {
    Collection<WfdiscRecord> wfdiscRecords = reader.read(WF_DISC_TEST_FILE);
    assertNotNull(wfdiscRecords);
    assertEquals(wfdiscRecords.size(), WF_DISC_TEST_FILE_ROWS);
    assertFalse(wfdiscRecords.contains(null));
  }

  @Test(expected = Exception.class)
  public void testReadOnNullPath() throws Exception {
    reader.read(null);
  }

  @Test(expected = Exception.class)
  public void testReadOnEmptyPath() throws Exception {
    reader.read("");
  }

  @Test(expected = Exception.class)
  public void testReadOnBadPath() throws Exception {
    reader.read("nonExistentPath");
  }
}

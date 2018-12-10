package gms.dataacquisition.cssloader.referencereaders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ListMultimap;
import gms.dataacquisition.cssreader.data.NetworkRecord;
import gms.dataacquisition.cssreader.referencereaders.FlatFileNetworkReader;
import java.net.URL;
import org.junit.Test;

public class FlatFileNetwordReaderTest {

  private static final String path1 = "networkfiles/networks.txt";



  @Test
  public void testReadFile1() {
    ListMultimap<String, NetworkRecord> map = null;
        FlatFileNetworkReader reader = new FlatFileNetworkReader();
    try {
      ClassLoader classLoader = ClassLoader.getSystemClassLoader();
      URL url = classLoader.getResource(path1);
      map = reader.read(url.getPath());

    }
    catch(Exception e) {

    }
    assertNotNull(map);
    assertEquals(23, map.size());
  }



}

package gms.dataacquisition.cssloader.referencereaders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ListMultimap;
import gms.dataacquisition.cssreader.data.SiteRecord;
import gms.dataacquisition.cssreader.referencereaders.FlatFileSiteReader;
import java.net.URL;
import org.junit.Test;

public class FlatFileSiteReaderTest {

  private static final String path1 = "sitefiles/sites.txt";



  @Test
  public void testReadFile1() {
    ListMultimap<String, SiteRecord> map = null;
    FlatFileSiteReader reader = new FlatFileSiteReader();
    try {
      ClassLoader classLoader = ClassLoader.getSystemClassLoader();
      URL url = classLoader.getResource(path1);
      map = reader.read(url.getPath());

    }
    catch(Exception e) {

    }
    assertNotNull(map);
    assertEquals(30, map.size());
  }



}

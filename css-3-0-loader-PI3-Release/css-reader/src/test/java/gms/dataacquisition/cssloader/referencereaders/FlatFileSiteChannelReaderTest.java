package gms.dataacquisition.cssloader.referencereaders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ListMultimap;
import gms.dataacquisition.cssreader.data.SiteChannelRecord;
import gms.dataacquisition.cssreader.referencereaders.FlatFileSiteChannelReader;
import java.net.URL;
import org.junit.Test;

public class FlatFileSiteChannelReaderTest {

  private static final String path1 = "sitechannelfiles/sitechannels.txt";


  @Test
  public void testReadFile1() {
    ListMultimap<Integer, SiteChannelRecord> map = null;
    FlatFileSiteChannelReader reader = new FlatFileSiteChannelReader();
    try {
      ClassLoader classLoader = ClassLoader.getSystemClassLoader();
      URL url = classLoader.getResource(path1);
      map = reader.read(url.getPath());

    } catch (Exception e) {

    }
    assertNotNull(map);
    assertEquals(50, map.size());
  }


}

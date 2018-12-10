package gms.dataacquisition.cssloader.referencereaders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ListMultimap;
import gms.dataacquisition.cssreader.data.SensorRecord;
import gms.dataacquisition.cssreader.referencereaders.FlatFileSensorReader;
import java.net.URL;
import org.junit.Test;

public class FlatFileSensorReaderTest {

  private static final String path1 = "sensorfiles/sensorcss30.txt";
  private static final String path2 = "sensorfiles/sensorp3.txt";



  @Test
  public void testReadFile1() {
    ListMultimap<Integer, SensorRecord> map = null;
    FlatFileSensorReader reader = new FlatFileSensorReader();
    try {
      ClassLoader classLoader = ClassLoader.getSystemClassLoader();
      URL url = classLoader.getResource(path1);
      map = reader.read(url.getPath());

    }
    catch(Exception e) {

    }
    assertNotNull(map);
    assertEquals(67, map.size());
  }



  @Test
  public void testReadFile2() {
    ListMultimap<Integer, SensorRecord> map = null;
    FlatFileSensorReader reader = new FlatFileSensorReader();
    try {
      ClassLoader classLoader = ClassLoader.getSystemClassLoader();
      URL url = classLoader.getResource(path2);
      map = reader.read(url.getPath());

    }
    catch(Exception e) {

    }
    assertNotNull(map);
    assertEquals(75, map.size());
  }



}

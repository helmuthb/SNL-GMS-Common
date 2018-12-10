package influxdb.repository;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Setup a connection to the time-series database.
 *
 */
public class TimeseriesDependencies {

  private static final org.slf4j.Logger logger = LoggerFactory
          .getLogger(TimeseriesDependencies.class);

  // Define location from the connection properties file.
  private static final String influxPropertiesFile = "/META-INF/influxdb.properties";

  // Declare one instance from the connection manager.
  private static InfluxDB influxDB = null;

  private static String databaseName;
  private static int flushRate;
  private static int flushSize;

  static {
    Properties props = readInfluxProperties();
    String url = "http://" + props.getProperty("host") + ":" + props.getProperty("port");
    databaseName = props.getProperty("databaseName");
    flushRate = Integer.valueOf(props.getProperty("batchFlushRate", "3000"));
    flushSize = Integer.valueOf(props.getProperty("batchFlushSize", "150000"));
    influxDB = InfluxDBFactory.connect(url, props.getProperty("user"), props.getProperty("pass"));
    influxDB.setDatabase(databaseName);
    influxDB.enableGzip();
    influxDB.enableBatch(
              TimeseriesDependencies.getFlushSize(),
              TimeseriesDependencies.getFlushRate(), TimeUnit.MILLISECONDS);

    logger.info("Created connection manager for " + databaseName +
            ", user: " + props.getProperty("user")+ ", pass: ******, " +
            "flush size: " + flushSize + ", flush rate: " + flushRate);
  }

  /**
   * Get the connection manager for the time-series database.
   *
   * @return The InfluxDB connection manager or null if unable to acquire the connection manager.
   */
  public static InfluxDB getConnection() {
    try {
      influxDB.ping();
      return influxDB;
    }
    catch(Exception ex) {
      logger.error("Could not get connection: " + ex);
      return null;
    }
  }


  public static String getDatabaseName() {
    return databaseName;
  }

  public static int getFlushRate() {
    return flushRate;
  }

  public static int getFlushSize() {
    return flushSize;
  }



  /**
   * Read the time-series database connection properties.
   *
   * @return A Properties object contains the connection attributes.
   * @throws IOException If unable to read the properties file.
   */
  private static Properties readInfluxProperties() {
    Properties props = new Properties();
    InputStream inputStream = null;
    try {
      inputStream = TimeseriesDependencies.class.getResourceAsStream(influxPropertiesFile);
      props.load(inputStream);
      inputStream.close();
    }
    catch(IOException ex) {
      logger.error("IOException on reading timeseries properties file " + influxPropertiesFile + "; " + ex);
      return props;
    }

    return props;
  }


}

package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.influx;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.LoggerFactory;

/**
 * Setup a connection to the time-series database and read the default database settings.
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
  private static int chunkSize;
  private static int chunkTimeout;

  static {
    Properties props = readInfluxProperties();
    String url = "http://" + props.getProperty("host") + ":" + props.getProperty("port");
    databaseName = props.getProperty("databaseName");
    flushRate = Integer.valueOf(props.getProperty("batchFlushRate", "500"));
    flushSize = Integer.valueOf(props.getProperty("batchFlushSize", "15000"));
    chunkSize = Integer.valueOf(props.getProperty("chunkSize", "25000"));
    chunkTimeout = Integer.valueOf(props.getProperty("chunkTimeout", "5000"));
    influxDB = InfluxDBFactory.connect(url, props.getProperty("user"), props.getProperty("pass"));
    influxDB.setDatabase(databaseName);
    influxDB.enableGzip();
    if (!influxDB.isBatchEnabled()) {
      influxDB.enableBatch(TimeseriesDependencies.getFlushSize(),
          TimeseriesDependencies.getFlushRate(), TimeUnit.MILLISECONDS);
    }
    logger.info("Created connection manager for " + databaseName +
        ", user: " + props.getProperty("user") +
        ", flush size: " + flushSize +
        ", flush rate: " + flushRate +
        ", chunk size: " + chunkSize);
  }

  /**
   * Get the connection manager for the time-series database.
   */
  public static InfluxDB getConnection() {
    influxDB.ping();
    return influxDB;
  }

  /**
   * Get the database name.
   *
   * @return A String.
   */
  public static String getDatabaseName() {
    return databaseName;
  }

  /**
   * Get the number of milliseconds at which an automatic flush will occur.
   *
   * @return An integer.
   */
  public static int getFlushRate() {
    return flushRate;
  }

  /**
   * Get the number of points stored which will trigger an automatic flush.
   *
   * @return An integer.
   */
  public static int getFlushSize() {
    return flushSize;
  }

  /**
   * Get the number of points which will be returned in a single chunk when reading.
   *
   * @return An integer.
   */
  public static int getChunkSize() {
    return chunkSize;
  }

  /**
   * Get the timeout value, in milliseconds, for the receipt of chunks.
   *
   * @return Value in milliseconds.
   */
  public static int getChunkTimeout() {
    return chunkTimeout;
  }

  /**
   * Read the time-series database connection properties.
   *
   * @return A Properties object contains the connection attributes.
   */
  private static Properties readInfluxProperties() {
    Properties props = new Properties();
    InputStream inputStream;
    try {
      inputStream = TimeseriesDependencies.class.getResourceAsStream(influxPropertiesFile);
      props.load(inputStream);
      inputStream.close();
    } catch (IOException ex) {
      logger.error(
          "IOException on reading timeseries properties file " + influxPropertiesFile + "; " + ex);
      return props;
    }

    return props;
  }


}

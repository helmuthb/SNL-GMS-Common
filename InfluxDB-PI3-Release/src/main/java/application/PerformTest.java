package application;

import flatfile.tester.FlatFileReadWriteTester;
import influxdb.tester.InfluxDbReadTester;
import influxdb.tester.InfluxDbThreadedWriteTester;
import influxdb.tester.InfluxDbWriteTester;
import postgres.tester.PostgresTester;


/**
 * Created by dvanwes on 11/16/17.
 */
public class PerformTest {

  public static void main(String[] args) {

    if (args == null || args.length < 1 || args[0] == null) {
      System.err.println("Provide one of these modes: r for read, w for write, t for threaded");
      System.exit(-1);
    }

    String mode = args[0];

    // Write tests
    if (mode.startsWith("w")) {

      try { FlatFileReadWriteTester.runWriteTests(); }
      catch (Exception e) { e.printStackTrace(); }

      try { PostgresTester.runWriteTests(); }
      catch (Exception e) { e.printStackTrace(); }

      try { InfluxDbWriteTester.run(); }
      catch (Exception e) { e.printStackTrace(); }
    }
    else if (mode.startsWith("r")) {
      // Read tests; should be run separately (i.e. in a new process)
      // and with a restarted container (don't destroy volume) to ensure caching is not happening
      try { FlatFileReadWriteTester.runReadTests(); }
      catch(Exception e) { e.printStackTrace(); }

      try { PostgresTester.runReadTests(); }
      catch(Exception e) { e.printStackTrace(); }

      try { InfluxDbReadTester.run(); }
      catch(Exception e) { e.printStackTrace(); }
    }
    else if (mode.startsWith("t")) {

      try { InfluxDbThreadedWriteTester.run(); }
      catch(Exception e) { e.printStackTrace(); }
    }
    else {
      System.err.println("Unknown mode: expecting a word that starts with either " +
              "w, r, or t (for write tests, read tests, or threaded tests)");
    }

    System.exit(0);
  }
}




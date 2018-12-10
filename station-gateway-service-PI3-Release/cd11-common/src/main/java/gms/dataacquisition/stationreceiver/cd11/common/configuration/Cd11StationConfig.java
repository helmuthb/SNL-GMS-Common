package gms.dataacquisition.stationreceiver.cd11.common.configuration;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11GapList;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Cd11StationConfig {

  private static Logger logger = LoggerFactory.getLogger(Cd11StationConfig.class);

  public static final List<Cd11NetworkDescriptor> fakeStations = new ArrayList<>();
  public static final String gapStoragePath = "shared-volume/gaps/";

  static {
    final UUID
        //fakeId_1 = UUID.fromString("11112f6f-73e9-4732-94b1-8a2eadc81f07"),
        //fakeId_2 = UUID.fromString("2222217e-f18d-478f-8df0-2443e83496b0"),
        fakeId_3 = UUID.fromString("33333462-aeb4-439a-bb6e-16002d30dfe6"),
        fakeId_4 = UUID.fromString("44444ebc-21c1-4835-9296-a5c1b7d4671b"),
        //fakeId_5 = UUID.fromString("555555e7-3555-4066-9ff1-754f92bd2e4d"),
        fakeId_6 = UUID.fromString("6666ec85-8fa7-407e-a6a9-ba705756df76"),
        fakeId_7 = UUID.fromString("77777971-9938-450b-9ee6-bab3e7cb34a5"),
        //fakeId_8 = UUID.fromString("888839f7-1553-4002-8d0b-181bc49c68d6"),
        //fakeId_9 = UUID.fromString("999c2d32-870b-4795-b820-e3bc9bbaddce"),
        //fakeId_10 = UUID.fromString("10102285-8fa7-407e-a6a9-ba705756df76"),
        fakeId_11 = UUID.fromString("101156c4-c31a-4363-b21e-6691e26de5d5"),
        fakeId_12 = UUID.fromString("10122f6f-73e9-4732-94b1-8a2eadc81f07"),
        fakeId_13 = UUID.fromString("101317e1-f18d-478f-8df0-2443e83496b0"),
        fakeId_14 = UUID.fromString("10144621-aeb4-439a-bb6e-16002d30dfe6"),
        fakeId_15 = UUID.fromString("10151ebc-21c1-4835-9296-a5c1b7d4671b"),
        fakeId_16 = UUID.fromString("1016c5e7-3555-4066-9ff1-754f92bd2e4d");

    // Populate the fake stations list.
    //fakeStations.add(new Cd11NetworkDescriptor(fakeId_1, "F01", 8100));
    //fakeStations.add(new Cd11NetworkDescriptor(fakeId_2, "F02", 8101));
    fakeStations.add(new Cd11NetworkDescriptor(fakeId_3, "F03", 8102));
    fakeStations.add(new Cd11NetworkDescriptor(fakeId_4, "F04", 8103));
    //fakeStations.add(new Cd11NetworkDescriptor(fakeId_5, "F05", 8104));
    fakeStations.add(new Cd11NetworkDescriptor(fakeId_6, "F06", 8105));
    fakeStations.add(new Cd11NetworkDescriptor(fakeId_7, "F07", 8106));
    //fakeStations.add(new Cd11NetworkDescriptor(fakeId_8, "F08", 8107));
    //fakeStations.add(new Cd11NetworkDescriptor(fakeId_9, "F09", 8108));
    //fakeStations.add(new Cd11NetworkDescriptor(fakeId_10, "F10", 8109));
    fakeStations.add(new Cd11NetworkDescriptor(fakeId_11, "F11", 8100));
    fakeStations.add(new Cd11NetworkDescriptor(fakeId_12, "F12", 8101));
    fakeStations.add(new Cd11NetworkDescriptor(fakeId_13, "F13", 8104));
    fakeStations.add(new Cd11NetworkDescriptor(fakeId_14, "F14", 8108));
    fakeStations.add(new Cd11NetworkDescriptor(fakeId_15, "F15", 8107));
    fakeStations.add(new Cd11NetworkDescriptor(fakeId_16, "F16", 8109));

    // Ensure that the fake gap storage path exists.
    File gapsDir = new File(gapStoragePath);
    if (!gapsDir.exists()) {
      gapsDir.mkdirs();
    }
  }

  public static Cd11GapList loadGapState(String stationName) {
    Path path = Paths.get(gapStoragePath + stationName + ".json");
    if (Files.exists(path)) {
      try {
        String json = new String(Files.readAllBytes(path));
        return new Cd11GapList(json);
      } catch (IOException e) {
        return new Cd11GapList();
      }
    } else {
      return new Cd11GapList();
    }
  }

  public static void persistGapState(String stationName, Cd11GapList cd11GapList)
      throws IOException {
    String path = gapStoragePath + stationName + ".json";
    try (PrintWriter out = new PrintWriter(path)) {
      out.println(cd11GapList.toJson());

      // Set file permissions.
      File file = new File(path);
      file.setReadable(true, false);
      file.setWritable(true, false);
      file.setExecutable(false, false);
    }
  }

  public static void clearGapState(String stationName) throws IOException {
    String path = gapStoragePath + stationName + ".json";
    File file = new File(path);
    if (!file.delete()) {
      logger.error("Gap State file could not be deleted: " + path);
    }
  }
}

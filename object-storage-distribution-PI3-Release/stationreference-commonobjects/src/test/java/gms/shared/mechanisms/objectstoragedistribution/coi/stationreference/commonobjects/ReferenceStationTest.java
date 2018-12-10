package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReferenceStationTest {

  private static final UUID id = UUID.fromString("1712f98e-ff83-4f3d-a832-a82a040221d9");
  private static final UUID versionId = UUID.fromString("589beef5-a304-4462-9374-6141277961e1");
  private static final UUID networkId = UUID.fromString("0332f98e-ff83-4f3d-a832-a82a04022144");
  private static final UUID stationId = UUID.fromString("7332f98e-ff83-4f3d-a832-a82a04022141");
  private static final UUID siteId = UUID.fromString("0152f98e-ff83-4f3d-a832-a82a04022978");
  private static final String name = "abc001"; // when stored it should be uppercase
  private static final InformationSource source = InformationSource.create("Internet",
      Instant.now(), "none");
  private static final String description = "This is a description.";
  private static final String comment = "It must be true.";
  private static final String aliasName = "Alias1";
  private static final StationType type = StationType.Hydroacoustic;
  private static final Instant actualTime = Instant.now().minusSeconds(50);
  private static final Instant systemTime = Instant.now();
  private static final double latitude = -13.56789;
  private static final double longitude = 89.04123;
  private static final double elevation = 376.43;

  private static final double precision = 0.00001;

  private static ReferenceAlias alias = ReferenceAlias.create(aliasName,
      StatusType.INACTIVE, "no comment", actualTime, systemTime);
  private static List<ReferenceAlias> aliases = List.of(alias);

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceStation.class);
  }

  @Test
  public void testReferenceStationCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceStation.class, "create", name, description, type, source, comment,
        latitude, longitude, elevation,
        actualTime, aliases);
  }

  @Test
  public void testReferenceStationFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceStation.class, "from",
        id, versionId, name, description, type, source, comment,
        latitude, longitude, elevation,
        actualTime, systemTime, aliases);
  }

  @Test
  public void testReferenceStationCreateNewVersionNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceStation.class, "createNewVersion",
        id, name, description, type, source, comment,
        latitude, longitude, elevation,
        actualTime, aliases);
  }

  @Test
  public void testAddAlias() {
    ReferenceStation station = ReferenceStation.create(name, description, type, source, comment,
        latitude, longitude, elevation, actualTime, new ArrayList<>());
    station.addAlias(alias);
    assertTrue(station.getAliases().size() == 1);
    assertEquals(alias, station.getAliases().get(0));
  }

  @Test
  public void testReferenceStationCreate() {
    ReferenceStation station = ReferenceStation.create(name, description, type, source, comment,
        latitude, longitude, elevation, actualTime, aliases);
    assertEquals(name.toUpperCase(), station.getName());
    assertEquals(description, station.getDescription());
    assertEquals(type, station.getStationType());
    assertEquals(source, station.getSource());
    assertEquals(comment, station.getComment());
    assertEquals(latitude, station.getLatitude(), precision);
    assertEquals(longitude, station.getLongitude(), precision);
    assertEquals(elevation, station.getElevation(), precision);
    assertEquals(actualTime, station.getActualChangeTime());
    assertNotEquals(systemTime, station.getSystemChangeTime());
    assertEquals(aliases, station.getAliases());
  }

  @Test
  public void testReferenceStationFrom() {
    ReferenceStation station = ReferenceStation.from(stationId, versionId, name,
        description, type, source, comment,
        latitude, longitude, elevation, actualTime, systemTime, aliases);
    assertEquals(stationId, station.getEntityId());
    assertEquals(versionId, station.getVersionId());
    assertEquals(name.toUpperCase(), station.getName());
    assertEquals(description, station.getDescription());
    assertEquals(type, station.getStationType());
    assertEquals(source, station.getSource());
    assertEquals(comment, station.getComment());
    assertEquals(latitude, station.getLatitude(), precision);
    assertEquals(longitude, station.getLongitude(), precision);
    assertEquals(elevation, station.getElevation(), precision);
    assertEquals(actualTime, station.getActualChangeTime());
    assertEquals(systemTime, station.getSystemChangeTime());
    assertEquals(aliases, station.getAliases());
  }


}

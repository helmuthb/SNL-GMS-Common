package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;

public class ReferenceChannelTest {

  private static final UUID id = UUID.fromString("1712f98e-ff83-4f3d-a832-a82a040221d9");
  private static final String name = "BHZ";
  private static final ChannelType type = ChannelType.BROADBAND_HIGH_GAIN_VERTICAL;
  private static final ChannelDataType dataType = ChannelDataType.SEISMIC_ARRAY;
  private static final int locationCode = 1;
  private static final double latitude = -13.56789;
  private static final double longitude = 89.04123;
  private static final double elevation = 376.43;
  private static final double depth = 123.456;
  private static final double verticalAngle = 12.34;
  private static final double horizontalAngle = 43.21;
  private static final double nominalSampleRate = 40;
  private static final Instant actualTime = Instant.now().minusSeconds(50);
  private static final Instant systemTime = Instant.now();
  private static final String comment = "It must be true.";
  private static final RelativePosition position = RelativePosition.create(1.1, 2.2, 3.3);

  private static final InformationSource informationSource = InformationSource.create(
      "TEST", Instant.now(), "TEST");

  private static List<ReferenceAlias> aliases = new ArrayList<>();

  private static final double precision = 0.00001;

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceChannel.class);
  }

  @Test
  public void testReferenceChannelCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceChannel.class, "create", name, type, dataType, locationCode,
        latitude, longitude, elevation, depth, verticalAngle, horizontalAngle, nominalSampleRate,
        actualTime, informationSource, comment, position, aliases);
  }

  @Test
  public void testReferenceChannelFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceChannel.class, "from", id, UUID.randomUUID(),
        name, type, dataType, locationCode,
        latitude, longitude, elevation, depth, verticalAngle, horizontalAngle, nominalSampleRate,
        actualTime, systemTime, informationSource, comment, position, aliases);
  }

  @Test
  public void testReferenceChannelCreateNewVersionNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceChannel.class, "createNewVersion", id,
        name, type, dataType, locationCode,
        latitude, longitude, elevation, depth, verticalAngle, horizontalAngle, nominalSampleRate,
        actualTime, informationSource, comment, position, aliases);
  }

  @Test
  public void testReferenceChannelCreate() {
    ReferenceChannel channel = ReferenceChannel.create(name, type, dataType, locationCode,
        latitude, longitude, elevation, depth, verticalAngle, horizontalAngle, nominalSampleRate,
        actualTime, informationSource, comment, position, aliases);
    assertEquals(name.toUpperCase(), channel.getName());
    assertEquals(type, channel.getType());
    assertEquals(dataType, channel.getDataType());
    assertEquals(locationCode, channel.getLocationCode());
    assertEquals(latitude, channel.getLatitude(), precision);
    assertEquals(longitude, channel.getLongitude(), precision);
    assertEquals(elevation, channel.getElevation(), precision);
    assertEquals(depth, channel.getDepth(), precision);
    assertEquals(verticalAngle, channel.getVerticalAngle(), precision);
    assertEquals(horizontalAngle, channel.getHorizontalAngle(), precision);
    assertEquals(nominalSampleRate, channel.getNominalSampleRate(), precision);
    assertEquals(actualTime, channel.getActualTime());
    assertNotEquals(systemTime, channel.getSystemTime());
    assertEquals(informationSource, channel.getInformationSource());
    assertEquals(comment, channel.getComment());
    assertEquals(position, channel.getPosition());
    assertEquals(aliases, channel.getAliases());
  }

  @Test
  public void testReferenceChannelFrom() {
    UUID versionId = UUID.randomUUID();
    ReferenceChannel channel = ReferenceChannel.from(id, versionId, name, type, dataType, locationCode,
        latitude, longitude, elevation, depth, verticalAngle, horizontalAngle, nominalSampleRate,
        actualTime, systemTime, informationSource, comment, position, aliases);
    assertEquals(id, channel.getEntityId());
    assertEquals(versionId, channel.getVersionId());
    assertEquals(name.toUpperCase(), channel.getName());
    assertEquals(type, channel.getType());
    assertEquals(dataType, channel.getDataType());
    assertEquals(locationCode, channel.getLocationCode());
    assertEquals(latitude, channel.getLatitude(), precision);
    assertEquals(longitude, channel.getLongitude(), precision);
    assertEquals(elevation, channel.getElevation(), precision);
    assertEquals(depth, channel.getDepth(), precision);
    assertEquals(verticalAngle, channel.getVerticalAngle(), precision);
    assertEquals(horizontalAngle, channel.getHorizontalAngle(), precision);
    assertEquals(nominalSampleRate, channel.getNominalSampleRate(), precision);
    assertEquals(actualTime, channel.getActualTime());
    assertEquals(systemTime, channel.getSystemTime());
    assertEquals(informationSource, channel.getInformationSource());
    assertEquals(comment, channel.getComment());
    assertEquals(position, channel.getPosition());
    assertEquals(aliases, channel.getAliases());
  }
}

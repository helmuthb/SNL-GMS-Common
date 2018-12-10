package gms.core.waveformqc.waveformqccontrol.objects;

import static org.junit.Assert.assertEquals;

import gms.core.waveformqc.waveformqccontrol.objects.ChannelSohSubtype;
import org.junit.Test;

/**
 * Tests for the {@link ChannelSohSubtype} enumeration
 */
public class ChannelSohSubtypeTests {

  /**
   * Verify each literal returns the correct rationale string
   */
  @Test
  public void testGetRationale() throws Exception {
    assertEquals("System created: dead sensor channel",
        ChannelSohSubtype.DEAD_SENSOR_CHANNEL.getRationale());
    assertEquals("System created: zeroed data", ChannelSohSubtype.ZEROED_DATA.getRationale());
    assertEquals("System created: clipped data", ChannelSohSubtype.CLIPPED.getRationale());
    assertEquals("System created: main power failure",
        ChannelSohSubtype.MAIN_POWER_FAILURE.getRationale());
    assertEquals("System created: backup power unstable",
        ChannelSohSubtype.BACKUP_POWER_UNSTABLE.getRationale());
    assertEquals("System created: calibration underway",
        ChannelSohSubtype.CALIBRATION_UNDERWAY.getRationale());
    assertEquals("System created: digitizer analog input shorted",
        ChannelSohSubtype.DIGITIZER_ANALOG_INPUT_SHORTED.getRationale());
    assertEquals("System created: digitizer calibration loop back",
        ChannelSohSubtype.DIGITIZER_CALIBRATION_LOOP_BACK.getRationale());
    assertEquals("System created: equipment housing open",
        ChannelSohSubtype.EQUIPMENT_HOUSING_OPEN.getRationale());
    assertEquals("System created: digitizing equipment open",
        ChannelSohSubtype.DIGITIZING_EQUIPMENT_OPEN.getRationale());
    assertEquals("System created: vault door opened",
        ChannelSohSubtype.VAULT_DOOR_OPENED.getRationale());
    assertEquals("System created: authentication seal broken",
        ChannelSohSubtype.AUTHENTICATION_SEAL_BROKEN.getRationale());
    assertEquals("System created: equipment moved",
        ChannelSohSubtype.EQUIPMENT_MOVED.getRationale());
    assertEquals("System created: clock differential too large",
        ChannelSohSubtype.CLOCK_DIFFERENTIAL_TOO_LARGE.getRationale());
    assertEquals("System created: GPS receiver off",
        ChannelSohSubtype.GPS_RECEIVER_OFF.getRationale());
    assertEquals("System created: GPS receiver unlocked",
        ChannelSohSubtype.GPS_RECEIVER_UNLOCKED.getRationale());
    assertEquals("System created: data time - time of last GPS synchronization > threshold",
        ChannelSohSubtype.DATA_TIME_GPS_SYNCHRONIZATION_TIME_DELTA_OVER_THRESHOLD.getRationale());
    assertEquals("System created: clock differential in microseconds > threshold",
        ChannelSohSubtype.CLOCK_DIFFERENTIAL_OVER_THRESHOLD.getRationale());
  }
}

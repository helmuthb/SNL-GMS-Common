package gms.core.waveformqc.waveformqccontrol.objects;

/**
 * Enumerates all of the possible acquired channel state-of-health status values potentially leading
 * to {@link gms.shared.mechanisms.objectstoragedistribution.coi.dataobject.stationprocessing.QcMask}
 * creation. The literals correspond to values from {@link gms.shared.mechanisms.objectstoragedistribution.coi.dataobject.dataacquisition.AcquiredChannelSoh.AcquiredChannelSohType}.
 */
public enum ChannelSohSubtype {
  DEAD_SENSOR_CHANNEL("System created: dead sensor channel"),
  ZEROED_DATA("System created: zeroed data"),
  CLIPPED("System created: clipped data"),
  MAIN_POWER_FAILURE("System created: main power failure"),
  BACKUP_POWER_UNSTABLE("System created: backup power unstable"),
  CALIBRATION_UNDERWAY("System created: calibration underway"),
  DIGITIZER_ANALOG_INPUT_SHORTED("System created: digitizer analog input shorted"),
  DIGITIZER_CALIBRATION_LOOP_BACK("System created: digitizer calibration loop back"),
  EQUIPMENT_HOUSING_OPEN("System created: equipment housing open"),
  DIGITIZING_EQUIPMENT_OPEN("System created: digitizing equipment open"),
  VAULT_DOOR_OPENED("System created: vault door opened"),
  AUTHENTICATION_SEAL_BROKEN("System created: authentication seal broken"),
  EQUIPMENT_MOVED("System created: equipment moved"),
  CLOCK_DIFFERENTIAL_TOO_LARGE("System created: clock differential too large"),
  GPS_RECEIVER_OFF("System created: GPS receiver off"),
  GPS_RECEIVER_UNLOCKED("System created: GPS receiver unlocked"),
  DATA_TIME_GPS_SYNCHRONIZATION_TIME_DELTA_OVER_THRESHOLD(
      "System created: data time - time of last GPS synchronization > threshold"),
  CLOCK_DIFFERENTIAL_OVER_THRESHOLD(
      "System created: clock differential in microseconds > threshold");

  private final String rationale;

  ChannelSohSubtype(String rationale) {
    this.rationale = rationale;
  }

  /**
   * Obtains a string representation of the ChannelSohSubtype literal suitable for use in a {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.dataobject.stationprocessing.QcMask}
   * rationale string
   *
   * @return string description of the literal status, not null
   */
  public String getRationale() {
    return rationale;
  }
}
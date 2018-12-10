package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.influx;

import java.time.Instant;
import java.util.UUID;

/**
 * A class to represent a single point in a waveform within the timeseries database.
 */
public class WaveformPoint implements Comparable<WaveformPoint> {

  public final double value;
  public final Instant time;
  public final UUID id;
  public final double sampleRate;

  public WaveformPoint(double value, Instant time, UUID segmentID, double sampleRate) {
    this.value = value;
    this.time = time;
    this.id = segmentID;
    this.sampleRate = sampleRate;
  }

  @Override
  public int compareTo(WaveformPoint o) {
    return this.time.compareTo(o.time);
  }
}

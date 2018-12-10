package gms.dataacquisition.cssloader.data;


import java.util.AbstractMap.SimpleEntry;
import java.util.Map;

/**
 * Contains format enumeration for waveforms and mappings from String to Format, Format to
 * WaveformReader.
 */
public class WaveformFormat {

  // Mapping from String format codes (CSS 3.0) to the corresponding Format enum.
  // This can't be done via the Enum name because some format codes have '#' in them (which can't be an enum entry).
  private static final Map<String, Format> formatStringLookup = Map.ofEntries(
      new SimpleEntry<>("a0", Format.A0),
      new SimpleEntry<>("b0", Format.B0),
      new SimpleEntry<>("c0", Format.C0),
      new SimpleEntry<>("a#", Format.A_Pound),
      new SimpleEntry<>("b#", Format.B_Pound),
      new SimpleEntry<>("c#", Format.C_Pound),
      new SimpleEntry<>("t4", Format.T4),
      new SimpleEntry<>("t8", Format.T8),
      new SimpleEntry<>("s4", Format.S4),
      new SimpleEntry<>("s2", Format.S2),
      new SimpleEntry<>("s3", Format.S3),
      new SimpleEntry<>("f4", Format.F4),
      new SimpleEntry<>("f8", Format.F8),
      new SimpleEntry<>("i4", Format.I4),
      new SimpleEntry<>("i2", Format.I2),
      new SimpleEntry<>("e1", Format.E1),
      new SimpleEntry<>("e#", Format.E_Pound),
      new SimpleEntry<>("g2", Format.G2));

  /**
   * Format enumeration.  Taken from CSS 3.0 schema.
   */
  public enum Format {
    A0, B0, C0, A_Pound, B_Pound, C_Pound, T4, T8,
    S4, S2, S3, F4, F8, I4, I2, E1, E_Pound, G2
  }

  /**
   * Looks up a Format enum, given the String format code (CSS 3.0).
   *
   * @param fc the format code, e.g. 's4' or 'b#'.
   * @return Format corresponding to the given format code, or null if the format code is unknown.
   */
  public static Format formatFor(String fc) {
    return formatStringLookup.get(fc);
  }
}

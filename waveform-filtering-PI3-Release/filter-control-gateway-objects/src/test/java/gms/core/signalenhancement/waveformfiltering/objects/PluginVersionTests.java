package gms.core.signalenhancement.waveformfiltering.objects;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PluginVersionTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testPluginVersionMajorNullThrowsException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error instantiating plugin version, major value cannot be null.");

    PluginVersion.from(null, 0, 0);
  }

  @Test
  public void testPluginVersionMinorNullThrowsException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error instantiating plugin version, minor value cannot be null.");

    PluginVersion.from(1, null, 0);
  }

  @Test
  public void testPluginVersionPatchNullThrowsException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error instantiating plugin version, patch value cannot be null.");

    PluginVersion.from(1, 0, null);
  }

  @Test
  public void testEqualsHashCode() {
    PluginVersion v1 = PluginVersion.from(1, 2, 3);
    PluginVersion v2 = PluginVersion.from(1, 2, 3);

    assertTrue(v1.equals(v2));
    assertTrue(v1.hashCode() == v2.hashCode());
  }

  @Test
  public void testEqualsExpectInequality() {
    assertFalse(PluginVersion.from(1, 2, 3).equals(PluginVersion.from(4, 2, 3)));
    assertFalse(PluginVersion.from(1, 2, 3).equals(PluginVersion.from(1, 4, 3)));
    assertFalse(PluginVersion.from(1, 2, 3).equals(PluginVersion.from(1, 2, 4)));
  }
}

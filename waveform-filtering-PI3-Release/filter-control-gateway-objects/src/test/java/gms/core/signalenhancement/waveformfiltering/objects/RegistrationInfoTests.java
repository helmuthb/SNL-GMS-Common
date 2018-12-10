package gms.core.signalenhancement.waveformfiltering.objects;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RegistrationInfoTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testFromNullNameExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Error instantiating RegistrationInfo, name cannot be null");
    RegistrationInfo.from(null, PluginVersion.from(1, 2, 3));
  }

  @Test
  public void testFromNullVersionExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Error instantiating RegistrationInfo, version cannot be null");
    RegistrationInfo.from("registeredName", null);
  }

  @Test
  public void testEqualsHashCode() {
    final String name = "registeredName";
    final PluginVersion version = PluginVersion.from(4, 3, 2);
    RegistrationInfo info1 = RegistrationInfo.from(name, version);
    RegistrationInfo info2 = RegistrationInfo.from(name, version);

    assertTrue(info1.equals(info2));
    assertTrue(info1.hashCode() == info2.hashCode());
  }

  @Test
  public void testEqualsExpectInequality() {
    final String name = "registeredName";
    final PluginVersion version = PluginVersion.from(4, 3, 2);
    final RegistrationInfo info1 = RegistrationInfo.from(name, version);

    RegistrationInfo info2 = RegistrationInfo.from("differentName", version);
    assertFalse(info1.equals(info2));

    info2 = RegistrationInfo.from(name, PluginVersion.from(9, 8, 7));
    assertFalse(info1.equals(info2));
  }
}

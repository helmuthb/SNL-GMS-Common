package gms.core.waveformqc.waveformqccontrol.config;

import com.netflix.config.validation.ValidationException;
import gms.core.waveformqc.waveformqccontrol.configuration.ConfigurationValidator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ServiceConfigurationValidatorTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testValidatePortTooLowValue() {
    exception.expect(ValidationException.class);
    exception.expectMessage("Port must be between 0 and 65535");

    ConfigurationValidator.validatePort(-1);
  }

  @Test
  public void testValidatePortTooHighValue() {
    exception.expect(ValidationException.class);
    exception.expectMessage("Port must be between 0 and 65535");

    ConfigurationValidator.validatePort(65536);
  }

  @Test
  public void testValidateMinThreadsTooLowValue() {
    exception.expect(ValidationException.class);
    exception.expectMessage("Minimum Threads must be greater than 0");

    ConfigurationValidator.validateMinThreads(-1, 5);
  }

  @Test
  public void testValidateMinThreadsHigherThanMaxThreads() {
    exception.expect(ValidationException.class);
    exception.expectMessage("Minimum Threads must be less than or equal to Maximum Threads");

    ConfigurationValidator.validateMinThreads(5, 4);
  }

  @Test
  public void testValidateMaxThreadsTooLowValue() {
    exception.expect(ValidationException.class);
    exception.expectMessage("Maximum Threads must be greater than or equal to 10");

    ConfigurationValidator.validateMaxThreads(9, 1);
  }

  @Test
  public void testValidateMaxThreadsHigherThanMinThreads() {
    exception.expect(ValidationException.class);
    exception.expectMessage("Minimum Threads must be less than or equal to Maximum Threads");

    ConfigurationValidator.validateMaxThreads(10, 11);
  }


}

package gms.core.waveformqc.waveformqccontrol.configuration;

import com.netflix.config.validation.ValidationException;

public class ConfigurationValidator {

  public static void validatePort(int port) {
    if (port < 0 || port > 65535) {
      throw new ValidationException("Port must be between 0 and 65535");
    }
  }

  public static void validateMinThreads(int minThreads, int maxThreads) {
    if (minThreads < 1) {
      throw new ValidationException("Minimum Threads must be greater than 0");
    }

    if (minThreads > maxThreads) {
      throw new ValidationException(
          "Minimum Threads must be less than or equal to Maximum Threads");
    }
  }

  public static void validateMaxThreads(int maxThreads, int minThreads) {
    if (maxThreads < 10) {
      throw new ValidationException("Maximum Threads must be greater than or equal to 10");
    }

    if (minThreads > maxThreads) {
      throw new ValidationException(
          "Minimum Threads must be less than or equal to Maximum Threads");
    }
  }

  public static void validateIdleTimeout(int idleTimeout) {
    if (idleTimeout < 1) {
      throw new ValidationException("Idle Timeout Millis must be greater than 0");
    }
  }
}

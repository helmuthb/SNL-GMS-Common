package gms.core.waveformqc.waveformqccontrol.plugin.util;

import java.util.Objects;
import java.util.function.Predicate;
import org.slf4j.Logger;

public class PluginUtility {

  /**
   * Private default constructor to prevent instantiation of this utility class
   */
  private PluginUtility() {
  }

  public static void validateArgument(Object argument, Predicate<Object> validator,
      Logger logger, String message) {
    if (!validator.test(argument)) {
      IllegalArgumentException e = new IllegalArgumentException(message);
      logger.error(message, e);
      throw e;
    }
  }

  public static void checkNotNull(Object checkedObject, Logger logger, String message) {
    validateArgument(checkedObject, Objects::nonNull, logger, message);
  }

}

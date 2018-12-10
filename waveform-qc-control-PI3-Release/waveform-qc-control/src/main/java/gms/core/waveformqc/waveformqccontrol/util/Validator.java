package gms.core.waveformqc.waveformqccontrol.util;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Parameter validation utility operations.
 */
public class Validator {

  /**
   * Private default constructor to prevent instantiation of this utility class
   */
  private Validator() {
  }

  /**
   * Validation method that requires the validator to return true given the input object, otherwise
   * logs the message and throws a IllegalArgumentException with that message.
   *
   * @param validator Predicate used to validate the input object.
   * @param object Input object to be validated.
   * @param exceptionMessage Message to log and throw in the event the object is invalid.
   * @param <T> Any class whose object needs to be validated.
   */
  public static <T> void requireArgumentTrue(Predicate<T> validator, T object,
      String exceptionMessage) {
    requireTrue(validator, object, ExceptionSupplier.illegalArgument(exceptionMessage));
  }

  /**
   * Validation method that requires the validator to return false given the input object, otherwise
   * logs the message and throws a RuntimeException with that message.
   *
   * @param validator Predicate used to validate the input object.
   * @param object Input object to be validated.
   * @param <T> Any class whose object needs to be validated.
   */
  public static <T> void requireFalse(Predicate<T> validator, T object,
      Supplier<? extends RuntimeException> exceptionSupplier) {
    requireTrue(validator.negate(), object, exceptionSupplier);
  }

  /**
   * Validation method that requires the validator to return true given the input object, otherwise
   * logs the message and throws a RuntimeException with that message.
   *
   * @param validator Predicate used to validate the input object.
   * @param object Input object to be validated.
   * @param <T> Any class whose object needs to be validated.
   */
  public static <T> void requireTrue(Predicate<T> validator, T object,
      Supplier<? extends RuntimeException> exceptionSupplier) {
    if (validator == null) {
      throw new IllegalArgumentException("Error validating, validator predicate cannot be null");
    }
    if (object == null) {
      throw new IllegalArgumentException("Error validating, object to validate cannot be null");
    }
    if (exceptionSupplier == null) {
      throw new IllegalArgumentException("Error validating, exception supplier cannot be null");
    }

    if (!validator.test(object)) {
      throw exceptionSupplier.get();
    }
  }

}

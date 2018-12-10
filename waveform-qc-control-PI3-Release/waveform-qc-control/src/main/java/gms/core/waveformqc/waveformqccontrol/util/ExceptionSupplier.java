package gms.core.waveformqc.waveformqccontrol.util;

import java.util.function.Supplier;

public class ExceptionSupplier {

  public static Supplier<IllegalStateException> illegalState(String message) {
    return () -> new IllegalStateException(message);
  }

  public static Supplier<NullPointerException> nullPointer(String message) {
    return () -> new NullPointerException(message);
  }

  public static Supplier<IllegalArgumentException> illegalArgument(String message) {
    return () -> new IllegalArgumentException(message);
  }


}

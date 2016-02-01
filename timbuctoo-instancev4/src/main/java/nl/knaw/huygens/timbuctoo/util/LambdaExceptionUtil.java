package nl.knaw.huygens.timbuctoo.util;

import java.util.function.Consumer;
import java.util.function.Function;

public final class LambdaExceptionUtil {

  @FunctionalInterface
  public interface Consumer_WithExceptions<T, E extends Exception> {
    void accept(T val) throws E;
  }

  @FunctionalInterface
  public interface Function_WithExceptions<T, R, E extends Exception> {
    R apply(T val) throws E;
  }

  public static <T, E extends Exception> Consumer<T> rethrowConsumer(Consumer_WithExceptions<T, E> consumer) throws E {
    return t -> {
      try {
        consumer.accept(t);
      } catch (Exception exception) {
        throwActualException(exception);
      }
    };
  }

  public static <T, R, E extends Exception> Function<T, R> rethrowFunction(Function_WithExceptions<T, R, E> function)
    throws E  {
    return t -> {
      try {
        return function.apply(t);
      } catch (Exception exception) {
        throwActualException(exception);
        return null;
      }
    };
  }

  @SuppressWarnings("unchecked")
  private static <E extends Exception> void throwActualException(Exception exception) throws E {
    throw (E) exception;
  }

}

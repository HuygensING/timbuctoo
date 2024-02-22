package nl.knaw.huygens.timbuctoo.util;

public record Tuple<T, U>(T left, U right) {
  public static <T, U> Tuple<T, U> tuple(T left, U right) {
    return new Tuple<>(left, right);
  }
}

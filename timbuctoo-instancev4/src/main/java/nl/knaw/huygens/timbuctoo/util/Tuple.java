package nl.knaw.huygens.timbuctoo.util;

public class Tuple<T, U> {
  public final T left;
  private final U right;

  public Tuple(T left, U right) {
    this.left = left;
    this.right = right;
  }

  public T getLeft() {
    return left;
  }

  public U getRight() {
    return right;
  }
}

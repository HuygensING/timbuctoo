package nl.knaw.huygens.timbuctoo.util;

import java.util.function.Function;

public class Either<L, R> {
  private final L left;
  private final R right;

  public Either(L left, R right) {
    this.left = left;
    this.right = right;
  }

  public static <L, R> Either<L, R> right(R right) {
    return new Either<>(null, right);
  }

  public static <L, R> Either<L, R> left(L left) {
    return new Either<>(left, null);
  }

  public L getLeft() {
    return left;
  }

  public R getRight() {
    return right;
  }

  public <U> Either<L, U> flatMap(Function<? super R, ? extends Either<L, ? extends U>> mapper) {
    if (right != null) {
      return (Either<L, U>) mapper.apply(right);
    } else {
      return (Either<L, U>) this;
    }
  }

  public <U> Either<L, U> map(Function<? super R, ? extends U> mapper) {
    if (right != null) {
      return Either.right(mapper.apply(right));
    } else {
      return (Either<L, U>) this;
    }
  }
}

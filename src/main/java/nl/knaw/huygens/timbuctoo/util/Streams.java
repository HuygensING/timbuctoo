package nl.knaw.huygens.timbuctoo.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Streams {
  public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    Set<Object> seen = ConcurrentHashMap.newKeySet();
    return t -> seen.add(keyExtractor.apply(t));
  }

  public static <R> Stream<Set<R>> combine(Stream<R> stream, BiPredicate<R, R> shouldCombinePred) {
    return combine(stream, shouldCombinePred, HashSet::new);
  }

  public static <R, C extends Collection<R>> Stream<C> combine(
      Stream<R> stream, BiPredicate<R, R> shouldCombinePred, Supplier<C> init) {
    final Spliterator<R> sp = stream.spliterator();
    final Spliterator<C> combineSp = new AbstractSpliterator<>(sp.estimateSize(), sp.characteristics()) {
      private R prev = null;
      private R cur = null;

      @Override
      public boolean tryAdvance(Consumer<? super C> action) {
        if (prev == null && !sp.tryAdvance(el -> prev = el)) {
          return false;
        }

        C combined = init.get();
        combined.add(prev);

        boolean canAdvance;
        boolean shouldCombine;
        do {
          canAdvance = sp.tryAdvance(el -> cur = el);
          shouldCombine = shouldCombinePred.test(prev, cur);
          if (canAdvance && shouldCombine) {
            combined.add(cur);
          }

          prev = cur;
        }
        while (canAdvance && shouldCombine);

        action.accept(combined);
        return canAdvance;
      }
    };

    return StreamSupport.stream(combineSp, false).onClose(stream::close);
  }
}

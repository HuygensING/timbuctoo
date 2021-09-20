package nl.knaw.huygens.timbuctoo.util;

import java.util.HashSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Streams {
  public static <R> Stream<Set<R>> combine(Stream<R> stream, BiPredicate<R, R> shouldCombinePred) {
    final Spliterator<R> sp = stream.spliterator();
    final Spliterator<Set<R>> combineSp = new AbstractSpliterator<>(sp.estimateSize(), sp.characteristics()) {
      private R prev = null;
      private R cur = null;

      @Override
      public boolean tryAdvance(Consumer<? super Set<R>> action) {
        if (prev == null && !sp.tryAdvance(el -> prev = el)) {
          return false;
        }

        Set<R> combined = new HashSet<>();
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

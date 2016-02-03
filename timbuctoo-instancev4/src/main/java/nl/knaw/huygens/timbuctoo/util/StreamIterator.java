package nl.knaw.huygens.timbuctoo.util;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamIterator {
  public static <S> Stream<S> stream(Iterator<S> input) {
    Iterable<S> it = () -> input;
    return StreamSupport.stream(it.spliterator(), false);
  }
}

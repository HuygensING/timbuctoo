package nl.knaw.huygens.timbuctoo.util;

import nl.knaw.huygens.timbuctoo.v5.util.AutoCloseableIterator;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterators.spliteratorUnknownSize;

public class StreamIterator {
  public static <S> Stream<S> stream(Iterator<S> input) {
    return StreamSupport.stream(spliteratorUnknownSize(input, 0), false);
  }

  public static <S> Stream<S> stream(AutoCloseableIterator<S> input) {
    return StreamSupport.stream(spliteratorUnknownSize(input, 0), false).onClose(input::close);
  }

}

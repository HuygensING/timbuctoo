package nl.knaw.huygens.timbuctoo.v5.util;

import java.util.Iterator;

public interface AutoCloseableIterator<T> extends Iterator<T>, AutoCloseable {
  @Override
  void close();
}

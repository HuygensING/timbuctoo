package nl.knaw.huygens.timbuctoo.v5.datastores.triples;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadHandler;
import nl.knaw.huygens.timbuctoo.v5.util.AutoCloseableIterator;

public interface TripleStore extends QuadHandler, AutoCloseable {
  AutoCloseableIterator<String[]> getTriples();

  AutoCloseableIterator<String[]> getTriples(String subject, String predicate);
}

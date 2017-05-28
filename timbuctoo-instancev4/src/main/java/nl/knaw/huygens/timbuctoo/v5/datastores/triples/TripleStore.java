package nl.knaw.huygens.timbuctoo.v5.datastores.triples;

import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.util.AutoCloseableIterator;

public interface TripleStore extends RdfProcessor, AutoCloseable {
  AutoCloseableIterator<String[]> getTriples();

  AutoCloseableIterator<String[]> getTriples(String subject, String predicate);
}

package nl.knaw.huygens.timbuctoo.v5.datastores.triples;

import nl.knaw.huygens.timbuctoo.v5.datastores.DataStore;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadHandler;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadLoader;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.util.AutoCloseableIterator;

public interface TripleStore extends AutoCloseable, DataStore<QuadLoader> {
  void getTriples(QuadHandler handler) throws LogProcessingFailedException;

  AutoCloseableIterator<String[]> getTriples(String subject, String predicate);
}

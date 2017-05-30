package nl.knaw.huygens.timbuctoo.v5.datastores.triples;

import nl.knaw.huygens.timbuctoo.v5.dataset.EntityProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;

import java.util.stream.Stream;

public interface TripleStore extends RdfProcessor, EntityProvider, AutoCloseable {
  Stream<String[]> getTriples();

  Stream<String[]> getTriples(String subject, String predicate);
}

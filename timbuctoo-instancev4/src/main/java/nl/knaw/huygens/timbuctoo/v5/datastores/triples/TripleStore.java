package nl.knaw.huygens.timbuctoo.v5.datastores.triples;

import nl.knaw.huygens.timbuctoo.v5.dataset.EntityProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.dto.Quad;

import java.util.stream.Stream;

public interface TripleStore extends RdfProcessor, EntityProvider, AutoCloseable {
  Stream<Quad> getQuads();

  Stream<Quad> getQuads(String subject, String predicate);
}

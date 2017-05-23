package nl.knaw.huygens.timbuctoo.v5.datastores.triples;

import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.datastores.DataStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadHandler;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadLoader;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;

import java.util.Optional;
import java.util.stream.Stream;

public interface TripleStore extends AutoCloseable, DataStore<QuadLoader> {
  void getQuads(QuadHandler handler) throws LogProcessingFailedException;

  Stream<Quad> getQuads(String subject);

  Stream<Quad> getQuads(String subject, String predicate);

  Stream<Tuple<String, Quad>> getQuadsWithoutGraph(String subject, String predicate, boolean ascending);

  Stream<Tuple<String, Quad>> getQuadsWithoutGraph(String marker, String subject, String predicate, boolean ascending);

  Optional<Quad> getFirst(String subject, String predicate);
}

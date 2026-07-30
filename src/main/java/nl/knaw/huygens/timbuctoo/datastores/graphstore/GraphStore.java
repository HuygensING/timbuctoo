package nl.knaw.huygens.timbuctoo.datastores.graphstore;

import nl.knaw.huygens.timbuctoo.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.CursorUri;

import java.util.stream.Stream;

public interface GraphStore {
  void put(String graph, String subject) throws RdfProcessingFailedException;

  void delete(String graph, String subject) throws RdfProcessingFailedException;

  Stream<String> ofGraph(String graph);

  Stream<CursorUri> getGraphs(String cursor);

  void close();

  void commit();

  void start();

  boolean isClean();

  void empty();
}

package nl.knaw.huygens.timbuctoo.datastores.defaultresourcesstore;

import nl.knaw.huygens.timbuctoo.dataset.OptimizedPatchListener;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.CursorUri;

import java.util.stream.Stream;

public interface DefaultResourcesStore extends OptimizedPatchListener {
  void putIri(String iri) throws RdfProcessingFailedException;

  void deleteIri(String iri) throws RdfProcessingFailedException;

  Stream<CursorUri> getDefaultResources(String cursor);

  void close();

  void commit();

  boolean isClean();

  void empty();
}

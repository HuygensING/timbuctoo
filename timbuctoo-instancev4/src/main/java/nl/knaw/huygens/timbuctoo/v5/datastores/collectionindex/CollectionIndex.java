package nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex;

import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;

import java.util.stream.Stream;

public interface CollectionIndex extends RdfProcessor, AutoCloseable {
  Stream<String> getSubjects(String collectionName);
}

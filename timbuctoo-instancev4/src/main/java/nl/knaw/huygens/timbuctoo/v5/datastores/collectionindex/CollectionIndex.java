package nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex;

import nl.knaw.huygens.timbuctoo.v5.datastores.DataStore;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadLoader;

import java.util.stream.Stream;

public interface CollectionIndex extends AutoCloseable, DataStore<QuadLoader> {
  Stream<String> getSubjects(String collectionName);
}

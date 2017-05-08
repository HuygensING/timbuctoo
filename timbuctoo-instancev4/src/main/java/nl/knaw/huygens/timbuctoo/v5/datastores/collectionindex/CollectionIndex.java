package nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex;

import nl.knaw.huygens.timbuctoo.v5.datastores.DataStore;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadLoader;
import nl.knaw.huygens.timbuctoo.v5.util.AutoCloseableIterator;

public interface CollectionIndex extends AutoCloseable, DataStore<QuadLoader> {
  AutoCloseableIterator<String> getSubjects(String collectionName);
}

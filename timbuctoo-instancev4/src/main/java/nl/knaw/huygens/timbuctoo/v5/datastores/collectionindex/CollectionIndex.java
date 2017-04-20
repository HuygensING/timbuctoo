package nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadHandler;
import nl.knaw.huygens.timbuctoo.v5.util.AutoCloseableIterator;

public interface CollectionIndex extends QuadHandler, AutoCloseable {
  AutoCloseableIterator<String> getSubjects(String collectionName);
}

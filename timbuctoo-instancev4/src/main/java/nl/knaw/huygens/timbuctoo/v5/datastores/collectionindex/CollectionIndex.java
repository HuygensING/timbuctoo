package nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex;

import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.util.AutoCloseableIterator;

public interface CollectionIndex extends RdfProcessor, AutoCloseable {
  AutoCloseableIterator<String> getSubjects(String collectionName);
}

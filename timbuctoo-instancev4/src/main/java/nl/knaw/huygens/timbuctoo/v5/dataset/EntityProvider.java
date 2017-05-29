package nl.knaw.huygens.timbuctoo.v5.dataset;

import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;

public interface EntityProvider {
  void processEntities(String cursor, EntityProcessor processor) throws RdfProcessingFailedException;
}

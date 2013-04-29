package nl.knaw.huygens.repository.storage;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.repository.model.VariationDocument;

public interface VariationStorage extends Storage {

  <T extends VariationDocument> List<T> getAllVariations(Class<T> type, String id) throws IOException;

  /**
   * Get the given variation of a document.
   * @param type
   * @param id
   * @param variation
   * @return
   * @throws IOException 
   */
  <T extends VariationDocument> T getVariation(Class<T> type, String id, String variation) throws IOException;
}

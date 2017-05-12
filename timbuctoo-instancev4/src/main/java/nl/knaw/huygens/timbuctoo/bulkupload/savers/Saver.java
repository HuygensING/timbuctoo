package nl.knaw.huygens.timbuctoo.bulkupload.savers;

import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ImportProperty;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ImportPropertyDescriptions;

import java.util.List;

public interface Saver<T> {
  T addEntity(T collection, List<ImportProperty> currentProperties);

  T addCollection(String collectionName);

  void addPropertyDescriptions(T collection, ImportPropertyDescriptions importPropertyDescriptions);
}

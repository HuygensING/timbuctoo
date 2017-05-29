package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.google.common.collect.ListMultimap;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;

public interface EntityProcessor {
  void start();

  void processEntity(String cursor, String subject, ListMultimap<String, PredicateData> addedPredicates) throws
    RdfProcessingFailedException;

  void finish();
}

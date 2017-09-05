package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.google.common.collect.ListMultimap;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PredicateData;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;

import java.util.Map;

public interface EntityProcessor {
  void start(int index);

  void processEntity(String cursor, String subject, ListMultimap<String, PredicateData> addedPredicates,
                     Map<String, Boolean> inverseLists) throws RdfProcessingFailedException;

  int getCurrentVersion();

  void finish();
}

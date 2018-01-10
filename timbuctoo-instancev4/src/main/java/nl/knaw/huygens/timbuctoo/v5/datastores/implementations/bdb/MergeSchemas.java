package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Predicate;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Type;

import java.util.HashMap;
import java.util.Map;

public class MergeSchemas {

  public MergeSchemas() {

  }

  public Map<String, Type> mergeSchema(Map<String, Type> generatedSchema, Map<String, Type> customSchema) {
    Map<String, Type> mergedSchema = new HashMap<>();
    mergedSchema.putAll(generatedSchema);

    MergePredicates mergePredicates = new MergePredicates();

    for (Map.Entry<String, Type> entry : customSchema.entrySet()) {
      if (mergedSchema.containsKey(entry.getKey())) {
        Type type = mergedSchema.get(entry.getKey());
        for (Predicate predicate : entry.getValue().getPredicates()) {
          type.getOrCreatePredicate(predicate.getName(), predicate.getDirection());
          Predicate generatedPredicate = mergedSchema.get(entry.getKey()).getPredicate(predicate.getName(),
            predicate.getDirection());
          Predicate mergedPredicate = mergePredicates.merge(predicate, generatedPredicate);
        }
      } else {
        mergedSchema.put(entry.getKey(), entry.getValue());
      }
    }

    return mergedSchema;
  }
}

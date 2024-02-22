package nl.knaw.huygens.timbuctoo.graphql.customschema;

import nl.knaw.huygens.timbuctoo.datastores.schemastore.dto.Predicate;
import nl.knaw.huygens.timbuctoo.datastores.schemastore.dto.Type;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class MergeSchemas {
  public Map<String, Type> mergeSchema(Map<String, Type> generatedSchema, Map<String, Type> customSchema) {
    for (Map.Entry<String, Type> entry : customSchema.entrySet()) {
      Collection<Predicate> mergedPredicates = new HashSet<>();

      if (generatedSchema.get(entry.getKey()) != null) {
        for (Predicate customPredicate : entry.getValue().getPredicates()) {
          if (generatedSchema.get(entry.getKey())
            .getPredicate(customPredicate.getName(), customPredicate.getDirection()) != null) {
            Predicate generatedPredicate = generatedSchema.get(entry.getKey())
              .getPredicate(customPredicate.getName(), customPredicate.getDirection());
            Predicate mergedPredicate = generatedPredicate.merge(customPredicate);
            mergedPredicate.setIsExplicit(true);
            mergedPredicates.add(mergedPredicate);
          } else {
            mergedPredicates.add(customPredicate);
          }
        }
        generatedSchema.get(entry.getKey()).setPredicates(mergedPredicates);
      } else {
        generatedSchema.put(entry.getKey(), entry.getValue());
      }
    }

    return generatedSchema;
  }
}

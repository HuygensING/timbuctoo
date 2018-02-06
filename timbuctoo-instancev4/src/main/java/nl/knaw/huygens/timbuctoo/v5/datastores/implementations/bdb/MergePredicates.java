package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Predicate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MergePredicates {
  public Predicate merge(Predicate predicate1, Predicate predicate2) {
    if (predicate1.getOwner() == null || predicate2.getOwner() == null) {
      throw new IllegalArgumentException("Predicate owner missing");
    }
    if (!Objects.equals(predicate1.getName(), predicate2.getName()) ||
      predicate1.getDirection() != predicate2.getDirection() ||
      !Objects.equals(predicate1.getOwner().getName(), predicate2.getOwner().getName())) {
      throw new IllegalArgumentException("Predicate name, direction and/or owner do not match");
    }
    Predicate mergedPredicate = new Predicate(predicate1.getName(), predicate1.getDirection());

    mergedPredicate.setOwner(predicate1.getOwner());

    mergedPredicate.setHasBeenList(predicate1.hasBeenList() || predicate2.hasBeenList());

    mergedPredicate.setHasBeenSingular(predicate1.isHasBeenSingular() || predicate2.isHasBeenSingular());

    mergedPredicate.setSubjectsWithThisPredicateAsList(predicate1.getSubjectsWithThisPredicateAsList() +
      predicate2.getSubjectsWithThisPredicateAsList());

    //Handle References
    Map<String, Long> mergedReferences = new HashMap<>();
    for (Map.Entry<String, Long> entry : predicate1.getReferenceTypes().entrySet()) {
      mergedReferences.put(entry.getKey(), entry.getValue());
    }
    mergedPredicate.setReferenceTypes(mergedReferences);
    for (Map.Entry<String, Long> entry : predicate2.getReferenceTypes().entrySet()) {
      mergedPredicate.incReferenceType(entry.getKey(), entry.getValue());
    }

    //Handle Values
    Map<String, Long> mergedValues = new HashMap<>();
    for (Map.Entry<String, Long> entry : predicate1.getValueTypes().entrySet()) {
      mergedValues.put(entry.getKey(), entry.getValue());
    }
    mergedPredicate.setValueTypes(mergedValues);
    for (Map.Entry<String, Long> entry : predicate2.getValueTypes().entrySet()) {
      mergedPredicate.incValueType(entry.getKey(), entry.getValue());
    }

    return mergedPredicate;

  }
}

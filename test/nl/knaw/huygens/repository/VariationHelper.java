package nl.knaw.huygens.repository;

import java.util.List;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.Reference;

import com.google.common.collect.Lists;

public class VariationHelper {
  public static List<Reference> createVariationsForType(Class<? extends Document> type, String id, String... variations) {
    List<Reference> variationReferences = Lists.<Reference> newArrayList();

    for (String variation : variations) {
      variationReferences.add(new Reference(type, id, variation));
    }

    return variationReferences;

  }
}

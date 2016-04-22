package nl.knaw.huygens.timbuctoo.search.description.indexes;


import nl.knaw.huygens.timbuctoo.search.description.IndexDescription;

import java.util.ArrayList;
import java.util.List;

class PersonIndexDescription implements IndexDescription {

  private static final String SORT_POSTFIX = "sort";

  private static final String[] SORT_FIELDS = {
    "names",
    "deathDate",
    "birthDate",
    "modified"
  };

  @Override
  public List<String> getSortIndexPropertyNames(List<String> vertexTypes) {
    List<String> fieldList = new ArrayList<>();
    for (String type : vertexTypes) {
      for (String field : SORT_FIELDS) {
        fieldList.add(String.format("%s_%s_%s", type, field, SORT_POSTFIX));
      }
    }
    return fieldList;
  }
}

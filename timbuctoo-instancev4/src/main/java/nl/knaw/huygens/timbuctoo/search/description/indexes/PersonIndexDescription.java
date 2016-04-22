package nl.knaw.huygens.timbuctoo.search.description.indexes;


import nl.knaw.huygens.timbuctoo.search.description.IndexDescription;

import java.util.ArrayList;
import java.util.List;

public class PersonIndexDescription implements IndexDescription {

  private static final String[] SORT_FIELD_FORMATS = {
    "%s_names_sort",
    "%s_deathDate_sort",
    "%s_birthDate_sort",
    "%s_modified_sort"
  };

  @Override
  public List<String> getSortIndexes(List<String> vertexTypes) {
    List<String> fieldList = new ArrayList<>();
    for (String type : vertexTypes) {
      for (String fieldFormat : SORT_FIELD_FORMATS) {
        fieldList.add(String.format(fieldFormat, type));
      }
    }
    return fieldList;
  }
}

package nl.knaw.huygens.timbuctoo.search.description;


import nl.knaw.huygens.timbuctoo.search.SearchDescription;

import java.util.Objects;

public class SearchDescriptionFactory {
  public SearchDescription create(String entityName) {
    if (Objects.equals(entityName, "wwperson")) {
      return new WwPersonSearchDescription();
    } else if (Objects.equals(entityName, "wwdocument")) {
      return new WwDocumentSearchDescription();
    }
    throw new IllegalArgumentException(String.format("No SearchDescription for '%s'", entityName));
  }
}

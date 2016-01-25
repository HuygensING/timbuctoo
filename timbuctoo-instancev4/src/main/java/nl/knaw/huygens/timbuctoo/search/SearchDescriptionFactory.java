package nl.knaw.huygens.timbuctoo.search;


import java.util.Objects;

public class SearchDescriptionFactory {
  public SearchDescription create(String entityName) {
    if (Objects.equals(entityName, "wwperson")) {
      return new WwPersonSearchDescription();
    }
    throw new IllegalArgumentException(String.format("No SearchDescription for '%s'", entityName));
  }
}

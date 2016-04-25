package nl.knaw.huygens.timbuctoo.search.description.indexes;

import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;

public interface IndexerSortFieldDescription {
  String getSortPropertyName();

  String getPropertyName();

  PropertyParser getParser();

  Comparable<?> getDefaultValue();
}

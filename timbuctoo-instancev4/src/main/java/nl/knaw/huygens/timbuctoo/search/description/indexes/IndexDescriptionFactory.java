package nl.knaw.huygens.timbuctoo.search.description.indexes;

import nl.knaw.huygens.timbuctoo.search.description.IndexDescription;

import java.util.List;
import java.util.Optional;

public class IndexDescriptionFactory {

  public Optional<IndexDescription> create(List<String> types) {
    if (types.contains("person")) {
      return Optional.of(new PersonIndexDescription(types));
    }
    return Optional.empty();
  }
}

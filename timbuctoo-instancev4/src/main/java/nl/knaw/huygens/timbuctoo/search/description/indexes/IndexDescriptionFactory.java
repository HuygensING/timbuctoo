package nl.knaw.huygens.timbuctoo.search.description.indexes;

import nl.knaw.huygens.timbuctoo.search.description.IndexDescription;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class IndexDescriptionFactory {

  public IndexDescription create(String type) {
    if (type.equals("wwperson")) {
      return new WwPersonIndexDescription();
    }
    return null;
  }

  public List<IndexDescription> getIndexersForTypes(List<String> types) {
    return types.stream().map(this::create).filter(type -> type != null).collect(toList());

  }
}

package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.language.Field;
import graphql.language.InlineFragment;
import graphql.language.Selection;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.BoundSubject;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class UnionDataFetcher implements DataFetcher {
  private final String predicate;
  private final boolean isList;
  private final String fieldName;
  private final Map<String, String> typeMappings;
  private final TripleStore tripleStore;

  public UnionDataFetcher(String predicate, boolean isList, String fieldName, Map<String, String> typeMappings,
                          TripleStore store) {
    this.predicate = predicate;
    this.isList = isList;
    this.fieldName = fieldName;
    this.typeMappings = typeMappings;
    this.tripleStore = store;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    if (environment.getSource() instanceof BoundSubject) {
      Set<String> requestedTypes = new HashSet<>();

      for (Field field : environment.getFields()) {
        if (field.getName().equals(fieldName)) {
          for (Selection selection : field.getSelectionSet().getSelections()) {
            if (selection instanceof InlineFragment) {
              InlineFragment fragment = (InlineFragment) selection;
              String typeUri = typeMappings.get(fragment.getTypeCondition().getName());
              requestedTypes.add(typeUri);
            }
          }
        }
      }
      BoundSubject source = environment.getSource();
      try (Stream<String[]> triples = tripleStore.getTriples(source.getValue(), predicate)) {
        if (isList) {
          return triples
            .map(triple -> makeItem(requestedTypes, triple))
            .filter(Objects::nonNull)
            .limit(20)
            .collect(toList());
        } else {
          return triples.findFirst()
            .map(triple -> makeItem(requestedTypes, triple))
            .orElse(null);
        }
      }
    } else {
      throw new IllegalStateException("Source is not a BoundSubject");
    }
  }

  private BoundSubject makeItem(Set<String> requestedTypes, String[] triple) {
    if (triple[3] == null) {
      return getTypes(triple[2], requestedTypes);
    } else {
      return verifyType(triple[2], triple[3], requestedTypes);
    }
  }

  private BoundSubject verifyType(String value, String type, Set<String> requestedTypes) {
    if (requestedTypes.contains(type)) {
      return new BoundSubject(value, type);
    } else {
      return null;
    }
  }

  private BoundSubject getTypes(String uri, Set<String> requestedTypes) {
    Set<String> types;
    try (Stream<String[]> triples = tripleStore.getTriples(uri, RDF_TYPE)) {
      types = triples
        .map(triple -> triple[2])
        .collect(Collectors.toSet());
    }
    for (String requestedType : requestedTypes) {
      if (types.contains(requestedType)) {
        return new BoundSubject(uri, requestedType);
      }
    }

    return null;
  }


}

package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.language.Field;
import graphql.language.InlineFragment;
import graphql.language.Selection;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.BoundSubject;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
      try (Stream<Quad> quads = tripleStore.getQuads(source.getValue(), predicate)) {
        Stream<BoundSubject> boundSubjects = quads
          .map(quad -> {
            if (quad.getValuetype().isPresent()) {
              return verifyType(quad.getObject(), quad.getValuetype().get(), requestedTypes);
            } else {
              return getTypes(quad.getObject(), requestedTypes);
            }
          });
        if (isList) {
          return boundSubjects
            .filter(Objects::nonNull)
            .limit(20)
            .collect(toList());
        } else {
          return boundSubjects
            .findFirst()
            .orElse(null);
        }
      }
    } else {
      throw new IllegalStateException("Source is not a BoundSubject");
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
    try (Stream<Quad> quads = tripleStore.getQuads(uri, RDF_TYPE)) {
      return quads
        .map(Quad::getObject)
        .filter(requestedTypes::contains)
        .map(type -> new BoundSubject(uri, type))
        .findFirst()
        .orElse(null);
    }
  }


}

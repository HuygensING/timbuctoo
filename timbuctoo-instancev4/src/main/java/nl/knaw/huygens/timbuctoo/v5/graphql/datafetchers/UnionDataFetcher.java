package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.language.Field;
import graphql.language.InlineFragment;
import graphql.language.Selection;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.util.AutoCloseableIterator;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.BoundSubject;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
      try (AutoCloseableIterator<String[]> triples = tripleStore.getTriples(source.getValue(), predicate)) {
        if (isList) {
          List<BoundSubject> result = new ArrayList<>();
          int count = 0;
          while (count++ < 20 && triples.hasNext()) {
            String[] triple = triples.next();
            BoundSubject typedSubject;
            if (triple[3] == null) {
              typedSubject = getTypes(triple[2], requestedTypes);
            } else {
              typedSubject = verifyType(triple[2], triple[3], requestedTypes);
            }
            if (typedSubject != null) {
              result.add(typedSubject);
            }
          }
          return result;
        } else {
          if (triples.hasNext()) {
            return getTypes(triples.next()[2], requestedTypes);
          } else {
            return null;
          }
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
    Set<String> types = new HashSet<>();
    try (AutoCloseableIterator<String[]> triples = tripleStore.getTriples(uri, RDF_TYPE)) {
      while (triples.hasNext()) {
        types.add(triples.next()[2]);
      }
    }
    for (String requestedType : requestedTypes) {
      if (types.contains(requestedType)) {
        return new BoundSubject(uri, requestedType);
      }
    }

    return null;
  }


}

package nl.knaw.huygens.timbuctoo.v5.graphql.entity;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeReference;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Predicate;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Type;
import nl.knaw.huygens.timbuctoo.v5.graphql.GraphQlTypesContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

public class DerivedSchemaTypeGenerator {

  public void makeGraphQlTypes(Map<String, Type> types, TypeNameStore typeNameStore,
                               GraphQlTypesContainer typesContainer) {
    for (Type type : types.values()) {
      List<GraphQLFieldDefinition> fieldDefinitions = type.getPredicates().stream()
        .map(predicate -> fieldForDerivedType(
          predicate,
          typesContainer,
          typeNameStore
        ))
        .filter(Objects::nonNull)
        .collect(toList());

      String typeUri = type.getName();
      typesContainer.objectType(typeNameStore.makeGraphQlname(typeUri), Optional.of(typeUri), fieldDefinitions);
    }
  }

  private static GraphQLFieldDefinition fieldForDerivedType(Predicate pred, GraphQlTypesContainer typesContainer,
                                                            TypeNameStore typeNameStore) {
    String fieldName = typeNameStore.makeGraphQlnameForPredicate(pred.getName(), pred.getDirection());
    if (pred.getReferenceTypes().size() == 0) {
      if (pred.getValueTypes().size() == 0) {
        System.out.println("This shouldn't happen! Typetracker has no types");
        return null;
      } else if (pred.getValueTypes().size() == 1) {
        return typesContainer.valueField(
          fieldName, pred.getValueTypes().iterator().next(), pred.isList(), pred.isOptional(), pred.getName()
        );
      } else {
        List<GraphQLObjectType> types = new ArrayList<>();
        for (String valueType : pred.getValueTypes()) {
          types.add(typesContainer.valueType(valueType));
        }
        ArrayList<GraphQLTypeReference> refs = newArrayList();
        return typesContainer.unionField(
          fieldName, refs, types, pred.getName(), pred.getDirection(), pred.isOptional(), pred.isList()
        );
      }
    } else {
      if (pred.getReferenceTypes().size() == 1 && pred.getValueTypes().size() == 0) {
        return typesContainer.objectField(
          fieldName,
          pred.getName(),
          pred.getDirection(),
          typeNameStore.makeGraphQlname(pred.getReferenceTypes().iterator().next()),
          pred.isList(),
          pred.isOptional()
        );
      } else {
        List<GraphQLTypeReference> refs = new ArrayList<>();
        List<GraphQLObjectType> values = new ArrayList<>();
        for (String referenceType : pred.getReferenceTypes()) {
          refs.add(new GraphQLTypeReference(typeNameStore.makeGraphQlname(referenceType)));
        }
        for (String valueType : pred.getValueTypes()) {
          values.add(typesContainer.valueType(valueType));
        }

        return typesContainer
          .unionField(fieldName, refs, values, pred.getName(), pred.getDirection(), pred.isOptional(), pred.isList());
      }
    }
  }


}

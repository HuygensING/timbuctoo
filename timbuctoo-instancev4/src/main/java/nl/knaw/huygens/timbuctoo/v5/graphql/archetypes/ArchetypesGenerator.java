package nl.knaw.huygens.timbuctoo.v5.graphql.archetypes;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import nl.knaw.huygens.timbuctoo.v5.graphql.archetypes.dto.Archetype;
import nl.knaw.huygens.timbuctoo.v5.graphql.archetypes.dto.Archetypes;
import nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema.GraphQlTypesContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

public class ArchetypesGenerator {

  public Map<String, GraphQLObjectType> makeGraphQlTypes(Archetypes archetypes, GraphQlTypesContainer typesContainer) {
    Map<String, GraphQLObjectType> result = new HashMap<>();

    for (Archetype archetype : archetypes.getArchetypes()) {
      List<GraphQLFieldDefinition> fieldDefinitions = archetype.getPredicateDefinitions().stream()
        .map(predicateDefinition -> {
          if (predicateDefinition.getReference().isPresent()) {
            return typesContainer.objectField(
              predicateDefinition.getName(),
              predicateDefinition.getDescription(),
              predicateDefinition.getPredicateUri(),
              predicateDefinition.getReference().get().getDirection(),
              predicateDefinition.getReference().get().getTargetArchetype(),
              predicateDefinition.isList(),
              true
            );
          } else {
            return typesContainer.valueField(
              predicateDefinition.getName(),
              predicateDefinition.getDescription(),
              predicateDefinition.isList(),
              true,
              predicateDefinition.getPredicateUri()
            );
          }
        })
        .collect(toList());
      result.put(
        archetype.getRequiredTypeUri(),
        typesContainer.objectType(archetype.getName(), archetype.getDescription(), empty(), fieldDefinitions)
      );
    }
    return result;
  }


}

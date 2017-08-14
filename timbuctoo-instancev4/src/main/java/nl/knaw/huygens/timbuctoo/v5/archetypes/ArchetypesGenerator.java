package nl.knaw.huygens.timbuctoo.v5.archetypes;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import nl.knaw.huygens.timbuctoo.v5.archetypes.dto.Archetype;
import nl.knaw.huygens.timbuctoo.v5.archetypes.dto.Archetypes;
import nl.knaw.huygens.timbuctoo.v5.graphql.GraphQlTypesContainer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

public class ArchetypesGenerator {

  public Set<GraphQLObjectType> makeGraphQlTypes(Archetypes archetypes, GraphQlTypesContainer typesContainer) {
    Set<GraphQLObjectType> result = new HashSet<>();

    for (Archetype archetype : archetypes.getArchetypes()) {
      List<GraphQLFieldDefinition> fieldDefinitions = archetype.getPredicateDefinitions().stream()
        .map(predicateDefinition -> {
          if (predicateDefinition.getReference().isPresent()) {
            return typesContainer.objectField(
              predicateDefinition.getName(),
              predicateDefinition.getPredicateUri(),
              predicateDefinition.getReference().get().getDirection(),
              predicateDefinition.getReference().get().getTargetArchetype(),
              predicateDefinition.isList(),
              true
            );
          } else {
            return typesContainer.valueField(
              predicateDefinition.getName(),
              predicateDefinition.isList(),
              true,
              predicateDefinition.getPredicateUri()
            );
          }
        })
        .collect(toList());
      result.add(typesContainer.objectType(archetype.getName(), empty(), fieldDefinitions));
    }
    return result;
  }


}

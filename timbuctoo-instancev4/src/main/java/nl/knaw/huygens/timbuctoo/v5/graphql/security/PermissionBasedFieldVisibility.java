package nl.knaw.huygens.timbuctoo.v5.graphql.security;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.visibility.GraphqlFieldVisibility;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PermissionBasedFieldVisibility implements GraphqlFieldVisibility {
  private final Set<String> dataSetNamesWithOutReadPermission;

  public PermissionBasedFieldVisibility(UserPermissionCheck userPermissionCheck, DataSetRepository dataSetRepository) {
    dataSetNamesWithOutReadPermission = dataSetRepository
        .getDataSets()
        .stream()
        .filter(dataSet -> !userPermissionCheck.hasPermission(dataSet.getMetadata(), Permission.READ))
        .map(dataSet -> dataSet.getMetadata().getCombinedId())
        .collect(Collectors.toSet());
  }

  @Override
  public List<GraphQLFieldDefinition> getFieldDefinitions(GraphQLFieldsContainer fieldsContainer) {
    List<GraphQLFieldDefinition> graphQlFieldDefinitions = new ArrayList<>();
    for (GraphQLFieldDefinition graphQlFieldDefinition : fieldsContainer.getFieldDefinitions()) {
      if (!dataSetNamesWithOutReadPermission.contains(graphQlFieldDefinition.getName())) {
        graphQlFieldDefinitions.add(graphQlFieldDefinition);
      }
    }
    return graphQlFieldDefinitions;
  }

  @Override
  public GraphQLFieldDefinition getFieldDefinition(GraphQLFieldsContainer fieldsContainer, String fieldName) {
    for (GraphQLFieldDefinition graphQlFieldDefinition : fieldsContainer.getFieldDefinitions()) {
      if (!dataSetNamesWithOutReadPermission.contains(graphQlFieldDefinition.getName()) &&
          graphQlFieldDefinition.getName().equals(fieldName)) {
        return graphQlFieldDefinition;
      }
    }
    return null;
  }
}

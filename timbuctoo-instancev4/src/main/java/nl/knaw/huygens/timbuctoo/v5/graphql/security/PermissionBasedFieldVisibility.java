package nl.knaw.huygens.timbuctoo.v5.graphql.security;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.visibility.GraphqlFieldVisibility;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PermissionBasedFieldVisibility implements GraphqlFieldVisibility {
  private final UserPermissionCheck userPermissionCheck;
  private final DataSetRepository dataSetRepository;

  public PermissionBasedFieldVisibility(UserPermissionCheck userPermissionCheck, DataSetRepository dataSetRepository) {
    this.userPermissionCheck = userPermissionCheck;
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public List<GraphQLFieldDefinition> getFieldDefinitions(GraphQLFieldsContainer fieldsContainer) {
    List<GraphQLFieldDefinition> graphQlFieldDefinitions = new ArrayList<>();

    Collection<DataSet> dataSets = dataSetRepository.getDataSets();

    Set<String> dataSetNamesWithOutReadPermission = dataSets.stream()
      .filter(dataSet -> !userPermissionCheck.hasPermission(dataSet.getMetadata(), Permission.READ))
      .map(dataSet -> dataSet.getMetadata().getCombinedId())
      .collect(Collectors.toSet());

    Iterator<GraphQLFieldDefinition> graphQlFieldDefinitionIterator = fieldsContainer.getFieldDefinitions().iterator();
    while (graphQlFieldDefinitionIterator.hasNext()) {
      GraphQLFieldDefinition graphQlFieldDefinition = graphQlFieldDefinitionIterator.next();
      if (!dataSetNamesWithOutReadPermission.contains(graphQlFieldDefinition.getName())) {
        graphQlFieldDefinitions.add(graphQlFieldDefinition);
      }
    }

    return graphQlFieldDefinitions;
  }

  @Override
  public GraphQLFieldDefinition getFieldDefinition(GraphQLFieldsContainer fieldsContainer, String fieldName) {
    Collection<DataSet> dataSets = dataSetRepository.getDataSets();

    Set<String> dataSetNamesWithOutReadPermission = dataSets.stream()
      .filter(dataSet -> !userPermissionCheck.hasPermission(dataSet.getMetadata(), Permission.READ))
      .map(dataSet -> dataSet.getMetadata().getCombinedId())
      .collect(Collectors.toSet());

    Iterator<GraphQLFieldDefinition> graphQlFieldDefinitionIterator = fieldsContainer.getFieldDefinitions().iterator();

    while (graphQlFieldDefinitionIterator.hasNext()) {
      GraphQLFieldDefinition graphQlFieldDefinition = graphQlFieldDefinitionIterator.next();

      if (!dataSetNamesWithOutReadPermission.contains(graphQlFieldDefinition.getName()) &&
        graphQlFieldDefinition.getName().equals(fieldName)) {
        return graphQlFieldDefinition;
      }

    }
    return null;
  }
}

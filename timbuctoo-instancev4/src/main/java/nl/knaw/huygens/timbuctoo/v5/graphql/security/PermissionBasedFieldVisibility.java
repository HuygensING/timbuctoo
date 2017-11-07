package nl.knaw.huygens.timbuctoo.v5.graphql.security;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.visibility.GraphqlFieldVisibility;

import java.util.List;

public class PermissionBasedFieldVisibility implements GraphqlFieldVisibility {
  private final UserPermissionCheck userPermissionCheck;

  public PermissionBasedFieldVisibility(UserPermissionCheck userPermissionCheck) {
    this.userPermissionCheck = userPermissionCheck;
  }

  @Override
  public List<GraphQLFieldDefinition> getFieldDefinitions(GraphQLFieldsContainer fieldsContainer) {
    return fieldsContainer.getFieldDefinitions();
  }

  @Override
  public GraphQLFieldDefinition getFieldDefinition(GraphQLFieldsContainer fieldsContainer, String fieldName) {
    return fieldsContainer.getFieldDefinition(fieldName);
  }
}

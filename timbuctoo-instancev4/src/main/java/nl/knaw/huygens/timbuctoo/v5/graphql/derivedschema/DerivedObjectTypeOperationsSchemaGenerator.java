package nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema;

import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Predicate;

import java.util.Set;

public class DerivedObjectTypeOperationsSchemaGenerator implements DerivedObjectTypeSchemaGenerator {
  private final String typeUri;
  private final GraphQlNameGenerator graphQlNameGenerator;
  private final String rootType;

  public DerivedObjectTypeOperationsSchemaGenerator(String typeUri, String rootType,
                                                    GraphQlNameGenerator graphQlNameGenerator) {
    this.typeUri = typeUri;
    this.graphQlNameGenerator = graphQlNameGenerator;
    this.rootType = rootType;
  }

  @Override
  public void objectField(String description, Predicate predicate, String typeUri) {
    // no action needed
  }

  @Override
  public void unionField(String description, Predicate predicate, Set<String> typeUris) {
    // no action needed
  }

  @Override
  public void valueField(String description, Predicate predicate, String typeUri) {
    // no action needed
  }

  @Override
  public StringBuilder getSchema() {
    String name = graphQlNameGenerator.createObjectTypeName(rootType, typeUri);
    StringBuilder schema = new StringBuilder();
    schema.append("type ").append(name).append("Mutations").append(" {\n")
          .append("  edit(").append("uri: String! ").append("entity: ").append(name).append("Input!): ")
          .append(name).append(" @editMutation(dataSet: ").append(rootType).append(")").append("\n")
          .append("}\n");

    return schema;
  }
}

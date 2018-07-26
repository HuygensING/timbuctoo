package nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema;

import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Predicate;

import java.util.Set;

public class DerivedObjectTypeOperationsSchemaGenerator implements DerivedObjectTypeSchemaGenerator {
  private final String typeUri;
  private final TypeNameStore typeNameStore;
  private final String rootType;

  public DerivedObjectTypeOperationsSchemaGenerator(String typeUri, TypeNameStore typeNameStore,
                                                    String rootType) {
    this.typeUri = typeUri;
    this.typeNameStore = typeNameStore;
    this.rootType = rootType;
  }

  @Override
  public void open() {
    // no action needed
  }

  @Override
  public void close() {
    // no action needed
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
    String name = rootType + "_" + typeNameStore.makeGraphQlname(typeUri);
    StringBuilder schema = new StringBuilder();

    schema.append("type ").append(name).append(" {\n")
          .append("  edit(").append("uri: String! ").append("entity: ").append(name).append("Input!): ")
          .append(name).append("\n")
          .append("}\n");

    return schema;
  }
}

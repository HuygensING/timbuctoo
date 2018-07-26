package nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema;

import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Predicate;

import java.util.Set;

public class DerivedQueryObjectTypeSchemaGenerator implements
  DerivedObjectTypeSchemaGenerator {
  private static final String ENTITY_INTERFACE_NAME = "Entity";

  private final String typeUri;
  private StringBuilder builder;
  private TypeNameStore typeNameStore;
  private String rootType;
  private final DerivedSchemaContainer derivedSchemaContainer;

  public DerivedQueryObjectTypeSchemaGenerator(String typeUri,
                                               TypeNameStore typeNameStore, String rootType,
                                               DerivedSchemaContainer derivedSchemaContainer) {
    this.typeUri = typeUri;
    this.typeNameStore = typeNameStore;
    this.rootType = rootType;
    this.derivedSchemaContainer = derivedSchemaContainer;
    this.builder = new StringBuilder();
  }

  @Override
  public void open() {
    String name = rootType + "_" + typeNameStore.makeGraphQlname(typeUri);
    builder.append("#")
           .append("Subjects that are a [")
           .append(typeNameStore.shorten(typeUri))
           .append("](")
           .append(typeUri)
           .append(")")
           .append("\n")
           .append("type ").append(name).append(" implements ").append(ENTITY_INTERFACE_NAME)
           .append(" @rdfType(uri: \"")
           //quotes and backslashes are not allowed in uri's anyway so this shouldn't happen
           .append(typeUri.replace("\"", "").replace("\\", ""))
           .append("\") {\n")
           .append("  uri: String! @uri\n")
           .append("  title: Value @entityTitle\n")
           .append("  description: Value @entityDescription\n")
           .append("  image: Value @entityImage\n")
           .append("  inOtherDataSets(dataSetIds: [String!]): [DataSetLink!]! @otherDataSets\n");
  }

  @Override
  public void close() {
    builder.append("}\n\n");
  }

  @Override
  public void objectField(String description, Predicate predicate, String typeUri) {
    makeFieldAndDeprecations(description, predicate, typeUri, false, true);
  }

  @Override
  public void unionField(String description, Predicate predicate, Set<String> typeUris) {
    String unionType = derivedSchemaContainer.unionType(typeUris);
    makeFieldAndDeprecations(description, predicate, unionType, true, true);
  }

  @Override
  public void valueField(String description, Predicate predicate, String typeUri) {
    String type = derivedSchemaContainer.valueType(typeUri);
    makeFieldAndDeprecations(description, predicate, type, true, false);
  }

  private void makeFieldAndDeprecations(String description, Predicate predicate, String targetType, boolean isValue,
                                        boolean isObject) {
    if (predicate.inUse() || predicate.isExplicit()) {
      //once a list, always a list
      if (predicate.isList() || predicate.hasBeenList()) {
        makeField(description, predicate, targetType, isValue, isObject, true);
        if (predicate.isHasBeenSingular()) {
          makeField(description, predicate, targetType, isValue, isObject, false);
          builder.append(
            " @deprecated(reason: \"This property only returns the first value of the list. Use the *List version to " +
              "retrieve all value\")\n");
        }
      } else {
        //never been a list
        makeField(description, predicate, targetType, isValue, isObject, false);
      }
    } else {
      if (predicate.hasBeenList()) {
        makeField(description, predicate, targetType, isValue, isObject, true);
        builder.append(" @deprecated(reason: \"There used to be entities with this property, but that is no " +
          "longer the case.\")\n");
      }
      if (predicate.isHasBeenSingular()) {
        makeField(description, predicate, targetType, isValue, isObject, false);
        builder.append(" @deprecated(reason: \"There used to be entities with this property, but that is no " +
          "longer the case.\")\n");
      }
    }

    builder.append("\n");
  }


  private void makeField(String description, Predicate predicate, String targetType, boolean isValue,
                         boolean isObject, boolean asList) {
    String fieldName = typeNameStore.makeGraphQlnameForPredicate(predicate.getName(), predicate.getDirection(), asList);
    if (description != null) {
      builder.append("  #").append(description).append("\n");
    }

    builder.append("  ");
    if (asList) {
      builder.append(derivedSchemaContainer.listType(fieldName, targetType));
    } else {
      builder.append(fieldName).append(": ").append(targetType);
    }
    final String safeName = predicate.getName().replace("\"", "");
    builder.append(" ")
           .append("@rdf(predicate: \"")
           .append(safeName)
           .append("\", direction: \"")
           .append(predicate.getDirection())
           .append("\", ")
           .append("isValue: ")
           .append(isValue)
           .append(", isObject: ")
           .append(isObject)
           .append(", isList: ")
           .append(asList)
           .append(")");
  }


  @Override
  public StringBuilder getSchema() {

    return builder;
  }
}

package nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema;

import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.PaginationArgumentsHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DerivedSchemaContainer {
  private final Map<String, DerivedTypeSchemaGenerator> types;
  private final Set<String> topLevelTypes;

  private final String rootType;
  private final GraphQlNameGenerator nameGenerator;
  private final PaginationArgumentsHelper argumentsHelper;

  DerivedSchemaContainer(String rootType, GraphQlNameGenerator graphQlNameGenerator,
                         PaginationArgumentsHelper argumentsHelper) {
    this.rootType = rootType;
    this.nameGenerator = graphQlNameGenerator;
    this.argumentsHelper = argumentsHelper;

    types = new HashMap<>();
    topLevelTypes = new HashSet<>();
  }

  String listType(String fieldName, String typeName) {
    final String listTypeName = argumentsHelper.makeListName(typeName);
    if (!types.containsKey(listTypeName)) {
      DerivedTypeSchemaGenerator listSchema = new DerivedListTypeSchemaGenerator(typeName,
        DerivedSchemaContainer.this.argumentsHelper);
      types.put(listTypeName, listSchema);

    }
    return argumentsHelper.makeListField(fieldName, typeName);
  }

  String collectionType(String fieldName, String typeName) {
    final String listTypeName = argumentsHelper.makeCollectionListName(typeName);
    if (!types.containsKey(listTypeName)) {
      DerivedTypeSchemaGenerator derivedCollectionTypeSchemaGenerator =
        new DerivedCollectionTypeSchemaGenerator(typeName, DerivedSchemaContainer.this.argumentsHelper);

      types.put(listTypeName, derivedCollectionTypeSchemaGenerator);
    }
    return argumentsHelper.makeCollectionListField(fieldName + "List", typeName);
  }


  public String unionType(Set<String> refs) {
    String unionName = "Union_";
    for (String type : refs) {
      unionName += type + "__";
    }
    if (!types.containsKey(unionName)) {
      DerivedTypeSchemaGenerator derivedUnionTypeSchemaGenerator = new DerivedUnionTypeSchemaGenerator(unionName, refs);
      types.put(unionName, derivedUnionTypeSchemaGenerator);
    }
    return unionName;
  }

  public String valueType(String typeUri) {
    final String name = nameGenerator.createValueTypeName(rootType, typeUri);
    if (!types.containsKey(name)) {

      DerivedTypeSchemaGenerator derivedValueTypeSchemaGenerator = new DerivedValueTypeSchemaGenerator(name);


      types.put(name, derivedValueTypeSchemaGenerator);


    }
    return name;
  }


  public DerivedObjectTypeSchemaGenerator addObjectType(String typeUri) {
    final String name = nameGenerator.createObjectTypeName(rootType, typeUri);
    if (!types.containsKey(name)) {
      DerivedObjectTypeSchemaGenerator value =
        new DerivedCompositeObjectTypeSchemaGenerator(typeUri, rootType, nameGenerator, this);
      types.put(name, value);
      topLevelTypes.add(typeUri);
      return value;
    }

    DerivedTypeSchemaGenerator derivedTypeSchemaGenerator = types.get(name);
    if (derivedTypeSchemaGenerator instanceof DerivedObjectTypeSchemaGenerator) {
      return (DerivedObjectTypeSchemaGenerator) derivedTypeSchemaGenerator;
    }
    // this should not happen
    throw new RuntimeException("Type with uri \"" + typeUri + "\" is not an object type");
  }

  public String getSchema() {
    StringBuilder total = new StringBuilder();
    addRootType(total);
    addRootTypeMutations(total);

    for (DerivedTypeSchemaGenerator derivedObjectTypeSchemaGenerator : types.values()) {
      total.append(derivedObjectTypeSchemaGenerator.getSchema());
    }



    return total.toString();
  }

  private void addRootTypeMutations(StringBuilder total) {
    if (topLevelTypes.isEmpty()) {
      return;
    }
    total.append("type ").append(rootType).append("Mutations").append("{\n");

    for (String uri : topLevelTypes) {
      String typename = nameGenerator.createObjectTypeName(rootType, uri);
      String name = typename.substring(rootType.length() + 1);
      total.append("  ").append(name).append(": ").append(typename).append("Mutations").append( " @passThrough\n");
    }
    total.append("}\n\n");
  }

  private void addRootType(StringBuilder total) {
    total.append("type ").append(rootType).append("{\n");
    total.append("  metadata: DataSetMetadata!");

    for (String uri : topLevelTypes) {
      String typename = nameGenerator.createObjectTypeName(rootType, uri);
      String name = typename.substring(rootType.length() + 1);
      total.append("  ").append(name).append("(uri: String!)").append(": ").append(typename).append(" " +
        "@fromCollection(uri: \"")
           .append(graphQlUri(uri)).append("\", listAll: false)\n");
      total.append("  ")
           .append(collectionType(name, typename))
           .append(" " + "@fromCollection(uri: \"")
           .append(graphQlUri(uri))
           .append("\", listAll: true)\n");
    }
    total.append("}\n\n");
  }

  String graphQlUri(String uri) {
    // quotes and backslashes are not allowed in uri's anyway so this shouldn't happen
    // http://facebook.github.io/graphql/October2016/#sec-String-Value
    return uri.replace("\\", "\\\\").replace("\"", "\\\"");
  }


}

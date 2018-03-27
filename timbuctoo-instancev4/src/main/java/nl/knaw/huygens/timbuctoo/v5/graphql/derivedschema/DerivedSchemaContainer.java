package nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema;

import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Predicate;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.PaginationArgumentsHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DerivedSchemaContainer {

  private static final String ENTITY_INTERFACE_NAME = "Entity";
  private static final String VALUE_INTERFACE_NAME = "Value";

  private final Map<String, StringBuilder> types;
  private final Set<String> topLevelTypes;
  private StringBuilder currentType = null;

  private final String rootType;
  private final TypeNameStore typeNameStore;
  private final PaginationArgumentsHelper argumentsHelper;

  DerivedSchemaContainer(String rootType, TypeNameStore typeNameStore,
                         PaginationArgumentsHelper argumentsHelper) {
    this.rootType = rootType;
    this.typeNameStore = typeNameStore;
    this.argumentsHelper = argumentsHelper;

    types = new HashMap<>();
    topLevelTypes = new HashSet<>();
  }

  public void objectField(String description, Predicate predicate, String typeUri) {
    makeFieldAndDeprecations(description, predicate, typeUri, false, true);
  }

  public void unionField(String description, Predicate predicate, Set<String> typeUris) {
    String unionType = unionType(typeUris);
    makeFieldAndDeprecations(description, predicate, unionType, true, true);
  }

  public void valueField(String description, Predicate predicate, String typeUri) {
    String type = valueType(typeUri);
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
          currentType.append(
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
        currentType.append(" @deprecated(reason: \"There used to be entities with this property, but that is no " +
          "longer the case.\")\n");
      }
      if (predicate.isHasBeenSingular()) {
        makeField(description, predicate, targetType, isValue, isObject, false);
        currentType.append(" @deprecated(reason: \"There used to be entities with this property, but that is no " +
          "longer the case.\")\n");
      }
    }

    currentType.append("\n");
  }

  private void makeField(String description, Predicate predicate, String targetType, boolean isValue,
                         boolean isObject, boolean asList) {
    String fieldName = typeNameStore.makeGraphQlnameForPredicate(predicate.getName(), predicate.getDirection(), asList);
    if (description != null) {
      currentType.append("  #").append(description).append("\n");
    }

    currentType.append("  ");
    if (asList) {
      currentType.append(listType(fieldName, targetType));
    } else {
      currentType.append(fieldName).append(": ").append(targetType);
    }
    final String safeName = predicate.getName().replace("\"", "");
    currentType.append(" ")
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

  private String listType(String fieldName, String typeName) {
    final String listTypeName = argumentsHelper.makeListName(typeName);
    if (!types.containsKey(listTypeName)) {
      StringBuilder builder = new StringBuilder();
      types.put(listTypeName, builder);
      builder.append(argumentsHelper.makePaginatedListDefinition(typeName));
    }
    return argumentsHelper.makeListField(fieldName, typeName);
  }

  private String collectionType(String fieldName, String typeName) {
    final String listTypeName = argumentsHelper.makeCollectionListName(typeName);
    if (!types.containsKey(listTypeName)) {
      StringBuilder builder = new StringBuilder();
      types.put(listTypeName, builder);
      builder.append(argumentsHelper.makeCollectionListDefinition(typeName));
    }
    return argumentsHelper.makeCollectionListField(fieldName + "List", typeName);
  }


  public String unionType(Set<String> refs) {
    String unionName = "Union_";
    for (String type : refs) {
      unionName += type + "__";
    }
    if (!types.containsKey(unionName)) {
      StringBuilder builder = new StringBuilder();
      types.put(unionName, builder);

      builder.append("union ").append(unionName).append(" = ");

      boolean needsJoinChar = false;
      for (String type : refs) {
        if (needsJoinChar) {
          builder.append(" | ");
        }
        builder.append(type);
        needsJoinChar = true;
      }
      builder.append("\n\n");
    }
    return unionName;
  }

  public String valueType(String typeUri) {
    final String name = getValueTypeName(typeUri);
    if (!types.containsKey(name)) {
      StringBuilder builder = new StringBuilder();
      types.put(name, builder);

      builder.append("type ").append(name).append(" implements ").append(VALUE_INTERFACE_NAME).append(" {\n")
        .append("  value: String!\n")
        .append("  type: String!\n")
        .append("}\n\n");
    }
    return name;
  }


  public String getValueTypeName(String typeUri) {
    //rootType prefix logic is also present in the ObjectTypeResolver of RdfWiringFactory
    return rootType + "_" + typeNameStore.makeGraphQlValuename(typeUri);
  }

  public String getObjectTypeName(String typeUri) {
    return rootType + "_" + typeNameStore.makeGraphQlname(typeUri);
  }

  public void openObjectType(String typeUri) {
    final String name = getObjectTypeName(typeUri);
    if (!types.containsKey(name)) {
      StringBuilder builder = new StringBuilder();
      types.put(name, builder);
      topLevelTypes.add(typeUri);
      currentType = builder;
      currentType
        .append("#")
        .append("Subjects that are a [")
        .append(typeNameStore.shorten(typeUri))
        .append("](")
        .append(typeUri)
        .append(")")
        .append("\n")

        .append("type ").append(name).append(" implements ").append(ENTITY_INTERFACE_NAME).append(" @rdfType(uri: \"")
        .append(typeUri.replace("\"", "")) //quotes are not allowed in uri's anyway so this shouldn't happen
        .append("\") {\n")
        .append("  uri: String! @uri\n")
        .append("  title: Value @entityTitle\n")
        .append("  description: Value @entityDescription\n")
        .append("  image: Value @entityImage\n");
    }
  }

  public void closeObjectType(String typeUri) {
    final String name = getObjectTypeName(typeUri);
    types.get(name).append("}\n\n");
    currentType = null;
  }

  public String getSchema() {
    StringBuilder total = new StringBuilder();
    total.append("type ").append(rootType).append("{\n");

    total.append("  metadata: DataSetMetadata!");


    for (String uri : topLevelTypes) {
      String typename = getObjectTypeName(uri);
      String name = typename.substring(rootType.length() + 1);
      total.append("  ").append(name).append("(uri: String!)").append(": ").append(typename).append(" " +
        "@fromCollection(uri: \"").append(uri.replace("\"", "\\\"")).append("\", listAll: false)\n");
      total.append("  ")
        .append(collectionType(name, typename))
        .append(" " +
          "@fromCollection(uri: \"")
        .append(uri.replace("\"", "\\\""))
        .append("\", listAll: true)\n");
    }

    total.append("}\n\n");
    for (StringBuilder stringBuilder : types.values()) {
      total.append(stringBuilder);
    }

    return total.toString();
  }

}

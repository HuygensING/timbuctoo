package nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema;

import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.PaginationArgumentsHelper;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

public class GraphQlTypesContainer {

  public static final String ENTITY_INTERFACE_NAME = "Entity";
  public static final String VALUE_INTERFACE_NAME = "Value";
  private static final Logger LOG = getLogger(GraphQlTypesContainer.class);

  final Map<String, StringBuilder> types;
  final Set<String> topLevelTypes;
  StringBuilder currentType = null;

  private final String rootType;
  private final TypeNameStore typeNameStore;
  private final PaginationArgumentsHelper argumentsHelper;
  private TypeDefinitionRegistry registry;

  public GraphQlTypesContainer(String rootType, TypeNameStore typeNameStore,
                               PaginationArgumentsHelper argumentsHelper) {
    this.rootType = rootType;
    this.typeNameStore = typeNameStore;
    this.argumentsHelper = argumentsHelper;

    types = new HashMap<>();
    topLevelTypes = new HashSet<>();
  }

  public void objectField(String fieldName, String description, String predicateUri,
                          Direction direction, String typeName, boolean isList,
                          boolean isOptional) {
    String dataFetcher = "@rdf(uri: \"" + predicateUri.replace("\"", "\\\"") + "\", direction: \"" + direction.name() +
      "\", isValue: false, isObject: true, isList: " + isList + ")";
    makeField(fieldName, description, typeName, isList, isOptional, dataFetcher);
  }

  public void unionField(String name, String description, Set<String> refs,
                         String predicateUri, Direction direction, boolean isOptional,
                         boolean isList) {
    String unionType = unionType(refs);
    String dataFetcher = "@rdf(uri: \"" + predicateUri.replace("\"", "\\\"") + "\", direction: \"" + direction.name() +
      "\", isValue: true, isObject: true, isList: " + isList + ")";
    makeField(name, description, unionType, isList, isOptional, dataFetcher);
  }

  public void valueField(String name, String description, String typeUri, boolean isList,
                         boolean isOptional,
                         String predicateUri) {
    String type = valueType(typeUri);
    String dataFetcher = "@rdf(uri: \"" + predicateUri.replace("\"", "\\\"") + "\", direction: \"OUT\", isValue: " +
      "true, isObject: false, isList: " + isList + ")";
    makeField(name, description, type, isList, isOptional, dataFetcher);
  }

  private void makeField(String name, String description, String targetType, boolean list,
                         boolean optional, String directive) {
    if (registry != null) {
      throw new IllegalStateException("Schema has already been built");
    }

    if (description != null) {
      currentType.append("  #").append(description).append("\n");
    }

    currentType.append("  ");
    if (list) {
      currentType.append(listType(name, targetType));
    } else {
      currentType.append(name).append(": ").append(targetType);
      if (!optional) {
        currentType.append("!");
      }
    }
    currentType.append(directive).append("\n");
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
    final String name = rootType + "_" + typeNameStore.makeGraphQlValuename(typeUri);
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

  public String objectType(String typeUri) {
    return rootType + "_" + typeNameStore.makeGraphQlname(typeUri);
  }

  public void openObjectType(String typeUri) {
    final String name = objectType(typeUri);
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

        .append("type ").append(name).append(" implements ").append(ENTITY_INTERFACE_NAME).append(" {\n")
        .append("  uri: String! @uri\n");
    }
  }

  public void closeObjectType(String typeUri) {
    final String name = objectType(typeUri);
    types.get(name).append("}\n\n");
    currentType = null;
  }

  public TypeDefinitionRegistry getSchema() {

    if (registry == null) {
      StringBuilder total = new StringBuilder();
      total.append("type ").append(rootType).append("{\n");

      for (String uri : topLevelTypes) {
        String typename = objectType(uri);
        String name = typename.substring(rootType.length() + 1);
        total.append("  ").append(name).append("(uri: String!)").append(": ").append(typename).append(" " +
          "@fromCollection(uri: \"").append(uri.replace("\"", "\\\"")).append("\", listAll: false)\n");
        total.append("  ").append(listType(name + "List", typename)).append(" " +
          "@fromCollection(uri: \"").append(uri.replace("\"", "\\\"")).append("\", listAll: true)\n");
      }

      total.append("}\n\n");
      for (StringBuilder stringBuilder : types.values()) {
        total.append(stringBuilder);
      }

      registry = new SchemaParser().parse(total.toString());
    }
    return registry;
  }

}

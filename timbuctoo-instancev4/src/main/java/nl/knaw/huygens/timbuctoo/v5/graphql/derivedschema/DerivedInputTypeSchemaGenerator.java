package nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Predicate;

import java.util.List;
import java.util.Set;

public class DerivedInputTypeSchemaGenerator implements DerivedObjectTypeSchemaGenerator {


  private final String typeUri;
  private final String rootType;
  private final GraphQlNameGenerator graphQlNameGenerator;
  private final List<GraphQlPredicate> replacements;
  private List<GraphQlPredicate> additions;
  private List<GraphQlPredicate> deletions;

  public DerivedInputTypeSchemaGenerator(String typeUri, String rootType, GraphQlNameGenerator graphQlNameGenerator) {
    this.typeUri = typeUri;
    this.rootType = rootType;
    this.graphQlNameGenerator = graphQlNameGenerator;
    replacements = Lists.newArrayList();
    additions = Lists.newArrayList();
    deletions = Lists.newArrayList();
  }

  @Override
  public void open() {
    // nothing to do
  }

  @Override
  public void close() {
    // nothing to do
  }

  @Override
  public void objectField(String description, Predicate predicate, String typeUri) {
    addPredicate(predicate);
  }

  @Override
  public void unionField(String description, Predicate predicate, Set<String> typeUris) {
    addPredicate(predicate);
  }

  @Override
  public void valueField(String description, Predicate predicate, String typeUri) {
    addPredicate(predicate);
  }

  private void addPredicate(Predicate predicate) {
    replacements.add(new GraphQlPredicate(predicate));
    if (predicate.isList()) {
      additions.add(new GraphQlPredicate(predicate));
      deletions.add(new GraphQlPredicate(predicate));
    }
  }

  @Override
  public StringBuilder getSchema() {
    StringBuilder schema = new StringBuilder();
    if (additions.isEmpty() && deletions.isEmpty() && replacements.isEmpty()) {
      return schema;
    }
    String name = graphQlNameGenerator.createObjectTypeName(rootType, typeUri);
    schema.append("input ").append(name).append("Input").append(" {\n");
    if (!additions.isEmpty()) {
      schema.append("  additions: ").append(name).append("AdditionsInput\n");
    }
    if (!deletions.isEmpty()) {
      schema.append("  deletions: ").append(name).append("DeletionsInput\n");
    }
    schema.append("  replacements: ").append(name).append("ReplacementsInput\n")
          .append("}\n\n");

    // Add the inputs
    inputFields(schema, name, "ReplacementsInput", replacements);

    if (!additions.isEmpty()) {
      inputFields(schema, name, "AdditionsInput", additions);
    }

    if (!deletions.isEmpty()) {
      inputFields(schema, name, "DeletionsInput", deletions);
    }

    return schema;
  }

  private void inputFields(StringBuilder schema, String name, String actionName, List<GraphQlPredicate> predicates) {
    schema.append("input ").append(name).append(actionName).append(" {\n");
    for (GraphQlPredicate predicate : predicates) {
      predicate.addToSchema(schema, actionName.startsWith("Replacements"));
    }
    schema.append("}\n\n");
  }


  private class GraphQlPredicate {
    private final Predicate predicate;

    private GraphQlPredicate(Predicate predicate) {
      this.predicate = predicate;
    }

    public void addToSchema(StringBuilder schema, boolean isReplacements) {
      boolean asList = predicate.isList() || predicate.hasBeenList();
      schema.append("  ").append(predName(asList)).append(": ").append(getInputType(asList))
            .append(getDeprecation(!predicate.inUse(), asList)).append("\n");

      if (isReplacements && predicate.isList() && predicate.isHasBeenSingular()) {
        // add field for deprecated single value field
        schema.append("  ").append(predName(false)).append(": ").append(getInputType(false)).append(
          getDeprecation(true, false)).append("\n");
      }
    }

    private String getDeprecation(boolean isDeprecated, boolean asList) {
      if (isDeprecated) {
        if (asList) {
          return " @deprecated(reason: \"There used to be entities with this property, but that is no longer the case" +
            ".\")";
        }

        if (predicate.isList()) {
          return " @deprecated(reason: \"This property only returns the first value of the list. Use the *List " +
            "version\")";
        } else {
          return " @deprecated(reason: \"There used to be entities with this property, but that is no " +
            "longer the case.\")";
        }
      }
      return "";
    }

    private String predName(boolean asList) {
      return graphQlNameGenerator.createFieldName(predicate.getName(), predicate.getDirection(), asList);
    }

    private String getInputType(boolean asList) {
      return asList ? "[PropertyInput!]" : "PropertyInput";
    }
  }
}

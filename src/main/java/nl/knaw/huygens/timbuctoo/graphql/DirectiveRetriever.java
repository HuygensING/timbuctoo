package nl.knaw.huygens.timbuctoo.graphql;

import graphql.language.StringValue;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import java.util.Optional;

public class DirectiveRetriever {
  public static Optional<String> getDirectiveArgument(GraphQLObjectType parentType, String directiveName,
                                                      String argumentName) {
    if (parentType.getDefinition().getDirectives(directiveName).isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(parentType.getDefinition().getDirectives(directiveName).getFirst())
      .map(d -> d.getArgument(argumentName))
      .map(v -> (StringValue) v.getValue())
      .map(StringValue::getValue);
  }

  public static Optional<String> getDirectiveArgument(GraphQLFieldDefinition field, String directiveName,
                                                      String argumentName) {
    if (field.getDefinition().getDirectives(directiveName).isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(field.getDefinition().getDirectives(directiveName).getFirst())
      .map(d -> d.getArgument(argumentName))
      .map(v -> (StringValue) v.getValue())
      .map(StringValue::getValue);
  }
}

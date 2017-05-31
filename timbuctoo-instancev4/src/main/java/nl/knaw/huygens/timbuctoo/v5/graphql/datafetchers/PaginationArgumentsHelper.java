package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;

import java.util.HashMap;
import java.util.Map;

import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;

public class PaginationArgumentsHelper {

  Map<String, GraphQLOutputType> paginatableTypes = new HashMap<>();

  static PaginationArguments getPaginationArguments(DataFetchingEnvironment environment) {
    if (environment.containsArgument("cursor")) {
      if (environment.containsArgument("count")) {
        return PaginationArguments.create(environment.getArgument("count"), environment.getArgument("cursor"));
      } else {
        return PaginationArguments.create((String) environment.getArgument("cursor"));
      }
    } else if (environment.containsArgument("count")) {
      return PaginationArguments.create((int) environment.getArgument("count"));
    } else {
      return PaginationArguments.create();
    }
  }

  public void makePaginatedList(GraphQLFieldDefinition.Builder result, GraphQLOutputType outputType) {
    GraphQLOutputType type = paginatableTypes.computeIfAbsent(
      outputType.getName(),
      name -> newObject()
        .name(outputType.getName() + "s")
        .field(newFieldDefinition()
          .name("prevCursor")
          .type(Scalars.GraphQLString)
        )
        .field(newFieldDefinition()
          .name("nextCursor")
          .type(Scalars.GraphQLString)
        )
        .field(newFieldDefinition()
          .name("items")
          .type(list(nonNull(outputType)))
        ).build()
    );
    result
      .type(type)
      .argument(newArgument()
        .name("cursor")
        .type(Scalars.GraphQLString)
        .defaultValue("")
        .description("Use either the empty string to start from the beginning, the string LAST to start from the " +
          "end, or a cursor returned by a previous call")
      )
      .argument(newArgument()
        .name("count")
        .type(Scalars.GraphQLInt)
        .defaultValue(20)
        .description("The amount of items to request. You might get less items then requested.")
      );
  }
}

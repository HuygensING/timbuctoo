package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;

import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLNonNull.nonNull;

public class PaginationArgumentsHelper {
  public static final int DEFAULT_COUNT = 20;

  static PaginationArguments getPaginationArguments(DataFetchingEnvironment environment) {
    String cursor = "";
    int count = DEFAULT_COUNT;
    if (environment.containsArgument("cursor")) {
      cursor = environment.getArgument("cursor");
    }

    if (environment.containsArgument("count")) {
      count = environment.getArgument("count");
    }

    return PaginationArguments.create(count, cursor);
  }

  public void makePaginatedList(GraphQLFieldDefinition.Builder result, GraphQLOutputType outputType) {
    result
      .type(list(nonNull(outputType)))
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
        .defaultValue(DEFAULT_COUNT)
        .description("The amount of items to request. You might get less items then requested.")
      );
  }
}

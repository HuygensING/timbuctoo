package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;

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

  public String makeListName(String outputTypeName) {
    return outputTypeName + "_List";
  }

  public String makeListField(String fieldName, String outputTypeName) {
    return fieldName + "(cursor: ID, count: Int): " + makeListName(outputTypeName);
  }

  public String makePaginatedListDefinition(String outputType) {

    //FIXME: add descriptions to arguments
    return "type " + makeListName(outputType) + " {\n" +
      "  prevCursor: ID\n" +
      "  nextCursor: ID\n" +
      "  items: [" + outputType + "!]!\n" +
      "}\n\n";
    //GraphQLObjectType listWrapper = preComputedListTypes.computeIfAbsent(outputType.getName(), x ->
    //  GraphQLObjectType.newObject()
    //    .name(outputType.getName() + "_List")
    //    .field(builder -> builder
    //      .name("prevCursor")
    //      .type(Scalars.GraphQLID)
    //    )
    //    .field(builder -> builder
    //      .name("nextCursor")
    //      .type(Scalars.GraphQLID)
    //    )
    //    .field(builder -> builder
    //      .name("items")
    //      .type(nonNull(list(nonNull(outputType))))
    //    )
    //    .build()
    //);
    //result
    //  .type(listWrapper)
    //  .argument(newArgument()
    //    .name("cursor")
    //    .type(Scalars.GraphQLString)
    //    .defaultValue("")
    //    .description("Use either the empty string to start from the beginning, the string LAST to start from the " +
    //      "end, or a cursor returned by a previous call")
    //  )
    //  .argument(newArgument()
    //    .name("count")
    //    .type(Scalars.GraphQLInt)
    //    .defaultValue(DEFAULT_COUNT)
    //    .description("The amount of items to request. You might get less items then requested.")
    //  );
  }
}

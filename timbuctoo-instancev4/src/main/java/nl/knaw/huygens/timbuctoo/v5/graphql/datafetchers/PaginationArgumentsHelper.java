package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;

public class PaginationArgumentsHelper {
  public static final int DEFAULT_COUNT = 20;

  static PaginationArguments getPaginationArguments(DataFetchingEnvironment environment) {
    String cursor = "";
    String searchQuery = null;
    int count = DEFAULT_COUNT;
    if (environment.containsArgument("cursor")) {
      cursor = environment.getArgument("cursor");
    }

    if (environment.containsArgument("count")) {
      count = environment.getArgument("count");
    }

    if (environment.containsArgument("elasticsearch")) {
      searchQuery = environment.getArgument("elasticsearch");
    }


    return PaginationArguments.create(count, cursor, searchQuery);
  }

  public String makeListName(String outputTypeName) {
    return outputTypeName + "_List";
  }

  public String makeListField(String fieldName, String outputTypeName) {
    //FIXME: how do you add descriptions to arguments?
    //    .name("cursor")
    //    .description("Use either the empty string to start from the beginning, the string LAST to start from the " +
    //      "end, or a cursor returned by a previous call")
    //  )
    //    .name("count")
    //    .description("The amount of items to request. You might get less items then requested.")
    //  );
    return fieldName + "(cursor: ID, count: Int, elasticsearch: String): " + makeListName(outputTypeName);
  }

  public String makePaginatedListDefinition(String outputType) {

    return "type " + makeListName(outputType) + " {\n" +
      "  prevCursor: ID\n" +
      "  nextCursor: ID\n" +
      "  items: [" + outputType + "!]!\n" +
      "}\n\n";
  }
}

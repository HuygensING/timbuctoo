package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import com.google.common.base.Charsets;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.graphql.collectionfilter.CollectionFilter;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.ConfiguredFilter;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DatabaseResult;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;
import nl.knaw.huygens.timbuctoo.v5.util.Graph;

import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PaginationArgumentsHelper {
  public static final int DEFAULT_COUNT = 20;
  private final Map<String, CollectionFilter> collectionFilters;
  private static final Base64.Decoder DECODER = Base64.getDecoder();

  public PaginationArgumentsHelper() {
    this.collectionFilters = new HashMap<>();
  }

  public PaginationArgumentsHelper(Map<String, CollectionFilter> collectionFilters) {
    this.collectionFilters = collectionFilters;
  }

  public PaginationArguments getPaginationArguments(DataFetchingEnvironment environment) {
    String cursor = "";
    if (environment.containsArgument("cursor") && (environment.getArgument("cursor") instanceof String)) {
      cursor = new String(DECODER.decode((String) environment.getArgument("cursor")), Charsets.UTF_8);
    }

    int count = DEFAULT_COUNT;
    if (environment.containsArgument("count")) {
      count = environment.getArgument("count");
    }

    Optional<Graph> graph = Optional.empty();
    if (environment.containsArgument("graph")) {
      graph = Optional.of(new Graph(environment.getArgument("graph")));
    }

    ConfiguredFilter filter = null;
    for (Map.Entry<String, CollectionFilter> entry : collectionFilters.entrySet()) {
      if (environment.containsArgument(entry.getKey())) {
        String searchQuery = environment.getArgument(entry.getKey());
        String cursorArg = cursor;
        int countArg = count;
        String dataSetId = ((DatabaseResult) environment.getSource()).getDataSet().getMetadata().getCombinedId();
        String fieldName = environment.getFieldDefinition().getName();

        filter = () -> entry.getValue().query(dataSetId, fieldName, searchQuery, cursorArg, countArg);
        break;
      }
    }

    ZonedDateTime timeSince = null;
    if (environment.containsArgument("updatedSince")) {
      timeSince = ZonedDateTime.parse(environment.getArgument("updatedSince"));
    } else if (environment.containsArgument("deletedSince")) {
      timeSince = ZonedDateTime.parse(environment.getArgument("deletedSince"));
    }

    return PaginationArguments.create(graph, count, cursor, Optional.ofNullable(filter), Optional.ofNullable(timeSince));
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
    return fieldName + "(cursor: ID, count: Int): " + makeListName(outputTypeName);
  }

  public String makePaginatedListDefinition(String outputType) {
    return "type " + makeListName(outputType) + " {\n" +
      "  prevCursor: ID\n" +
      "  nextCursor: ID\n" +
      "  items: [" + outputType + "!]!\n" +
      "}\n\n";
  }

  public String makeCollectionListName(String outputTypeName) {
    return outputTypeName + "_CollectionList";
  }

  public String makeCollectionListField(String fieldName, String outputTypeName) {
    String customFilters = collectionFilters.keySet().stream()
                                            .map(k -> k + ": String").collect(Collectors.joining(", "));
    if (!customFilters.isEmpty()) {
      customFilters = ", " + customFilters;
    }
    return fieldName + "(graph: String, cursor: ID, count: Int" + customFilters + ", updatedSince: String): " +
        makeCollectionListName(outputTypeName);
  }

  public String makeCollectionListDefinition(String outputType) {
    return "type " + makeCollectionListName(outputType) + " {\n" +
      "  total: Int\n" +
      "  prevCursor: ID\n" +
      "  nextCursor: ID\n" +
      "  facets: [Facet!]!\n" +
      "  items: [" + outputType + "!]!\n" +
      "}\n\n";
  }
}

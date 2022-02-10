package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers;

import com.github.jsonldjava.shaded.com.google.common.collect.Lists;
import com.google.common.base.Charsets;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.PaginationArgumentsHelper;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.ContextData;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.ImmutableStringList;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.StringList;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GraphsDataFetcher implements DataFetcher<StringList> {
  private static final Base64.Encoder ENCODER = Base64.getEncoder();
  private static final PaginationArgumentsHelper ARGUMENTS_HELPER = new PaginationArgumentsHelper();

  private final DataSetRepository dataSetRepository;

  public GraphsDataFetcher(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public StringList get(DataFetchingEnvironment environment) {
    if (environment.getSource() instanceof DataSetMetaData) {
      final DataSetMetaData input = environment.getSource();
      final Optional<User> userOpt = ((ContextData) environment.getContext()).getUser();
      final User user = userOpt.orElse(null);
      final DataSet dataSet = dataSetRepository.getDataSet(user, input.getOwnerId(), input.getDataSetId()).get();

      final PaginationArguments arguments = ARGUMENTS_HELPER.getPaginationArguments(environment);
      final String cursor = arguments.getCursor();
      try (Stream<String> graphStream = dataSet.getGraphStore().getGraphs(cursor)) {
        int count = arguments.getCount();
        if (count < 0 || count > 10_000) {
          count = 10_000;
        }

        count += 1; //to determine if we reached the end of the list we keep track of one extra
        String[] cursors = new String[3];

        List<String> graphs = graphStream
            .limit(count)
            .peek(graph -> {
              if (cursors[0] == null) {
                cursors[0] = graph;
              }
              cursors[1] = cursors[2]; //keep track of both the cursor of the last item and the before-last item
              cursors[2] = graph;
            })
            .collect(Collectors.toList());

        if (!graphs.isEmpty()) {
          String prevCursor = arguments.getCursor().equals("") ? null : cursors[0];
          String nextCursor = graphs.size() == count ? cursors[1] : null;

          return ImmutableStringList
              .builder()
              .prevCursor(Optional.ofNullable(encode(prevCursor)))
              .nextCursor(Optional.ofNullable(encode(nextCursor)))
              .items(graphs.size() == count ? graphs.subList(0, count - 1) : graphs)
              .build();
        }
      }
    }

    return ImmutableStringList
        .builder()
        .prevCursor(Optional.empty())
        .nextCursor(Optional.empty())
        .items(Lists.newArrayList())
        .build();
  }

  private static String encode(String cursor) {
    if (cursor == null) {
      return null;
    } else {
      return ENCODER.encodeToString(cursor.getBytes(Charsets.UTF_8));
    }
  }
}

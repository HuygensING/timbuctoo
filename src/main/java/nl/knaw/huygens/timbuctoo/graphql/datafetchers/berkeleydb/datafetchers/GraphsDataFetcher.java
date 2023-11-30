package nl.knaw.huygens.timbuctoo.graphql.datafetchers.berkeleydb.datafetchers;

import com.github.jsonldjava.shaded.com.google.common.collect.Lists;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.CursorUri;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.PaginationArgumentsHelper;
import nl.knaw.huygens.timbuctoo.graphql.rootquery.dataproviders.StringList;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.PaginationArguments;

import java.util.Optional;
import java.util.stream.Stream;

public class GraphsDataFetcher implements DataFetcher<StringList> {
  private static final PaginationArgumentsHelper ARGUMENTS_HELPER = new PaginationArgumentsHelper();

  private final DataSetRepository dataSetRepository;

  public GraphsDataFetcher(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public StringList get(DataFetchingEnvironment environment) {
    if (environment.getSource() instanceof DataSetMetaData) {
      final DataSetMetaData input = environment.getSource();
      final Optional<User> userOpt = environment.getGraphQlContext().get("user");
      final User user = userOpt.orElse(null);
      final DataSet dataSet = dataSetRepository.getDataSet(user, input.getOwnerId(), input.getDataSetId()).get();

      final PaginationArguments arguments = ARGUMENTS_HELPER.getPaginationArguments(environment);
      final String cursor = arguments.getCursor();

      try (Stream<CursorUri> graphStream = dataSet.getGraphStore().getGraphs(cursor)) {
        final PaginatedList<String> paginatedList = PaginationHelper.getPaginatedList(
            graphStream,
            CursorUri::getUri,
            arguments,
            Optional.empty()
        );

        return StringList.create(
            paginatedList.getPrevCursor(),
            paginatedList.getNextCursor(),
            paginatedList.getItems()
        );
      }
    }

    return StringList.create(
        Optional.empty(),
        Optional.empty(),
        Lists.newArrayList()
    );
  }
}

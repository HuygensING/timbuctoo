package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.util.LambdaOriginatedException;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportStatusReport;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.RdfCreator;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.RootData;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.ContextData;
import nl.knaw.huygens.timbuctoo.v5.graphql.security.UserPermissionCheck;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class IndexConfigDataFetcher implements DataFetcher {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final DataSetRepository dataSetRepository;

  public IndexConfigDataFetcher(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public Object get(DataFetchingEnvironment env) {
    String dataSetId = env.getArgument("dataSet");
    String collectionUri = env.getArgument("collectionUri");
    Object viewConfig = env.getArgument("indexConfig");

    Tuple<String, String> userAndDataSet = DataSetMetaData.splitCombinedId(dataSetId);

    ContextData contextData = env.getContext();
    UserPermissionCheck userPermissionCheck = contextData.getUserPermissionCheck();

    Optional<User> currentUser = ((RootData) env.getRoot()).getCurrentUser();
    if (!currentUser.isPresent()) {
      throw new RuntimeException("User is not provided");
    }

    String ownerId = userAndDataSet.getLeft();
    String dataSetName = userAndDataSet.getRight();
    Optional<DataSet> dataSetExists = dataSetRepository.getDataSet(
      contextData.getUser().get(),
      ownerId,
      dataSetName
    );
    if (dataSetExists.isPresent() && userPermissionCheck.getPermissions(dataSetExists.get().getMetadata())
      .contains(Permission.ADMIN)) {
      DataSet dataSet = dataSetExists.get();
      final QuadStore quadStore = dataSet.getQuadStore();
      final String baseUri = dataSet.getMetadata().getBaseUri();
      Supplier<RdfCreator> supplier = () -> {
        try {
          return new StringPredicatesRdfCreator(
            quadStore,
            ImmutableMap.of(
              Tuple.tuple(collectionUri, RdfConstants.TIM_HASINDEXERCONFIG),
              Optional.of(OBJECT_MAPPER.writeValueAsString(viewConfig))
            ),
            baseUri
          );
        } catch (JsonProcessingException e) {
          throw new LambdaOriginatedException(e);
        }
      };
      try {
        ImportStatusReport statusReport = dataSet.getImportManager().generateLog(baseUri, baseUri, supplier).get();
        if (statusReport.hasErrors()) {
          throw new RuntimeException(statusReport.getLastError());
        }
        return viewConfig;
      } catch (LogStorageFailedException | InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    } else {
      throw new RuntimeException("Dataset does not exist");
    }
  }
}

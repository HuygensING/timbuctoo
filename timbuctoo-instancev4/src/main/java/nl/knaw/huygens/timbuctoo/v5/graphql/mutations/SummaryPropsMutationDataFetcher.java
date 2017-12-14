package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.ContextData;
import nl.knaw.huygens.timbuctoo.v5.graphql.security.UserPermissionCheck;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static java.util.Optional.ofNullable;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_SUMMARYDESCRIPTIONPREDICATE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_SUMMARYIMAGEPREDICATE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_SUMMARYTITLEPREDICATE;

public class SummaryPropsMutationDataFetcher implements DataFetcher {
  private static final Logger LOG = LoggerFactory.getLogger(SummaryPropsMutationDataFetcher.class);
  private final DataSetRepository dataSetRepository;

  public SummaryPropsMutationDataFetcher(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public Object get(DataFetchingEnvironment env) {
    String dataSetId = env.getArgument("dataSet");
    String collectionUri = env.getArgument("collectionUri");
    Map viewConfig = env.getArgument("summaryProperties");

    ContextData contextData = env.getContext();
    User user = contextData.getUser().get();
    UserPermissionCheck userPermissionCheck = contextData.getUserPermissionCheck();

    Tuple<String, String> userAndDataSet = DataSetMetaData.splitCombinedId(dataSetId);

    String ownerId = userAndDataSet.getLeft();
    String dataSetName = userAndDataSet.getRight();
    Optional<DataSet> dataSet = dataSetRepository.getDataSet(user, ownerId, dataSetName);
    if (dataSet.isPresent() &&
      userPermissionCheck.getPermissions(dataSetRepository.getDataSet(user, ownerId, dataSetName).get()
        .getMetadata())
        .contains(Permission.ADMIN)) {
      dataSet.get().getQuadStore();
      try {
        final String baseUri = dataSet.get().getMetadata().getBaseUri();
        dataSet.get().getImportManager().generateLog(
          baseUri,
          baseUri,
          new StringPredicatesRdfCreator(
            dataSet.get().getQuadStore(),
            ImmutableMap.of(
              Tuple.tuple(collectionUri, TIM_SUMMARYTITLEPREDICATE),
              ofNullable((String) viewConfig.get("title")),

              Tuple.tuple(collectionUri, TIM_SUMMARYDESCRIPTIONPREDICATE),
              ofNullable((String) viewConfig.get("description")),

              Tuple.tuple(collectionUri, TIM_SUMMARYIMAGEPREDICATE),
              ofNullable((String) viewConfig.get("image"))
            ),
            baseUri
          )
        ).get();
        return new LazyTypeSubjectReference(collectionUri, dataSet.get());
      } catch (LogStorageFailedException | InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    } else {
      throw new RuntimeException("Dataset does not exist");
    }
  }

}

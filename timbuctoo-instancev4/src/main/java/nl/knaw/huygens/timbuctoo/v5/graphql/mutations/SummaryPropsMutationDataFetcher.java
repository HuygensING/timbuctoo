package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.ContextData;
import nl.knaw.huygens.timbuctoo.v5.graphql.security.UserPermissionCheck;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
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
    UserPermissionCheck userPermissionCheck = contextData.getUserPermissionCheck();

    Tuple<String, String> userAndDataSet = PromotedDataSet.splitCombinedId(dataSetId);

    String ownerId = userAndDataSet.getLeft();
    String dataSetName = userAndDataSet.getRight();
    if (dataSetRepository.dataSetExists(ownerId, dataSetName) &&
      userPermissionCheck.getPermissions(ownerId, dataSetRepository.getDataSet(ownerId, dataSetName).get()
        .getMetadata())
        .contains(Permission.ADMIN)) {
      DataSet dataSet = dataSetRepository.unsafeGetDataSetWithoutCheckingPermissions(ownerId, dataSetName).get();
      dataSet.getQuadStore();
      try {
        final String baseUri = dataSet.getMetadata().getBaseUri();
        dataSet.getImportManager().generateLog(
          baseUri,
          baseUri,
          new StringPredicatesRdfCreator(
            dataSet.getQuadStore(),
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
        return new LazyTypeSubjectReference(collectionUri, dataSet);
      } catch (LogStorageFailedException | InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    } else {
      throw new RuntimeException("Dataset does not exist");
    }
  }

}

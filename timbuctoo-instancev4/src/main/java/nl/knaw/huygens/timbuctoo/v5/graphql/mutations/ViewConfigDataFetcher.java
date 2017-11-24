package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.ContextData;
import nl.knaw.huygens.timbuctoo.v5.graphql.security.UserPermissionCheck;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.HAS_VIEW_CONFIG;

public class ViewConfigDataFetcher implements DataFetcher {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final Logger LOG = LoggerFactory.getLogger(ViewConfigDataFetcher.class);
  private final DataSetRepository dataSetRepository;

  public ViewConfigDataFetcher(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public Object get(DataFetchingEnvironment env) {
    String dataSetId = env.getArgument("dataSet");
    String collectionUri = env.getArgument("collectionUri");
    Object viewConfig = env.getArgument("viewConfig");

    ContextData contextData = env.getContext();

    UserPermissionCheck userPermissionCheck = contextData.getUserPermissionCheck();

    Tuple<String, String> ownerAndDataSet = PromotedDataSet.splitCombinedId(dataSetId);

    String ownerId = ownerAndDataSet.getLeft();
    String dataSetName = ownerAndDataSet.getRight();

    Optional<DataSet> dataSet = dataSetRepository.getDataSet(contextData.getUser().get().getPersistentId(),
      ownerId, dataSetName);
    if (dataSet.isPresent() && userPermissionCheck.getPermissions(dataSet.get().getMetadata())
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
              Tuple.tuple(collectionUri, HAS_VIEW_CONFIG), Optional.of(OBJECT_MAPPER.writeValueAsString(viewConfig))
            ),
            baseUri
          )
        ).get();
        return viewConfig;
      } catch (LogStorageFailedException | InterruptedException | ExecutionException | JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    } else {
      throw new RuntimeException("Dataset does not exist");
    }
  }

}

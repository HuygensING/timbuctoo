package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto.PredicateMutation;

import java.util.concurrent.ExecutionException;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.HAS_VIEW_CONFIG;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;

public class ViewConfigDataFetcher implements DataFetcher {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final DataSetRepository dataSetRepository;

  public ViewConfigDataFetcher(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public Object get(DataFetchingEnvironment env) {
    String collectionUri = env.getArgument("collectionUri");
    Object viewConfig = env.getArgument("viewConfig");
    DataSet dataSet = MutationHelpers.getDataSet(env, dataSetRepository::getDataSet);

    MutationHelpers.checkPermissions(env, dataSet.getMetadata());
    try {
      MutationHelpers.addMutation(
        dataSet,
        new PredicateMutation()
          .withReplacement(collectionUri, HAS_VIEW_CONFIG, OBJECT_MAPPER.writeValueAsString(viewConfig), STRING)
      );
      return viewConfig;
    } catch (LogStorageFailedException | InterruptedException | ExecutionException | JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}

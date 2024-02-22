package nl.knaw.huygens.timbuctoo.graphql.mutations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.graphql.mutations.dto.PredicateMutation;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.security.dto.Permission;

import java.util.concurrent.ExecutionException;

import static nl.knaw.huygens.timbuctoo.graphql.mutations.dto.PredicateMutation.replace;
import static nl.knaw.huygens.timbuctoo.util.RdfConstants.TIM_HASINDEXERCONFIG;

public class IndexConfigMutation extends Mutation {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final DataSetRepository dataSetRepository;

  public IndexConfigMutation(Runnable schemaUpdater, DataSetRepository dataSetRepository) {
    super(schemaUpdater);
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public Object executeAction(DataFetchingEnvironment env) {
    String collectionUri = env.getArgument("collectionUri");
    Object indexConfig = env.getArgument("indexConfig");
    DataSet dataSet = MutationHelpers.getDataSet(env, dataSetRepository::getDataSet);
    MutationHelpers.checkPermission(env, dataSet.getMetadata(), Permission.CONFIG_INDEX);
    try {
      MutationHelpers.addMutation(
        dataSet,
        new PredicateMutation()
          .entity(
            collectionUri,
            replace(TIM_HASINDEXERCONFIG, PredicateMutation.value(OBJECT_MAPPER.writeValueAsString(indexConfig)))
          )
      );
      return indexConfig;
    } catch (LogStorageFailedException | InterruptedException | ExecutionException | JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}

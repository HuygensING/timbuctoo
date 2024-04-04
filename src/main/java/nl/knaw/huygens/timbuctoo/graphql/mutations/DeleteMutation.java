package nl.knaw.huygens.timbuctoo.graphql.mutations;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.graphql.mutations.dto.DeleteMutationChangeLog;
import nl.knaw.huygens.timbuctoo.util.UriHelper;
import nl.knaw.huygens.timbuctoo.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.util.Graph;

import java.util.Map;

public class DeleteMutation extends ChangeMutation<DeleteMutationChangeLog, DeleteMutation.RemovedEntity> {
  public DeleteMutation(Runnable schemaUpdater, DataSetRepository dataSetRepository, UriHelper uriHelper,
                        String dataSetId) {
    super(schemaUpdater, dataSetRepository, uriHelper, dataSetId, Permission.DELETE, true);
  }

  @Override
  protected DeleteMutationChangeLog createChangelog(Graph graph, String uri, Map entity) throws JsonProcessingException {
    return new DeleteMutationChangeLog(graph, uri, entity);
  }

  @Override
  protected RemovedEntity createResponse(Graph graph, String uri, DataSet dataSet) {
    return new RemovedEntity(graph.uri(), uri);
  }

  protected record RemovedEntity(String graph, String uri) {
  }
}

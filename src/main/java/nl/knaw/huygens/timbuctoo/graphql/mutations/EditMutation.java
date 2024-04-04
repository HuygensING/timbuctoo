package nl.knaw.huygens.timbuctoo.graphql.mutations;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.graphql.mutations.dto.EditMutationChangeLog;
import nl.knaw.huygens.timbuctoo.util.UriHelper;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.berkeleydb.datafetchers.QuadStoreLookUpSubjectByUriFetcher;
import nl.knaw.huygens.timbuctoo.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.util.Graph;

import java.util.Map;
import java.util.Optional;

public class EditMutation extends ChangeMutation<EditMutationChangeLog, SubjectReference> {
  private final QuadStoreLookUpSubjectByUriFetcher subjectFetcher;

  public EditMutation(Runnable schemaUpdater, DataSetRepository dataSetRepository, UriHelper uriHelper,
                      QuadStoreLookUpSubjectByUriFetcher subjectFetcher, String dataSetId) {
    super(schemaUpdater, dataSetRepository, uriHelper, dataSetId, Permission.WRITE, true);
    this.subjectFetcher = subjectFetcher;
  }

  @Override
  protected EditMutationChangeLog createChangelog(Graph graph, String uri, Map entity) throws JsonProcessingException {
    return new EditMutationChangeLog(graph, uri, entity);
  }

  @Override
  protected SubjectReference createResponse(Graph graph, String uri, DataSet dataSet) {
    return subjectFetcher.getItemInGraph(uri, Optional.of(graph), dataSet);
  }
}

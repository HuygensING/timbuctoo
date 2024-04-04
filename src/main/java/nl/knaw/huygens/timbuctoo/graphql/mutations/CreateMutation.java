package nl.knaw.huygens.timbuctoo.graphql.mutations;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.graphql.mutations.dto.CreateMutationChangeLog;
import nl.knaw.huygens.timbuctoo.util.UriHelper;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.berkeleydb.datafetchers.QuadStoreLookUpSubjectByUriFetcher;
import nl.knaw.huygens.timbuctoo.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.util.Graph;

import java.util.Map;
import java.util.Optional;

public class CreateMutation extends ChangeMutation<CreateMutationChangeLog, SubjectReference> {
  private final QuadStoreLookUpSubjectByUriFetcher subjectFetcher;
  protected final String typeUri;

  public CreateMutation(Runnable schemaUpdater, DataSetRepository dataSetRepository, UriHelper uriHelper,
                        QuadStoreLookUpSubjectByUriFetcher subjectFetcher, String dataSetId, String typeUri) {
    super(schemaUpdater, dataSetRepository, uriHelper, dataSetId, Permission.CREATE, false);
    this.subjectFetcher = subjectFetcher;
    this.typeUri = typeUri;
  }

  @Override
  protected CreateMutationChangeLog createChangelog(Graph graph, String uri, Map entity) throws JsonProcessingException {
    return new CreateMutationChangeLog(graph, uri, typeUri, entity);
  }

  @Override
  protected SubjectReference createResponse(Graph graph, String uri, DataSet dataSet) {
    return subjectFetcher.getItemInGraph(uri, Optional.of(graph), dataSet);
  }
}

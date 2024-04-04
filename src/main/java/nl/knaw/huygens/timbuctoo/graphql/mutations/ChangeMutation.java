package nl.knaw.huygens.timbuctoo.graphql.mutations;

import com.fasterxml.jackson.core.JsonProcessingException;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.graphql.mutations.dto.ChangeLog;
import nl.knaw.huygens.timbuctoo.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.util.Graph;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.util.UriHelper;
import nl.knaw.huygens.timbuctoo.util.UserUriCreator;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.graphql.mutations.MutationHelpers.getDataSet;
import static nl.knaw.huygens.timbuctoo.graphql.mutations.MutationHelpers.checkPermission;
import static nl.knaw.huygens.timbuctoo.graphql.mutations.MutationHelpers.getUser;

public abstract class ChangeMutation<C extends ChangeLog, T> extends Mutation<T> {
  private final DataSetRepository dataSetRepository;
  private final String dataSetName;
  private final String ownerId;
  private final UserUriCreator userUriCreator;
  private final Permission permission;
  private final boolean shouldExist;

  public ChangeMutation(Runnable schemaUpdater, DataSetRepository dataSetRepository, UriHelper uriHelper,
                        String dataSetId, Permission permission, boolean shouldExist) {
    super(schemaUpdater);
    this.dataSetRepository = dataSetRepository;
    Tuple<String, String> dataSetIdSplit = DataSetMetaData.splitCombinedId(dataSetId);
    dataSetName = dataSetIdSplit.right();
    ownerId = dataSetIdSplit.left();
    userUriCreator = new UserUriCreator(uriHelper);
    this.permission = permission;
    this.shouldExist = shouldExist;
  }

  @Override
  public T executeAction(DataFetchingEnvironment environment) {
    final Graph graph = new Graph(environment.getArgument("graph"));
    final String uri = environment.getArgument("uri");
    final Map entity = environment.getArgument("entity");

    User user = getUser(environment);
    DataSet dataSet = getDataSet(environment, dataSetRepository::getDataSet, ownerId, dataSetName);
    checkPermission(environment, dataSet.getMetadata(), permission);

    try (Stream<CursorQuad> quads = dataSet.getQuadStore().getQuadsInGraph(uri, Optional.of(graph))) {
      if ((shouldExist && quads.findAny().isEmpty()) || (!shouldExist && quads.findAny().isPresent())) {
        String message = "Subject with uri '" + uri + "' ";
        message += shouldExist ? "does not exist " : "already exists ";
        message += graph.isDefaultGraph() ? "in the default graph" : "in graph '" + graph + "'";
        throw new RuntimeException(message);
      }
    }

    try {
      dataSet.getImportManager().generateLog(
          dataSet.getMetadata().getBaseUri(), null,
          new GraphQlToRdfPatch(graph.uri(), uri, userUriCreator.create(user), createChangelog(graph, uri, entity))
      ).get(); // Wait until the data is processed
    } catch (LogStorageFailedException | JsonProcessingException | InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    return createResponse(graph, uri, dataSet);
  }

  protected abstract C createChangelog(Graph graph, String uri, Map entity) throws JsonProcessingException;

  protected abstract T createResponse(Graph graph, String uri, DataSet dataSet);
}

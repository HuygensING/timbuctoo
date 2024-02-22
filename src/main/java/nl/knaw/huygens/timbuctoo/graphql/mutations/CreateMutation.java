package nl.knaw.huygens.timbuctoo.graphql.mutations;

import com.fasterxml.jackson.core.JsonProcessingException;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.graphql.mutations.dto.CreateMutationChangeLog;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.util.UriHelper;
import nl.knaw.huygens.timbuctoo.util.UserUriCreator;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.berkeleydb.datafetchers.QuadStoreLookUpSubjectByUriFetcher;
import nl.knaw.huygens.timbuctoo.graphql.security.UserPermissionCheck;
import nl.knaw.huygens.timbuctoo.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.util.Graph;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class CreateMutation extends Mutation {
  private final DataSetRepository dataSetRepository;
  private final QuadStoreLookUpSubjectByUriFetcher subjectFetcher;
  private final String dataSetName;
  private final String ownerId;
  private final String typeUri;
  private final UserUriCreator userUriCreator;

  public CreateMutation(Runnable schemaUpdater, DataSetRepository dataSetRepository, UriHelper uriHelper,
                        QuadStoreLookUpSubjectByUriFetcher subjectFetcher,
                        String dataSetId, String typeUri) {
    super(schemaUpdater);
    this.dataSetRepository = dataSetRepository;
    this.subjectFetcher = subjectFetcher;
    Tuple<String, String> dataSetIdSplit = DataSetMetaData.splitCombinedId(dataSetId);
    dataSetName = dataSetIdSplit.right();
    ownerId = dataSetIdSplit.left();
    this.typeUri = typeUri;
    userUriCreator = new UserUriCreator(uriHelper);
  }

  @Override
  public Object executeAction(DataFetchingEnvironment environment) {
    final Graph graph = new Graph(environment.getArgument("graph"));
    final String uri = environment.getArgument("uri");
    final Map entity = environment.getArgument("entity");

    Optional<User> userOpt = environment.getGraphQlContext().get("user");
    if (userOpt.isEmpty()) {
      throw new RuntimeException("User should be logged in.");
    }

    User user = userOpt.get();
    Optional<DataSet> dataSetOpt = dataSetRepository.getDataSet(user, ownerId, dataSetName);
    if (dataSetOpt.isEmpty()) {
      throw new RuntimeException("Data set is not available.");
    }

    DataSet dataSet = dataSetOpt.get();
    UserPermissionCheck userPermissionCheck = environment.getGraphQlContext().get("userPermissionCheck");
    if (!userPermissionCheck.hasPermission(dataSet.getMetadata(), Permission.CREATE)) {
      throw new RuntimeException("User should have permissions to create entities of the data set.");
    }

    try (Stream<CursorQuad> quads = dataSet.getQuadStore().getQuadsInGraph(uri, Optional.of(graph))) {
      if (quads.findAny().isPresent()) {
        if (graph.isDefaultGraph()) {
          throw new RuntimeException("Subject with uri '" + uri + "' already exists in the default graph");
        } else {
          throw new RuntimeException("Subject with uri '" + uri + "' already exists in graph '" + graph + "'");
        }
      }
    }

    try {
      dataSet.getImportManager().generateLog(
        dataSet.getMetadata().getBaseUri(), null,
        new GraphQlToRdfPatch(graph.uri(), uri, userUriCreator.create(user),
            new CreateMutationChangeLog(graph, uri, typeUri, entity))
      ).get(); // Wait until the data is processed
    } catch (LogStorageFailedException | JsonProcessingException | InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    return subjectFetcher.getItemInGraph(uri, Optional.of(graph), dataSet);
  }
}

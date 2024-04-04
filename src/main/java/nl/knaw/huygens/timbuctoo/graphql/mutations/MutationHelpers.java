package nl.knaw.huygens.timbuctoo.graphql.mutations;

import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.graphql.mutations.dto.PredicateMutation;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.graphql.security.UserPermissionCheck;
import nl.knaw.huygens.timbuctoo.security.dto.Permission;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class MutationHelpers {
  public static User getUser(DataFetchingEnvironment env) {
    Optional<User> user = env.getGraphQlContext().get("user");
    return user.orElseThrow(() -> new RuntimeException("You are not logged in"));
  }

  public static void checkPermission(DataFetchingEnvironment env, DataSetMetaData dataSetMetaData,
                                     Permission permission) throws RuntimeException {
    UserPermissionCheck userPermissionCheck = env.getGraphQlContext().get("userPermissionCheck");
    if (!userPermissionCheck.hasPermission(dataSetMetaData, permission)) {
      throw new RuntimeException("You do not have permission '" + permission + "' for this data set.");
    }
  }

  public static DataSet getDataSet(DataFetchingEnvironment env, DataSetFetcher fetcher,
                                   String ownerId, String dataSetName) {
    User user = getUser(env);
    return fetcher.getDataSet(user, ownerId, dataSetName)
        .orElseThrow(() -> new RuntimeException("Dataset does not exist"));
  }

  public static DataSet getDataSet(DataFetchingEnvironment env, DataSetFetcher fetcher) {
    String dataSetId = env.getArgument("dataSetId");
    Tuple<String, String> userAndDataSet = DataSetMetaData.splitCombinedId(dataSetId);

    String ownerId = userAndDataSet.left();
    String dataSetName = userAndDataSet.right();

    return getDataSet(env, fetcher, ownerId, dataSetName);
  }

  public static void addMutations(DataSet dataSet, PredicateMutation... mutation)
      throws LogStorageFailedException, ExecutionException, InterruptedException {

    final String baseUri = dataSet.getMetadata().getBaseUri();
    dataSet.getImportManager().generateLog(baseUri, null, new PredicateMutationRdfPatcher(mutation)).get();
  }

  public interface DataSetFetcher {
    Optional<DataSet> getDataSet(User user, String ownerId, String dataSetName);
  }
}

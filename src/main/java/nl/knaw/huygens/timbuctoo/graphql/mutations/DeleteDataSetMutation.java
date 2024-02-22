package nl.knaw.huygens.timbuctoo.graphql.mutations;

import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.NotEnoughPermissionsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DeleteDataSetMutation extends Mutation {
  private static final Logger LOG = LoggerFactory.getLogger(DeleteDataSetMutation.class);
  private final DataSetRepository dataSetRepository;

  public DeleteDataSetMutation(Runnable schemaUpdater, DataSetRepository dataSetRepository) {
    super(schemaUpdater);
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public Object executeAction(DataFetchingEnvironment environment) {
    String combinedId = environment.getArgument("dataSetId");
    Tuple<String, String> userAndDataSet = DataSetMetaData.splitCombinedId(combinedId);
    User user = MutationHelpers.getUser(environment);

    try {
      dataSetRepository.removeDataSet(userAndDataSet.left(), userAndDataSet.right(), user);
      return new RemovedDataSet(combinedId);
    } catch (IOException e) {
      LOG.error("Data set deletion exception", e);
      throw new RuntimeException("Data set could not be deleted");
    } catch (NotEnoughPermissionsException e) {
      throw new RuntimeException("You do not have enough permissions");
    } catch (DataSetRepository.DataSetDoesNotExistException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public record RemovedDataSet(String dataSetId) {
  }
}

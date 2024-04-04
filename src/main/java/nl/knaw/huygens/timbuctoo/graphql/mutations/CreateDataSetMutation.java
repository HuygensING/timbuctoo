package nl.knaw.huygens.timbuctoo.graphql.mutations;

import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.DataSetCreationException;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.IllegalDataSetNameException;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.DataSetWithDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.graphql.mutations.MutationHelpers.getUser;

public class CreateDataSetMutation extends Mutation<DataSetWithDatabase> {
  private static final Logger LOG = LoggerFactory.getLogger(CreateDataSetMutation.class);

  private final DataSetRepository dataSetRepository;

  public CreateDataSetMutation(Runnable schemaUpdater, DataSetRepository dataSetRepository) {
    super(schemaUpdater);
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public DataSetWithDatabase executeAction(DataFetchingEnvironment env) {
    User currentUser = getUser(env);

    String dataSetName = env.getArgument("dataSetName");
    String baseUri = env.getArgument("baseUri");

    try {
      return new DataSetWithDatabase(
        dataSetRepository.createDataSet(currentUser, dataSetName, Optional.ofNullable(baseUri)),
        env.getGraphQlContext().get("userPermissionCheck")
      );
    } catch (DataStoreCreationException e) {
      LOG.error("Data set creation exception", e);
      throw new RuntimeException("Data set could not be created");
    } catch (IllegalDataSetNameException e) {
      throw new RuntimeException("Data set id is not supported: " + e.getMessage());
    } catch (DataSetCreationException e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}

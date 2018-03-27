package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.ContextData;
import nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration.SummaryProp;
import nl.knaw.huygens.timbuctoo.v5.graphql.security.UserPermissionCheck;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_SUMMARYDESCRIPTIONPREDICATE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_SUMMARYIMAGEPREDICATE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_SUMMARYTITLEPREDICATE;

public class SummaryPropsMutationDataFetcher implements DataFetcher {
  private static final Logger LOG = LoggerFactory.getLogger(SummaryPropsMutationDataFetcher.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new GuavaModule());
  private final DataSetRepository dataSetRepository;

  public SummaryPropsMutationDataFetcher(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public Object get(DataFetchingEnvironment env) {
    String dataSetId = env.getArgument("dataSetId");
    String collectionUri = env.getArgument("collectionUri");
    Map viewConfig = env.getArgument("summaryProperties");

    ContextData contextData = env.getContext();
    User user = contextData.getUser().get();
    UserPermissionCheck userPermissionCheck = contextData.getUserPermissionCheck();

    Tuple<String, String> userAndDataSet = DataSetMetaData.splitCombinedId(dataSetId);

    String ownerId = userAndDataSet.getLeft();
    String dataSetName = userAndDataSet.getRight();
    Optional<DataSet> dataSetOpt = dataSetRepository.getDataSet(user, ownerId, dataSetName);
    if (dataSetOpt.isPresent() &&
      userPermissionCheck.getPermissions(dataSetRepository.getDataSet(user, ownerId, dataSetName).get()
        .getMetadata()).contains(Permission.ADMIN)) {
      DataSet dataSet = dataSetOpt.get();
      dataSet.getQuadStore();
      try {
        final String baseUri = dataSet.getMetadata().getBaseUri();
        dataSet.getImportManager().generateLog(
          baseUri,
          baseUri,
          new StringPredicatesRdfCreator(
            dataSet.getQuadStore(),
            ImmutableMap.of(
              Tuple.tuple(collectionUri, TIM_SUMMARYTITLEPREDICATE),
              getValue(viewConfig, "title"),

              Tuple.tuple(collectionUri, TIM_SUMMARYDESCRIPTIONPREDICATE),
              getValue(viewConfig, "description"),

              Tuple.tuple(collectionUri, TIM_SUMMARYIMAGEPREDICATE),
              getValue(viewConfig, "image")
            ),
            baseUri
          )
        ).get();
        return new LazyTypeSubjectReference(collectionUri, dataSet);
      } catch (LogStorageFailedException | InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    } else {
      throw new RuntimeException("Dataset does not exist");
    }
  }

  private Optional<String> getValue(Map viewConfig, String valueName) {

    Object value = viewConfig.get(valueName);
    if (value != null) {
      try {
        String valueAsString = OBJECT_MAPPER.writeValueAsString(value);

        // check if the value can be parsed to SummaryProp
        OBJECT_MAPPER.readValue(valueAsString, SummaryProp.class);

        return of(valueAsString);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(String.format("Could not process '%s' property", valueName));
      } catch (IOException e) {
        throw new RuntimeException(String.format("Could not parse summary prop '%s' for '%s'", value, valueName));
      }
    }

    return empty();
  }

}

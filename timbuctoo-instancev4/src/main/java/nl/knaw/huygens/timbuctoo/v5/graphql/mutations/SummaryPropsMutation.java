package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration.SummaryProp;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto.PredicateMutation;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto.PredicateMutation.replace;
import static nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto.PredicateMutation.value;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_SUMMARYDESCRIPTIONPREDICATE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_SUMMARYIMAGEPREDICATE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_SUMMARYTITLEPREDICATE;

public class SummaryPropsMutation implements DataFetcher {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new GuavaModule());
  private final DataSetRepository dataSetRepository;

  public SummaryPropsMutation(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public Object get(DataFetchingEnvironment env) {
    DataSet dataSet = MutationHelpers.getDataSet(env, dataSetRepository::getDataSet);
    MutationHelpers.checkAdminPermissions(env, dataSet.getMetadata());
    try {
      String collectionUri = env.getArgument("collectionUri");
      Map data = env.getArgument("summaryProperties");
      final PredicateMutation mutation = new PredicateMutation();
      mutation.entity(
        collectionUri,
        getValue(data, "title").map(v -> replace(TIM_SUMMARYTITLEPREDICATE, value(v))).orElse(null),
        getValue(data, "image").map(v -> replace(TIM_SUMMARYIMAGEPREDICATE, value(v))).orElse(null),
        getValue(data, "description").map(v -> replace(TIM_SUMMARYDESCRIPTIONPREDICATE, value(v))).orElse(null)
      );

      MutationHelpers.addMutation(dataSet, mutation);
      return new LazyTypeSubjectReference(collectionUri, dataSet);
    } catch (LogStorageFailedException | InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
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
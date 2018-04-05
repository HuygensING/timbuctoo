package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto.PredicateMutation;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto.PredicateMutation.replace;
import static nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto.PredicateMutation.subject;
import static nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto.PredicateMutation.value;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDFS_LABEL;

public class CollectionMetadataMutation implements DataFetcher {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new GuavaModule());
  private final DataSetRepository dataSetRepository;

  public CollectionMetadataMutation(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public Object get(DataFetchingEnvironment env) {
    DataSet dataSet = MutationHelpers.getDataSet(env, dataSetRepository::getDataSet);
    MutationHelpers.checkAdminPermissions(env, dataSet.getMetadata());
    try {
      String collectionUri = env.getArgument("collectionUri");
      Map data = env.getArgument("metadata");
      final PredicateMutation mutation = new PredicateMutation();
      mutation.entity(
        collectionUri,
        getValue(data, "title").map(v -> replace(RDFS_LABEL, value(v))).orElse(null),
        getValue(data, "archeType").map(v -> replace("http://www.w3.org/2000/01/rdf-schema#subClassOf", subject(v))).orElse(null)
      );

      MutationHelpers.addMutation(dataSet, mutation);
      return new LazyTypeSubjectReference(collectionUri, dataSet);
    } catch (LogStorageFailedException | InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private Optional<String> getValue(Map viewConfig, String valueName) {
    return Optional.ofNullable((String) viewConfig.get(valueName));
  }

}

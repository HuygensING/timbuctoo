package nl.knaw.huygens.timbuctoo.graphql.mutations;

import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.graphql.mutations.dto.PredicateMutation;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.security.dto.Permission;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static nl.knaw.huygens.timbuctoo.graphql.mutations.dto.PredicateMutation.replace;
import static nl.knaw.huygens.timbuctoo.graphql.mutations.dto.PredicateMutation.value;
import static nl.knaw.huygens.timbuctoo.util.RdfConstants.RDFS_LABEL;

public class CollectionMetadataMutation extends Mutation {
  private final DataSetRepository dataSetRepository;

  public CollectionMetadataMutation(Runnable schemaUpdater, DataSetRepository dataSetRepository) {
    super(schemaUpdater);
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public Object executeAction(DataFetchingEnvironment env) {
    DataSet dataSet = MutationHelpers.getDataSet(env, dataSetRepository::getDataSet);
    MutationHelpers.checkPermission(env, dataSet.getMetadata(), Permission.EDIT_COLLECTION_METADATA);
    try {
      String collectionUri = env.getArgument("collectionUri");
      Map data = env.getArgument("metadata");
      final PredicateMutation mutation = new PredicateMutation();
      mutation.entity(
          collectionUri,
          List.of(
              getValue(data, "title").map(v -> replace(RDFS_LABEL, PredicateMutation.value(v))).orElse(null),
              getValue(data, "archeType").map(v -> PredicateMutation.replace("http://www.w3.org/2000/01/rdf-schema#subClassOf", PredicateMutation.subject(v))).orElse(null)
          )
      );

      MutationHelpers.addMutation(dataSet, mutation);
      return new LazyTypeSubjectReference(collectionUri, Optional.empty(), dataSet);
    } catch (LogStorageFailedException | InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private Optional<String> getValue(Map viewConfig, String valueName) {
    return Optional.ofNullable((String) viewConfig.get(valueName));
  }

}

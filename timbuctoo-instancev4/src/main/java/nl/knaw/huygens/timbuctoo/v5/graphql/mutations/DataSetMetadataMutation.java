package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.EntityMetadataProp;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.Metadata;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.MetadataProp;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.SimpleMetadataProp;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.UriMetadataProp;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.ContextData;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DataSetWithDatabase;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto.PredicateMutation;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto.PredicateMutation.MutationOperation;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.v5.graphql.mutations.MutationHelpers.addMutation;
import static nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto.PredicateMutation.getOrCreate;
import static nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto.PredicateMutation.languageString;
import static nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto.PredicateMutation.replace;
import static nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto.PredicateMutation.subject;
import static nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto.PredicateMutation.value;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class DataSetMetadataMutation extends Mutation {
  private final DataSetRepository dataSetRepository;
  private final Metadata metadata;

  public DataSetMetadataMutation(Runnable schemaUpdater, DataSetRepository dataSetRepository,
                                 Metadata metadata) {
    super(schemaUpdater);
    this.dataSetRepository = dataSetRepository;
    this.metadata = metadata;
  }

  @Override
  public Object executeAction(DataFetchingEnvironment env) {
    DataSet dataSet = MutationHelpers.getDataSet(env, dataSetRepository::getDataSet);
    MutationHelpers.checkPermission(env, dataSet.getMetadata(), Permission.EDIT_DATASET_METADATA);

    try {
      Map<?, ?> md = env.getArgument("metadata");
      final String baseUri = dataSet.getMetadata().getBaseUri().endsWith("/") ?
          dataSet.getMetadata().getBaseUri() :
          dataSet.getMetadata().getBaseUri() + "/";

      List<MutationOperation> mutationOperations = createMutationOperations(metadata.getProps(), md, baseUri);
      if (metadata.getRdfType().isPresent()) {
        mutationOperations.add(replace(RDF_TYPE, subject(metadata.getRdfType().get())));
      }

      addMutation(dataSet, new PredicateMutation().entity(baseUri, mutationOperations));

      return new DataSetWithDatabase(dataSet, env.<ContextData>getContext().getUserPermissionCheck());
    } catch (LogStorageFailedException | InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private static List<MutationOperation> createMutationOperations(Map<String, MetadataProp> metadataProps,
                                                                  Map<?, ?> md, String baseUri) {
    return metadataProps
        .keySet()
        .stream()
        .map(name -> withMetadataProp(name, metadataProps.get(name), baseUri, md))
        .collect(Collectors.toList());
  }

  private static MutationOperation withMetadataProp(String name, MetadataProp metadataProp,
                                                    String baseUri, Map<?, ?> md) {
    if (md.containsKey(name)) {
      if (metadataProp instanceof UriMetadataProp && md.get(name) instanceof Map &&
          ((Map<?, ?>) md.get(name)).containsKey("uri")) {
        String uri = (String) ((Map<?, ?>) md.get(name)).get("uri");

        return replace(metadataProp.getPredicate(), subject(uri));
      } else if (metadataProp instanceof SimpleMetadataProp) {
        String value = (String) md.get(name);
        SimpleMetadataProp simpleMetadataProp = (SimpleMetadataProp) metadataProp;

        if (simpleMetadataProp.getLanguage().isPresent()) {
          return replace(metadataProp.getPredicate(), languageString(value, simpleMetadataProp.getLanguage().get()));
        } else {
          return replace(metadataProp.getPredicate(), value(value, simpleMetadataProp.getValueType()));
        }
      } else if (metadataProp instanceof EntityMetadataProp) {
        Map<?, ?> entityMd = (Map<?, ?>) md.get(name);
        EntityMetadataProp entityMetadataProp = (EntityMetadataProp) metadataProp;
        String entityUri = entityMetadataProp.getEntityUriFor(baseUri);
        Map<String, MetadataProp> properties = entityMetadataProp.getProperties();

        List<MutationOperation> mutationOperations = properties
            .keySet()
            .stream()
            .map(propName -> withMetadataProp(propName, properties.get(propName), baseUri, entityMd))
            .collect(Collectors.toList());

        return getOrCreate(metadataProp.getPredicate(), entityUri, mutationOperations);
      }
    }

    return null;
  }
}

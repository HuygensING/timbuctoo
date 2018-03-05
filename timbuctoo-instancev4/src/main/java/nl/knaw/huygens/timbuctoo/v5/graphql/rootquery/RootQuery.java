package nl.knaw.huygens.timbuctoo.v5.graphql.rootquery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetImportStatus;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportStatus;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.EntryImportStatus;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.IllegalDataSetNameException;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.ReadAndMergeWithExistingSchema;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Type;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.SupportedExportFormats;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.RdfWiringFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.ContextData;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DataSetWithDatabase;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.RootData;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema.DerivedSchemaTypeGenerator;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.DeleteDataSetDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.IndexConfigDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.MakePublicDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.ExtendSchemaDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.SummaryPropsMutationDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.ViewConfigDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.CollectionMetadata;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.CollectionMetadataList;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.ImmutableCollectionMetadata;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.ImmutableCollectionMetadataList;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.ImmutableProperty;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.ImmutablePropertyList;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.ImmutableStringList;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.MimeTypeDescription;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.Property;
import nl.knaw.huygens.timbuctoo.v5.graphql.security.UserPermissionCheck;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.io.Resources.getResource;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.HAS_VIEW_CONFIG;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_HASINDEXERCONFIG;

public class RootQuery implements Supplier<GraphQLSchema> {

  private static final Logger LOG = LoggerFactory.getLogger(RootQuery.class);


  private final DataSetRepository dataSetRepository;
  private final SupportedExportFormats supportedFormats;
  private final String archetypes;
  private final RdfWiringFactory wiringFactory;
  private final DerivedSchemaTypeGenerator typeGenerator;
  private final ObjectMapper objectMapper;
  private final SchemaParser schemaParser;
  private final String staticQuery;

  public RootQuery(DataSetRepository dataSetRepository, SupportedExportFormats supportedFormats,
                   String archetypes, RdfWiringFactory wiringFactory,
                   DerivedSchemaTypeGenerator typeGenerator, ObjectMapper objectMapper)
    throws IOException {
    this.dataSetRepository = dataSetRepository;
    this.supportedFormats = supportedFormats;
    this.archetypes = archetypes;
    this.wiringFactory = wiringFactory;
    this.typeGenerator = typeGenerator;
    this.objectMapper = objectMapper;
    staticQuery = Resources.toString(getResource(RootQuery.class, "schema.graphql"), Charsets.UTF_8);
    schemaParser = new SchemaParser();
  }

  public synchronized GraphQLSchema rebuildSchema() {
    final TypeDefinitionRegistry staticQuery = schemaParser.parse(this.staticQuery);
    if (archetypes != null && !archetypes.isEmpty()) {
      staticQuery.merge(schemaParser.parse(
        archetypes +
          "extend type DataSetMetadata {\n" +
          "  archetypes: Archetypes! @passThrough\n" +
          "}\n" +
          "\n")
      );
    }
    TypeDefinitionRegistry registry = new TypeDefinitionRegistry();
    registry.merge(staticQuery);

    final RuntimeWiring.Builder wiring = RuntimeWiring.newRuntimeWiring();

    wiring.type("Query", builder -> builder
      .dataFetcher("promotedDataSets", env -> dataSetRepository.getPromotedDataSets()
        .stream()
        .map(DataSetWithDatabase::new)
        .collect(Collectors.toList()))
      .dataFetcher("allDataSets", env -> dataSetRepository.getDataSets()
        .stream()
        .map(DataSetWithDatabase::new)
        .filter(x -> {
          if (x.isPublished()) {
            return true;
          } else {
            ContextData contextData = env.getContext();
            UserPermissionCheck userPermissionCheck = contextData.getUserPermissionCheck();
            return userPermissionCheck.getPermissions(x.getDataSet().getMetadata()).contains(Permission.READ);
          }
        })
        .collect(Collectors.toList()))
      .dataFetcher("dataSetMetadata", env -> {
        final String dataSetId = env.getArgument("dataSetId");
        ContextData context = env.getContext();
        final User user = context.getUser().orElse(null);

        Tuple<String, String> splitCombinedId = DataSetMetaData.splitCombinedId(dataSetId);

        return dataSetRepository.getDataSet(user, splitCombinedId.getLeft(), splitCombinedId.getRight())
          .map(DataSetWithDatabase::new);
      })
      .dataFetcher("aboutMe", env -> ((RootData) env.getRoot()).getCurrentUser().orElse(null))
      .dataFetcher("availableExportMimetypes", env -> supportedFormats.getSupportedMimeTypes().stream()
        .map(MimeTypeDescription::create)
        .collect(Collectors.toList())
      )
    );
    wiring.type("DataSetMetadata", builder -> builder

      .dataFetcher("currentImportStatus", env -> {
        DataSetMetaData input = env.getSource();
        Optional<User> currentUser = ((RootData) env.getRoot()).getCurrentUser();
        if (!currentUser.isPresent()) {
          throw new RuntimeException("User is not provided");
        }
        return dataSetRepository.getDataSet(
          currentUser.get(),
          input.getOwnerId(),
          input.getDataSetId()
        ).map(dataSet -> dataSet.getImportManager().getImportStatus());
      })
      .dataFetcher("dataSetImportStatus", env -> {
        Optional<User> currentUser = ((RootData) env.getRoot()).getCurrentUser();
        if (!currentUser.isPresent()) {
          throw new RuntimeException("User is not provided");
        }
        DataSetMetaData input = env.getSource();
        return dataSetRepository.getDataSet(
          currentUser.get(),
          input.getOwnerId(),
          input.getDataSetId()
        ).map(dataSet -> dataSet.getImportManager().getDataSetImportStatus());
      })
      .dataFetcher("collectionList", env -> getCollections(env.getSource(), ((ContextData) env.getContext()).getUser()))

      .dataFetcher("collection", env -> {
        String collectionId = (String) env.getArguments().get("collectionId");
        if (collectionId != null && collectionId.endsWith("List")) {
          collectionId = collectionId.substring(0, collectionId.length() - "List".length());
        }
        DataSetMetaData input = env.getSource();
        ContextData context = env.getContext();
        final User user = context.getUser().orElse(null);

        final DataSet dataSet = dataSetRepository.getDataSet(user, input.getOwnerId(), input.getDataSetId()).get();
        final TypeNameStore typeNameStore = dataSet.getTypeNameStore();
        String collectionUri = typeNameStore.makeUri(collectionId);
        if (dataSet.getSchemaStore().getStableTypes() == null ||
          dataSet.getSchemaStore().getStableTypes().get(collectionUri) == null) {
          return null;
        } else {
          return getCollection(dataSet, typeNameStore, dataSet.getSchemaStore().getStableTypes().get(collectionUri));
        }
      })
      .dataFetcher("dataSetId", env -> ((DataSetMetaData) env.getSource()).getCombinedId())
      .dataFetcher("dataSetName", env -> ((DataSetMetaData) env.getSource()).getDataSetId())
      .dataFetcher("ownerId", env -> ((DataSetMetaData) env.getSource()).getOwnerId())
    );
    wiring.type("CurrentImportStatus", builder -> builder
      .dataFetcher("elapsedTime", env -> {
        final String timeUnit = env.getArgument("unit");
        return ((ImportStatus) env.getSource()).getElapsedTime(timeUnit);
      })
    );
    wiring.type("DataSetImportStatus", builder -> builder
      .dataFetcher("lastImportDuration", env -> {
        final String timeUnit = env.getArgument("unit");
        return ((DataSetImportStatus) env.getSource()).getLastImportDuration(timeUnit);
      })
    );
    wiring.type("EntryImportStatus", builder -> builder
      .dataFetcher("elapsedTime", env -> {
        final String timeUnit = env.getArgument("unit");
        return ((EntryImportStatus) env.getSource()).getElapsedTime(timeUnit);
      })

    );
    wiring.type("CollectionMetadata", builder -> builder
      .dataFetcher("indexConfig", env -> {
        SubjectReference source = env.getSource();
        final QuadStore qs = source.getDataSet().getQuadStore();
        try (Stream<CursorQuad> quads = qs.getQuads(source.getSubjectUri(), TIM_HASINDEXERCONFIG, Direction.OUT, "")) {
          final Map result = quads.findFirst()
            .map(q -> {
              try {
                return objectMapper.readValue(q.getObject(), Map.class);
              } catch (IOException e) {
                LOG.error("Value not a Map", e);
                return new HashMap<>();
              }
            })
            .orElse(new HashMap());
          if (!result.containsKey("facet") || !(result.get("facet") instanceof List)) {
            result.put("facet", new ArrayList<>());
          }
          if (!result.containsKey("fullText") || !(result.get("fullText") instanceof List)) {
            result.put("fullText", new ArrayList<>());
          }
          return result;
        }
      })
      .dataFetcher("viewConfig", env -> {
        SubjectReference source = env.getSource();
        final QuadStore qs = source.getDataSet().getQuadStore();
        try (Stream<CursorQuad> quads = qs.getQuads(source.getSubjectUri(), HAS_VIEW_CONFIG, Direction.OUT, "")) {
          return quads.findFirst().map(q -> {
            try {
              return objectMapper.readValue(q.getObject(), List.class);
            } catch (IOException e) {
              LOG.error("view config not available", e);
              return new ArrayList();
            }
          }).orElse(new ArrayList());
        }
      })
    );

    wiring.type("AboutMe", builder -> builder
      .dataFetcher("dataSets", env -> (Iterable) () -> dataSetRepository
        .getDataSetsWithWriteAccess(env.getSource())
        .stream().map(DataSetWithDatabase::new).iterator()
      )
      .dataFetcher("id", env -> ((User) env.getSource()).getPersistentId())
      .dataFetcher("name", env -> ((User) env.getSource()).getDisplayName())
      .dataFetcher("personalInfo", env -> "http://example.com")
      .dataFetcher("canCreateDataSet", env -> true)
    );

    wiring.type("Mutation", builder -> builder
      .dataFetcher("setViewConfig", new ViewConfigDataFetcher(dataSetRepository))
      .dataFetcher("setSummaryProperties", new SummaryPropsMutationDataFetcher(dataSetRepository))
      .dataFetcher("setIndexConfig", new IndexConfigDataFetcher(dataSetRepository))
      .dataFetcher("createDataSet", (DataFetchingEnvironment environment) -> {
        Optional<User> currentUser = ((RootData) environment.getRoot()).getCurrentUser();
        if (!currentUser.isPresent()) {
          throw new RuntimeException("User is not provided");
        }

        String dataSetName = environment.getArgument("dataSetName");
        try {
          Optional<DataSet> dataSetOpt =
            dataSetRepository.getDataSet(
              currentUser.get(),
              "u" + currentUser.get().getPersistentId(),
              dataSetName
            );
          if (dataSetOpt.isPresent()) {
            return dataSetOpt.get().getMetadata();
          }
          return dataSetRepository.createDataSet(currentUser.get(), dataSetName).getMetadata();
        } catch (DataStoreCreationException e) {
          LOG.error("Data set creation exception", e);
          throw new RuntimeException("Data set could not be created");
        } catch (IllegalDataSetNameException e) {
          LOG.error("Data set creation exception", e);
          throw new RuntimeException("Data set id is not supported: " + e.getMessage());
        }
      }).dataFetcher("deleteDataSet", new DeleteDataSetDataFetcher(dataSetRepository))
      .dataFetcher("publish", new MakePublicDataFetcher(dataSetRepository))
      .dataFetcher("extendSchema", new ExtendSchemaDataFetcher(dataSetRepository))
    );

    wiring.wiringFactory(wiringFactory);
    StringBuilder root = new StringBuilder("type DataSets {\n sillyWorkaroundWhenNoDataSetsAreVisible: Boolean\n");

    boolean[] dataSetAvailable = new boolean[]{false};


    dataSetRepository.getDataSets().forEach(dataSet -> {
      final DataSetMetaData dataSetMetaData = dataSet.getMetadata();
      final String name = dataSetMetaData.getCombinedId();


      Map<String, Type> types = dataSet.getSchemaStore().getStableTypes();

      ReadAndMergeWithExistingSchema readAndMergeWithExistingSchema = new ReadAndMergeWithExistingSchema();

      types = readAndMergeWithExistingSchema.readAndMergeExistingSchemas(dataSet, types);

      if (types != null) {

        dataSetAvailable[0] = true;
        root.append("  ")
          .append(name)
          .append(":")
          .append(name)
          .append(" @dataSet(userId:\"")
          .append(dataSetMetaData.getOwnerId())
          .append("\", dataSetId:\"")
          .append(dataSetMetaData.getDataSetId())
          .append("\")\n");

        wiring.type(name, c -> c
          .dataFetcher("metadata", env -> new DataSetWithDatabase(dataSet))
        );

        final String schema = typeGenerator.makeGraphQlTypes(
          name,
          types,
          dataSet.getTypeNameStore()
        );

        staticQuery.merge(schemaParser.parse(schema));
      }
    });
    root.append("}\n\nextend type Query {\n  #The actual dataSets\n  dataSets: DataSets @passThrough\n}\n\n");

    if (dataSetAvailable[0]) {
      staticQuery.merge(schemaParser.parse(root.toString()));
    }

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    return schemaGenerator.makeExecutableSchema(staticQuery, wiring.build());
  }

  public CollectionMetadataList getCollections(DataSetMetaData input, Optional<User> userOpt) {
    final User user = userOpt.orElse(null);
    final DataSet dataSet = dataSetRepository.getDataSet(user, input.getOwnerId(), input.getDataSetId()).get();

    final TypeNameStore typeNameStore = dataSet.getTypeNameStore();
    final List<CollectionMetadata> colls = dataSet
      .getSchemaStore()
      .getStableTypes().values().stream()
      .map(x -> {
        return getCollection(dataSet, typeNameStore, x);
      })
      .collect(Collectors.toList());
    return ImmutableCollectionMetadataList.builder()
      .nextCursor(Optional.empty())
      .prevCursor(Optional.empty())
      .items(colls)
      .build();
  }

  public CollectionMetadata getCollection(DataSet dataSet, TypeNameStore typeNameStore, Type collectionType) {
    final long occurrences = collectionType.getSubjectsWithThisType();
    final String collectionId = typeNameStore.makeGraphQlname(collectionType.getName());
    final String fullyQualifiedTypeName = dataSet.getMetadata().getCombinedId() + "_" + collectionId;
    return ImmutableCollectionMetadata.builder()
      .subjectUri(collectionType.getName())
      .types(Collections.emptySet())
      .dataSet(dataSet)
      .collectionId(collectionId)
      .collectionListId(collectionId + "List")
      .itemType(fullyQualifiedTypeName)
      .total(occurrences)
      .properties(ImmutablePropertyList.builder()
        .prevCursor(Optional.empty())
        .nextCursor(Optional.empty())
        .items(() -> collectionType.getPredicates().stream().map(pred -> {
            return (Property) ImmutableProperty.builder()
              .density(getDensity(occurrences, pred.getSubjectsWithThisPredicate()))
              .isList(pred.isList())
              .uri(pred.getName())
              .shortenedUri(typeNameStore.shorten(pred.getName()))
              .isInverse(pred.getDirection() == Direction.IN)
              .name(typeNameStore.makeGraphQlnameForPredicate(
                pred.getName(), pred.getDirection(), pred.isList())
              )
              .referencedCollections(ImmutableStringList.builder()
                .prevCursor(Optional.empty())
                .nextCursor(Optional.empty())
                .items(() -> pred.getUsedReferenceTypes().stream()
                  .filter(t -> !t.equals(RdfConstants.UNKNOWN))
                  .map(typeNameStore::makeGraphQlname)
                  .iterator())
                .build()
              )
              .isValueType(!pred.getValueTypes().isEmpty())
              .build();
          }
        ).iterator())
        .build())
      .build();
  }

  public long getDensity(long allOccurrences, long predOccurrences) {
    final long percentage = allOccurrences > 0 ? (predOccurrences * 100) / allOccurrences : 0;
    if (percentage == 0 && predOccurrences > 0) {
      return 1;//don't return 0 unless it's actually an empty predicate
    } else if (percentage == 100 && predOccurrences < allOccurrences) {
      return 99;//don't return 100 unless it's actually a 100%
    } else {
      return percentage;
    }
  }

  @Override
  public GraphQLSchema get() {
    return rebuildSchema();
  }

}

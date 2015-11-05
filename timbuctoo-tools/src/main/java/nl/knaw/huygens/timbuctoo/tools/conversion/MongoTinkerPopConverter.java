package nl.knaw.huygens.timbuctoo.tools.conversion;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import com.tinkerpop.blueprints.Graph;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MongoTinkerPopConverter {
  private static final Logger LOG = LoggerFactory.getLogger(MongoTinkerPopConverter.class);
  private Graph graph;
  private TinkerPopConversionStorage graphStorage;
  private TypeRegistry registry;
  private ElementConverterFactory converterFactory;
  private MongoConversionStorage mongoStorage;
  private IdGenerator idGenerator;
  private Map<String, String> oldIdNewIdMap;
  private Map<String, Object> oldIdLatestVertexIdMap;

  public MongoTinkerPopConverter(TinkerPopConversionStorage graphStorage, Graph graph, IdGenerator idGenerator, ElementConverterFactory converterFactory, TypeRegistry registry,
      MongoConversionStorage mongoStorage) {
    this.graphStorage = graphStorage;
    this.idGenerator = idGenerator;
    this.graph = graph;
    this.converterFactory = converterFactory;
    this.registry = registry;
    this.mongoStorage = mongoStorage;
    this.oldIdNewIdMap = Maps.newHashMap();
    oldIdLatestVertexIdMap = Maps.newHashMap();

  }

  public static void main(String[] args) throws Exception {
    Stopwatch stopwatch = Stopwatch.createStarted();
    Injector injector = ToolsInjectionModule.createInjectorWithoutSolr();
    Graph graph = injector.getInstance(Graph.class);
    ElementConverterFactory converterFactory = injector.getInstance(ElementConverterFactory.class);
    IdGenerator idGenerator = injector.getInstance(IdGenerator.class);
    TinkerPopConversionStorage graphStorage = injector.getInstance(TinkerPopConversionStorage.class);

    MongoConversionStorage mongoStorage = injector.getInstance(MongoConversionStorage.class);
    TypeRegistry registry = injector.getInstance(TypeRegistry.class);

    MongoTinkerPopConverter converter = new MongoTinkerPopConverter(graphStorage, graph, idGenerator, converterFactory, registry, mongoStorage);

    try {
      converter.convertSystemEntities();
      converter.convertDomainEntities();
    } finally {
      graph.shutdown();
      mongoStorage.close();
    }

    LOG.info("Done in {}", stopwatch.stop());
  }

  public void convertDomainEntities() throws StorageException, IllegalAccessException, InterruptedException {
    for (Class<? extends DomainEntity> type : registry.getPrimitiveDomainEntityTypes()) {
      if (Relation.class.isAssignableFrom(type)) {
        createRelationConverter().convert();
      } else {
        createDomainEntityConverter(type).convert();
      }
    }
  }

  private RelationCollectionConverter createRelationConverter() {
    return new RelationCollectionConverter(mongoStorage, graph, graphStorage, registry, idGenerator, oldIdNewIdMap, oldIdLatestVertexIdMap);
  }

  private <T extends DomainEntity> DomainEntityCollectionConverter<T> createDomainEntityConverter(Class<T> type) {
    return new DomainEntityCollectionConverter<T>(type, graph, graphStorage, idGenerator, converterFactory, mongoStorage, registry, oldIdNewIdMap, oldIdLatestVertexIdMap);
  }

  public void convertSystemEntities() throws Exception {
    for (Class<? extends SystemEntity> type : registry.getSystemEntityTypes()) {
      if (!SearchResult.class.isAssignableFrom(type)) { //Search results don't need to be converted because we delete them periodically anyway
        SystemEntityCollectionConverter<? extends SystemEntity> systemEntityConverter = createSystemEntityConverter(type);
        systemEntityConverter.convert();
      }
    }

  }

  private <T extends SystemEntity> SystemEntityCollectionConverter<T> createSystemEntityConverter(Class<T> type) {
    return new SystemEntityCollectionConverter<T>(type, mongoStorage, graph, graphStorage, converterFactory, idGenerator, oldIdNewIdMap);
  }

}

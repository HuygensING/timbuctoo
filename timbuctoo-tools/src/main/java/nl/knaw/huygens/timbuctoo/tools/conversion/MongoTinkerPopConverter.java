package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import com.tinkerpop.blueprints.Graph;

public class MongoTinkerPopConverter {
  private static final Logger LOG = LoggerFactory.getLogger(MongoTinkerPopConverter.class);
  private Graph graph;
  private GraphStorage graphStorage;
  private TypeRegistry registry;
  private ElementConverterFactory converterFactory;
  private MongoConversionStorage mongoStorage;
  private IdGenerator idGenerator;
  private Map<String, String> oldIdNewIdMap;

  public MongoTinkerPopConverter(GraphStorage graphStorage, Graph graph, IdGenerator idGenerator, ElementConverterFactory converterFactory, TypeRegistry registry, MongoConversionStorage mongoStorage) {
    this.graphStorage = graphStorage;
    this.idGenerator = idGenerator;
    this.graph = graph;
    this.converterFactory = converterFactory;
    this.registry = registry;
    this.mongoStorage = mongoStorage;
    oldIdNewIdMap = Maps.newConcurrentMap();
  }

  public static void main(String[] args) throws Exception {
    Stopwatch stopwatch = Stopwatch.createStarted();
    Injector injector = ToolsInjectionModule.createInjector();
    Graph graph = injector.getInstance(Graph.class);
    ElementConverterFactory converterFactory = injector.getInstance(ElementConverterFactory.class);
    IdGenerator idGenerator = injector.getInstance(IdGenerator.class);
    GraphStorage graphStorage = injector.getInstance(GraphStorage.class);

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
    return null;
  }

  private <T extends DomainEntity> DomainEntityCollectionConverter<T> createDomainEntityConverter(Class<T> type) {
    return new DomainEntityCollectionConverter<T>(type, graph, graphStorage, idGenerator, converterFactory, mongoStorage, oldIdNewIdMap, registry);
  }

  public void convertSystemEntities() throws Exception {
    for (Class<? extends SystemEntity> type : registry.getSystemEntityTypes()) {
      SystemEntityCollectionConverter<? extends SystemEntity> systemEntityConverter = createSystemEntityConverter(type);
      systemEntityConverter.convert();
    }

  }

  private <T extends SystemEntity> SystemEntityCollectionConverter<T> createSystemEntityConverter(Class<T> type) {
    return new SystemEntityCollectionConverter<T>(type, mongoStorage, graph, graphStorage, converterFactory, idGenerator, oldIdNewIdMap);
  }

}

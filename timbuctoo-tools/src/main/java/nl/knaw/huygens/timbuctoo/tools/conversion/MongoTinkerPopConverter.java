package nl.knaw.huygens.timbuctoo.tools.conversion;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.inject.Injector;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class MongoTinkerPopConverter {
  private static final Logger LOG = LoggerFactory.getLogger(MongoTinkerPopConverter.class);
  private Graph graph;
  private GraphStorage graphStorage;
  private TypeRegistry registry;
  private ElementConverterFactory converterFactory;
  private MongoStorage mongoStorage;
  private IdGenerator idGenerator;

  public MongoTinkerPopConverter(GraphStorage graphStorage, Graph graph, IdGenerator idGenerator, ElementConverterFactory converterFactory, TypeRegistry registry, MongoStorage mongoStorage) {
    this.graphStorage = graphStorage;
    this.idGenerator = idGenerator;
    this.graph = graph;
    this.converterFactory = converterFactory;
    this.registry = registry;
    this.mongoStorage = mongoStorage;
  }

  public static void main(String[] args) throws Exception {
    LOG.info("Start importing system entities");

    Injector injector = ToolsInjectionModule.createInjector();
    Graph graph = injector.getInstance(Graph.class);
    ElementConverterFactory converterFactory = injector.getInstance(ElementConverterFactory.class);
    IdGenerator idGenerator = injector.getInstance(IdGenerator.class);
    GraphStorage graphStorage = injector.getInstance(GraphStorage.class);

    MongoStorage mongoStorage = injector.getInstance(MongoStorage.class);
    TypeRegistry registry = injector.getInstance(TypeRegistry.class);

    MongoTinkerPopConverter converter = new MongoTinkerPopConverter(graphStorage, graph, idGenerator, converterFactory, registry, mongoStorage);

    converter.convertSystemEntities();

    graph.shutdown();
    mongoStorage.close();

  }

  private void convertSystemEntities() throws Exception {
    Stopwatch stopwatch = Stopwatch.createStarted();

    for (Class<? extends SystemEntity> type : registry.getSystemEntityTypes()) {
      LOG.info("converting {}", type.getSimpleName());
      convertSystemEntitiesOf(type);
    }

    LOG.info("Done in {}", stopwatch.stop());

  }

  private <T extends SystemEntity> void convertSystemEntitiesOf(Class<T> type) throws StorageException, ConversionException, IllegalAccessException {
    for (StorageIterator<T> iterator = mongoStorage.getSystemEntities(type); iterator.hasNext();) {
      T entity = iterator.next();

      convertEntity(type, entity);
    }
  }

  private <T extends Entity> void convertEntity(Class<T> type, T entity) throws ConversionException, StorageException, IllegalAccessException {
    EntityConversionChecker<T> conversionChecker = new EntityConversionChecker<T>(type, mongoStorage, graphStorage);
    String oldId = entity.getId();
    String newId = addNewId(type, entity);

    LOG.info("Converting \"{}\" with old id \"{}\" and new id \"{}\"", type.getSimpleName(), oldId, newId);

    addToGraph(type, entity);

    conversionChecker.verifyConversion(oldId, newId);
  }

  public <T extends Entity> void addToGraph(Class<T> type, T relationType) throws ConversionException {
    VertexConverter<T> converter = converterFactory.forType(type);

    Vertex vertex = graph.addVertex(null);
    converter.addValuesToElement(vertex, relationType);
  }

  public <T extends Entity> String addNewId(Class<T> type, T entity) {
    // add a new id, the old Id range was not dependable enough
    String newId = idGenerator.nextIdFor(type);
    entity.setId(newId);
    return newId;
  }
}

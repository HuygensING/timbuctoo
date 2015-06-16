package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.HashMap;
import java.util.Iterator;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
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
import com.google.common.collect.Maps;
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
  private HashMap<String, String> oldIdNewIdMap;

  public MongoTinkerPopConverter(GraphStorage graphStorage, Graph graph, IdGenerator idGenerator, ElementConverterFactory converterFactory, TypeRegistry registry, MongoStorage mongoStorage) {
    this.graphStorage = graphStorage;
    this.idGenerator = idGenerator;
    this.graph = graph;
    this.converterFactory = converterFactory;
    this.registry = registry;
    this.mongoStorage = mongoStorage;
    oldIdNewIdMap = Maps.newHashMap();
  }

  public static void main(String[] args) throws Exception {
    Stopwatch stopwatch = Stopwatch.createStarted();
    Injector injector = ToolsInjectionModule.createInjector();
    Graph graph = injector.getInstance(Graph.class);
    ElementConverterFactory converterFactory = injector.getInstance(ElementConverterFactory.class);
    IdGenerator idGenerator = injector.getInstance(IdGenerator.class);
    GraphStorage graphStorage = injector.getInstance(GraphStorage.class);

    MongoStorage mongoStorage = injector.getInstance(MongoStorage.class);
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

  private void convertDomainEntities() throws StorageException, IllegalAccessException {
    for (Class<? extends DomainEntity> type : registry.getDomainEntityTypes()) {
      LOG.info("converting {}", type.getSimpleName());
      if (!Relation.class.isAssignableFrom(type)) {
        convertDomainEntitiesOf(type);
      }
    }
  }

  private <T extends DomainEntity> void convertDomainEntitiesOf(Class<T> type) throws StorageException, IllegalAccessException {
    //    int i = 0;
    for (StorageIterator<T> iterator = mongoStorage.getDomainEntities(type); iterator.hasNext();) {
      T entity = iterator.next();

      convertEntity(type, entity);
      //      i++;
      //      if (i >= 5) {
      //        break;
      //      }
    }
  }

  private void convertSystemEntities() throws Exception {

    for (Class<? extends SystemEntity> type : registry.getSystemEntityTypes()) {
      LOG.info("converting {}", type.getSimpleName());
      convertSystemEntitiesOf(type);
    }

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
    String newId = addNewIdToEntity(type, entity);

    LOG.info("Converting \"{}\" with old id \"{}\" and new id \"{}\"", type.getSimpleName(), oldId, newId);

    addPropertiesToVertex(type, entity, getVertex(entity.getId(), entity.getRev()));

    conversionChecker.verifyConversion(oldId, newId);
  }

  private <T extends Entity> void addPropertiesToVertex(Class<T> type, T entity, Vertex vertex) throws ConversionException {
    VertexConverter<T> converter = converterFactory.forType(type);

    converter.addValuesToElement(vertex, entity);
  }

  private Vertex getVertex(String id, int rev) {
    Iterable<Vertex> vertices = graph.query().has(Entity.ID_DB_PROPERTY_NAME, id)//
        .has(Entity.REVISION_PROPERTY_NAME, rev).vertices();

    Iterator<Vertex> iterator = vertices.iterator();
    return iterator.hasNext() ? iterator.next() : graph.addVertex(null);
  }

  private <T extends Entity> String addNewIdToEntity(Class<T> type, T entity) {
    String oldId = entity.getId();

    String newId = null;
    // if another variation is already converted reuse the id
    if (oldIdNewIdMap.containsKey(oldId)) {
      newId = oldIdNewIdMap.get(oldId);
    } else {
      // add a new id, the old Id range was not dependable enough
      newId = idGenerator.nextIdFor(type);
      oldIdNewIdMap.put(oldId, newId);
    }

    entity.setId(newId);
    return newId;
  }
}

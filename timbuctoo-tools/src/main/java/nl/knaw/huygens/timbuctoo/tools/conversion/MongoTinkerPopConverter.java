package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.HashMap;
import java.util.List;

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
import com.google.common.collect.Lists;
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
    for (Class<? extends DomainEntity> type : registry.getPrimitiveDomainEntityTypes()) {

      if (!Relation.class.isAssignableFrom(type)) {
        convertDomainEntitiesOf(type);
      }
    }
  }

  private <T extends DomainEntity> void convertDomainEntitiesOf(Class<T> type) throws StorageException, IllegalAccessException {
    VertexConverter<T> converter = converterFactory.forType(type);
    EntityConversionChecker<T> conversionChecker = new EntityConversionChecker<T>(type, mongoStorage, graphStorage);

    LOG.info("Converting {}", type.getSimpleName());

    for (StorageIterator<T> iterator = mongoStorage.getDomainEntities(type); iterator.hasNext();) {
      List<Class<? extends DomainEntity>> variantTypes = Lists.newArrayList();
      T entity = iterator.next();

      String oldId = entity.getId();
      String newId = addNewIdToEntity(type, entity);

      Vertex vertex = graph.addVertex(null);

      converter.addValuesToElement(vertex, entity);

      for (T variant : mongoStorage.getAllVariations(type, oldId)) {
        variant.setId(newId);
        addVariantToVertex(vertex, variant);
        variantTypes.add(variant.getClass());
      }

      verifyConversion(oldId, newId, variantTypes);
      conversionChecker.verifyConversion(oldId, newId);

    }

    System.out.println();
  }

  private void verifyConversion(String oldId, String newId, List<Class<? extends DomainEntity>> variantTypes) throws IllegalArgumentException, IllegalAccessException, StorageException {
    for (Class<? extends DomainEntity> type : variantTypes) {
      EntityConversionChecker<? extends DomainEntity> entityConversionChecker = getEntityConverter(type);

      entityConversionChecker.verifyConversion(oldId, newId);
    }
  }

  private <T extends DomainEntity> EntityConversionChecker<T> getEntityConverter(Class<T> type) {
    return new EntityConversionChecker<T>(type, mongoStorage, graphStorage);
  }

  private <T extends DomainEntity> void addVariantToVertex(Vertex vertex, T variant) throws ConversionException {
    @SuppressWarnings("unchecked")
    Class<T> type = (Class<T>) variant.getClass();
    VertexConverter<T> converter = converterFactory.forType(type);

    converter.addValuesToElement(vertex, variant);
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

      convertSystemEntity(type, entity);
    }
  }

  private <T extends SystemEntity> Vertex convertSystemEntity(Class<T> type, T entity) throws ConversionException, StorageException, IllegalAccessException {
    EntityConversionChecker<T> conversionChecker = new EntityConversionChecker<T>(type, mongoStorage, graphStorage);
    String oldId = entity.getId();
    String newId = addNewIdToEntity(type, entity);

    LOG.info("Converting \"{}\" with old id \"{}\" and new id \"{}\"", type.getSimpleName(), oldId, newId);

    Vertex vertex = graph.addVertex(null);
    addPropertiesToVertex(type, entity, vertex);

    conversionChecker.verifyConversion(oldId, newId);

    return vertex;
  }

  private <T extends Entity> void addPropertiesToVertex(Class<T> type, T entity, Vertex vertex) throws ConversionException {
    VertexConverter<T> converter = converterFactory.forType(type);

    converter.addValuesToElement(vertex, entity);
  }

  private <T extends Entity> String addNewIdToEntity(Class<T> type, T entity) {
    String oldId = entity.getId();

    String newId = idGenerator.nextIdFor(type);
    oldIdNewIdMap.put(oldId, newId);

    entity.setId(newId);
    return newId;
  }
}

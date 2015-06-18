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
  private MongoConversionStorage mongoStorage;
  private IdGenerator idGenerator;
  private HashMap<String, String> oldIdNewIdMap;

  public MongoTinkerPopConverter(GraphStorage graphStorage, Graph graph, IdGenerator idGenerator, ElementConverterFactory converterFactory, TypeRegistry registry, MongoConversionStorage mongoStorage) {
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

  private void convertDomainEntities() throws StorageException, IllegalAccessException {
    for (Class<? extends DomainEntity> type : registry.getPrimitiveDomainEntityTypes()) {

      if (!Relation.class.isAssignableFrom(type)) {
        convertDomainEntitiesOf(type);
      }
    }
  }

  private <T extends DomainEntity> void convertDomainEntitiesOf(Class<T> type) throws StorageException, IllegalAccessException {
    VertexConverter<T> converter = converterFactory.forType(type);
    LOG.info("Converting {}", type.getSimpleName());

    for (StorageIterator<T> iterator = mongoStorage.getDomainEntities(type); iterator.hasNext();) {
      T entity = iterator.next();
      String oldId = entity.getId();
      String newId = mapOldIdtoNewId(type, entity);

      AllVersionVariationMap<T> versions = mongoStorage.getAllVersionVariationsMapOf(type, oldId);
      for (Integer revision : versions.revisionsInOrder()) {

        addVariationsToVertex(type, converter, versions.get(revision), oldId, newId, revision);
      }
    }

    System.out.println();
  }

  private <T extends DomainEntity> void addVariationsToVertex(Class<T> type, VertexConverter<T> converter, List<T> allVariations, String oldId, String newId, Integer revision)
      throws ConversionException, StorageException, IllegalAccessException {
    List<Class<? extends DomainEntity>> variantTypes = Lists.newArrayList();

    Vertex vertex = graph.addVertex(null);

    for (T variant : allVariations) {
      variant.setId(newId);
      addVariantToVertex(vertex, variant);
      variantTypes.add(variant.getClass());
    }

    verifyConversion(variantTypes, oldId, newId, revision);
  }

  private void verifyConversion(List<Class<? extends DomainEntity>> variantTypes, String oldId, String newId, Integer revision) throws IllegalArgumentException, IllegalAccessException,
      StorageException {
    for (Class<? extends DomainEntity> type : variantTypes) {
      DomainEntityConversionVerifier<? extends DomainEntity> conversionVerifier = getEntityConverter(type, revision);
      conversionVerifier.verifyConversion(oldId, newId);
    }
  }

  private <T extends DomainEntity> DomainEntityConversionVerifier<T> getEntityConverter(Class<T> type, int revision) {
    return new DomainEntityConversionVerifier<T>(type, mongoStorage, graphStorage, revision);
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
    SystemEntityConversionVerifier<T> conversionChecker = new SystemEntityConversionVerifier<T>(type, mongoStorage, graphStorage);
    String oldId = entity.getId();
    String newId = addNewIdToEntity(type, entity);

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
    String newId = mapOldIdtoNewId(type, entity);

    entity.setId(newId);
    return newId;
  }

  public <T extends Entity> String mapOldIdtoNewId(Class<T> type, T entity) {
    String oldId = entity.getId();

    String newId = idGenerator.nextIdFor(type);
    oldIdNewIdMap.put(oldId, newId);
    return newId;
  }
}

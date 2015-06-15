package nl.knaw.huygens.timbuctoo.tools.conversion;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.RelationType;
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

  }

  private void convertSystemEntities() throws Exception {
    Stopwatch stopwatch = Stopwatch.createStarted();
    EntityConversionChecker conversionChecker = new EntityConversionChecker(RelationType.class, mongoStorage, graphStorage);
    for (StorageIterator<RelationType> iterator = mongoStorage.getSystemEntities(RelationType.class); iterator.hasNext();) {
      RelationType relationType = iterator.next();

      String oldId = relationType.getId();
      String newId = addNewId(relationType);

      LOG.info("Converting \"{}\" with old id \"{}\" and new id \"{}\"", RelationType.class.getSimpleName(), oldId, newId);

      addToGraph(relationType);

      conversionChecker.verifyConversion(oldId, newId);
    }

    LOG.info("Done in {}", stopwatch.stop());

  }

  public void addToGraph(RelationType relationType) throws ConversionException {
    VertexConverter<RelationType> converter = converterFactory.forType(RelationType.class);

    Vertex vertex = graph.addVertex(null);
    converter.addValuesToElement(vertex, relationType);
  }

  public String addNewId(RelationType relationType) {
    // add a new id, the old Id range was not dependable enough
    String newId = idGenerator.nextIdFor(RelationType.class);
    relationType.setId(newId);
    return newId;
  }
}

package nl.knaw.huygens.timbuctoo.tools.conversion;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SystemEntityCollectionConverter<T extends SystemEntity> {
  private static final Logger LOG = LoggerFactory.getLogger(SystemEntityCollectionConverter.class);
  private final Class<T> type;
  private final MongoConversionStorage mongoStorage;
  private final Graph graph;
  private final TinkerPopConversionStorage graphStorage;
  private final ElementConverterFactory converterFactory;
  private final IdGenerator idGenerator;
  private final Map<String, String> oldIdNewIdMap;

  public SystemEntityCollectionConverter(Class<T> type, MongoConversionStorage mongoStorage, Graph graph, TinkerPopConversionStorage graphStorage, ElementConverterFactory converterFactory,
      IdGenerator idGenerator, Map<String, String> oldIdNewIdMap) {
    this.type = type;
    this.mongoStorage = mongoStorage;
    this.graph = graph;
    this.graphStorage = graphStorage;
    this.converterFactory = converterFactory;
    this.idGenerator = idGenerator;
    this.oldIdNewIdMap = oldIdNewIdMap;
  }

  public void convert() throws StorageException, ConversionException, IllegalAccessException {
    LOG.info("Converting {}", type.getSimpleName());
    for (StorageIterator<T> iterator = mongoStorage.getSystemEntities(type); iterator.hasNext();) {
      T entity = iterator.next();

      convertSystemEntity(type, entity);
    }
  }

  private Vertex convertSystemEntity(Class<T> type, T entity) throws ConversionException, StorageException, IllegalAccessException {
    SystemEntityConversionVerifier<T> conversionChecker = new SystemEntityConversionVerifier<T>(type, mongoStorage, graphStorage);
    String oldId = entity.getId();
    String newId = mapOldIdtoNewId(type, entity);
    entity.setId(newId);

    Vertex vertex = graph.addVertex(null);
    vertex.setProperty(ElementFields.IS_LATEST, true);
    addPropertiesToVertex(type, entity, vertex);

    conversionChecker.verifyConversion(oldId, newId, vertex.getId());

    return vertex;
  }

  private <U extends Entity> void addPropertiesToVertex(Class<T> type, T entity, Vertex vertex) throws ConversionException {
    VertexConverter<T> converter = converterFactory.forType(type);

    converter.addValuesToElement(vertex, entity);
  }

  private <U extends Entity> String mapOldIdtoNewId(Class<T> type, T entity) {
    String oldId = entity.getId();

    String newId = idGenerator.nextIdFor(type);
    oldIdNewIdMap.put(oldId, newId);
    return newId;
  }
}

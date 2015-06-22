package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Graph;

public class DomainEntityCollectionConverter<T extends DomainEntity> {
  private static final Logger LOG = LoggerFactory.getLogger(DomainEntityCollectionConverter.class);

  private final Class<T> type;
  private final IdGenerator idGenerator;

  private final MongoConversionStorage mongoStorage;
  private final Map<String, String> oldIdNewIdMap;

  private DomainEntityConverter entityConverter;

  public DomainEntityCollectionConverter(Class<T> type, Graph graph, GraphStorage graphStorage, IdGenerator idGenerator, ElementConverterFactory converterFactory, MongoConversionStorage mongoStorage,
      Map<String, String> oldIdNewIdMap, TypeRegistry typeRegistry) {
    this(type, idGenerator, mongoStorage, oldIdNewIdMap, new DomainEntityConverter(mongoStorage, idGenerator, graph, graphStorage, typeRegistry));
  }

  public DomainEntityCollectionConverter(Class<T> type, IdGenerator idGenerator, MongoConversionStorage mongoStorage, Map<String, String> oldIdNewIdMap, DomainEntityConverter entityConverter) {
    this.type = type;
    this.idGenerator = idGenerator;
    this.mongoStorage = mongoStorage;
    this.oldIdNewIdMap = oldIdNewIdMap;
    this.entityConverter = entityConverter;

  }

  public void convert() throws StorageException, IllegalAccessException {
    LOG.info("Converting {}", type.getSimpleName());

    for (StorageIterator<T> iterator = mongoStorage.getDomainEntities(type); iterator.hasNext();) {
      T entity = iterator.next();
      String oldId = entity.getId();
      mapOldIdtoNewId(type, entity.getId());

      String newId = entityConverter.convert(type, oldId);
      oldIdNewIdMap.put(oldId, newId);
    }

    System.out.println();
  }

  private <U extends Entity> String mapOldIdtoNewId(Class<U> type, String oldId) {
    String newId = idGenerator.nextIdFor(type);
    oldIdNewIdMap.put(oldId, newId);
    return newId;
  }
}

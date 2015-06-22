package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

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
import com.tinkerpop.blueprints.TransactionalGraph;

public class DomainEntityCollectionConverter<T extends DomainEntity> implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(DomainEntityCollectionConverter.class);

  private final Class<T> type;
  private final IdGenerator idGenerator;

  private final MongoConversionStorage mongoStorage;
  private final Map<String, String> oldIdNewIdMap;

  private DomainEntityConverter entityConverter;
  private CountDownLatch countDownLatch;

  private Graph graph;

  public DomainEntityCollectionConverter(Class<T> type, Graph graph, GraphStorage graphStorage, IdGenerator idGenerator, ElementConverterFactory converterFactory, MongoConversionStorage mongoStorage,
      Map<String, String> oldIdNewIdMap, TypeRegistry typeRegistry, CountDownLatch countDownLatch) {
    this(type, idGenerator, mongoStorage, oldIdNewIdMap, new DomainEntityConverter(mongoStorage, idGenerator, graph, graphStorage, typeRegistry), countDownLatch);
    this.graph = graph;
  }

  public DomainEntityCollectionConverter(Class<T> type, IdGenerator idGenerator, MongoConversionStorage mongoStorage, Map<String, String> oldIdNewIdMap, DomainEntityConverter entityConverter,
      CountDownLatch countDownLatch) {
    this.type = type;
    this.idGenerator = idGenerator;
    this.mongoStorage = mongoStorage;
    this.oldIdNewIdMap = oldIdNewIdMap;
    this.entityConverter = entityConverter;
    this.countDownLatch = countDownLatch;

  }

  public void convert() {
    String simpleName = type.getSimpleName();
    LOG.info("Start converting for {}", simpleName);
    try {
      for (StorageIterator<T> iterator = mongoStorage.getDomainEntities(type); iterator.hasNext();) {
        T entity = iterator.next();
        String oldId = entity.getId();
        mapOldIdtoNewId(type, entity.getId());

        try {
          String newId = entityConverter.convert(type, oldId);
          oldIdNewIdMap.put(oldId, newId);
        } catch (IllegalArgumentException | IllegalAccessException e) {
          LOG.error("Could not convert \"{}\" with id \"{}\"", simpleName, oldId);
        }
      }
    } catch (StorageException e) {
      LOG.error("Could not retrieve DomainEntities of type \"{}\"", simpleName);
    } finally {
      LOG.info("End converting for {}.", simpleName);
      this.countDownLatch.countDown();
      LOG.info("Incomplete tasks: {}", countDownLatch.getCount());
      if (graph instanceof TransactionalGraph) {
        ((TransactionalGraph) graph).commit();
      }
    }
  }

  private <U extends Entity> String mapOldIdtoNewId(Class<U> type, String oldId) {
    String newId = idGenerator.nextIdFor(type);
    oldIdNewIdMap.put(oldId, newId);
    return newId;
  }

  @Override
  public void run() {
    this.convert();
  }
}

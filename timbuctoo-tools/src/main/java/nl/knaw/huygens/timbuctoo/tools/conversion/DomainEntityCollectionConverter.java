package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;

public class DomainEntityCollectionConverter<T extends DomainEntity> {
  private static final Logger LOG = LoggerFactory.getLogger(DomainEntityCollectionConverter.class);
  private static final int NUMBER_OF_PROCESSORS = Runtime.getRuntime().availableProcessors();

  private final Class<T> type;
  private final MongoConversionStorage mongoStorage;
  private final Graph graph;
  private final DomainEntityConverterFactory entityConverterFactory;

  public DomainEntityCollectionConverter(Class<T> type, Graph graph, GraphStorage graphStorage, IdGenerator idGenerator, ElementConverterFactory converterFactory, MongoConversionStorage mongoStorage,
      Map<String, String> oldIdNewIdMap, TypeRegistry typeRegistry) {
    this(type, mongoStorage, new DomainEntityConverterFactory(mongoStorage, graph, typeRegistry, graphStorage, oldIdNewIdMap), graph);

  }

  public DomainEntityCollectionConverter(Class<T> type, MongoConversionStorage mongoStorage, DomainEntityConverterFactory entityConverterFactory, Graph graph) {
    this.type = type;
    this.mongoStorage = mongoStorage;
    this.entityConverterFactory = entityConverterFactory;
    this.graph = graph;
  }

  public void convert() {
    String simpleName = type.getSimpleName();
    ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_PROCESSORS);

    LOG.info("Start converting for {} on {} processors", simpleName, NUMBER_OF_PROCESSORS);
    try {
      for (StorageIterator<T> iterator = mongoStorage.getDomainEntities(type); iterator.hasNext();) {
        T entity = iterator.next();
        String oldId = entity.getId();
        executor.execute(entityConverterFactory.createConverterRunnable(type, oldId));
      }

      executor.shutdown();
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

    } catch (StorageException e) {
      LOG.error("Could not retrieve DomainEntities of type \"{}\"", simpleName);
    } catch (InterruptedException e) {
      LOG.error("Executor failed", e);
    } finally {
      LOG.info("End converting for {}.", simpleName);
      if (graph instanceof TransactionalGraph) {
        ((TransactionalGraph) graph).commit();
      }
    }
  }
}

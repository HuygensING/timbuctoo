package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;

public class DomainEntityCollectionConverter<T extends DomainEntity> {
  private static final Logger LOG = LoggerFactory.getLogger(DomainEntityCollectionConverter.class);

  private final Class<T> type;
  private final MongoConversionStorage mongoStorage;
  private final Graph graph;
  private final DomainEntityConverterFactory entityConverterFactory;

  public DomainEntityCollectionConverter(Class<T> type, Graph graph, TinkerPopConversionStorage graphStorage, IdGenerator idGenerator, ElementConverterFactory converterFactory,
      MongoConversionStorage mongoStorage, TypeRegistry typeRegistry, Map<String, String> oldIdNewIdMap, Map<String, Object> oldIdLatestVertexIdMap) {
    this(type, mongoStorage, new DomainEntityConverterFactory(mongoStorage, graph, typeRegistry, graphStorage, oldIdNewIdMap, oldIdLatestVertexIdMap), graph);

  }

  public DomainEntityCollectionConverter(Class<T> type, MongoConversionStorage mongoStorage, DomainEntityConverterFactory entityConverterFactory, Graph graph) {
    this.type = type;
    this.mongoStorage = mongoStorage;
    this.entityConverterFactory = entityConverterFactory;
    this.graph = graph;
  }

  public void convert() {
    String simpleName = type.getSimpleName();
    LOG.info("Start converting for {}", simpleName);
    List<DomainEntityConverter<T>> converters = Lists.newArrayList();
    try {
      //first create the jobs to prevent a mongo cursor timeout exception.      
      for (StorageIterator<T> iterator = mongoStorage.getDomainEntities(type); iterator.hasNext();) {
        T entity = iterator.next();
        String oldId = entity.getId();
        converters.add(entityConverterFactory.create(type, oldId));

      }
      Stopwatch stopwatch = Stopwatch.createStarted();
      int number = 0;
      for (DomainEntityConverter<T> converter : converters) {
        String oldId = converter.getOldId();
        try {
          converter.convert();

          if (number % 1000 == 0) {
            commit();
            LOG.info("Time per conversion: {} ms, number of conversions {}", (double) stopwatch.elapsed(TimeUnit.MILLISECONDS) / number, number);
          }

          number++;
        } catch (IllegalArgumentException | IllegalAccessException e) {
          LOG.error("Could not convert {} with id \"{}\"", simpleName, oldId);
        }
      }
    } catch (StorageException e) {
      LOG.error("Could not retrieve DomainEntities of type \"{}\"", simpleName);
    } finally {
      LOG.info("End converting for {}.", simpleName);
      commit();
    }
  }

  private void commit() {
    if (graph instanceof TransactionalGraph) {
      ((TransactionalGraph) graph).commit();
    }
  }
}

package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.Map;

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
    LOG.info("Start converting for {}", simpleName);
    try {
      for (StorageIterator<T> iterator = mongoStorage.getDomainEntities(type); iterator.hasNext();) {
        T entity = iterator.next();
        String oldId = entity.getId();

        try {
          entityConverterFactory.create(type, oldId).convert();
        } catch (IllegalArgumentException | IllegalAccessException e) {
          LOG.error("Could not convert \"{}\" with id \"{}\"", simpleName, oldId);
        }
      }
    } catch (StorageException e) {
      LOG.error("Could not retrieve DomainEntities of type \"{}\"", simpleName);
    } finally {
      LOG.info("End converting for {}.", simpleName);
      if (graph instanceof TransactionalGraph) {
        ((TransactionalGraph) graph).commit();
      }
    }
  }
}

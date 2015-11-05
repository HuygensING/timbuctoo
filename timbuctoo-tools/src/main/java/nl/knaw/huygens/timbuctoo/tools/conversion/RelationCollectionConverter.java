package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;

public class RelationCollectionConverter {
  private static final Logger LOG = LoggerFactory.getLogger(RelationCollectionConverter.class);
  private RelationConverter versionConverter;
  private MongoConversionStorage mongoStorage;
  private Graph graph;

  public RelationCollectionConverter(MongoConversionStorage mongoStorage, Graph graph, TinkerPopConversionStorage graphStorage, TypeRegistry typeRegistry, IdGenerator idGenerator,
      Map<String, String> oldIdNewIdMap, Map<String, Object> oldIdLatestVertexIdMap) {
    this(new RelationConverter(mongoStorage, graph, graphStorage, typeRegistry, oldIdNewIdMap, oldIdLatestVertexIdMap, idGenerator), mongoStorage);
    this.graph = graph;
  }

  RelationCollectionConverter(RelationConverter versionConverter, MongoConversionStorage mongoStorage) {
    this.versionConverter = versionConverter;
    this.mongoStorage = mongoStorage;

  }

  public void convert() throws StorageException, IllegalArgumentException, IllegalAccessException {
    LOG.info("Start converting for Relation");
    try {

      List<String> relationIds = Lists.newArrayList();

      //first create the jobs to prevent a mongo cursor timeout exception.
      for (StorageIterator<Relation> relations = mongoStorage.getDomainEntities(Relation.class); relations.hasNext();) {
        relationIds.add(relations.next().getId());

      }
      int number = 0;
      Stopwatch stopwatch = Stopwatch.createStarted();
      for (String id : relationIds) {
        versionConverter.convert(id);
        if (number % 1000 == 0) {
          commit();
          LOG.info("Time per conversion: {} ms, number of conversions {}", (double) stopwatch.elapsed(TimeUnit.MILLISECONDS) / number, number);
        }
        number++;

      }
    } finally {
      commit();
      LOG.info("End converting for Relation");
    }

  }

  private void commit() {
    if (graph instanceof TransactionalGraph) {
      ((TransactionalGraph) graph).commit();
    }
  }
}

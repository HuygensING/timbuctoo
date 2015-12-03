package nl.knaw.huygens.timbuctoo.tools.conversion;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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

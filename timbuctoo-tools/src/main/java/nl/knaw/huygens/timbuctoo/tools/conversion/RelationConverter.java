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

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields;

import java.util.Map;

public class RelationConverter {

  private static final Class<Relation> TYPE = Relation.class;
  private final MongoConversionStorage mongoStorage;
  private final RelationRevisionConverter revisionConverter;
  private final IdGenerator idGenerator;
  private Map<String, String> oldIdNewIdMap;

  public RelationConverter(MongoConversionStorage mongoStorage, Graph graph, TinkerPopConversionStorage graphStorage, TypeRegistry typeRegistry, Map<String, String> oldIdNewIdMap,
                           Map<String, Object> oldIdLatestVertexIdMap, IdGenerator idGenerator) {
    this(mongoStorage, new RelationRevisionConverter(graph, mongoStorage, graphStorage, typeRegistry, oldIdNewIdMap, oldIdLatestVertexIdMap), idGenerator, oldIdNewIdMap);
  }

  RelationConverter(MongoConversionStorage mongoStorage, RelationRevisionConverter revisionConverter, IdGenerator idGenerator, Map<String, String> oldIdNewIdMap) {
    this.mongoStorage = mongoStorage;
    this.revisionConverter = revisionConverter;
    this.idGenerator = idGenerator;
    this.oldIdNewIdMap = oldIdNewIdMap;
  }

  public void convert(String oldId) throws StorageException, IllegalArgumentException, IllegalAccessException {
    AllVersionVariationMap<Relation> map = mongoStorage.getAllVersionVariationsMapOf(TYPE, oldId);
    String newId = idGenerator.nextIdFor(TYPE);
    oldIdNewIdMap.put(oldId, newId);

    Edge edge = null;
    for (int revision : map.revisionsInOrder()) {
      edge = revisionConverter.convert(oldId, newId, map.get(revision), revision);
      edge.setProperty(ElementFields.IS_LATEST, false);
    }

    edge.setProperty(ElementFields.IS_LATEST, true);
  }
}

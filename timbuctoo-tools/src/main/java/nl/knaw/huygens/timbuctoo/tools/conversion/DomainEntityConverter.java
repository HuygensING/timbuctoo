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

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Vertex;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexDuplicator;

import java.util.List;
import java.util.Map;

public class DomainEntityConverter<T extends DomainEntity> {
  private MongoConversionStorage mongoStorage;
  private IdGenerator idGenerator;
  private RevisionConverter revisionConverter;
  private VertexDuplicator vertexDuplicator;
  private Class<T> type;
  private String oldId;
  private Map<String, String> oldIdNewIdMap;
  private Map<String, Object> oldIdLatestVertexIdMap;

  public DomainEntityConverter(Class<T> type, String oldId, MongoConversionStorage mongoStorage, IdGenerator idGenerator, RevisionConverter revisionConverter, VertexDuplicator vertexDuplicator,
                               Map<String, String> oldIdNewIdMap, Map<String, Object> oldIdLatestVertexIdMap) {
    this.type = type;
    this.oldId = oldId;
    this.mongoStorage = mongoStorage;
    this.idGenerator = idGenerator;
    this.revisionConverter = revisionConverter;
    this.vertexDuplicator = vertexDuplicator;
    this.oldIdNewIdMap = oldIdNewIdMap;
    this.oldIdLatestVertexIdMap = oldIdLatestVertexIdMap;
  }

  public void convert() throws StorageException, IllegalArgumentException, IllegalAccessException {
    String newId = idGenerator.nextIdFor(type);
    AllVersionVariationMap<T> versions = mongoStorage.getAllVersionVariationsMapOf(type, oldId);
    List<Vertex> revisions = Lists.newArrayList();

    for (Integer revision : versions.revisionsInOrder()) {
      Vertex vertex = revisionConverter.convert(oldId, newId, versions.get(revision), revision);
      revisions.add(vertex);
    }

    linkRevisions(revisions);

    oldIdNewIdMap.put(oldId, newId);
  }

  private void linkRevisions(List<Vertex> revisions) {
    Vertex prev = null;
    for (Vertex revision : revisions) {
      if (prev != null) {
        prev.setProperty(ElementFields.IS_LATEST, false);
        prev.addEdge(SystemRelationType.VERSION_OF.name(), revision);
      }
      prev = revision;
    }

    prev.setProperty(ElementFields.IS_LATEST, true);

    if (prev.getProperty(DomainEntity.DB_PID_PROP_NAME) != null) {
      // duplicate the latest node
      prev = vertexDuplicator.duplicate(prev);
    }

    oldIdLatestVertexIdMap.put(oldId, prev.getId());
  }

  public String getOldId() {
    return oldId;
  }
}

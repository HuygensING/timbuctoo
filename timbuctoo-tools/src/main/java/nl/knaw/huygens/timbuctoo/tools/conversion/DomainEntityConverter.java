package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexDuplicator;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Vertex;

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

    Object latestVertexId = null;
    for (Integer revision : versions.revisionsInOrder()) {
      Vertex vertex = revisionConverter.convert(oldId, newId, versions.get(revision), revision);
      revisions.add(vertex);
      latestVertexId = vertex.getId();
    }

    linkRevisions(revisions);

    oldIdLatestVertexIdMap.put(oldId, latestVertexId);
    oldIdNewIdMap.put(oldId, newId);
  }

  private void linkRevisions(List<Vertex> revisions) {
    Vertex prev = null;
    for (Vertex revision : revisions) {
      if (prev != null) {
        prev.addEdge(SystemRelationType.VERSION_OF.name(), revision);
      }
      prev = revision;
    }

    if (prev.getProperty(DomainEntity.PID) != null) {
      // duplicate the latest node
      vertexDuplicator.duplicate(prev);
    }
  }

  public String getOldId() {
    return oldId;
  }
}

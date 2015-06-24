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

  public DomainEntityConverter(Class<T> type, String oldId, MongoConversionStorage mongoStorage, IdGenerator idGenerator, RevisionConverter revisionConverter, VertexDuplicator vertexDuplicator,
      Map<String, String> oldIdNewIdMap) {
    this.type = type;
    this.oldId = oldId;
    this.mongoStorage = mongoStorage;
    this.idGenerator = idGenerator;
    this.revisionConverter = revisionConverter;
    this.vertexDuplicator = vertexDuplicator;
    this.oldIdNewIdMap = oldIdNewIdMap;
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

package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexDuplicator;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class DomainEntityConverter {
  private MongoConversionStorage mongoStorage;
  private IdGenerator idGenerator;
  private RevisionConverter revisionConverter;
  private VertexDuplicator vertexDuplicator;

  public DomainEntityConverter(MongoConversionStorage mongoStorage, IdGenerator idGenerator, Graph graph, GraphStorage graphStorage, TypeRegistry typeRegistry) {
    this(mongoStorage, idGenerator, new RevisionConverter(graph, mongoStorage, graphStorage, typeRegistry), new VertexDuplicator(graph));
  }

  DomainEntityConverter(MongoConversionStorage mongoStorage, IdGenerator idGenerator, RevisionConverter revisionConverter, VertexDuplicator vertexDuplicator) {
    this.mongoStorage = mongoStorage;
    this.idGenerator = idGenerator;
    this.revisionConverter = revisionConverter;
    this.vertexDuplicator = vertexDuplicator;
  }

  public <T extends DomainEntity> String convert(Class<T> type, String oldId) throws StorageException, IllegalArgumentException, IllegalAccessException {
    String newId = idGenerator.nextIdFor(type);
    AllVersionVariationMap<T> versions = mongoStorage.getAllVersionVariationsMapOf(type, oldId);
    List<Vertex> revisions = Lists.newArrayList();

    for (Integer revision : versions.revisionsInOrder()) {
      Vertex vertex = revisionConverter.convert(oldId, newId, versions.get(revision), revision);
      revisions.add(vertex);
    }

    linkRevisions(revisions);

    return newId;
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
}

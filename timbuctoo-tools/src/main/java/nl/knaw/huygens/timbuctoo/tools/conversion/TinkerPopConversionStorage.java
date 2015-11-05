package nl.knaw.huygens.timbuctoo.tools.conversion;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.TinkerPopStorage;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import com.google.inject.Inject;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class TinkerPopConversionStorage extends TinkerPopStorage {

  private Graph db;
  private ElementConverterFactory converterFactory;

  @Inject
  public TinkerPopConversionStorage(Graph db, TypeRegistry typeRegistry) {
    super(db, typeRegistry);
    this.db = db;
    this.converterFactory = new ElementConverterFactory(typeRegistry);
  }

  public <T extends Entity> T getEntityByVertexId(Class<T> type, Object id) throws StorageException {
    Vertex vertex = db.getVertex(id);

    if (vertex == null) {
      throw new StorageException("Vertex with Id" + id + "not found");
    }

    return converterFactory.forType(type).convertToEntity(vertex);
  }

  public <T extends Relation> T getRelationByEdgeId(Class<T> type, Object id) throws StorageException {
    Edge edge = db.getEdge(id);

    if (edge == null) {
      throw new StorageException("Vertex with Id" + id + "not found");
    }

    return converterFactory.forRelation(type).convertToEntity(edge);
  }
}

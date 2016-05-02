package nl.knaw.huygens.timbuctoo.bulkupload.parsedworkbook;

import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.Optional;

public class SaveingState implements AutoCloseable {
  private final HashMap<String, HashMap<String, Vertex>> vertexByUniqueId = new HashMap<>();
  private final HashMap<String, HashMap<Integer, Vertex>> vertexByIndex = new HashMap<>();
  private final GraphWrapper wrapper;
  private int saveCounter;
  private Transaction tx;

  public SaveingState(GraphWrapper wrapper) {
    this.wrapper = wrapper;
    tx = wrapper.getGraph().tx();
  }

  public void allowCommit() {
    if (saveCounter++ > 500) {
      saveCounter = 0;
      tx.commit();
      tx = wrapper.getGraph().tx();
    }
  }

  @Override
  public void close() {
    tx.commit();
  }

  public void addIndexedVertex(Collection collection, String uniqueValue, Vertex vertex) {
    HashMap<String, Vertex> vertices = vertexByUniqueId.get(collection.getCollectionName());
    if (vertices == null) {
      vertices = new HashMap<>();
      vertexByUniqueId.put(collection.getCollectionName(), vertices);
    }
    vertices.put(uniqueValue, vertex);
  }

  public void newVertex(Collection collection, Vertex vertex, int index) {
    HashMap<Integer, Vertex> vertices = vertexByIndex.get(collection.getCollectionName());
    if (vertices == null) {
      vertices = new HashMap<>();
      vertexByIndex.put(collection.getCollectionName(), vertices);
    }
    vertices.put(index, vertex);
  }

  public HashMap<Integer, Vertex> getVerticeList(Collection collection) {
    return vertexByIndex.get(collection.getCollectionName());
  }

  public Optional<Vertex> getVertexById(String collectionName, String uniqueValue) {
    final HashMap<String, Vertex> collectionVertices = vertexByUniqueId.get(collectionName);
    if (collectionVertices == null) {
      return Optional.empty();
    } else {
      return Optional.ofNullable(collectionVertices.get(uniqueValue));
    }
  }
}

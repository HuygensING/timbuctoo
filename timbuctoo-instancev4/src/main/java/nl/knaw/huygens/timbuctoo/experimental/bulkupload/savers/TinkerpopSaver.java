package nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.UUID;

public class TinkerpopSaver implements AutoCloseable, Saver {
  private final GraphWrapper wrapper;
  private final Vertex vre;
  private int saveCounter;
  private Transaction tx;
  private final int maxVerticesPerTransaction;

  public TinkerpopSaver(GraphWrapper wrapper, Vertex vre, int maxVerticesPerTransaction) {
    this.wrapper = wrapper;
    tx = wrapper.getGraph().tx();
    this.maxVerticesPerTransaction = maxVerticesPerTransaction;
    this.vre = vre;
  }

  private void allowCommit() {
    if (saveCounter++ > maxVerticesPerTransaction) {
      saveCounter = 0;
      tx.commit();
      tx = wrapper.getGraph().tx();
    }
  }

  @Override
  public void close() {
    tx.commit();
  }

  @Override
  public Vertex addEntity(Vertex collection, HashMap<String, Object> currentProperties) {
    allowCommit();
    Vertex result;
    result = wrapper.getGraph().addVertex();

    //FIXME re-use code from crudservice create
    result.property("rev", 1);
    result.property("tim_id", UUID.randomUUID().toString());
    result.property("isLatest", true);

    collection.addEdge("hasVertex", result);
    currentProperties.forEach(result::property);

    return result;
  }

  @Override
  public Vertex addCollection(String collectionName) {
    Vertex collection = wrapper.getGraph().addVertex("name", collectionName);
    vre.addEdge("hasCollection", collection);
    return collection;
  }

}

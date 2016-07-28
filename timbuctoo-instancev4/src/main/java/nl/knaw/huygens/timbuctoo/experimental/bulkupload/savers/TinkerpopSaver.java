package nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers;

import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;

public class TinkerpopSaver implements AutoCloseable, Saver {
  private final Vres vres;
  private final GraphWrapper wrapper;
  private final Vertex vre;
  private int saveCounter;
  private Transaction tx;
  private final int maxVerticesPerTransaction;

  public TinkerpopSaver(Vres vres, GraphWrapper wrapper, String vreName, int maxVerticesPerTransaction) {
    this.vres = vres;
    this.wrapper = wrapper;
    tx = wrapper.getGraph().tx();
    this.maxVerticesPerTransaction = maxVerticesPerTransaction;
    this.vre = initVre(vreName);
  }

  private Vertex initVre(String vreName) {
    //FIXME namespace vrename per user

    Vertex result = null;
    try (Transaction tx = wrapper.getGraph().tx()) {
      final GraphTraversal<Vertex, Vertex> vre = wrapper.getGraph().traversal().V()
        .hasLabel(Vre.DATABASE_LABEL)
        .has(Vre.VRE_NAME_PROPERTY_NAME, vreName);
      if (vre.hasNext()) {
        result = vre.next();
        result.vertices(Direction.BOTH, "hasRawCollection").forEachRemaining(coll -> {
          coll.vertices(Direction.BOTH, "hasItem").forEachRemaining(vertex -> {
            vertex.remove();
          });
          coll.remove();
        });
      } else {
        result = wrapper.getGraph().addVertex(T.label, Vre.DATABASE_LABEL, Vre.VRE_NAME_PROPERTY_NAME, vreName);
      }
      tx.commit();
    }

    vres.reload();
    return result;
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

    Vertex result = wrapper.getGraph().addVertex();

    collection.addEdge("hasItem", result);
    currentProperties.forEach(result::property);

    return result;
  }

  @Override
  public Vertex addCollection(String collectionName) {
    Vertex collection = wrapper.getGraph().addVertex("name", collectionName);
    vre.addEdge("hasRawCollection", collection);
    return collection;
  }

}

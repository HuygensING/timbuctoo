package nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers;

import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Map;

public class TinkerpopSaver implements AutoCloseable, Saver {
  public static final String RAW_COLLECTION_EDGE_NAME = "hasRawCollection";
  public static final String RAW_ITEM_EDGE_NAME = "hasItem";
  public static final String RAW_COLLECTION_NAME_PROPERTY_NAME = "name";
  public static final String FIRST_RAW_ITEM_EDGE_NAME = "hasFirstItem";
  public static final String NEXT_RAW_ITEM_EDGE_NAME = "hasNextItem";
  private final Vres vres;
  private final GraphWrapper wrapper;
  private final Vertex vre;
  private final int maxVerticesPerTransaction;
  private int saveCounter;
  private Transaction tx;

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
        result.vertices(Direction.BOTH, RAW_COLLECTION_EDGE_NAME).forEachRemaining(coll -> {
          coll.vertices(Direction.BOTH, RAW_ITEM_EDGE_NAME).forEachRemaining(vertex -> {
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
  public Vertex addEntity(Vertex rawCollection, Map<String, Object> currentProperties) {
    allowCommit();

    Vertex result = wrapper.getGraph().addVertex();

    rawCollection.addEdge(RAW_ITEM_EDGE_NAME, result);
    currentProperties.forEach(result::property);

    if (!rawCollection.edges(Direction.OUT, FIRST_RAW_ITEM_EDGE_NAME).hasNext()) {
      rawCollection.addEdge(FIRST_RAW_ITEM_EDGE_NAME, result);
    } else {
      Vertex previous = wrapper.getGraph().traversal().V(rawCollection.id())
                               .out(RAW_ITEM_EDGE_NAME)
                               .not(__.where(__.out(NEXT_RAW_ITEM_EDGE_NAME)))
                               .next();

      previous.addEdge(NEXT_RAW_ITEM_EDGE_NAME, result);
    }

    return result;
  }

  @Override
  public Vertex addCollection(String collectionName) {
    Vertex collection = wrapper.getGraph().addVertex(RAW_COLLECTION_NAME_PROPERTY_NAME, collectionName);
    vre.addEdge(RAW_COLLECTION_EDGE_NAME, collection);
    return collection;
  }

}

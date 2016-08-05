package nl.knaw.huygens.timbuctoo.bulkupload.savers;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class CollectionAdder {
  private final GraphWrapper graphWrapper;

  public CollectionAdder(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
  }

  public void addToLastItemOfCollection(Vertex rawCollection, Vertex result) {
    if (!rawCollection.edges(Direction.OUT, TinkerpopSaver.FIRST_RAW_ITEM_EDGE_NAME).hasNext()) {
      rawCollection.addEdge(TinkerpopSaver.FIRST_RAW_ITEM_EDGE_NAME, result);
    } else {
      Vertex previous = graphWrapper.getGraph().traversal().V(rawCollection.id())
                                    .out(TinkerpopSaver.RAW_ITEM_EDGE_NAME)
                                    .not(__.where(__.out(TinkerpopSaver.NEXT_RAW_ITEM_EDGE_NAME)))
                                    .next();

      previous.addEdge(TinkerpopSaver.NEXT_RAW_ITEM_EDGE_NAME, result);
    }
  }

  public void addToCollection(Vertex rawCollection, Vertex result) {
    rawCollection.addEdge(TinkerpopSaver.RAW_ITEM_EDGE_NAME, result);
  }
}

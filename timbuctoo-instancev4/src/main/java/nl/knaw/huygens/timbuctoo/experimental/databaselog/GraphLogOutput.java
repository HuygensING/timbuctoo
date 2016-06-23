package nl.knaw.huygens.timbuctoo.experimental.databaselog;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GraphLogOutput implements LogOutput {
  public static final Logger LOG = LoggerFactory.getLogger(GraphLogOutput.class);
  private final GraphWrapper graphWrapper;
  private Vertex currentVertex;
  private Transaction tx;
  private int numberOfEntriesWithoutCommit;

  public GraphLogOutput(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
    this.numberOfEntriesWithoutCommit = 0;
  }

  @Override
  public void prepareToWrite() {
    LOG.info("Prepare to write");
    tx = graphWrapper.getGraph().tx();
    if (!tx.isOpen()) {
      tx.open();
    }
    currentVertex = graphWrapper.getGraph().addVertex("history");
    commit();
  }

  @Override
  public void newVertex(Vertex vertex) {
    Vertex previousVertex = currentVertex;
    currentVertex = graphWrapper.getGraph().addVertex("createVertexEntry");
    previousVertex.addEdge("NEXT_ITEM", currentVertex);
    commit();
  }

  @Override
  public void updateVertex(Vertex vertex) {
    Vertex previousVertex = currentVertex;
    currentVertex = graphWrapper.getGraph().addVertex("updateVertexEntry");
    previousVertex.addEdge("NEXT_ITEM", currentVertex);
    commit();
  }

  @Override
  public void newEdge(Edge edge) {
    Vertex previousVertex = currentVertex;
    currentVertex = graphWrapper.getGraph().addVertex("createEdgeEntry");
    previousVertex.addEdge("NEXT_ITEM", currentVertex);
    currentVertex.property("inVertex", edge.inVertex().value("tim_id"));
    currentVertex.property("outVertex", edge.outVertex().value("tim_id"));
    commit();
  }

  @Override
  public void updateEdge(Edge edge) {
    Vertex previousVertex = currentVertex;
    currentVertex = graphWrapper.getGraph().addVertex("updateEdgeEntry");
    previousVertex.addEdge("NEXT_ITEM", currentVertex);
    currentVertex.property("inVertex", edge.inVertex().value("tim_id"));
    currentVertex.property("outVertex", edge.outVertex().value("tim_id"));
    commit();
  }

  private void commit() {
    if (++numberOfEntriesWithoutCommit >= 5000) {
      numberOfEntriesWithoutCommit = 0;
      tx.commit();
      tx.open();
    }
  }

  @Override
  public void newProperty(Property property) {
    currentVertex.property(property.key(), property.value());
  }

  @Override
  public void updateProperty(Property property) {
    currentVertex.property(property.key(), property.value());
  }

  @Override
  public void deleteProperty(String propertyName) {
  }

  @Override
  public void finishWriting() {
    tx.commit();
    tx.close();
    LOG.info("Finish writing");
  }
}

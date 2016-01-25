package nl.knaw.huygens.timbuctoo.search;

import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tinkerpop.api.impl.Neo4jGraphAPIImpl;

public class TestGraphBuilder {

  private final Graph neo4jGraph;

  private TestGraphBuilder() {
    GraphDatabaseService graphDatabaseService = new TestGraphDatabaseFactory().newImpermanentDatabase();
    Neo4jGraphAPIImpl neo4jGraphApi = new Neo4jGraphAPIImpl(graphDatabaseService);
    neo4jGraph = Neo4jGraph.open(neo4jGraphApi);
  }

  public static TestGraphBuilder newGraph() {
    return new TestGraphBuilder();
  }

  public Graph build() {
    return neo4jGraph;
  }

  public TestGraphBuilder withVertex(VertexBuilder vertex) {
    vertex.build(neo4jGraph);
    return this;
  }
}

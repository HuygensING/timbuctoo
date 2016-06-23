package nl.knaw.huygens.timbuctoo.util;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tinkerpop.api.impl.Neo4jGraphAPIImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static nl.knaw.huygens.timbuctoo.util.Neo4jHelper.cleanDb;

public class TestGraphBuilder {
  private static GraphDatabaseService neo4jDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
  private final List<VertexBuilder> vertexBuilders = new ArrayList<>();
  private final Map<String, VertexBuilder> identifiableVertexBuilders = new HashMap<>();

  {
    neo4jDb.beginTx();
  }

  private TestGraphBuilder() {
  }

  public static TestGraphBuilder newGraph() {
    return new TestGraphBuilder();
  }

  public Graph build() {
    //When creating a new database you have to close the previous one, because
    //each database will create a new thread. This fills up the available
    //threads when running thousands of tests.
    //
    //Closing the database is very slow however (testruns go from less then a
    //second to 3 minutes on a machine with an SSD) so instead we now re-use
    //the same neo4j database that we clear when the function to create a new
    //one is called.
    cleanDb(neo4jDb);
    Neo4jGraphAPIImpl neo4jGraphApi = new Neo4jGraphAPIImpl(neo4jDb);
    Neo4jGraph neo4jGraph = Neo4jGraph.open(neo4jGraphApi);
    Map<String, Vertex> identifiableVertices = new HashMap<>();
    //Create all identifiable vertices
    identifiableVertexBuilders.forEach(
      (key, builder) -> identifiableVertices.put(key, builder.build(neo4jGraph.addVertex()))
    );
    //now we can add the links
    identifiableVertexBuilders.forEach(
      (key, builder) -> builder.setRelations(identifiableVertices.get(key), identifiableVertices)
    );
    //finally add all the non-identifiable vertices
    vertexBuilders.forEach(
      (builder) -> {
        Vertex vertex = neo4jGraph.addVertex();
        builder.build(vertex);
        builder.setRelations(vertex, identifiableVertices);
      }
    );
    return neo4jGraph;
  }

  public GraphWrapper wrap() {
    final Graph graph = build();
    return new GraphWrapper() {
      @Override
      public Graph getGraph() {
        return graph;
      }

      @Override
      public GraphTraversalSource getLatestState() {
        throw new UnsupportedOperationException("Not implemented yet");
      }

      @Override
      public GraphTraversal<Vertex, Vertex> getCurrentEntitiesFor(String... entityTypeNames) {
        throw new UnsupportedOperationException("Not implemented yet");
      }
    };
  }

  public TestGraphBuilder withVertex(String id, Consumer<VertexBuilder> vertexBuilderConfig) {
    if (identifiableVertexBuilders.containsKey(id)) {
      throw new RuntimeException("Key " + id + " is used twice");
    }
    VertexBuilder vb = new VertexBuilder();
    vertexBuilderConfig.accept(vb);
    identifiableVertexBuilders.put(id, vb);
    return this;
  }

  public TestGraphBuilder withVertex(Consumer<VertexBuilder> vertexBuilderConfig) {
    VertexBuilder vb = new VertexBuilder();
    vertexBuilderConfig.accept(vb);
    vertexBuilders.add(vb);
    return this;
  }
}

package nl.knaw.huygens.timbuctoo.search.description;

import nl.knaw.huygens.timbuctoo.search.description.facet.FacetDescriptionFactory;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.SearchRequestV2_1;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.SubgraphStrategy;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.tinkerpop.api.impl.Neo4jGraphAPIImpl;

import java.io.File;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;
import static org.assertj.core.api.Assertions.assertThat;

public class PerformanceSearchDescriptionTest {
  private static final SubgraphStrategy LATEST_ELEMENTS =
          SubgraphStrategy.build().edgeCriterion(has("isLatest", true)).vertexCriterion(has("isLatest", true)).create();

  private GraphDatabaseService graphDatabase;
  private Neo4jGraph graph;

  private Wrapper wrapper;
  private FacetDescriptionFactory facetDescriptionFactory;
  private PropertyDescriptorFactory propertyDescriptorFactory;

  private class Wrapper implements GraphWrapper {

    @Override
    public Graph getGraph() {
      return graph;
    }

    @Override
    public GraphTraversalSource getLatestState() {
      return GraphTraversalSource.build().with(LATEST_ELEMENTS).create(graph);
    }
  }

  @Before
  public void setup() {
    graphDatabase = new GraphDatabaseFactory()
            .newEmbeddedDatabaseBuilder(new File("./src/spec/resources/database"))
            .setConfig(GraphDatabaseSettings.allow_store_upgrade, "true")
            .newGraphDatabase();

    graph = Neo4jGraph.open(new Neo4jGraphAPIImpl(graphDatabase));

    PropertyParserFactory propertyParserFactory = new PropertyParserFactory();
    facetDescriptionFactory = new FacetDescriptionFactory(propertyParserFactory);
    propertyDescriptorFactory = new PropertyDescriptorFactory(propertyParserFactory);

    wrapper = new Wrapper();
  }

  @After
  public void finalize() throws Exception {
    graph.close();
  }

  @Test
  public void testOriginal() {
    WwDocumentSearchDescription instance = new WwDocumentSearchDescription(propertyDescriptorFactory, facetDescriptionFactory);
    long before = System.currentTimeMillis();
    instance.execute(wrapper, new SearchRequestV2_1());
    long timed = System.currentTimeMillis() - before;
    assertThat(timed).isGreaterThan(0);
    System.out.println(timed);
  }

  @Test
  public void testPerformance() {
    PerformanceWwDocumentSearchDescription instance = new PerformanceWwDocumentSearchDescription(
            propertyDescriptorFactory, facetDescriptionFactory);
    PerformanceWwPersonSearchDescription instance2 = new PerformanceWwPersonSearchDescription(
            propertyDescriptorFactory, facetDescriptionFactory
    );

/*
    long before = System.currentTimeMillis();
    instance.execute(wrapper, new SearchRequestV2_1());
    long timed = System.currentTimeMillis() - before;
    System.out.println("DOCUMENT TIME: " + timed);
*/

    long before2 = System.currentTimeMillis();
    instance2.execute(wrapper, new SearchRequestV2_1());
    long timed2 = System.currentTimeMillis() - before2;
    System.out.println("PERSON TIME: " + timed2);
  }
}

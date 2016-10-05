package nl.knaw.huygens.timbuctoo.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.crud.HandleAdder;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.vres.DatabaseConfiguredVres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.commons.lang.NotImplementedException;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.HashMap;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;


public class LoadSaveVresTest {

  @Test
  public void itLoadsTheConfigurationsFromAGraph() throws JsonProcessingException {
    final HashMap<String, String> keywordTypes = new HashMap<>();
    keywordTypes.put("key", "value");
    final String keywordTypesJson = new ObjectMapper().writeValueAsString(keywordTypes);

    final Graph graph = newGraph()
      .withVertex("documents", v -> {
        v.withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "documents")
          .withProperty(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "document")
          .withProperty(Collection.IS_RELATION_COLLECTION_PROPERTY_NAME, false);
      })
      .withVertex(v -> {

        v.withLabel(Vre.DATABASE_LABEL)
          .withProperty(Vre.VRE_NAME_PROPERTY_NAME, "VreA")
          .withProperty(Vre.KEYWORD_TYPES_PROPERTY_NAME,
           keywordTypesJson)
          .withOutgoingRelation(Vre.HAS_COLLECTION_RELATION_NAME, "documents");
      })
      .build();

    GraphWrapper graphWrapper = mock(GraphWrapper.class);
    given(graphWrapper.getGraph()).willReturn(graph);

    DatabaseConfiguredVres instance = new DatabaseConfiguredVres(new DataAccess(graphWrapper, null, null,
      mock(HandleAdder.class)));

    assertThat(instance.getVre("VreA"), instanceOf(Vre.class));
    assertThat(instance.getCollection("documents").get(), instanceOf(Collection.class));
    assertThat(instance.getCollectionForType("document").get(), instanceOf(Collection.class));
    assertThat(instance.getVres().get("VreA"), instanceOf(Vre.class));
  }

  @Test
  public void onlyReloadReloadsTheConfigurationsFromTheGraph() throws JsonProcessingException {
    Graph graph = newGraph()
      .withVertex(v -> {
        v.withLabel(Vre.DATABASE_LABEL)
          .withProperty(Vre.VRE_NAME_PROPERTY_NAME, "VreA");
      })
      .build();

    MockWrapper graphWrapper = new MockWrapper();
    graphWrapper.graph = graph;

    DatabaseConfiguredVres instance = new DatabaseConfiguredVres(new DataAccess(graphWrapper, null, null,
      mock(HandleAdder.class)));

    assertThat(instance.getVre("VreA"), instanceOf(Vre.class));
    assertThat(instance.getVre("VreB"), CoreMatchers.equalTo(null));

    graph = newGraph()
      .withVertex(v -> {
        v.withLabel(Vre.DATABASE_LABEL)
          .withProperty(Vre.VRE_NAME_PROPERTY_NAME, "VreB");
      })
      .build();
    graphWrapper.graph = graph;

    assertThat(instance.getVre("VreA"), instanceOf(Vre.class));
    assertThat(instance.getVre("VreB"), CoreMatchers.equalTo(null));

    instance.reload();

    assertThat(instance.getVre("VreB"), instanceOf(Vre.class));
    assertThat(instance.getVre("VreA"), CoreMatchers.equalTo(null));
  }


  private class MockWrapper implements GraphWrapper {
    private Graph graph;

    @Override
    public Graph getGraph() {
      return graph;
    }

    @Override
    public GraphTraversalSource getLatestState() {
      return graph.traversal();
    }

    @Override
    public GraphTraversal<Vertex, Vertex> getCurrentEntitiesFor(String... entityTypeNames) {
      throw new NotImplementedException();
    }
  }
}

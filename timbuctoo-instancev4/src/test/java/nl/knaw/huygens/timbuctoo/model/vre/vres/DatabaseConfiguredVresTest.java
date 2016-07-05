package nl.knaw.huygens.timbuctoo.model.vre.vres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;

import java.util.HashMap;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class DatabaseConfiguredVresTest {

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

    DatabaseConfiguredVres instance = new DatabaseConfiguredVres(graphWrapper);

    assertThat(instance.getVre("VreA"), instanceOf(Vre.class));
    assertThat(instance.getCollection("documents").get(), instanceOf(Collection.class));
    assertThat(instance.getCollectionForType("document").get(), instanceOf(Collection.class));
    assertThat(instance.getVres().get("VreA"), instanceOf(Vre.class));
    assertThat(instance.getKeywordTypes().get("VreA"), instanceOf(Map.class));
    assertThat(instance.getKeywordTypes().get("VreA").get("key"), equalTo("value"));
  }

  @Test
  public void onlyReloadReloadsTheConfigurationsFromTheGraph() throws JsonProcessingException {
    Graph graph = newGraph()
      .withVertex(v -> {
        v.withLabel(Vre.DATABASE_LABEL)
          .withProperty(Vre.VRE_NAME_PROPERTY_NAME, "VreA");
      })
      .build();

    GraphWrapper graphWrapper = mock(GraphWrapper.class);
    final OngoingStubbing<Graph> graphOngoingStubbing = when(graphWrapper.getGraph()).thenReturn(graph);

    DatabaseConfiguredVres instance = new DatabaseConfiguredVres(graphWrapper);

    assertThat(instance.getVre("VreA"), instanceOf(Vre.class));
    assertThat(instance.getVre("VreB"), CoreMatchers.equalTo(null));

    graph = newGraph()
      .withVertex(v -> {
        v.withLabel(Vre.DATABASE_LABEL)
          .withProperty(Vre.VRE_NAME_PROPERTY_NAME, "VreB");
      })
      .build();
    graphOngoingStubbing.thenReturn(graph);

    assertThat(instance.getVre("VreA"), instanceOf(Vre.class));
    assertThat(instance.getVre("VreB"), CoreMatchers.equalTo(null));

    instance.reload();

    assertThat(instance.getVre("VreB"), instanceOf(Vre.class));
    assertThat(instance.getVre("VreA"), CoreMatchers.equalTo(null));
  }
}

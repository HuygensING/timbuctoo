package nl.knaw.huygens.timbuctoo.model.vre;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.model.vre.Vre.DATABASE_LABEL;
import static nl.knaw.huygens.timbuctoo.model.vre.Vre.HAS_COLLECTION_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Vre.KEYWORD_TYPES_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Vre.VRE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.VreBuilder.vre;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

public class VreTest {
  private Graph graph;

  @Before
  public void setUp() {
    graph = newGraph().build();
  }


  @Test
  public void saveCreatesAVertexForTheVre() {
    final Vre vre = new Vre("VreName");

    final Vertex result = vre.save(graph, Optional.empty());

    assertThat(result, likeVertex()
      .withLabel(DATABASE_LABEL)
      .withProperty(VRE_NAME_PROPERTY_NAME, "VreName")
    );
  }

  @Test
  public void saveReplacesAnExistingVertexForTheVre() {
    final Vertex existingVertex = graph.addVertex(DATABASE_LABEL);
    existingVertex.property(VRE_NAME_PROPERTY_NAME, "VreName");
    final Vre vre = new Vre("VreName");

    final Vertex result = vre.save(graph, Optional.empty());

    assertThat(result, equalTo(existingVertex));
  }

  @Test
  public void saveAddsKeywordTypesWhenAvailable() throws JsonProcessingException {
    final Vre vre = new Vre("VreName");
    Map<String, String> keyWordTypes = Maps.newHashMap();
    keyWordTypes.put("typeA", "valueA");
    keyWordTypes.put("typeB", "valueB");

    final Vertex result = vre.save(graph, Optional.of(keyWordTypes));

    assertThat(result.property(KEYWORD_TYPES_PROPERTY_NAME).value(),
      equalTo(new ObjectMapper().writeValueAsString(keyWordTypes))
    );
  }

  @Test
  public void saveAddsVreCollectionsToTheVre() {
    Vre vre = vre("VreName", "prefix")
      .withCollection("prefixpersons")
      .withCollection("prefixdocuments")
      .build();

    final Vertex savedVertex = vre.save(graph, Optional.empty());
    final List<Vertex> result = Lists.newArrayList(savedVertex.vertices(Direction.OUT, HAS_COLLECTION_RELATION_NAME));

    assertThat(result, containsInAnyOrder(
      likeVertex()
        .withLabel(Collection.DATABASE_LABEL)
        .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "prefixpersons"),
      likeVertex()
        .withLabel(Collection.DATABASE_LABEL)
        .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "prefixdocuments")
    ));

  }
}

package nl.knaw.huygens.timbuctoo.experimental.databaselog;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Set;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.util.ElementMatcher.likeElement;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class GraphLogValidatorTest {

  public static final String VERTEX_ID_1 = "id1";
  public static final String VERTEX_ID_2 = "id2";
  public static final UUID REL_1_ID = UUID.fromString("ff65089c-2ded-4af0-95e7-0476979f96b8");
  public static final UUID REL_2_ID = UUID.fromString("a628b090-ec7f-4608-9356-61728355ad5a");

  @Test
  public void validateCollectsTheVerticesThatAreMissingALogEntry() {
    GraphWrapper wrapper = newGraph().withVertex(v -> v.withTimId(VERTEX_ID_1)
                                                       .withProperty("rev", 1)
                                                       .withLabel("document"))
                                     .withVertex(v -> v.withTimId(VERTEX_ID_2)
                                                       .withProperty("rev", 1)
                                                       .withLabel("document"))
                                     .withVertex(v -> v.withTimId(VERTEX_ID_1)
                                                       .withProperty("rev", 2)
                                                       .withLabel("document"))
                                     .withVertex(v -> v.withTimId(VERTEX_ID_2)
                                                       .withProperty("rev", 2)
                                                       .withLabel("document"))
                                     .withVertex(v -> v.withLabel("history")
                                                       .withOutgoingRelation("NEXT_ITEM", "v1"))
                                     .withVertex("v1", v -> v.withTimId(VERTEX_ID_1)
                                                             .withProperty("rev", 1)
                                                             .withLabel("createVertexEntry")
                                                             .withOutgoingRelation("NEXT_ITEM", "v2")
                                     )
                                     .withVertex("v2", v -> v.withTimId(VERTEX_ID_1)
                                                             .withProperty("rev", 2)
                                                             .withLabel("updateVertexEntry")
                                                             .withOutgoingRelation("NEXT_ITEM", "v3"))
                                     .withVertex("v3", v -> v.withLabel("createEdgeEntry")
                                                             .withTimId("relId1")
                                                             .withProperty("rev", 1)
                                                             .withOutgoingRelation("NEXT_ITEM", "v4"))
                                     .withVertex("v4", v -> v.withLabel("updateEdgeEntry")
                                                             .withTimId("relId1")
                                                             .withProperty("rev", 2))
                                     .wrap();
    GraphLogValidator graphLogValidator = new GraphLogValidator(wrapper);

    Set<? extends Element> result = graphLogValidator.validate();

    assertThat(result, containsInAnyOrder(
      likeElement().ofType(Vertex.class).withTimId(VERTEX_ID_2).withProperty("rev", 1),
      likeElement().ofType(Vertex.class).withTimId(VERTEX_ID_2).withProperty("rev", 2)
    ));
  }

  @Ignore
  @Test
  public void validateCollectsTheEdgesThatAreMissingALogEntry() {
    GraphWrapper wrapper = newGraph().withVertex("v1", v -> v.withTimId(VERTEX_ID_1)
                                                             .withProperty("rev", 1)
                                                             .withLabel("document"))
                                     .withVertex("v2", v -> v.withTimId(VERTEX_ID_2)
                                                             .withProperty("rev", 1)
                                                             .withLabel("document")
                                                             .withOutgoingRelation("relatedTo", "v1",
                                                               r -> r.withTim_id(REL_1_ID)
                                                                     .withRev(1)
                                                             )
                                                             .withOutgoingRelation("relatedTo", "v1",
                                                               r -> r.withTim_id(REL_1_ID)
                                                                     .withRev(2)
                                                             )
                                                             .withOutgoingRelation("otherRelation", "v1",
                                                               r -> r.withTim_id(REL_2_ID)
                                                                     .withRev(1))
                                                             .withOutgoingRelation("otherRelation", "v1",
                                                               r -> r.withTim_id(REL_2_ID)
                                                                     .withRev(2))
                                     )
                                     .withVertex("v3", v -> v.withLabel("createVertexEntry")
                                                             .withTimId(VERTEX_ID_1)
                                                             .withProperty("rev", 1)
                                                             .withOutgoingRelation("NEXT_ITEM", "v4")
                                     )
                                     .withVertex("v4", v -> v.withLabel("updateVertexEntry")
                                                             .withTimId(VERTEX_ID_2)
                                                             .withProperty("rev", 1)
                                                             .withOutgoingRelation("NEXT_ITEM", "v5")

                                     )
                                     .withVertex("v5", v -> v.withLabel("createEdgeEntry")
                                                             .withTimId(REL_2_ID.toString())
                                                             .withProperty("rev", 1)
                                                             .withOutgoingRelation("NEXT_ITEM", "v6")
                                     )
                                     .withVertex("v6", v -> v.withLabel("updateEdgeEntry")
                                                             .withTimId(REL_2_ID.toString())
                                                             .withProperty("rev", 2)
                                     )
                                     .wrap();
    GraphLogValidator graphLogValidator = new GraphLogValidator(wrapper);

    Set<Element> result = graphLogValidator.validate();

    assertThat(result, containsInAnyOrder(
      likeElement().ofType(Edge.class).withTimId(REL_1_ID.toString()).withProperty("rev", 1),
      likeElement().ofType(Edge.class).withTimId(REL_1_ID.toString()).withProperty("rev", 2)
    ));
  }
}

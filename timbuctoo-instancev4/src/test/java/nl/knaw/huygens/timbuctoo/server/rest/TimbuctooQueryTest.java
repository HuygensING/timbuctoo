package nl.knaw.huygens.timbuctoo.server.rest;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.server.rest.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.server.rest.VertexBuilder.vertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class TimbuctooQueryTest {

  public static final WwPersonSearchDescription DESCRIPTION = new WwPersonSearchDescription();

  @Test
  public void returnsASearchRefsWithTheRefsOfTheVerticesWithTheTypeOfTheDescription() {
    TimbuctooQuery instance = new TimbuctooQuery(DESCRIPTION);
    Graph graph = newGraph()
      .withVertex(vertex().withType("wwperson").withId("id1"))
      .withVertex(vertex().withType("otherperson").withId("id2"))
      .withVertex(vertex().withType("otherperson").withType("wwperson").withId("id3"))
      .build();
    SearchResult searchResult = instance.execute(graph);

    assertThat(searchResult.getRefs(), containsInAnyOrder(
      new EntityRef("wwperson", "id1"),
      new EntityRef("wwperson", "id3")));
    assertThat(searchResult.getFullTextSearchFields(),
      containsInAnyOrder(DESCRIPTION.getFullTextSearchFields().toArray()));
    assertThat(searchResult.getSortableFields(), containsInAnyOrder(DESCRIPTION.getSortableFields().toArray()));
  }

}

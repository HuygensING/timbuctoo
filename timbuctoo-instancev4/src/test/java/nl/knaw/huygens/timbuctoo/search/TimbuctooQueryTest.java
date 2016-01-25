package nl.knaw.huygens.timbuctoo.search;

import nl.knaw.huygens.timbuctoo.model.PersonName;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.hamcrest.Matchers;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.search.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.search.VertexBuilder.vertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class TimbuctooQueryTest {

  public static final SearchDescription DESCRIPTION = new WwPersonSearchDescription();

  @Test
  public void returnsASearchRefsWithTheRefsOfTheVerticesWithTheTypeOfTheDescription() {
    TimbuctooQuery instance = new TimbuctooQuery(DESCRIPTION);
    Graph graph = newGraph()
      .withVertex(vertex().withType("wwperson").isLatest(true).withId("id1"))
      .withVertex(vertex().withType("otherperson").isLatest(true).withId("id2"))
      .withVertex(vertex().withType("otherperson").isLatest(true).withType("wwperson").withId("id3"))
      .build();
    SearchResult searchResult = instance.execute(graph);

    assertThat(searchResult.getRefs(), Matchers.containsInAnyOrder(
      EntityRefMatcher.likeEntityRef().withId("id1").withType("wwperson"),
      EntityRefMatcher.likeEntityRef().withId("id3").withType("wwperson")));
    assertThat(searchResult.getFullTextSearchFields(),
      containsInAnyOrder(DESCRIPTION.getFullTextSearchFields().toArray()));
    assertThat(searchResult.getSortableFields(), containsInAnyOrder(DESCRIPTION.getSortableFields().toArray()));
  }

  @Test
  public void returnsOnlyTheLatestRefsInTheSearchResult() {
    TimbuctooQuery instance = new TimbuctooQuery(DESCRIPTION);

    PersonNames names1 = new PersonNames();
    PersonName name = PersonName.newInstance("forename", "surname");
    names1.list.add(name);
    names1.list.add(PersonName.newInstance("forename2", "surname2"));

    Graph graph = newGraph()
      .withVertex(vertex()
        .withType("wwperson")
        .withProperty("wwperson_names", names1)
        .withId("id1")
        .isLatest(true))
      .withVertex(vertex()
        .withType("wwperson")
        .withId("id1")
        .isLatest(false))
      .build();

    SearchResult searchResult = instance.execute(graph);

    assertThat(searchResult.getRefs(),
      Matchers.contains(EntityRefMatcher.likeEntityRef().withDisplayName(name.getShortName())));
  }

}

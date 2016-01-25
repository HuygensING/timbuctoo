package nl.knaw.huygens.timbuctoo.search;

import nl.knaw.huygens.timbuctoo.model.PersonName;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.search.description.SearchDescriptionFactory;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.hamcrest.Matchers;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.search.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.search.VertexBuilder.vertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class TimbuctooQueryTest {

  public static final SearchDescription WWPERSON_DESC = new SearchDescriptionFactory().create("wwperson");

  @Test
  public void returnsASearchRefsWithTheRefsOfTheVerticesWithTheTypeOfTheDescription() {
    TimbuctooQuery instance = new TimbuctooQuery(WWPERSON_DESC);
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
      containsInAnyOrder(WWPERSON_DESC.getFullTextSearchFields().toArray()));
    assertThat(searchResult.getSortableFields(), containsInAnyOrder(WWPERSON_DESC.getSortableFields().toArray()));
  }

  @Test
  public void returnsOnlyTheLatestRefsInTheSearchResult() {
    TimbuctooQuery instance = new TimbuctooQuery(WWPERSON_DESC);

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

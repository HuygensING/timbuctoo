package nl.knaw.huygens.timbuctoo.search.description.facet;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Before;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.search.description.facet.DefaultFacetOptionMatcher.likeDefaultFacetOption;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class WwPersonLanguageFacetDescriptionTest {

  public static final String NAME = "name";
  private WwPersonLanguageFacetDescription instance;

  @Before
  public void setUp() throws Exception {
    instance = new WwPersonLanguageFacetDescription(NAME);
  }

  @Test
  public void getFacetReturnsAFacetWithTheTypeListAndTheNameGivenInTheConstructor() {
    Graph graph = newGraph().build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getName(), is(NAME));
    assertThat(facet.getType(), is("LIST"));
  }

  @Test
  public void getFacetReturnsAFacetWithoutOptionsIfTheFoundPersonsHaveNotCreatedAnyDocuments() {
    Graph graph = newGraph()
      .withVertex("person1", v -> v.withTimId("id"))
      .withVertex("person2", v -> v.withTimId("id2"))
      .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(), is(empty()));
  }

  @Test
  public void getFacetGroupsTheResultsOfAllTheLanguagesOfTheDocumentsCreatedByThePerson() {
    Graph graph = newGraph()
      .withVertex("person1", v -> v.withTimId("id"))
      .withVertex("document1", v -> v.withOutgoingRelation("isCreatedBy", "person1"))
      .withVertex("language1", v -> v.withProperty("wwlanguage_name", "Language1")
                                     .withIncomingRelation("hasWorkLanguage", "document1"))
      .withVertex("language2", v -> v.withProperty("wwlanguage_name", "Language2")
                                     .withIncomingRelation("hasWorkLanguage", "document1"))
      .withVertex("document2", v -> v.withOutgoingRelation("isCreatedBy", "person1"))
      .withVertex("language3", v -> v.withProperty("wwlanguage_name", "Language3")
                                     .withIncomingRelation("hasWorkLanguage", "document2"))
      .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(), containsInAnyOrder(
      likeDefaultFacetOption().withName("Language1"),
      likeDefaultFacetOption().withName("Language2"),
      likeDefaultFacetOption().withName("Language3")));
  }

  @Test
  public void getFacetDoesNotSumTheCountsOfTheLanguageOfOnePerson() {
    Graph graph = newGraph()
      .withVertex("person1", v -> v.withTimId("id"))
      .withVertex("language1", v -> v.withProperty("wwlanguage_name", "Language1"))
      .withVertex("language2", v -> v.withProperty("wwlanguage_name", "Language2"))
      .withVertex("language3", v -> v.withProperty("wwlanguage_name", "Language3"))
      .withVertex("document1", v -> v.withOutgoingRelation("isCreatedBy", "person1")
                                     .withOutgoingRelation("hasWorkLanguage", "language1")
                                     .withOutgoingRelation("hasWorkLanguage", "language2"))
      .withVertex("document2", v -> v.withOutgoingRelation("isCreatedBy", "person1")
                                     .withOutgoingRelation("hasWorkLanguage", "language2")
                                     .withOutgoingRelation("hasWorkLanguage", "language3"))
      .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(), containsInAnyOrder(
      new Facet.DefaultOption("Language1", 1),
      new Facet.DefaultOption("Language2", 1),
      new Facet.DefaultOption("Language3", 1)));
  }

  @Test
  public void getFacetSumsCountsTheNumberOfPersonsThatHaveWrittenInACertainLanguage() {
    Graph graph = newGraph()
      .withVertex("person1", v -> v.withTimId("id"))
      .withVertex("person2", v -> v.withTimId("id2"))
      .withVertex("language1", v -> v.withProperty("wwlanguage_name", "Language1"))
      .withVertex("document1", v -> v.withOutgoingRelation("isCreatedBy", "person1")
                                     .withOutgoingRelation("hasWorkLanguage", "language1"))
      .withVertex("document2", v -> v.withOutgoingRelation("isCreatedBy", "person2")
                                     .withOutgoingRelation("hasWorkLanguage", "language1"))
      .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(), contains(new Facet.DefaultOption("Language1", 2L)));
  }
}

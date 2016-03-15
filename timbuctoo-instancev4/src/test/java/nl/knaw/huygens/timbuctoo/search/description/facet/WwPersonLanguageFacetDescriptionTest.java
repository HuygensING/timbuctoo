package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.search.FacetValue;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.ListFacetValue;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.search.VertexMatcher.likeVertex;
import static nl.knaw.huygens.timbuctoo.search.description.facet.DefaultFacetOptionMatcher.likeDefaultFacetOption;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class WwPersonLanguageFacetDescriptionTest {

  public static final String FACET_NAME = "name";
  private WwPersonLanguageFacetDescription instance;

  @Before
  public void setUp() throws Exception {
    instance = new WwPersonLanguageFacetDescription(FACET_NAME);
  }

  @Test
  public void getFacetReturnsAFacetWithTheTypeListAndTheNameGivenInTheConstructor() {
    Graph graph = newGraph().build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getName(), is(FACET_NAME));
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

  @Test
  public void filterDoesNotAddAFilterWhenTheFacetValuesDoesNotContainTheFacet() {
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex("person1", v -> v.withTimId("id"))
      .withVertex("person2", v -> v.withTimId("id2"))
      .withVertex("language1", v -> v.withTimId("id3").withProperty("wwlanguage_name", "Language1"))
      .withVertex("document1", v -> v.withTimId("id4").withOutgoingRelation("isCreatedBy", "person1")
                                     .withOutgoingRelation("hasWorkLanguage", "language1"))
      .withVertex("document2", v -> v.withTimId("id5").withOutgoingRelation("isCreatedBy", "person2")
                                     .withOutgoingRelation("hasWorkLanguage", "language1"))
      .build().traversal().V();
    List<FacetValue> facets = Lists.newArrayList();

    instance.filter(traversal, facets);

    assertThat(traversal.toList(), containsInAnyOrder(
      likeVertex().withTimId("id"),
      likeVertex().withTimId("id2"),
      likeVertex().withTimId("id3"),
      likeVertex().withTimId("id4"),
      likeVertex().withTimId("id5")));
  }

  @Test
  public void filterDoesNotAddAFilterWhenTheFacetValueIsTheWrongType() {
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex("person1", v -> v.withTimId("id"))
      .withVertex("person2", v -> v.withTimId("id2"))
      .withVertex("language1", v -> v.withTimId("id3").withProperty("wwlanguage_name", "Language1"))
      .withVertex("document1", v -> v.withTimId("id4").withOutgoingRelation("isCreatedBy", "person1")
                                     .withOutgoingRelation("hasWorkLanguage", "language1"))
      .withVertex("document2", v -> v.withTimId("id5").withOutgoingRelation("isCreatedBy", "person2")
                                     .withOutgoingRelation("hasWorkLanguage", "language1"))
      .build().traversal().V();
    List<FacetValue> facets = Lists.newArrayList((FacetValue) () -> FACET_NAME);

    instance.filter(traversal, facets);

    assertThat(traversal.toList(), containsInAnyOrder(
      likeVertex().withTimId("id"),
      likeVertex().withTimId("id2"),
      likeVertex().withTimId("id3"),
      likeVertex().withTimId("id4"),
      likeVertex().withTimId("id5")));
  }

  @Test
  public void filterDoesNotAddAFilterWhenTheFacetValueHasNoValues() {
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex("person1", v -> v.withTimId("id"))
      .withVertex("person2", v -> v.withTimId("id2"))
      .withVertex("language1", v -> v.withTimId("id3").withProperty("wwlanguage_name", "Language1"))
      .withVertex("document1", v -> v.withTimId("id4").withOutgoingRelation("isCreatedBy", "person1")
                                     .withOutgoingRelation("hasWorkLanguage", "language1"))
      .withVertex("document2", v -> v.withTimId("id5").withOutgoingRelation("isCreatedBy", "person2")
                                     .withOutgoingRelation("hasWorkLanguage", "language1"))
      .build().traversal().V();
    List<FacetValue> facets = Lists.newArrayList(new ListFacetValue(FACET_NAME, Lists.newArrayList()));

    instance.filter(traversal, facets);

    assertThat(traversal.toList(), containsInAnyOrder(
      likeVertex().withTimId("id"),
      likeVertex().withTimId("id2"),
      likeVertex().withTimId("id3"),
      likeVertex().withTimId("id4"),
      likeVertex().withTimId("id5")));
  }

  @Test
  public void filterAddsAFilterToTheGraphTraversal() {
    String language1 = "Language1";
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex("person1", v -> v.withTimId("pers1"))
      .withVertex("person2", v -> v.withTimId("pers2"))
      .withVertex("language1", v -> v.withTimId("lang1").withProperty("wwlanguage_name", language1))
      .withVertex("language2", v -> v.withTimId("lang2").withProperty("wwlanguage_name", "Language2"))
      .withVertex("document1", v -> v.withTimId("doc1").withOutgoingRelation("isCreatedBy", "person1")
                                     .withOutgoingRelation("hasWorkLanguage", "language1"))
      .withVertex("document2", v -> v.withTimId("doc2").withOutgoingRelation("isCreatedBy", "person2")
                                     .withOutgoingRelation("hasWorkLanguage", "language2"))
      .build().traversal().V();
    List<FacetValue> facets = Lists.newArrayList(new ListFacetValue(FACET_NAME, Lists.newArrayList(language1)));

    instance.filter(traversal, facets);

    assertThat(traversal.toList(), contains(likeVertex().withTimId("pers1")));
  }

  @Test
  public void filterAddsAnOrBetweenMultipleValues() {
    String language1 = "Language1";
    String language2 = "Language2";
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex("person1", v -> v.withTimId("pers1"))
      .withVertex("person2", v -> v.withTimId("pers2"))
      .withVertex("language1", v -> v.withTimId("lang1").withProperty("wwlanguage_name", language1))
      .withVertex("language2", v -> v.withTimId("lang2").withProperty("wwlanguage_name", language2))
      .withVertex("document1", v -> v.withTimId("doc1").withOutgoingRelation("isCreatedBy", "person1")
                                     .withOutgoingRelation("hasWorkLanguage", "language1"))
      .withVertex("document2", v -> v.withTimId("doc2").withOutgoingRelation("isCreatedBy", "person2")
                                     .withOutgoingRelation("hasWorkLanguage", "language2"))
      .build().traversal().V();
    ArrayList<String> values = Lists.newArrayList(language1, language2);
    List<FacetValue> facets = Lists.newArrayList(new ListFacetValue(FACET_NAME, values));

    instance.filter(traversal, facets);

    assertThat(traversal.toList(), containsInAnyOrder(
      likeVertex().withTimId("pers1"),
      likeVertex().withTimId("pers2")));
  }

}

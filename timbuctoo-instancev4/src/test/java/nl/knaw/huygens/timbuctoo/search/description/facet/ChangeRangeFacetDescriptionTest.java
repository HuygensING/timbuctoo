package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.Change;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

public class ChangeRangeFacetDescriptionTest {

  public static final String FACET_NAME = "facetName";
  public static final String PROPERTY_NAME = "propertyName";
  public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMdd");

  @Test
  public void getFacetReturnsTheFacetWithTheNameAndTheTypeRange() {
    ChangeRangeFacetDescription instance = new ChangeRangeFacetDescription(FACET_NAME, PROPERTY_NAME);
    Graph graph = newGraph().build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet, allOf(hasProperty("name", equalTo(FACET_NAME)), hasProperty("type", equalTo("RANGE"))));
  }

  @Test
  public void getFacetReturnsARangeOptionWithDefaultValuesWhenTheStoredDatabaseIsNotValid() {
    ChangeRangeFacetDescription instance = new ChangeRangeFacetDescription(FACET_NAME, PROPERTY_NAME);
    Graph graph = newGraph().withVertex(
      v -> v.withTimId("id").withProperty(PROPERTY_NAME, "invalidChange")
    ).build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet, hasProperty("options", contains(new Facet.RangeOption(0, 0))));
  }

  @Test
  public void getFacetReturnsARangeOptionWithTheLowestAndTheHighestValues() {
    ChangeRangeFacetDescription instance = new ChangeRangeFacetDescription(FACET_NAME, PROPERTY_NAME);
    Graph graph = newGraph().withVertex(v -> v.withProperty(PROPERTY_NAME, serializedChangeWithDate("20150101")))
                            .withVertex(v -> v.withProperty(PROPERTY_NAME, serializedChangeWithDate("10000302")))
                            .withVertex(v -> v.withProperty(PROPERTY_NAME, serializedChangeWithDate("21000302")))
                            .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet, hasProperty("options", contains(new Facet.RangeOption(10000302, 21000302))));
  }

  /**
   * Serializes a Change with a json object mapper.
   *
   * @param dateString a date in the format yyyyMMdd
   * @return the serialized Change
   */
  private String serializedChangeWithDate(String dateString) {
    try {
      Date date = FORMAT.parse(dateString);
      Change change = new Change(date.getTime(), "notImportant", "notImportant");

      return new ObjectMapper().writeValueAsString(change);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }


  }

}

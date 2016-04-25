package nl.knaw.huygens.timbuctoo.search.description.indexes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.PersonName;
import nl.knaw.huygens.timbuctoo.model.PersonNameComponent;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

public class WwDocumentIndexDescriptionTest {

  @Test
  public void getSortIndexPropertyNamesReturnsPropertyNamesForAllTypesAndFields() {
    WwDocumentIndexDescription instance = new WwDocumentIndexDescription();

    Set<String> results = instance.getSortFieldDescriptions().stream()
            .map(IndexerSortFieldDescription::getSortPropertyName)
            .collect(Collectors.toSet());

    assertThat(results, containsInAnyOrder(
            "wwdocument_creator_sort",
            "modified_sort"
    ));
  }

  @Test
  public void addIndexedSortPropertiesSetsTheSortIndexProperties() throws JsonProcessingException {
    long timeStampOnJan20th2016 = 1453290593000L;
    Graph graph = newGraph()
            .withVertex("creator", v -> v
                    .withVre("ww")
                    .withType("person")
                    .withProperty("wwperson_names_sort", "testsur2, testfore")
            )
            .withVertex(v -> v
                    .withVre("ww")
                    .withType("document")
                    .withTimId("123")
                    .withProperty("modified", getChange(timeStampOnJan20th2016))
                    .withOutgoingRelation("isCreatedBy", "creator")
            )
            .build();

    WwDocumentIndexDescription instance = new WwDocumentIndexDescription();
    Vertex vertex = graph.traversal().V().has("tim_id", "123").toList().get(0);

    instance.addIndexedSortProperties(vertex);

    assertThat(vertex.property("wwdocument_creator_sort").value(), equalTo("testsur2, testfore"));
    assertThat(vertex.property("modified_sort").value(), equalTo(timeStampOnJan20th2016));

  }

  @Test
  public void addIndexedSortPropertiesSetsTheSortIndexPropertyToEmptyStringWhenPropertyIsMissing() {
    Graph graph = newGraph()
            .withVertex(v -> v
                    .withVre("ww")
                    .withType("document"))
            .build();
    WwDocumentIndexDescription instance = new WwDocumentIndexDescription();
    Vertex vertex = graph.traversal().V().toList().get(0);

    instance.addIndexedSortProperties(vertex);


    assertThat(vertex.property("wwdocument_creator_sort").value(), equalTo(""));
    assertThat(vertex.property("modified_sort").value(), equalTo(0L));

  }


  private String getChange(long timeStamp) {
    Change change = new Change(timeStamp, "user", "vre");
    String changeString;
    try {
      changeString = new ObjectMapper().writeValueAsString(change);
    } catch (JsonProcessingException e) {
      changeString = "";
    }
    return changeString;
  }
}

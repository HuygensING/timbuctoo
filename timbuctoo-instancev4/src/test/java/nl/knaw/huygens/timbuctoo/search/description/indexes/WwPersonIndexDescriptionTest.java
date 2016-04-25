package nl.knaw.huygens.timbuctoo.search.description.indexes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.PersonName;
import nl.knaw.huygens.timbuctoo.model.PersonNameComponent;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

public class WwPersonIndexDescriptionTest {

  @Test
  public void getSortIndexPropertyNamesReturnsPropertyNamesForAllTypesAndFields() {
    WwPersonIndexDescription instance = new WwPersonIndexDescription();

    Set<String> results = instance.getSortFieldDescriptions().stream()
            .map(IndexerSortFieldDescription::getSortPropertyName)
            .collect(Collectors.toSet());

    assertThat(results, containsInAnyOrder(
            "wwperson_names_sort",
            "wwperson_deathDate_sort",
            "wwperson_birthDate_sort",
            "modified_sort"
    ));
  }

  @Test
  public void addIndexedSortPropertiesSetsTheSortIndexProperties() throws JsonProcessingException {
    long timeStampOnJan20th2016 = 1453290593000L;
    Graph graph = newGraph()
            .withVertex(v -> v
                    .withVre("ww")
                    .withType("person")
                    .withProperty("wwperson_names", getPersonName("testfore", "testsur2"))
                    .withProperty("wwperson_deathDate", "\"2015-05-01\"")
                    .withProperty("wwperson_birthDate", "\"2010-05-01\"")
                    .withProperty("modified", getChange(timeStampOnJan20th2016))
            )
            .build();
    WwPersonIndexDescription instance = new WwPersonIndexDescription();
    Vertex vertex = graph.traversal().V().toList().get(0);

    instance.addIndexedSortProperties(vertex);

    assertThat(vertex.property("wwperson_names_sort").value(), equalTo("testsur2, testfore"));
    assertThat(vertex.property("wwperson_birthDate_sort").value(), equalTo(2010));
    assertThat(vertex.property("wwperson_deathDate_sort").value(), equalTo(2015));
    assertThat(vertex.property("modified_sort").value(), equalTo(timeStampOnJan20th2016));

  }

  @Test
  public void addIndexedSortPropertiesSetsTheSortIndexPropertyToEmptyStringWhenPropertyIsMissing() {
    Graph graph = newGraph()
            .withVertex(v -> v
                    .withVre("ww")
                    .withType("person"))
            .build();
    WwPersonIndexDescription instance = new WwPersonIndexDescription();
    Vertex vertex = graph.traversal().V().toList().get(0);

    instance.addIndexedSortProperties(vertex);

    assertThat(vertex.property("wwperson_names_sort").value(), equalTo(""));
    assertThat(vertex.property("wwperson_birthDate_sort").value(), equalTo(0));
    assertThat(vertex.property("wwperson_deathDate_sort").value(), equalTo(0));
    assertThat(vertex.property("modified_sort").value(), equalTo(0L));

  }


  private String getPersonName(String foreName, String surName) {
    PersonName name = new PersonName();
    name.addNameComponent(PersonNameComponent.Type.FORENAME, foreName);
    name.addNameComponent(PersonNameComponent.Type.SURNAME, surName);
    String nameProp;
    try {
      nameProp = new ObjectMapper().writeValueAsString(name);
    } catch (IOException e) {
      nameProp = "";
    }

    return "{\"list\": [" + nameProp + "]}";
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

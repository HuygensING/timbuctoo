package nl.knaw.huygens.timbuctoo.rdf.conversion;

import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.json.StringEscapeUtils;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.core.dto.property.ArrayProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.HyperLinksProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.PersonNamesProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.StringProperty;
import nl.knaw.huygens.timbuctoo.model.PersonName;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.rdf.Triple;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

public class TriplePropertyConverterTest {

  private static final String PROP_NAME = "propName";
  private static final String SUBJECT_URI = "http://example.com/subject";
  private TriplePropertyConverter instance;

  @Before
  public void setUp() throws Exception {
    instance = new TriplePropertyConverter(mock(Collection.class), SUBJECT_URI);
  }

  @Test
  public void toAddsThePropertyNameToTheLeftOfTheTuple() throws Exception {
    Tuple<String, List<Triple>> to = instance.to(new StringProperty(PROP_NAME, "stringValue"));

    assertThat(to.getLeft(), is(PROP_NAME));
  }

  @Test
  public void toCreatesATripleWithAValueForAString() throws Exception {
    Tuple<String, List<Triple>> to = instance.to(new StringProperty(PROP_NAME, "stringValue"));

    assertThat(to.getRight(), contains(allOf(
      hasProperty("subject", is(SUBJECT_URI)),
      hasProperty("predicate", endsWith(PROP_NAME)),
      hasProperty("object", is("stringValue"))
    )));
  }

  @Test
  public void toCreatesMultipleTriplesForAnArrayProperty() throws Exception {
    Tuple<String, List<Triple>> to = instance.to(new ArrayProperty(PROP_NAME, "[ \"v1\", \"v2\", \"v3\" ]"));

    assertThat(to.getRight(), containsInAnyOrder(
      hasProperty("object", is("v1")),
      hasProperty("object", is("v2")),
      hasProperty("object", is("v3"))
    ));
  }

  @Test
  public void toCreatesABlankNodeForALink() throws Exception {
    Tuple<String, List<Triple>> to = instance.to(
      new HyperLinksProperty(PROP_NAME, "[{\"url\":\"http://www.example.org\",\"label\":\"label\"}]")
    );

    assertThat(to.getRight(), containsInAnyOrder(
      allOf(
        hasProperty("subject", is(SUBJECT_URI)),
        hasProperty("predicate", endsWith(PROP_NAME)),
        hasProperty("object", startsWith("_:"))
      ),
      allOf(
        hasProperty("subject", startsWith("_:")),
        hasProperty("predicate", endsWith(PROP_NAME + "url")),
        hasProperty("object", is("http://www.example.org"))
      ),
      allOf(
        hasProperty("subject", startsWith("_:")),
        hasProperty("predicate", endsWith(PROP_NAME + "label")),
        hasProperty("object", is("label"))
      )
    ));
  }

  @Test
  public void toCreatesATripleWithAJsonVersionOfPersonNames() throws Exception {
    PersonNames value = new PersonNames();
    value.list.add(PersonName.newInstance("forename", "surname"));
    ObjectMapper objectMapper = new ObjectMapper();
    String objectValue = "\"" + StringEscapeUtils.escapeJava(objectMapper.writeValueAsString(value)) + "\"";

    Tuple<String, List<Triple>> to = instance.to(new PersonNamesProperty(PROP_NAME, value));

    assertThat(to.getRight(), contains(
      allOf(
        hasProperty("subject", is(SUBJECT_URI)),
        hasProperty("predicate", endsWith(PROP_NAME)),
        hasProperty("object", startsWith(objectValue))
      )
    ));
  }

  @Test
  public void toAddsTheValueTypePersonNames() throws Exception {
    PersonNames value = new PersonNames();
    value.list.add(PersonName.newInstance("forename", "surname"));

    Tuple<String, List<Triple>> to = instance.to(new PersonNamesProperty(PROP_NAME, value));

    assertThat(to.getRight(), contains(
      allOf(
        hasProperty("datatype", is("http://timbuctoo.huygens.knaw.nl/personnames"))
      )
    ));
  }

}

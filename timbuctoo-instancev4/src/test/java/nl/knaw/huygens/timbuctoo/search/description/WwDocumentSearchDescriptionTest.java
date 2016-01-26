package nl.knaw.huygens.timbuctoo.search.description;

import nl.knaw.huygens.timbuctoo.model.Gender;
import nl.knaw.huygens.timbuctoo.model.PersonName;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.search.EntityRef;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;


import static nl.knaw.huygens.timbuctoo.search.MockVertexBuilder.vertex;
import static nl.knaw.huygens.timbuctoo.search.MockVertexBuilder.vertexWithId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;

public class WwDocumentSearchDescriptionTest {
  private WwDocumentSearchDescription instance;

  @Before
  public void setUp() throws Exception {
    instance = new WwDocumentSearchDescription();
  }

  @Test
  public void createRefCreatesARefWithTheIdOfTheVertexAndTheTypeOfTheDescription() {
    String id = "id";

    Vertex vertex = vertexWithId(id).build();

    EntityRef actualRef = instance.createRef(vertex);

    assertThat(actualRef.getId(), is(id));
    assertThat(actualRef.getType(), is(instance.getType()));
  }

  @Test
  public void createRefAddsADisplayNameToTheRefWhichIsTheAuthorsDashTheTitleBracketsTheDate() {
    PersonNames names1 = new PersonNames();
    PersonNames names2 = new PersonNames();
    names1.list.add(PersonName.newInstance("forename", "surname"));
    names2.list.add(PersonName.newInstance("forename2", "surname2"));


    Vertex authorVertex = vertex().withProperty("wwperson_names", names1).build();
    Vertex authorVertex2 = vertex().withProperty("wwperson_names", names2).build();

    Vertex vertex =
        vertexWithId("id")
        .withOutgoingRelation("isCreatedBy", authorVertex)
        .withOutgoingRelation("isCreatedBy", authorVertex2)
        .withProperty("date", "1850")
        .withProperty("wwdocument_title", "the title")
        .build();


    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is("forename surname; forename2 surname2 - the title (1850)"));
  }

  @Test
  public void createRefAddsADisplayNameToTheRefWithoutTheDateIfDateIsNotAvailable() {
    PersonNames names1 = new PersonNames();
    names1.list.add(PersonName.newInstance("forename", "surname"));
    Vertex authorVertex = vertex().withProperty("wwperson_names", names1).build();

    Vertex vertex = vertexWithId("id")
        .withOutgoingRelation("isCreatedBy", authorVertex)
        .withProperty("wwdocument_title", "the title")
        .build();


    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is("forename surname - the title"));
  }

  @Test
  public void createRefAddsADisplayNameToTheRefWithoutTheAuthorIfNotAvailable() {

    Vertex vertex = vertexWithId("id")
        .withProperty("date", "1850")
        .withProperty("wwdocument_title", "the title")
        .build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is("the title (1850)"));
  }

  @Test
  public void createRefAddsParensEmptyWhenNoTitleOrOtherDataIsAvailable() {
    Vertex vertex = vertexWithId("id").build();


    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is("(empty)"));
  }

  @Test
  public void createRefAddsSemiColonSeparatedTheNamesOfTheAuthors() {
    PersonNames names1 = new PersonNames();
    PersonNames names2 = new PersonNames();
    names1.list.add(PersonName.newInstance("forename", "surname"));
    names2.list.add(PersonName.newInstance("forename2", "surname2"));


    Vertex authorVertex = vertex().withProperty("wwperson_names", names1).build();
    Vertex authorVertex2 = vertex().withProperty("wwperson_names", names2).build();

    Vertex vertex = vertexWithId("id")
        .withOutgoingRelation("isCreatedBy", authorVertex)
        .withOutgoingRelation("isCreatedBy", authorVertex2)
        .build();


    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry("authorName", "forename surname; forename2 surname2"));
  }

  @Test
  public void createRefsAddsDataWithTheKeyIdWithTheIdOfTheVertex() {
    String id = "id";
    Vertex vertex = vertexWithId(id).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry("_id", id));
  }

  @Test
  public void createRefAddsTheAuthorGenderToTheData() {
    Vertex authorVertex = vertex().withProperty("wwperson_gender", Gender.UNKNOWN).build();
    Vertex vertex = vertexWithId("id")
        .withOutgoingRelation("isCreatedBy", authorVertex)
        .build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry("authorGender", "UNKNOWN"));
  }

  @Test
  public void createRefAddsTheDateToTheData() {
    Vertex vertex = vertexWithId("id")
        .withProperty("date", "1850")
        .build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry("date", "1850"));
  }
}

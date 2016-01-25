package nl.knaw.huygens.timbuctoo.search.description;

import nl.knaw.huygens.timbuctoo.model.PersonName;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.search.EntityRef;
import nl.knaw.huygens.timbuctoo.search.MockVertexBuilder;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class WwDocumentSearchDescriptionTest {
  private WwDocumentSearchDescription instance;

  @Before
  public void setUp() throws Exception {
    instance = new WwDocumentSearchDescription();
  }

  @Test
  public void createRefCreatesARefWithTheIdOfTheVertexAndTheTypeOfTheDescription() {
    String id = "id";

    Vertex vertex = MockVertexBuilder.vertexWithId(id).build();

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


    Vertex authorVertex = MockVertexBuilder.vertex().withProperty("wwperson_names", names1).build();
    Vertex authorVertex2 = MockVertexBuilder.vertex().withProperty("wwperson_names", names2).build();

    Vertex vertex = MockVertexBuilder
        .vertexWithId("id")
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
    Vertex authorVertex = MockVertexBuilder.vertex().withProperty("wwperson_names", names1).build();

    Vertex vertex = MockVertexBuilder
        .vertexWithId("id")
        .withOutgoingRelation("isCreatedBy", authorVertex)
        .withProperty("wwdocument_title", "the title")
        .build();


    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is("forename surname - the title"));
  }

  @Test
  public void createRefAddsADisplayNameToTheRefWithoutTheAuthorIfNotAvailable() {

    Vertex vertex = MockVertexBuilder
        .vertexWithId("id")
        .withProperty("date", "1850")
        .withProperty("wwdocument_title", "the title")
        .build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is("the title (1850)"));
  }

}

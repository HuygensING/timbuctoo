package nl.knaw.huygens.timbuctoo.server.rest;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.server.rest.MockVertexBuilder.vertexWithId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class WwPersonSearchDescriptionTest {

  private WwPersonSearchDescription instance;

  @Before
  public void setUp() throws Exception {
    instance = new WwPersonSearchDescription();
  }

  @Test
  public void createRefCreatesARefWithTheIdOfTheVertexAndTheTypeOfTheDescription() {
    String id = "id";
    EntityRef expectedRef = new EntityRef(instance.getType(), id);
    WwPersonSearchDescription.Names names = new WwPersonSearchDescription.Names();
    PersonName name1 = PersonName.newInstance("forename", "surname");
    names.list.add(name1);
    names.list.add(PersonName.newInstance("forename2", "surname2"));
    Vertex vertex = vertexWithId(id).withProperty("names", names).build();

    EntityRef actualRef = instance.createRef(vertex);

    assertThat(actualRef.getId(), is(id));
    assertThat(actualRef.getType(), is(instance.getType()));
  }

  @Test
  public void createRefAddsADisplayNameToTheRefWhichIsTheFirstNameOfTheVertex() {
    String id = "id";
    WwPersonSearchDescription.Names names = new WwPersonSearchDescription.Names();
    PersonName name1 = PersonName.newInstance("forename", "surname");
    names.list.add(name1);
    names.list.add(PersonName.newInstance("forename2", "surname2"));
    Vertex vertex = vertexWithId(id).withProperty("wwperson_names", names).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is(name1.getShortName()));
  }

  @Test
  public void createRefAddsTheTempNameAsDisplayNameWhenNamesDoesNotExist() {
    String id = "id";
    String tempName = "temp name";
    Vertex vertex = vertexWithId(id).withProperty("wwperson_tempName", tempName).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is(tempName));
  }

  @Test
  public void createRefAddsNoDisplayNameWithNamesAndTempNameAreNotAvailable() {
    String id = "id";
    Vertex vertex = vertexWithId(id).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is(nullValue()));
  }

  @Test
  public void createAddsNoDisplayNameWhenTheDeserializationOfNamesFails() {
    String id = "id";
    String invalidNames = "invalidNames";
    Vertex vertex = vertexWithId(id).withProperty("wwperson_names", invalidNames).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is(nullValue()));
  }

  @Test
  public void createRefAddsDataPropertyToTheRef() {
    String id = "id";
    Vertex vertex = vertexWithId(id)
      .build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), is(notNullValue()));
  }

  @Test
  public void createRefsAddsDataWithTheKeyIdWithTheIdOfTheVertex() {
    String id = "id";
    Vertex vertex = vertexWithId(id)
      .build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry("_id", id));
  }

  @Test
  public void createRefsAddsNamePropertyToDataWithValueMatchingDisplayName() {
    String id = "id";
    WwPersonSearchDescription.Names names = new WwPersonSearchDescription.Names();
    PersonName name1 = PersonName.newInstance("forename", "surname");
    names.list.add(name1);
    names.list.add(PersonName.newInstance("forename2", "surname2"));
    Vertex vertex = vertexWithId(id).withProperty("wwperson_names", names).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry("name", name1.getShortName()));
  }

  @Test
  public void createRefsAddsANamePropertyToDataWithTheValueNullWhenTheVertexDoesNotContainAName() {
    String id = "id";
    Vertex vertex = vertexWithId(id).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry(equalTo("name"), nullValue()));
  }

  @Test
  public void createRefAddsABirthDateWithValueNullWhenTheVertexDoesNotContainTheProperty() {
    String id = "id";
    Vertex vertex = vertexWithId(id).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry(equalTo("birthDate"), nullValue()));
  }

  @Test
  public void createRefAddsTheBirthDateToTheData() {
    String id = "id";
    Vertex vertex = vertexWithId(id).withProperty("wwperson_birthDate", "1486-09-14").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry("birthDate", 1486));
  }

  @Test
  public void createRefAddsADeathDateWithValueNullWhenTheVertexDoesNotContainTheProperty() {
    String id = "id";
    Vertex vertex = vertexWithId(id).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry(equalTo("deathDate"), nullValue()));
  }

  @Test
  public void createRefAddsTheDeathDateToTheData() {
    String id = "id";
    Vertex vertex = vertexWithId(id).withProperty("wwperson_deathDate", "1486-09-14").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry("deathDate", 1486));
  }

}

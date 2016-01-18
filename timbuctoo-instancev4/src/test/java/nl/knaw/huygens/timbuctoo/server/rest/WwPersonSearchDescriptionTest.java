package nl.knaw.huygens.timbuctoo.server.rest;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.server.rest.MockVertexBuilder.vertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class WwPersonSearchDescriptionTest {

  @Test
  public void createRefCreatesARefWithTheIdOfTheVertexAndTheTypeOfTheDescription() {
    WwPersonSearchDescription instance = new WwPersonSearchDescription();
    String id = "id";
    EntityRef expectedRef = new EntityRef(instance.getType(), id);
    WwPersonSearchDescription.Names names = new WwPersonSearchDescription.Names();
    PersonName name1 = PersonName.newInstance("forename", "surname");
    names.list.add(name1);
    names.list.add(PersonName.newInstance("forename2", "surname2"));
    Vertex vertex = vertex().withId(id).withProperty("names", names).build();

    EntityRef actualRef = instance.createRef(vertex);

    assertThat(actualRef.getId(), is(id));
    assertThat(actualRef.getType(), is(instance.getType()));
  }

  @Test
  public void createRefAddsADisplayNameToTheRefWhichIsTheFirstNameOfTheVertex() {
    WwPersonSearchDescription instance = new WwPersonSearchDescription();
    String id = "id";
    WwPersonSearchDescription.Names names = new WwPersonSearchDescription.Names();
    PersonName name1 = PersonName.newInstance("forename", "surname");
    names.list.add(name1);
    names.list.add(PersonName.newInstance("forename2", "surname2"));
    Vertex vertex = vertex().withId(id).withProperty("names", names).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is(name1.getShortName()));
  }

  @Test
  public void createRefAddsTheTempNameAsDisplayNameWhenNamesDoesNotExist() {
    WwPersonSearchDescription instance = new WwPersonSearchDescription();
    String id = "id";
    String tempName = "temp name";
    Vertex vertex = vertex().withId(id).withProperty("tempName", tempName).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is(tempName));
  }

  @Test
  public void createRefAddsNoDisplayNameWithNamesAndTempNameAreNotAvailable() {
    WwPersonSearchDescription instance = new WwPersonSearchDescription();
    String id = "id";
    Vertex vertex = vertex().withId(id).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is(nullValue()));
  }

  // TODO: what to do when the deserialization of names fails.
  @Test
  public void createAddsNoDisplayNameWhenTheDeserializationOfNamesFails() {
    WwPersonSearchDescription instance = new WwPersonSearchDescription();
    String id = "id";
    String invalidNames = "invalidNames";
    Vertex vertex = vertex().withId(id).withProperty("names", invalidNames).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is(nullValue()));
  }

  @Test
  public void createRefAddsDataPropertyToTheRef() {
    WwPersonSearchDescription instance = new WwPersonSearchDescription();
    String id = "id";
    Vertex vertex = vertex()
      .withId(id)
      .build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), is(notNullValue()));
  }

}

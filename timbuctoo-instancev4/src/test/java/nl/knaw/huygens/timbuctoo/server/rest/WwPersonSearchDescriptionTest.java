package nl.knaw.huygens.timbuctoo.server.rest;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.server.rest.LocationNames.LocationType.COUNTRY;
import static nl.knaw.huygens.timbuctoo.server.rest.MockVertexBuilder.vertex;
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

    Vertex vertex = vertexWithId(id).build();

    EntityRef actualRef = instance.createRef(vertex);

    assertThat(actualRef.getId(), is(id));
    assertThat(actualRef.getType(), is(instance.getType()));
  }

  @Test
  public void createRefAddsADisplayNameToTheRefWhichIsTheFirstNameOfTheVertex() {
    WwPersonSearchDescription.Names names = new WwPersonSearchDescription.Names();
    PersonName name1 = PersonName.newInstance("forename", "surname");
    names.list.add(name1);
    names.list.add(PersonName.newInstance("forename2", "surname2"));
    Vertex vertex = vertexWithId("id").withProperty("wwperson_names", names).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is(name1.getShortName()));
  }

  @Test
  public void createRefAddsTheTempNameAsDisplayNameWhenNamesDoesNotExist() {
    String tempName = "temp name";
    Vertex vertex = vertexWithId("id").withProperty("wwperson_tempName", tempName).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is(tempName));
  }

  @Test
  public void createRefAddsNoDisplayNameWithNamesAndTempNameAreNotAvailable() {
    Vertex vertex = vertexWithId("id").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is(nullValue()));
  }

  @Test
  public void createAddsNoDisplayNameWhenTheDeserializationOfNamesFails() {
    String invalidNames = "invalidNames";
    Vertex vertex = vertexWithId("id").withProperty("wwperson_names", invalidNames).build();

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
    WwPersonSearchDescription.Names names = new WwPersonSearchDescription.Names();
    PersonName name1 = PersonName.newInstance("forename", "surname");
    names.list.add(name1);
    names.list.add(PersonName.newInstance("forename2", "surname2"));
    Vertex vertex = vertexWithId("id").withProperty("wwperson_names", names).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry("name", name1.getShortName()));
  }

  @Test
  public void createRefsAddsANamePropertyToDataWithTheValueNullWhenTheVertexDoesNotContainAName() {
    Vertex vertex = vertexWithId("id").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry(equalTo("name"), nullValue()));
  }

  @Test
  public void createRefAddsABirthDateWithValueNullWhenTheVertexDoesNotContainTheProperty() {
    Vertex vertex = vertexWithId("id").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry(equalTo("birthDate"), nullValue()));
  }

  @Test
  public void createRefAddsTheBirthDateToTheData() {
    Vertex vertex = vertexWithId("id").withProperty("wwperson_birthDate", "1486-09-14").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry("birthDate", "1486"));
  }

  @Test
  public void createRefAddsADeathDateWithValueNullWhenTheVertexDoesNotContainTheProperty() {
    Vertex vertex = vertexWithId("id").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry(equalTo("deathDate"), nullValue()));
  }

  @Test
  public void createRefAddsTheDeathDateToTheData() {
    Vertex vertex = vertexWithId("id").withProperty("wwperson_deathDate", "1486-09-14").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry("deathDate", "1486"));
  }

  @Test
  public void createRefAddsAGenderWithValueNullWhenTheVertexDoesNotContainTheProperty() {
    Vertex vertex = vertexWithId("id").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry(equalTo("gender"), nullValue()));
  }

  @Test
  public void createRefAddsTheGenderToTheData() {
    Vertex vertex = vertexWithId("id").withProperty("wwperson_gender", "UNKNOWN").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry("gender", "UNKNOWN"));
  }

  @Test
  public void createRefAddsAModifiedDateWithValueNullWhenTheVertexDoesNotContainTheProperty() {
    Vertex vertex = vertexWithId("id").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry(equalTo("modified_date"), nullValue()));
  }

  @Test
  public void createRefAddsAModifiedDateWithValueNullWhenTheValueCouldNotBeRead() {
    Vertex vertex = vertexWithId("id").withProperty("modified", "malformedChange").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry(equalTo("modified_date"), nullValue()));
  }

  @Test
  public void createRefAddsModifiedDateToTheData() {
    long timeStampOnJan20th2016 = 1453290593000L;
    Change change = new Change(timeStampOnJan20th2016, "user", "vre");

    Vertex vertex = vertexWithId("id").withProperty("modified", change).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry("modified_date", "20160120"));
  }

  @Test
  public void createRefAddsNullForResidenceLocationWhenThePersonHasNoResidenceLocations() {
    Vertex vertex = vertexWithId("id").withProperty("modified", "malformedChange").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry(equalTo("residenceLocation"), nullValue()));
  }

  @Test
  public void createRefAddsSemiColonSeparatedTheNamesOfTheResidenceLocations() {
    Vertex location1 = locationVertexWithName("testCountry");
    Vertex location2 = locationVertexWithName("otherCountry");
    Vertex wwPersonVertex = vertexWithId("id")
      .withOutgoingRelation("hasResidenceLocation", location1)
      .withOutgoingRelation("hasResidenceLocation", location2)
      .build();

    EntityRef ref = instance.createRef(wwPersonVertex);

    assertThat(ref.getData(), hasEntry("residenceLocation", "testCountry;otherCountry"));
  }

  private Vertex locationVertexWithName(String name) {
    LocationNames names = new LocationNames("test");
    names.addCountryName("test", name);

    return vertex()
      .withProperty("names", names)
      .withProperty("locationType", COUNTRY)
      .build();
  }

}

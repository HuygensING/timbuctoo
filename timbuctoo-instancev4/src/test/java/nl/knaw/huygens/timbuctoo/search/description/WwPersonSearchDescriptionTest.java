package nl.knaw.huygens.timbuctoo.search.description;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.Datable;
import nl.knaw.huygens.timbuctoo.model.Gender;
import nl.knaw.huygens.timbuctoo.model.LocationNames;
import nl.knaw.huygens.timbuctoo.model.PersonName;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.search.EntityRef;
import nl.knaw.huygens.timbuctoo.search.MockVertexBuilder;
import nl.knaw.huygens.timbuctoo.search.SearchDescription;
import nl.knaw.huygens.timbuctoo.search.description.facet.FacetDescriptionFactory;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static nl.knaw.huygens.timbuctoo.model.LocationNames.LocationType.COUNTRY;
import static nl.knaw.huygens.timbuctoo.search.MockVertexBuilder.vertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class WwPersonSearchDescriptionTest {

  private DefaultSearchDescription instance;
  private FacetDescriptionFactory facetDescriptionFactory;
  private PropertyParserFactory propertyParserFactory;
  private PropertyDescriptorFactory propertyDescriptorFactory;

  @Before
  public void setUp() throws Exception {
    propertyParserFactory = new PropertyParserFactory();
    facetDescriptionFactory = new FacetDescriptionFactory(propertyParserFactory);
    propertyDescriptorFactory = new PropertyDescriptorFactory(propertyParserFactory);
    HashMap<String, PropertyDescriptor> dataPropertyDescriptors = Maps.newHashMap();
    setupPropertyDescriptors(dataPropertyDescriptors);
    instance = new DefaultSearchDescription(
      propertyDescriptorFactory
        .getLocal(SearchDescription.ID_DB_PROP, new PropertyParserFactory().getParser(String.class)),
      propertyDescriptorFactory.getComposite(
        propertyDescriptorFactory.getLocal("wwperson_names", propertyParserFactory.getParser(PersonNames.class)),
        propertyDescriptorFactory.getLocal("wwperson_tempName", propertyParserFactory.getParser(String.class))),
      Lists.newArrayList(
        facetDescriptionFactory.createListFacetDescription("dynamic_s_gender",
          "wwperson_gender",
          propertyParserFactory.getParser(Gender.class))), dataPropertyDescriptors, Lists.newArrayList(
            "dynamic_k_modified",
            "dynamic_k_birthDate",
            "dynamic_sort_name",
            "dynamic_k_deathDate"), Lists.newArrayList(
              "dynamic_t_tempspouse",
              "dynamic_t_notes",
              "dynamic_t_name"), "wwperson"
    );
  }

  private void setupPropertyDescriptors(HashMap<String, PropertyDescriptor> dataPropertyDescriptors) {
    dataPropertyDescriptors.put("birthDate", propertyDescriptorFactory
      .getLocal("wwperson_birthDate", propertyParserFactory.getParser(Datable.class)));
    dataPropertyDescriptors.put("deathDate", propertyDescriptorFactory
      .getLocal("wwperson_deathDate", propertyParserFactory.getParser(Datable.class)));
    dataPropertyDescriptors.put("gender", propertyDescriptorFactory
      .getLocal("wwperson_gender", propertyParserFactory.getParser(Gender.class)));
    dataPropertyDescriptors.put("modified_date",
      propertyDescriptorFactory.getLocal("modified", propertyParserFactory.getParser(Change.class)));
    dataPropertyDescriptors.put("residenceLocation", propertyDescriptorFactory.getDerived(
      "hasResidenceLocation",
      "names",
      propertyParserFactory
        .getParser(LocationNames.class)));
    dataPropertyDescriptors.put("name", propertyDescriptorFactory.getComposite(
      propertyDescriptorFactory.getLocal("wwperson_names", propertyParserFactory.getParser(PersonNames.class)),
      propertyDescriptorFactory.getLocal("wwperson_tempName", propertyParserFactory.getParser(String.class))));
    dataPropertyDescriptors
      .put("_id", propertyDescriptorFactory.getLocal("tim_id", new PropertyParserFactory().getParser(String.class)));
  }

  @Test
  public void createRefCreatesARefWithTheIdOfTheVertexAndTheTypeOfTheDescription() {
    String id = "id";
    EntityRef expectedRef = new EntityRef(instance.getType(), id);

    Vertex vertex = MockVertexBuilder.vertexWithId(id).build();

    EntityRef actualRef = instance.createRef(vertex);

    assertThat(actualRef.getId(), is(id));
    assertThat(actualRef.getType(), is(instance.getType()));
  }

  @Test
  public void createRefAddsADisplayNameToTheRefWhichIsTheFirstNameOfTheVertex() {
    PersonNames names = new PersonNames();
    PersonName name1 = PersonName.newInstance("forename", "surname");
    names.list.add(name1);
    names.list.add(PersonName.newInstance("forename2", "surname2"));
    Vertex vertex = MockVertexBuilder.vertexWithId("id").withProperty("wwperson_names", names).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is(name1.getShortName()));
  }

  @Test
  public void createRefAddsTheTempNameAsDisplayNameWhenNamesDoesNotExist() {
    String tempName = "temp name";
    Vertex vertex = MockVertexBuilder.vertexWithId("id").withProperty("wwperson_tempName", tempName).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is(tempName));
  }

  @Test
  public void createRefAddsNoDisplayNameWithNamesAndTempNameAreNotAvailable() {
    Vertex vertex = MockVertexBuilder.vertexWithId("id").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is(nullValue()));
  }

  @Test
  public void createAddsNoDisplayNameWhenTheDeserializationOfNamesFails() {
    String invalidNames = "invalidNames";
    Vertex vertex = MockVertexBuilder.vertexWithId("id").withProperty("wwperson_names", invalidNames).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getDisplayName(), is(nullValue()));
  }

  @Test
  public void createRefAddsDataPropertyToTheRef() {
    String id = "id";
    Vertex vertex = MockVertexBuilder.vertexWithId(id)
                                     .build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), is(notNullValue()));
  }

  @Test
  public void createRefsAddsDataWithTheKeyIdWithTheIdOfTheVertex() {
    String id = "id";
    Vertex vertex = MockVertexBuilder.vertexWithId(id)
                                     .build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry("_id", id));
  }

  @Test
  public void createRefsAddsNamePropertyToDataWithValueMatchingDisplayName() {
    PersonNames names = new PersonNames();
    PersonName name1 = PersonName.newInstance("forename", "surname");
    names.list.add(name1);
    names.list.add(PersonName.newInstance("forename2", "surname2"));
    Vertex vertex = MockVertexBuilder.vertexWithId("id").withProperty("wwperson_names", names).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry("name", name1.getShortName()));
  }

  @Test
  public void createRefsAddsANamePropertyToDataWithTheValueNullWhenTheVertexDoesNotContainAName() {
    Vertex vertex = MockVertexBuilder.vertexWithId("id").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry(equalTo("name"), nullValue()));
  }

  @Test
  public void createRefAddsABirthDateWithValueNullWhenTheVertexDoesNotContainTheProperty() {
    Vertex vertex = MockVertexBuilder.vertexWithId("id").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry(equalTo("birthDate"), nullValue()));
  }

  @Test
  public void createRefAddsTheBirthDateToTheData() {
    Vertex vertex = MockVertexBuilder.vertexWithId("id").withProperty("wwperson_birthDate", "1486-09-14").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry("birthDate", "1486"));
  }

  @Test
  public void createRefAddsADeathDateWithValueNullWhenTheVertexDoesNotContainTheProperty() {
    Vertex vertex = MockVertexBuilder.vertexWithId("id").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry(equalTo("deathDate"), nullValue()));
  }

  @Test
  public void createRefAddsTheDeathDateToTheData() {
    Vertex vertex = MockVertexBuilder.vertexWithId("id").withProperty("wwperson_deathDate", "1486-09-14").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry("deathDate", "1486"));
  }

  @Test
  public void createRefAddsAGenderWithValueNullWhenTheVertexDoesNotContainTheProperty() {
    Vertex vertex = MockVertexBuilder.vertexWithId("id").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry(equalTo("gender"), nullValue()));
  }

  @Test
  public void createRefAddsTheGenderToTheData() {
    Vertex vertex = MockVertexBuilder.vertexWithId("id").withProperty("wwperson_gender", Gender.UNKNOWN).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry("gender", "UNKNOWN"));
  }

  @Test
  public void createRefAddsAModifiedDateWithValueNullWhenTheVertexDoesNotContainTheProperty() {
    Vertex vertex = MockVertexBuilder.vertexWithId("id").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry(equalTo("modified_date"), nullValue()));
  }

  @Test
  public void createRefAddsAModifiedDateWithValueNullWhenTheValueCouldNotBeRead() {
    Vertex vertex = MockVertexBuilder.vertexWithId("id").withProperty("modified", "malformedChange").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry(equalTo("modified_date"), nullValue()));
  }

  @Test
  public void createRefAddsModifiedDateToTheData() {
    long timeStampOnJan20th2016 = 1453290593000L;
    Change change = new Change(timeStampOnJan20th2016, "user", "vre");

    Vertex vertex = MockVertexBuilder.vertexWithId("id").withProperty("modified", change).build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry("modified_date", "20160120"));
  }

  @Test
  public void createRefAddsNullForResidenceLocationWhenThePersonHasNoResidenceLocations() {
    Vertex vertex = MockVertexBuilder.vertexWithId("id").withProperty("modified", "malformedChange").build();

    EntityRef ref = instance.createRef(vertex);

    assertThat(ref.getData(), hasEntry(equalTo("residenceLocation"), nullValue()));
  }

  @Test
  public void createRefAddsSemiColonSeparatedTheNamesOfTheResidenceLocations() {
    Vertex location1 = locationVertexWithName("testCountry");
    Vertex location2 = locationVertexWithName("otherCountry");
    Vertex wwPersonVertex = MockVertexBuilder.vertexWithId("id")
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

package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.PersonName;
import nl.knaw.huygens.timbuctoo.model.PersonNameComponent;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.rdf.UriBearingPersonNames;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.model.PersonNameStubs.forename;
import static nl.knaw.huygens.timbuctoo.model.PersonNameStubs.surname;
import static nl.knaw.huygens.timbuctoo.rdf.UriBearingPersonNamesJsonStringMatcher.matchesPersonNames;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class PersonNamesTripleProcessorTest {
  public static final String NAME_TYPE = "http://www.w3.org/2001/XMLSchema#string";
  private static final String PERSON_URI = "http://example.com/Jan";
  private static final String TEI_NAMESPACE = "http://www.tei-c.org/ns/1.0/";
  private static final String PREDICATE_URI = TEI_NAMESPACE + "forename";
  private static final String DIFFERENT_PERSON_URI = "http://example.com/Other_Jan";
  private static final String NAMES_PROPERTY_NAME = "names";
  private static final String PERSON_NAMES_TYPE_NAME = "person-names";
  private static final String FORENAME = "Jan";
  private static final RDFDatatype NAME_DATA_TYPE = TypeMapper.getInstance().getSafeTypeByName(NAME_TYPE);
  private static final LiteralLabel FORENAME_LITERAL = LiteralLabelFactory.create(FORENAME, NAME_DATA_TYPE);
  private static final String SURNAME = "Pietersz.";
  private static final String VRE_NAME = "vreName";
  private Entity entity;
  private PersonNamesTripleProcessor instance;

  @Before
  public void setup() {
    final Database database = mock(Database.class);
    instance = new PersonNamesTripleProcessor(database);
    entity = mock(Entity.class);
    given(database.findOrCreateEntity(VRE_NAME, PERSON_URI)).willReturn(entity);
  }

  @Test
  public void processCreatesANewName() throws IOException {
    given(entity.getPropertyValue(NAMES_PROPERTY_NAME)).willReturn(Optional.empty());

    instance.process(VRE_NAME, PERSON_URI, PREDICATE_URI, FORENAME_LITERAL, true);

    verify(entity).addProperty(
      eq(NAMES_PROPERTY_NAME),
      argThat(matchesPersonNames().withPersonName(0, forename(FORENAME), PERSON_URI)),
      eq(PERSON_NAMES_TYPE_NAME)
    );
  }


  @Test
  public void processAddsANewNameComponentWithTheSameSubjectUri() throws IOException {
    final UriBearingPersonNames existing = new UriBearingPersonNames();
    final PersonName existingName = new PersonName();
    existingName.addNameComponent(PersonNameComponent.Type.SURNAME, SURNAME);
    existing.list.add(existingName);
    existing.nameUris.put(PERSON_URI, 0);
    given(entity.getPropertyValue(NAMES_PROPERTY_NAME))
      .willReturn(Optional.of(new ObjectMapper().writeValueAsString(existing)));

    instance.process(VRE_NAME, PERSON_URI, PREDICATE_URI, FORENAME_LITERAL, true);

    verify(entity).addProperty(
      eq(NAMES_PROPERTY_NAME),
      argThat(matchesPersonNames().withPersonName(0, PersonName.newInstance(FORENAME, SURNAME), PERSON_URI)),
      eq(PERSON_NAMES_TYPE_NAME)
    );
  }

  @Test
  public void processAddsANewNameWithADifferentSubjectUri() throws IOException {
    final UriBearingPersonNames existing = new UriBearingPersonNames();
    final PersonName existingName = surname(SURNAME);
    existing.list.add(existingName);
    existing.nameUris.put(DIFFERENT_PERSON_URI, 0);
    given(entity.getPropertyValue(NAMES_PROPERTY_NAME))
      .willReturn(Optional.of(new ObjectMapper().writeValueAsString(existing)));

    instance.process(VRE_NAME, PERSON_URI, PREDICATE_URI, FORENAME_LITERAL, true);

    verify(entity).addProperty(
      eq(NAMES_PROPERTY_NAME),
      argThat(matchesPersonNames()
        .withPersonName(0, surname(SURNAME), DIFFERENT_PERSON_URI)
        .withPersonName(1, forename(FORENAME), PERSON_URI)
      ),
      eq(PERSON_NAMES_TYPE_NAME)
    );
  }

  @Test
  public void processRemovesTheNameFromTheRightPersonNameForARetraction() throws Exception {
    final UriBearingPersonNames existing = new UriBearingPersonNames();
    final PersonName existingName1 = forename(FORENAME);
    existing.list.add(existingName1);
    existing.nameUris.put(DIFFERENT_PERSON_URI, 0);
    final PersonName existingName2 = PersonName.newInstance(FORENAME, SURNAME);
    existing.list.add(existingName2);
    existing.nameUris.put(PERSON_URI, 1);
    given(entity.getPropertyValue(NAMES_PROPERTY_NAME))
      .willReturn(Optional.of(new ObjectMapper().writeValueAsString(existing)));

    instance.process(VRE_NAME, PERSON_URI, PREDICATE_URI, FORENAME_LITERAL, false);

    verify(entity).addProperty(
      eq(NAMES_PROPERTY_NAME),
      argThat(matchesPersonNames()
        .withPersonName(0, forename(FORENAME), DIFFERENT_PERSON_URI)
        .withPersonName(1, surname(SURNAME), PERSON_URI)
      ),
      eq(PERSON_NAMES_TYPE_NAME)
    );
  }

  @Test
  public void processRemovesThePersonNameIfTheRetractionRemovesTheLastNameComponent() throws Exception {
    final UriBearingPersonNames existing = new UriBearingPersonNames();
    final PersonName existingName1 = forename(FORENAME);
    existing.list.add(existingName1);
    existing.nameUris.put(PERSON_URI, 0);
    final PersonName existingName2 = forename(FORENAME);
    existing.list.add(existingName2);
    existing.nameUris.put(DIFFERENT_PERSON_URI, 1);
    given(entity.getPropertyValue(NAMES_PROPERTY_NAME))
      .willReturn(Optional.of(new ObjectMapper().writeValueAsString(existing)));

    instance.process(VRE_NAME, PERSON_URI, PREDICATE_URI, FORENAME_LITERAL, false);

    verify(entity).addProperty(
      eq(NAMES_PROPERTY_NAME),
      argThat(matchesPersonNames()
        .withPersonName(0, forename(FORENAME), DIFFERENT_PERSON_URI)
      ),
      eq(PERSON_NAMES_TYPE_NAME)
    );
  }

}

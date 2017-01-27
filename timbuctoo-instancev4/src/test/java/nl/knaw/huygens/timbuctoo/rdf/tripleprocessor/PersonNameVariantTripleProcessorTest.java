package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.PersonNameComponent;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.rdf.UriBearingPersonNames;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.model.PersonNameStubs.forename;
import static nl.knaw.huygens.timbuctoo.rdf.UriBearingPersonNamesJsonStringMatcher.matchesPersonNames;
import static nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.TripleProcessorDispatcher.TIM_IS_NAME_VARIANT_OF;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class PersonNameVariantTripleProcessorTest {
  private static final String SUBJECT_URI = "http://example.com/Jan";
  private static final String OBJECT_URI = "http://example.com/Piet";
  private static final String NAMES_PROPERTY_NAME = "names";
  private static final String PERSON_NAMES_TYPE_NAME = "person-names";
  private static final String SUBJECT_FORENAME = "Jan";
  private static final String OBJECT_FORENAME = "Piet";
  private static final String VRE_NAME = "vreName";
  private Entity subjectEntity;
  private Entity objectEntity;
  private PersonNameVariantTripleProcessor instance;
  private Database database;

  @Before
  public void setup() {
    database = mock(Database.class);
    instance = new PersonNameVariantTripleProcessor(database);
    subjectEntity = mock(Entity.class);
    objectEntity = mock(Entity.class);
    given(database.findEntity(VRE_NAME, SUBJECT_URI)).willReturn(Optional.of(subjectEntity));
    given(database.findEntity(VRE_NAME, OBJECT_URI)).willReturn(Optional.of(objectEntity));
  }

  @Test
  public void processAddsThePersonNamesOfTheObjectEntityToTheSubjectEntity() throws IOException {
    final String subjectName = makeForename(SUBJECT_FORENAME, SUBJECT_URI);
    final String objectName = makeForename(OBJECT_FORENAME, OBJECT_URI);
    given(subjectEntity.getPropertyValue(NAMES_PROPERTY_NAME)).willReturn(Optional.of(subjectName));
    given(objectEntity.getPropertyValue(NAMES_PROPERTY_NAME)).willReturn(Optional.of(objectName));

    instance.process(VRE_NAME, SUBJECT_URI, TIM_IS_NAME_VARIANT_OF, OBJECT_URI, true);

    verify(objectEntity).addProperty(
      eq(NAMES_PROPERTY_NAME),
      argThat(
        matchesPersonNames()
          .withPersonName(0, forename(OBJECT_FORENAME), OBJECT_URI)
          .withPersonName(1, forename(SUBJECT_FORENAME), SUBJECT_URI)
      ),
      eq(PERSON_NAMES_TYPE_NAME));
    verify(database).addRdfSynonym(VRE_NAME, objectEntity, OBJECT_URI);
    verify(database).purgeEntity(VRE_NAME, subjectEntity);
  }

  @Test
  public void processCreatesANewNameForTheObjectEntityIfNoneIsPresent() throws IOException {
    final String subjectName = makeForename(SUBJECT_FORENAME, SUBJECT_URI);
    given(subjectEntity.getPropertyValue(NAMES_PROPERTY_NAME)).willReturn(Optional.of(subjectName));
    given(objectEntity.getPropertyValue(NAMES_PROPERTY_NAME)).willReturn(Optional.empty());

    instance.process(VRE_NAME, SUBJECT_URI, TIM_IS_NAME_VARIANT_OF, OBJECT_URI, true);

    verify(objectEntity).addProperty(
      eq(NAMES_PROPERTY_NAME),
      argThat(matchesPersonNames().withPersonName(0, forename(SUBJECT_FORENAME), SUBJECT_URI)),
      eq(PERSON_NAMES_TYPE_NAME));
    verify(database).addRdfSynonym(VRE_NAME, objectEntity, OBJECT_URI);
    verify(database).purgeEntity(VRE_NAME, subjectEntity);
  }

  private String makeForename(String forename, String uri) throws JsonProcessingException {
    final UriBearingPersonNames names = new UriBearingPersonNames()
      .addNameComponent(uri, PersonNameComponent.Type.FORENAME, forename);
    return new ObjectMapper().writeValueAsString(names);
  }
}

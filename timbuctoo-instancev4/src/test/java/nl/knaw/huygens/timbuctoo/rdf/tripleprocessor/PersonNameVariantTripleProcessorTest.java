package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.PersonName;
import nl.knaw.huygens.timbuctoo.model.PersonNameComponent;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.rdf.TripleHelper;
import nl.knaw.huygens.timbuctoo.rdf.UriBearingPersonNames;
import org.apache.jena.graph.Triple;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PersonNameVariantTripleProcessorTest {
  private static final String PERSON_URI = "http://example.com/Jan";
  private static final String TIM_IS_NAME_VARIANT_OF = "http://timbuctoo.huygens.knaw.nl/isNameVariantOf";
  private static final String DIFFERENT_PERSON_URI = "http://example.com/Other_Jan";
  private static final String NAMES_PROPERTY_NAME = "names";
  private static final String PERSON_NAMES_TYPE_NAME = "person-names";

  private Triple makeTriple(String subject, String predicate, String object) {
    return TripleHelper.createTripleIterator(
      "<" + subject + "> " +
        "<" + predicate + "> " +
        "<" + object + "> .").next();
  }

  @Test
  public void processAddsThePersonNamesOfTheObjectEntityToTheSubjectEntity() throws IOException {
    final Triple triple = makeTriple(PERSON_URI, TIM_IS_NAME_VARIANT_OF, DIFFERENT_PERSON_URI);
    final Database database = mock(Database.class);
    final PersonNameVariantTripleProcessor instance = new PersonNameVariantTripleProcessor(database);
    final String vreName = "vreName";
    final String foreNameA = "Jan 1";
    final String foreNameB = "Jan 2";
    final Entity subjectEntity = mock(Entity.class);
    final Entity objectEntity = mock(Entity.class);
    final String subjectName = makeName(foreNameA, PERSON_URI);
    final String objectName = makeName(foreNameB, DIFFERENT_PERSON_URI);
    given(database.findEntity(vreName, triple.getSubject())).willReturn(Optional.of(subjectEntity));
    given(database.findEntity(vreName, triple.getObject())).willReturn(Optional.of(objectEntity));
    given(subjectEntity.getPropertyValue(NAMES_PROPERTY_NAME)).willReturn(Optional.of(subjectName));
    given(objectEntity.getPropertyValue(NAMES_PROPERTY_NAME)).willReturn(Optional.of(objectName));

    instance.process(vreName, true, triple);

    final ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<String> propertyName = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<String> typeName = ArgumentCaptor.forClass(String.class);
    verify(objectEntity).addProperty(propertyName.capture(), json.capture(), typeName.capture());
    verify(database).addRdfSynonym(vreName, objectEntity, triple.getObject());
    verify(database).purgeEntity(vreName, subjectEntity);
    assertThat(propertyName.getValue(), equalTo(NAMES_PROPERTY_NAME));
    assertThat(typeName.getValue(), equalTo(PERSON_NAMES_TYPE_NAME));
    assertThat(getNameResult(json).list.size(), equalTo(2));
    assertThat(getNameResult(json).list.get(0).getFullName(), equalTo(foreNameB));
    assertThat(getNameResult(json).list.get(1).getFullName(), equalTo(foreNameA));
    assertThat(getNameResult(json).nameUris.get(PERSON_URI), equalTo(1));
    assertThat(getNameResult(json).nameUris.get(DIFFERENT_PERSON_URI), equalTo(0));
  }

  @Test
  public void processCreatesANewNameForTheObjectEntityIfNoneIsPresent() throws IOException {
    final Triple triple = makeTriple(PERSON_URI, TIM_IS_NAME_VARIANT_OF, DIFFERENT_PERSON_URI);
    final Database database = mock(Database.class);
    final PersonNameVariantTripleProcessor instance = new PersonNameVariantTripleProcessor(database);
    final String vreName = "vreName";
    final String foreNameA = "Jan 1";
    final Entity subjectEntity = mock(Entity.class);
    final Entity objectEntity = mock(Entity.class);
    final String subjectName = makeName(foreNameA, PERSON_URI);
    given(database.findEntity(vreName, triple.getSubject())).willReturn(Optional.of(subjectEntity));
    given(database.findEntity(vreName, triple.getObject())).willReturn(Optional.of(objectEntity));
    given(subjectEntity.getPropertyValue(NAMES_PROPERTY_NAME)).willReturn(Optional.of(subjectName));
    given(objectEntity.getPropertyValue(NAMES_PROPERTY_NAME)).willReturn(Optional.empty());

    instance.process(vreName, true, triple);

    final ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<String> propertyName = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<String> typeName = ArgumentCaptor.forClass(String.class);
    verify(objectEntity).addProperty(propertyName.capture(), json.capture(), typeName.capture());
    verify(database).addRdfSynonym(vreName, objectEntity, triple.getObject());
    verify(database).purgeEntity(vreName, subjectEntity);
    assertThat(propertyName.getValue(), equalTo(NAMES_PROPERTY_NAME));
    assertThat(typeName.getValue(), equalTo(PERSON_NAMES_TYPE_NAME));
    assertThat(getNameResult(json).list.size(), equalTo(1));
    assertThat(getNameResult(json).list.get(0).getFullName(), equalTo(foreNameA));
    assertThat(getNameResult(json).nameUris.get(PERSON_URI), equalTo(0));
  }

  private String makeName(String forename, String uri) throws JsonProcessingException {
    final UriBearingPersonNames names = new UriBearingPersonNames();
    final PersonName name = new PersonName();
    name.addNameComponent(PersonNameComponent.Type.FORENAME, forename);
    names.list.add(name);
    names.nameUris.put(uri, 0);
    return new ObjectMapper().writeValueAsString(names);
  }

  private UriBearingPersonNames getNameResult(ArgumentCaptor<String> json) throws IOException {
    return new ObjectMapper().readValue(json.getValue(), UriBearingPersonNames.class);
  }
}

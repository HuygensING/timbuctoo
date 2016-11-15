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

public class PersonNamesTripleProcessorTest {
  private static final String PERSON_URI = "http://example.com/Jan";
  private static final String TEI_NAMESPACE = "http://www.tei-c.org/ns/1.0/";
  private static final String DIFFERENT_PERSON_URI = "http://example.com/Other_Jan";
  private static final String NAMES_PROPERTY_NAME = "names";
  private static final String PERSON_NAMES_TYPE_NAME = "person-names";

  private Triple makeTriple(String subject, String predicate, String object) {
    return TripleHelper.createTripleIterator(
    "<" + subject + "> " +
      "<" + predicate + "> " +
      "\"" + object + "\"^^<http://www.w3.org/2001/XMLSchema#string> .").next();
  }

  @Test
  public void processCreatesANewName() throws IOException {
    final String foreName = "Jan";
    final String vreName = "vreName";
    final Database database = mock(Database.class);
    final Triple triple = makeTriple(PERSON_URI, TEI_NAMESPACE + "forename", foreName);
    final PersonNamesTripleProcessor instance = new PersonNamesTripleProcessor(database);
    final Entity entity = mock(Entity.class);
    given(database.findOrCreateEntity(vreName, triple.getSubject())).willReturn(entity);
    given(entity.getPropertyValue(NAMES_PROPERTY_NAME)).willReturn(Optional.empty());

    instance.process(vreName, true, triple);

    final ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<String> propertyName = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<String> typeName = ArgumentCaptor.forClass(String.class);
    verify(entity).addProperty(propertyName.capture(), json.capture(), typeName.capture());
    assertThat(propertyName.getValue(), equalTo(NAMES_PROPERTY_NAME));
    assertThat(typeName.getValue(), equalTo(PERSON_NAMES_TYPE_NAME));
    assertThat(getNameResult(json).list.get(0).getFullName(), equalTo(foreName));
    assertThat(getNameResult(json).nameUris.get(PERSON_URI), equalTo(0));
  }

  @Test
  public void processAddsANewNameComponentWithTheSameSubjectUri() throws IOException {
    final String foreName = "Jan";
    final String surName = "Pietersz.";
    final String vreName = "vreName";
    final Database database = mock(Database.class);
    final Triple triple = makeTriple(PERSON_URI, TEI_NAMESPACE + "forename", foreName);
    final PersonNamesTripleProcessor instance = new PersonNamesTripleProcessor(database);
    final Entity entity = mock(Entity.class);
    final UriBearingPersonNames existing = new UriBearingPersonNames();
    final PersonName existingName = new PersonName();
    existingName.addNameComponent(PersonNameComponent.Type.SURNAME, surName);
    existing.list.add(existingName);
    existing.nameUris.put(PERSON_URI, 0);
    given(database.findOrCreateEntity(vreName, triple.getSubject())).willReturn(entity);
    given(entity.getPropertyValue(NAMES_PROPERTY_NAME))
      .willReturn(Optional.of(new ObjectMapper().writeValueAsString(existing)));

    instance.process(vreName, true, triple);

    final ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<String> propertyName = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<String> typeName = ArgumentCaptor.forClass(String.class);
    verify(entity).addProperty(propertyName.capture(), json.capture(), typeName.capture());
    assertThat(propertyName.getValue(), equalTo(NAMES_PROPERTY_NAME));
    assertThat(typeName.getValue(), equalTo(PERSON_NAMES_TYPE_NAME));
    assertThat(getNameResult(json).list.size(), equalTo(1));
    assertThat(getNameResult(json).list.get(0).getFullName(), equalTo(foreName + " " + surName));
    assertThat(getNameResult(json).nameUris.get(PERSON_URI), equalTo(0));
  }

  @Test
  public void processAddsANewNameWithADifferentSubjectUri() throws IOException {
    final String foreName = "Jan";
    final String surName = "Pietersz.";
    final String vreName = "vreName";
    final Database database = mock(Database.class);
    final Triple triple = makeTriple(DIFFERENT_PERSON_URI, TEI_NAMESPACE + "forename", foreName);
    final PersonNamesTripleProcessor instance = new PersonNamesTripleProcessor(database);
    final Entity entity = mock(Entity.class);
    final UriBearingPersonNames existing = new UriBearingPersonNames();
    final PersonName existingName = new PersonName();
    existingName.addNameComponent(PersonNameComponent.Type.SURNAME, surName);
    existing.list.add(existingName);
    existing.nameUris.put(PERSON_URI, 0);
    given(database.findOrCreateEntity(vreName, triple.getSubject())).willReturn(entity);
    given(entity.getPropertyValue(NAMES_PROPERTY_NAME))
      .willReturn(Optional.of(new ObjectMapper().writeValueAsString(existing)));

    instance.process(vreName, true, triple);

    final ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<String> propertyName = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<String> typeName = ArgumentCaptor.forClass(String.class);
    verify(entity).addProperty(propertyName.capture(), json.capture(), typeName.capture());

    assertThat(getNameResult(json).list.size(), equalTo(2));
    assertThat(getNameResult(json).list.get(0).getFullName(), equalTo(surName));
    assertThat(getNameResult(json).list.get(1).getFullName(), equalTo(foreName));
    assertThat(getNameResult(json).nameUris.get(PERSON_URI), equalTo(0));
    assertThat(getNameResult(json).nameUris.get(DIFFERENT_PERSON_URI), equalTo(1));
    assertThat(propertyName.getValue(), equalTo(NAMES_PROPERTY_NAME));
    assertThat(typeName.getValue(), equalTo(PERSON_NAMES_TYPE_NAME));
  }

  private UriBearingPersonNames getNameResult(ArgumentCaptor<String> json) throws IOException {
    return new ObjectMapper().readValue(json.getValue(), UriBearingPersonNames.class);
  }
}

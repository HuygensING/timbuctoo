package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.model.properties.converters.ArrayToEncodedArrayConverter;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AltLabelTripleProcessorTest {
  public static final String VRE_NAME = "vreName";
  private static final String SUBJECT_URI = "http://tl.dbpedia.org/resource/Abadan,_Iran";
  private static final String PREDICATE_NAME = "altLabel";
  private static final String PREDICATE_URI = "http://www.w3.org/2004/02/skos/core#" + PREDICATE_NAME;
  private static final String VALUE = "Abadan";
  private static final String OBJECT_TYPE_URI = "http://www.w3.org/2001/XMLSchema#string";
  private static final String EXISTING_ALT_LABEL = "Abadan, Iran";
  private static final String TIM_TYPE_ID = new ArrayToEncodedArrayConverter().getUniqueTypeIdentifier();
  private Entity entity;
  private AltLabelTripleProcessor instance;

  @Before
  public void setup() {
    final Database database = mock(Database.class);
    entity = mock(Entity.class);
    instance = new AltLabelTripleProcessor(database);
    given(database.findOrCreateEntity(VRE_NAME, SUBJECT_URI)).willReturn(entity);
  }

  @Test
  public void processAddsANewAltLabel() {
    given(entity.getPropertyValue(PREDICATE_NAME)).willReturn(Optional.empty());

    instance.process(VRE_NAME, SUBJECT_URI, PREDICATE_URI, VALUE, OBJECT_TYPE_URI, true);

    verify(entity).addProperty(PREDICATE_NAME, jsnA(jsn(VALUE)).toString(), TIM_TYPE_ID);
  }

  @Test
  public void processAddsToExistingAltLabel() {
    final String existingValue = jsnA(jsn(EXISTING_ALT_LABEL)).toString();
    given(entity.getPropertyValue(PREDICATE_NAME)).willReturn(Optional.of(existingValue));

    instance.process(VRE_NAME, SUBJECT_URI, PREDICATE_URI, VALUE, OBJECT_TYPE_URI, true);

    verify(entity).addProperty(PREDICATE_NAME, jsnA(jsn(EXISTING_ALT_LABEL), jsn(VALUE)).toString(), TIM_TYPE_ID);
  }

  @Test
  public void processRemovesEntryFromExistingAltLabel() {
    final String existingValue = jsnA(jsn(EXISTING_ALT_LABEL), jsn(VALUE)).toString();
    given(entity.getPropertyValue(PREDICATE_NAME)).willReturn(Optional.of(existingValue));

    instance.process(VRE_NAME, SUBJECT_URI, PREDICATE_URI, VALUE, OBJECT_TYPE_URI, false);

    verify(entity).addProperty(PREDICATE_NAME, jsnA(jsn(EXISTING_ALT_LABEL)).toString(), TIM_TYPE_ID);
  }

  @Test
  public void processRemovesExistingAltLabel() {
    final String existingValue = jsnA(jsn(VALUE)).toString();
    given(entity.getPropertyValue(PREDICATE_NAME)).willReturn(Optional.of(existingValue));

    instance.process(VRE_NAME, SUBJECT_URI, PREDICATE_URI, VALUE, OBJECT_TYPE_URI, false);

    verify(entity).removeProperty(PREDICATE_NAME);
  }
}

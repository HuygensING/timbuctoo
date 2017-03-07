package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.internal.verification.VerificationModeFactory.atLeast;

public class SameAsTripleProcessorTest {
  private static final String SUBJECT_URI = "http://tl.dbpedia.org/resource/Abadan,_Iran";
  private static final String OBJECT_URI = "http://n3.dbpedia.org/resource/Abadan,_Iran";
  private static final String SAME_AS_URI = "http://www.w3.org/2002/07/owl#sameAs";
  public static final String VRE_NAME = "vreName";
  private Database database;
  private SameAsTripleProcessor instance;

  @Before
  public void setUp() throws Exception {
    database = mock(Database.class);
    instance = new SameAsTripleProcessor(database);
  }

  @Test
  public void shouldCopyAndRemoveObjectEntityWhenBothEntitiesExist() {
    final Entity subjectEntity = mock(Entity.class);
    final Entity objectEntity = mock(Entity.class);
    final Optional<Entity> subjectEntityOptional = Optional.of(subjectEntity);
    final Optional<Entity> objectEntityOptional = Optional.of(objectEntity);
    final List<Map<String, String>> objectProperties = Lists.newArrayList();
    final Map<String, String> objectProperty = Maps.newHashMap();
    final String unprefixedPropertyName = "the-name";
    final String propertyType = "the-type";
    final String propertyValue = "a value";

    objectProperty.put(LocalProperty.PROPERTY_TYPE_NAME, propertyType);
    objectProperty.put(LocalProperty.CLIENT_PROPERTY_NAME, unprefixedPropertyName);
    objectProperty.put("value", propertyValue);
    objectProperties.add(objectProperty);

    given(database.findEntity(VRE_NAME, SUBJECT_URI)).willReturn(subjectEntityOptional);
    given(database.findEntity(VRE_NAME, OBJECT_URI)).willReturn(objectEntityOptional);
    given(objectEntity.getProperties()).willReturn(objectProperties);
    given(subjectEntity.getProperty(unprefixedPropertyName)).willReturn(Optional.empty());


    instance.process(VRE_NAME, SUBJECT_URI, SAME_AS_URI, OBJECT_URI, true);

    // Verify initial load *and* reload of subject entity
    verify(database, atLeast(2)).findEntity(VRE_NAME, SUBJECT_URI);
    verify(subjectEntity).addProperty(unprefixedPropertyName, propertyValue, propertyType);
    verify(database).copyEdgesFromObjectIntoSubject(subjectEntity, objectEntity);
    verify(database).purgeEntity(VRE_NAME, objectEntity);
    verify(database).addRdfSynonym(VRE_NAME, subjectEntity, OBJECT_URI);
  }

  @Test
  public void doesNothingWhenTheObjectIsBlank() {
    instance.process(VRE_NAME, SUBJECT_URI, SAME_AS_URI, "", true);

    verifyZeroInteractions(database);
  }


}

package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.rdf.TripleHelper;
import org.apache.jena.graph.Triple;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SameAsTripleProcessorTest {
  private static final String ABADAN_URI = "http://tl.dbpedia.org/resource/Abadan,_Iran";
  private static final String ABADAN_URI_2 = "http://n3.dbpedia.org/resource/Abadan,_Iran";
  private static final String SAME_AS_URI = "http://www.w3.org/2002/07/owl#sameAs";

  private static final String ABADAN_IS_PART_OF_IRAN_TRIPLE =
    "<" + ABADAN_URI + "> " +
      "<" + SAME_AS_URI + "> " +
      "<" + ABADAN_URI_2 + "> .";

  @Test
  public void shouldCopyAndRemoveObjectEntityWhenBothEntitiesExist() {
    final Database database = mock(Database.class);
    final String vreName = "vreName";
    final Entity subjectEntity = mock(Entity.class);
    final Entity objectEntity = mock(Entity.class);
    final Optional<Entity> subjectEntityOptional = Optional.of(subjectEntity);
    final Optional<Entity> objectEntityOptional = Optional.of(objectEntity);
    final Triple triple = TripleHelper.createTripleIterator(ABADAN_IS_PART_OF_IRAN_TRIPLE).next();
    final SameAsTripleProcessor instance = new SameAsTripleProcessor(database);
    final List<Map<String, String>> objectProperties = Lists.newArrayList();
    final Map<String, String> objectProperty = Maps.newHashMap();
    final String unprefixedPropertyName = "the-name";
    final String propertyType = "the-type";
    final String propertyValue = "a value";

    objectProperty.put(LocalProperty.PROPERTY_TYPE_NAME, propertyType);
    objectProperty.put(LocalProperty.CLIENT_PROPERTY_NAME, unprefixedPropertyName);
    objectProperty.put("value", propertyValue);
    objectProperties.add(objectProperty);

    given(database.findEntity(vreName, triple.getSubject())).willReturn(subjectEntityOptional);
    given(database.findEntity(vreName, triple.getObject())).willReturn(objectEntityOptional);
    given(objectEntity.getProperties()).willReturn(objectProperties);
    given(subjectEntity.getProperty(unprefixedPropertyName)).willReturn(Optional.empty());


    instance.process(vreName, true, triple);


    verify(database).copyEdgesFromObjectIntoSubject(vreName, subjectEntity, objectEntity);
    verify(subjectEntity).addProperty(unprefixedPropertyName, propertyValue, propertyType);
  }
}

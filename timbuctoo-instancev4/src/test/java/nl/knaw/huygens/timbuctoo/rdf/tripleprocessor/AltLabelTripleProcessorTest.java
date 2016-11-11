package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.model.properties.converters.ArrayToEncodedArrayConverter;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.rdf.TripleHelper;
import org.apache.jena.graph.Triple;
import org.junit.Test;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AltLabelTripleProcessorTest {
  private static final String ABADAN_URI = "http://tl.dbpedia.org/resource/Abadan,_Iran";
  private static final String ABADAN_ALT_LABEL = "Abadan";
  private static final String ALT_LABEL_URI = "http://www.w3.org/2004/02/skos/core#altLabel";
  private static final String ABADAN_IRAN_ALT_LABEL = "Abadan, Iran";

  private static final String ABADAN_HAS_ALT_LABEL =
    "<" + ABADAN_URI + "> " +
      "<" + ALT_LABEL_URI + "> " +
      "\"" + ABADAN_ALT_LABEL + "\"^^<http://www.w3.org/2001/XMLSchema#string> .";

  @Test
  public void processAddsANewAltLabel() {
    final Database database = mock(Database.class);
    final String vreName = "vreName";
    final Entity entity = mock(Entity.class);
    final AltLabelTripleProcessor instance = new AltLabelTripleProcessor(database);
    final Triple triple = TripleHelper.createTripleIterator(ABADAN_HAS_ALT_LABEL).next();
    final String propertyName = triple.getPredicate().getLocalName();
    given(entity.getPropertyValue(propertyName)).willReturn(Optional.empty());
    given(database.findOrCreateEntity(vreName, triple.getSubject())).willReturn(entity);

    instance.process(vreName, true, triple);

    verify(entity).addProperty(propertyName, jsnA(jsn(ABADAN_ALT_LABEL)).toString(),
      new ArrayToEncodedArrayConverter().getUniqueTypeIdentifier());
  }

  @Test
  public void processAddsToExistingAltLabel() {
    final Database database = mock(Database.class);
    final String vreName = "vreName";
    final Entity entity = mock(Entity.class);
    final AltLabelTripleProcessor instance = new AltLabelTripleProcessor(database);
    final Triple triple = TripleHelper.createTripleIterator(ABADAN_HAS_ALT_LABEL).next();
    final String propertyName = triple.getPredicate().getLocalName();
    final String existingValue = jsnA(jsn(ABADAN_IRAN_ALT_LABEL)).toString();
    given(entity.getPropertyValue(propertyName)).willReturn(Optional.of(existingValue));

    given(database.findOrCreateEntity(vreName, triple.getSubject())).willReturn(entity);

    instance.process(vreName, true, triple);

    verify(entity).addProperty(propertyName, jsnA(jsn(ABADAN_IRAN_ALT_LABEL), jsn(ABADAN_ALT_LABEL)).toString(),
      new ArrayToEncodedArrayConverter().getUniqueTypeIdentifier());
  }

  @Test
  public void processRemovesEntryFromExistingAltLabel() {
    final Database database = mock(Database.class);
    final String vreName = "vreName";
    final Entity entity = mock(Entity.class);
    final AltLabelTripleProcessor instance = new AltLabelTripleProcessor(database);
    final Triple triple = TripleHelper.createTripleIterator(ABADAN_HAS_ALT_LABEL).next();
    final String propertyName = triple.getPredicate().getLocalName();
    final String existingValue = jsnA(jsn(ABADAN_IRAN_ALT_LABEL), jsn(ABADAN_ALT_LABEL)).toString();
    given(entity.getPropertyValue(propertyName)).willReturn(Optional.of(existingValue));

    given(database.findOrCreateEntity(vreName, triple.getSubject())).willReturn(entity);

    instance.process(vreName, false, triple);

    verify(entity).addProperty(propertyName, jsnA(jsn(ABADAN_IRAN_ALT_LABEL)).toString(),
      new ArrayToEncodedArrayConverter().getUniqueTypeIdentifier());
  }

  @Test
  public void processRemovesExistingAltLabel() {
    final Database database = mock(Database.class);
    final String vreName = "vreName";
    final Entity entity = mock(Entity.class);
    final AltLabelTripleProcessor instance = new AltLabelTripleProcessor(database);
    final Triple triple = TripleHelper.createTripleIterator(ABADAN_HAS_ALT_LABEL).next();
    final String propertyName = triple.getPredicate().getLocalName();
    final String existingValue = jsnA(jsn(ABADAN_ALT_LABEL)).toString();
    given(entity.getPropertyValue(propertyName)).willReturn(Optional.of(existingValue));

    given(database.findOrCreateEntity(vreName, triple.getSubject())).willReturn(entity);

    instance.process(vreName, false, triple);

    verify(entity).removeProperty(propertyName);
  }
}

package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.core.RdfImportSession;
import org.junit.Test;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class PropertyTripleProcessorTest {
  private static final String OBJECT_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString";
  private static final String SUBJECT_URI = "http://tl.dbpedia.org/resource/Abadan,_Iran";
  private static final String PREDICATE_URI = "http://www.georss.org/georss/point";
  private static final String OBJECT_VALUE = "30.35 48.28333333333333";

  @Test
  public void processAddsThePropertyToTheEntity() {
    RdfImportSession rdfImportSession = mock(RdfImportSession.class);
    PropertyTripleProcessor instance = new PropertyTripleProcessor(rdfImportSession);

    instance.process("vreName", SUBJECT_URI, PREDICATE_URI, OBJECT_VALUE, OBJECT_TYPE, true);

    verify(rdfImportSession).assertProperty(
      eq(SUBJECT_URI),
      argThat(allOf(
        hasProperty("predicateUri", equalTo(PREDICATE_URI)),
        hasProperty("value", equalTo(OBJECT_VALUE)),
        hasProperty("typeUri", equalTo(OBJECT_TYPE))
      ))
    );
  }

  @Test
  public void processRemovesThePropertyFromTheEntityIfTheCallIsARetraction() {
    RdfImportSession rdfImportSession = mock(RdfImportSession.class);
    PropertyTripleProcessor instance = new PropertyTripleProcessor(rdfImportSession);

    instance.process("vreName", SUBJECT_URI, PREDICATE_URI, OBJECT_VALUE, OBJECT_TYPE, false);

    verify(rdfImportSession).retractProperty(
      eq(SUBJECT_URI),
      argThat(allOf(
        hasProperty("predicateUri", equalTo(PREDICATE_URI)),
        hasProperty("typeUri", equalTo(OBJECT_TYPE))
      ))
    );
  }
}

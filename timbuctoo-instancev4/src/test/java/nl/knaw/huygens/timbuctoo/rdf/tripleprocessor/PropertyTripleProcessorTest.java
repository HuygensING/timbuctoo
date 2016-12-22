package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.core.RdfImportSession;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.TripleHelper;
import org.apache.jena.graph.Triple;
import org.junit.Test;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class PropertyTripleProcessorTest {
  public static final String DEFAULT_RDF_TYPE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString";
  private static final String PREDICATE_URI = "http://www.georss.org/georss/point";
  private static final String VALUE = "30.35 48.28333333333333";
  private static final String ABADAN_URI = "http://tl.dbpedia.org/resource/Abadan,_Iran";
  private static final String ABADAN_POINT_TRIPLE =
    "<" + ABADAN_URI + "> " +
      "<" + PREDICATE_URI + "> " +
      "\"" + VALUE + "\"@tl .";
  private static final Triple TRIPLE = TripleHelper.createSingleTriple(ABADAN_POINT_TRIPLE);

  @Test
  public void processAddsThePropertyToTheEntity() {
    RdfImportSession rdfImportSession = mock(RdfImportSession.class);
    PropertyTripleProcessor instance = new PropertyTripleProcessor(mock(Database.class), rdfImportSession);

    instance.process("vreName", true, TRIPLE);

    verify(rdfImportSession).assertProperty(
      eq(ABADAN_URI),
      argThat(allOf(
        hasProperty("predicateUri", equalTo(PREDICATE_URI)),
        hasProperty("value", equalTo(VALUE)),
        hasProperty("typeUri", equalTo(DEFAULT_RDF_TYPE_URI))
      ))
    );
  }

  @Test
  public void processRemovesThePropertyFromTheEntityIfTheCallIsARetraction() {
    RdfImportSession rdfImportSession = mock(RdfImportSession.class);
    PropertyTripleProcessor instance = new PropertyTripleProcessor(mock(Database.class), rdfImportSession);

    instance.process("vreName", false, TRIPLE);

    verify(rdfImportSession).retractProperty(
      eq(ABADAN_URI),
      argThat(allOf(
        hasProperty("predicateUri", equalTo(PREDICATE_URI)),
        hasProperty("typeUri", equalTo(DEFAULT_RDF_TYPE_URI))
      ))
    );
  }
}

package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.core.RdfImportSession;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.RdfReadProperty;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class PersonNamesTripleProcessorTest {

  private static final String SUBJECT_URI = "http://example.org/subject";
  private static final String PREDICATE_URI = "http://example.org/predicate";
  private static final String OBJECT_VALUE = "{\"components\":[{\"type\":\"FORENAME\"," +
    "\"value\":\"Jacob\"},{\"type\":\"SURNAME\",\"value\":\"Goethals Vercruyssen\"}]}";
  private static final String OBJECT_TYPE_URI = "http://timbuctoo.huygens.knaw.nl/types/personnames";
  private static final String VRE_NAME = "vreName";
  private RdfImportSession rdfImportSession;
  private PersonNamesTripleProcessor instance;

  @Before
  public void setUp() throws Exception {
    rdfImportSession = mock(RdfImportSession.class);
    given(rdfImportSession.retrieveProperty(anyString(), anyString())).willReturn(Optional.empty());
    instance = new PersonNamesTripleProcessor(rdfImportSession);
  }

  @Test
  public void processAssertionAddsANameWhenTheEntityDoesNotHaveAName() {
    instance.process(VRE_NAME, SUBJECT_URI, PREDICATE_URI, OBJECT_VALUE, OBJECT_TYPE_URI, true);

    verify(rdfImportSession).assertProperty(
      eq(SUBJECT_URI),
      argThat(allOf(
        hasProperty("predicateUri", equalTo(PREDICATE_URI)),
        hasProperty("value", equalTo("{\"list\":[{\"components\":[{\"type\":\"FORENAME\"," +
          "\"value\":\"Jacob\"},{\"type\":\"SURNAME\",\"value\":\"Goethals Vercruyssen\"}]}]}")),
        hasProperty("typeUri", equalTo(OBJECT_TYPE_URI))
      ))
    );
  }

  @Test
  public void processAssertAddsSecondName() {
    given(rdfImportSession.retrieveProperty(SUBJECT_URI, PREDICATE_URI)).willReturn(Optional.of(
      new RdfReadProperty(PREDICATE_URI, "{\"list\":[{\"components\":[{\"type\":\"FORENAME\"," +
        "\"value\":\"Jan\"},{\"type\":\"SURNAME\",\"value\":\"Jansen\"}]}]}")
    ));

    instance.process(VRE_NAME, SUBJECT_URI, PREDICATE_URI, OBJECT_VALUE, OBJECT_TYPE_URI, true);

    verify(rdfImportSession).assertProperty(
      eq(SUBJECT_URI),
      argThat(allOf(
        hasProperty("predicateUri", equalTo(PREDICATE_URI)),
        hasProperty("value", equalTo("{\"list\":[{\"components\":[{\"type\":\"FORENAME\"," +
          "\"value\":\"Jan\"},{\"type\":\"SURNAME\",\"value\":\"Jansen\"}]},{\"components\":[{\"type\":\"FORENAME\"," +
          "\"value\":\"Jacob\"},{\"type\":\"SURNAME\",\"value\":\"Goethals Vercruyssen\"}]}]}")),
        hasProperty("typeUri", equalTo(OBJECT_TYPE_URI))
      ))
    );
  }
}

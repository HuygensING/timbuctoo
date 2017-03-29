package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.core.RdfImportSession;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.RdfReadProperty;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.PersonNamesTripleProcessor.NAMES_TYPE_ID;
import static nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.PersonNamesTripleProcessor.PERSON_NAMES_TYPE_URI;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class PersonNamesTripleProcessorTest {

  private static final String SUBJECT_URI = "http://example.org/subject";
  public static final String PROPERTY_NAME = "predicate";
  private static final String PREDICATE_URI = "http://example.org/" + PROPERTY_NAME;
  private static final String OBJECT_VALUE = "{\"components\":[{\"type\":\"FORENAME\"," +
    "\"value\":\"Jacob\"},{\"type\":\"SURNAME\",\"value\":\"Goethals Vercruyssen\"}]}";
  private static final String OBJECT_TYPE_URI = "http://timbuctoo.huygens.knaw.nl/datatypes/person-name";
  private static final String VRE_NAME = "vreName";
  private PersonNamesTripleProcessor instance;
  private Database database;
  private Entity entity;

  @Before
  public void setUp() throws Exception {
    RdfImportSession rdfImportSession = mock(RdfImportSession.class);
    database = mock(Database.class);
    instance = new PersonNamesTripleProcessor(rdfImportSession, database);
    entity = mock(Entity.class);
    given(entity.getPropertyValue(PROPERTY_NAME)).willReturn(Optional.empty());
    given(database.findOrCreateEntity(VRE_NAME, SUBJECT_URI)).willReturn(entity);

  }

  @Test
  public void processAssertionAssertsThePropertyToTheSubject() {
    instance.process(VRE_NAME, SUBJECT_URI, PREDICATE_URI, OBJECT_VALUE, OBJECT_TYPE_URI, true);

    verify(entity).addProperty(
      PROPERTY_NAME,
      "{\"list\":[{\"components\":[{\"type\":\"FORENAME\"," +
      "\"value\":\"Jacob\"},{\"type\":\"SURNAME\",\"value\":\"Goethals Vercruyssen\"}]}]}",
      NAMES_TYPE_ID
    );
  }

  @Test
  public void processAssertAddsSecondName() {
    given(entity.getPropertyValue(PROPERTY_NAME)).willReturn(Optional.of(
      "{\"list\":[{\"components\":[{\"type\":\"FORENAME\"," +
        "\"value\":\"Jan\"},{\"type\":\"SURNAME\",\"value\":\"Jansen\"}]}]}"
    ));

    instance.process(VRE_NAME, SUBJECT_URI, PREDICATE_URI, OBJECT_VALUE, OBJECT_TYPE_URI, true);

    verify(entity).addProperty(
      PROPERTY_NAME,
      "{\"list\":[{\"components\":[{\"type\":\"FORENAME\"," +
        "\"value\":\"Jan\"},{\"type\":\"SURNAME\",\"value\":\"Jansen\"}]},{\"components\":[{\"type\":\"FORENAME\"," +
        "\"value\":\"Jacob\"},{\"type\":\"SURNAME\",\"value\":\"Goethals Vercruyssen\"}]}]}",
      NAMES_TYPE_ID
    );
  }
}

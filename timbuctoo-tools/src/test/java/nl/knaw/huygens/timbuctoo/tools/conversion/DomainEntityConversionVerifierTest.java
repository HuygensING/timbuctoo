package nl.knaw.huygens.timbuctoo.tools.conversion;

import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import test.model.projectb.ProjectBPerson;

import com.google.common.collect.Lists;

public class DomainEntityConversionVerifierTest {
  private MongoConversionStorage mongoStorage;
  private GraphStorage graphStorage;
  private PropertyVerifier propertyVerifier;
  private DomainEntityConversionVerifier<ProjectBPerson> instance;
  private ProjectBPerson mongoVersion;
  private ProjectBPerson graphVersion;

  private static final String NEW_ID = "newId";
  private static final int REVISION = 12;
  private static final String OLD_ID = "oldId";
  private static final Class<ProjectBPerson> TYPE = ProjectBPerson.class;

  @Before
  public void setup() throws Exception {

    mongoStorage = mock(MongoConversionStorage.class);
    graphStorage = mock(GraphStorage.class);
    propertyVerifier = mock(PropertyVerifier.class);
    instance = new DomainEntityConversionVerifier<ProjectBPerson>(TYPE, mongoStorage, graphStorage, propertyVerifier, REVISION);

    mongoVersion = new ProjectBPerson();
    when(mongoStorage.getRevision(TYPE, OLD_ID, REVISION)).thenReturn(mongoVersion);
    graphVersion = new ProjectBPerson();
    when(graphStorage.getDomainEntityRevision(TYPE, NEW_ID, REVISION)).thenReturn(graphVersion);
  }

  @Test
  public void verifyConversionRetrievesTheMongoVersionAndTheGraphVersionOfAnObject() throws Exception {
    // action
    instance.verifyConversion(OLD_ID, NEW_ID);

    // verify
    verify(mongoStorage).getRevision(TYPE, OLD_ID, REVISION);
    verify(graphStorage).getDomainEntityRevision(TYPE, NEW_ID, REVISION);
  }

  @Test
  public void verifyConversionGetsTheLatestVersionFromMongoIfTheRevisionCannotBeFoundInTheVersionCollection() throws Exception {
    // setup
    when(mongoStorage.getRevision(TYPE, OLD_ID, REVISION)).thenReturn(null);
    when(mongoStorage.getEntity(TYPE, OLD_ID)).thenReturn(mongoVersion);

    // action
    instance.verifyConversion(OLD_ID, NEW_ID);

    // verify
    verify(mongoStorage).getRevision(TYPE, OLD_ID, REVISION);
    verify(mongoStorage).getEntity(TYPE, OLD_ID);
    verify(graphStorage).getDomainEntityRevision(TYPE, NEW_ID, REVISION);
  }

  @Test
  public void verifyConversionGetsTheLatestVersionFromTheGraphIfTheRevisionHasNoPID() throws Exception {
    // setup
    when(graphStorage.getDomainEntityRevision(TYPE, NEW_ID, REVISION)).thenReturn(null);
    when(graphStorage.getEntity(TYPE, NEW_ID)).thenReturn(graphVersion);

    // action
    instance.verifyConversion(OLD_ID, NEW_ID);

    // verify
    verify(mongoStorage).getRevision(TYPE, OLD_ID, REVISION);
    verify(graphStorage).getDomainEntityRevision(TYPE, NEW_ID, REVISION);
    verify(graphStorage).getEntity(TYPE, NEW_ID);
  }

  @Test
  public void verifyConversionVerifiesAllTheFieldsExceptId() throws Exception {
    // action
    instance.verifyConversion(OLD_ID, NEW_ID);

    // verify

    // entity
    verifyCheck("created");
    verifyCheck("modified");
    verifyCheck("rev");

    // domain entity
    verifyCheck("displayName");
    verifyCheck("pid");
    verifyCheck("deleted");
    verifyCheck("relationCount");
    verifyCheck("properties");
    verifyCheck("relations");
    verifyCheck("names");
    verifyCheck("variations");

    // person
    verifyCheck("names");
    verifyCheck("gender");
    verifyCheck("birthDate");
    verifyCheck("deathDate");
    verifyCheck("types");
    verifyCheck("links");
    verifyCheck("floruit");

  }

  private void verifyCheck(String fieldName) {
    verify(propertyVerifier).check(argThat(is(fieldName)), any(), any());
  }

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void verifyConversionThrowsAnVerificationExceptionIfOneOrMoreOfTheFieldsContainsDifferentValues() throws Exception {
    // setup
    when(propertyVerifier.hasInconsistentProperties()).thenReturn(true);
    Mismatch mismatch = new Mismatch("fieldName", "oldValue", "newValue");
    ArrayList<Mismatch> mismatches = Lists.newArrayList(mismatch);
    when(propertyVerifier.getMismatches()).thenReturn(mismatches);

    exception.expect(VerificationException.class);
    exception.expectMessage(mismatch.toString());

    // instance
    instance.verifyConversion(OLD_ID, NEW_ID);
  }
}

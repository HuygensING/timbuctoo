package nl.knaw.huygens.timbuctoo.tools.conversion;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Lists;

public class RelationTypeConversionCheckerTest {

  private static final Class<RelationType> TYPE = RelationType.class;
  private RelationType mongoType;
  private RelationType graphType;
  private MongoStorage mongoStorage;
  private String mongoId;
  private GraphStorage graphStorage;
  private String graphId;
  private RelationTypeConversionChecker instance;
  private Change defaultCreated;
  private boolean defaultDerived;
  private String defaultInverseName;
  private Change defaultModified;
  private boolean defaultReflexive;
  private String defaultRegularName;
  private int defaultRev;
  private String defaultSourceTypeName;
  private boolean defaultSymmetric;
  private String defaultTargetTypeName;
  private PropertyVerifier propertyVerifier;

  @Before
  public void setup() throws StorageException {
    defaultCreated = Change.newInternalInstance();
    defaultCreated.setTimeStamp(1000l);
    defaultDerived = true;
    defaultInverseName = "inverseName";
    defaultModified = Change.newInternalInstance();
    defaultReflexive = false;
    defaultRegularName = "regularName";
    defaultRev = 8;
    defaultSourceTypeName = "sourceTypeName";
    defaultSymmetric = true;
    defaultTargetTypeName = "targeTypeName";

    mongoType = new RelationType();
    graphType = new RelationType();

    mongoStorage = mock(MongoStorage.class);
    mongoId = "mongoId";
    when(mongoStorage.getEntity(TYPE, mongoId)).thenReturn(mongoType);

    graphStorage = mock(GraphStorage.class);
    graphId = "graphId";
    when(graphStorage.getEntity(TYPE, graphId)).thenReturn(graphType);

    propertyVerifier = mock(PropertyVerifier.class);

    instance = new RelationTypeConversionChecker(mongoStorage, graphStorage, propertyVerifier);
  }

  @Test
  public void verifyConversionRetrievesTheMongoVersionAndTheGraphVersionOfAnObject() throws Exception {
    // action
    instance.verifyConversion(mongoId, graphId);

    // verify
    verify(mongoStorage).getEntity(TYPE, mongoId);
    verify(graphStorage).getEntity(TYPE, graphId);
  }

  @Test
  public void verifyConversionVerifiesAllTheFieldsExceptId() throws Exception {
    // setup
    setDefaultProperties(mongoType);
    setDefaultProperties(graphType);

    // action
    instance.verifyConversion(mongoId, graphId);

    // action
    verify(propertyVerifier, times(10)).check(anyString(), any(), any());
  }

  public void setDefaultProperties(RelationType relationType) {
    relationType.setCreated(defaultCreated);
    relationType.setDerived(defaultDerived);
    relationType.setInverseName(defaultInverseName);
    relationType.setModified(defaultModified);
    relationType.setReflexive(defaultReflexive);
    relationType.setRegularName(defaultRegularName);
    relationType.setRev(defaultRev);
    relationType.setSourceTypeName(defaultSourceTypeName);
    relationType.setSymmetric(defaultSymmetric);
    relationType.setTargetTypeName(defaultTargetTypeName);
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
    instance.verifyConversion(mongoId, graphId);

  }
}

package nl.knaw.huygens.timbuctoo.tools.conversion;

import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Lists;

public class SystemEntityConversionVerifierTest {

  private static final String MONGO_ID = "mongoId";
  private static final String GRAPH_ID = "graphId";
  private static final Object NEW_INTERNAL_ID = "newInternalId";
  private static final Class<RelationType> TYPE = RelationType.class;
  private RelationType mongoType;
  private RelationType graphType;
  private MongoStorage mongoStorage;
  private TinkerPopConversionStorage graphStorage;
  private SystemEntityConversionVerifier<RelationType> instance;
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
    when(mongoStorage.getEntity(TYPE, MONGO_ID)).thenReturn(mongoType);

    graphStorage = mock(TinkerPopConversionStorage.class);
    when(graphStorage.getEntityByVertexId(TYPE, NEW_INTERNAL_ID)).thenReturn(graphType);

    propertyVerifier = mock(PropertyVerifier.class);

    instance = new SystemEntityConversionVerifier<RelationType>(RelationType.class, mongoStorage, graphStorage, propertyVerifier);
  }

  @Test
  public void verifyConversionRetrievesTheMongoVersionAndTheGraphVersionOfAnObject() throws Exception {
    // action
    instance.verifyConversion(MONGO_ID, GRAPH_ID, NEW_INTERNAL_ID);

    // verify
    verify(mongoStorage).getEntity(TYPE, MONGO_ID);
    verify(graphStorage).getEntityByVertexId(TYPE, NEW_INTERNAL_ID);
  }

  @Test
  public void verifyConversionVerifiesAllTheFieldsExceptId() throws Exception {
    // setup
    setDefaultProperties(mongoType);
    mongoType.setId(MONGO_ID);
    setDefaultProperties(graphType);
    graphType.setId(GRAPH_ID);

    // action
    instance.verifyConversion(MONGO_ID, GRAPH_ID, NEW_INTERNAL_ID);

    // action
    verify(propertyVerifier, never()).check(argThat(is("id")), anyString(), anyString());

    verifyCheck("created", defaultCreated, defaultCreated);
    verifyCheck("derived", defaultDerived, defaultDerived);
    verifyCheck("inverseName", defaultInverseName, defaultInverseName);
    verifyCheck("modified", defaultModified, defaultModified);
    verifyCheck("reflexive", defaultReflexive, defaultReflexive);
    verifyCheck("regularName", defaultRegularName, defaultRegularName);
    verifyCheck("rev", defaultRev, defaultRev);
    verifyCheck("sourceTypeName", defaultSourceTypeName, defaultSourceTypeName);
    verifyCheck("symmetric", defaultSymmetric, defaultSymmetric);
    verifyCheck("targetTypeName", defaultTargetTypeName, defaultTargetTypeName);
  }

  private void verifyCheck(String fieldName, Object oldValue, Object newValue) {
    verify(propertyVerifier).check(fieldName, oldValue, newValue);
  }

  private void setDefaultProperties(RelationType relationType) {
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
    instance.verifyConversion(MONGO_ID, GRAPH_ID, NEW_INTERNAL_ID);

  }
}

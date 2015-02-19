package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.CREATED_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.MODIFIED_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.util.Change;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import test.model.TestSystemEntityWrapper;

public class EntityWrapperTest {
  private static final int REVISION = 1;
  private static final Change MODIFIED = new Change();
  private static final Change CREATED = MODIFIED;
  private static final String ID = "id";
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private static final String TYPE_NAME = TypeNames.getInternalName(TYPE);
  private static final TestSystemEntityWrapper ENTITY = new TestSystemEntityWrapper();
  private Node nodeMock;
  private FieldWrapper fieldWrapperMock1;
  private FieldWrapper fieldWrapperMock2;
  private EntityWrapper instance;

  @Before
  public void setUp() {
    fieldWrapperMock1 = mock(FieldWrapper.class);
    fieldWrapperMock2 = mock(FieldWrapper.class);
    nodeMock = mock(Node.class);

    instance = new EntityWrapper();
    instance.addFieldWrapper(fieldWrapperMock1);
    instance.addFieldWrapper(fieldWrapperMock2);
    instance.setEntity(ENTITY);
    instance.setId(ID);
    instance.setCreated(CREATED);
    instance.setModified(MODIFIED);
    instance.setRev(REVISION);
  }

  @Test
  public void addValuesToNodeLetsTheFieldWrappersAddTheirValuesToTheNode() throws Exception {
    // action
    instance.addValuesToNode(nodeMock);

    // verify
    verify(nodeMock).addLabel(DynamicLabel.label(TYPE_NAME));
    verify(fieldWrapperMock1).addValueToNode(nodeMock);
    verify(fieldWrapperMock2).addValueToNode(nodeMock);
  }

  @Test(expected = IllegalArgumentException.class)
  public void addValuesThrowsAnIllegalArgumentExceptionWhenOneOfTheFieldWrappersDoes() throws Exception {
    addValuesFieldMapperThrowsException(IllegalArgumentException.class);
  }

  @Test(expected = IllegalAccessException.class)
  public void addValuesThrowsAnIllegalArgumentAccessWhenOneOfTheFieldWrappersDoes() throws Exception {
    addValuesFieldMapperThrowsException(IllegalAccessException.class);
  }

  private void addValuesFieldMapperThrowsException(Class<? extends Exception> exceptionToThrow) throws Exception {
    // setup
    doThrow(exceptionToThrow).when(fieldWrapperMock1).addValueToNode(nodeMock);

    // action
    instance.addValuesToNode(nodeMock);

    // verify
    verify(nodeMock).addLabel(DynamicLabel.label(TYPE_NAME));
    verify(fieldWrapperMock1).addValueToNode(nodeMock);
    verifyZeroInteractions(fieldWrapperMock2);
  }

  @Test
  public void addAdministrativeValuesAddsIdRevisionCreatedAndModified() {
    // action
    instance.addAdministrativeValues(nodeMock);

    verify(nodeMock).setProperty(ID_PROPERTY_NAME, ID);
    verify(nodeMock).setProperty(REVISION_PROPERTY_NAME, REVISION);
    verify(nodeMock).setProperty(CREATED_PROPERTY_NAME, CREATED);
    verify(nodeMock).setProperty(MODIFIED_PROPERTY_NAME, MODIFIED);
  }
}

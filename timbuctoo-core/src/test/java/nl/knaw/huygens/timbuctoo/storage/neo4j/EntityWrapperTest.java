package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import nl.knaw.huygens.timbuctoo.config.TypeNames;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import test.model.TestSystemEntityWrapper;

public class EntityWrapperTest {
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private static final String TYPE_NAME = TypeNames.getInternalName(TYPE);
  private static final TestSystemEntityWrapper ENTITY = new TestSystemEntityWrapper();
  private static final Node NODE_MOCK = mock(Node.class);
  private static final FieldWrapper FIELD_WRAPPER_MOCK_1 = mock(FieldWrapper.class);
  private static final FieldWrapper FIELD_WRAPPER_MOCK_2 = mock(FieldWrapper.class);
  private EntityWrapper instance;

  @Before
  public void setUp() {
    instance = new EntityWrapper();
    instance.addFieldWrapper(FIELD_WRAPPER_MOCK_1);
    instance.addFieldWrapper(FIELD_WRAPPER_MOCK_2);
    instance.setEntity(ENTITY);
  }

  @Test
  public void addValuesToNodeLetsTheFieldWrappersAddTheirValuesToTheNode() throws Exception {
    // action
    instance.addValuesToNode(NODE_MOCK);

    // verify
    verify(NODE_MOCK).addLabel(DynamicLabel.label(TYPE_NAME));
    verify(FIELD_WRAPPER_MOCK_1).addValueToNode(NODE_MOCK);
    verify(FIELD_WRAPPER_MOCK_2).addValueToNode(NODE_MOCK);
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
    doThrow(exceptionToThrow).when(FIELD_WRAPPER_MOCK_1).addValueToNode(NODE_MOCK);

    // action
    instance.addValuesToNode(NODE_MOCK);

    // verify
    verify(NODE_MOCK).addLabel(DynamicLabel.label(TYPE_NAME));
    verify(FIELD_WRAPPER_MOCK_1).addValueToNode(NODE_MOCK);
    verifyZeroInteractions(FIELD_WRAPPER_MOCK_2);
  }
}

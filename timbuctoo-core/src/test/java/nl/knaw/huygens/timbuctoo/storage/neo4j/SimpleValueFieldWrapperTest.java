package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import test.model.TestSystemEntityWrapper;

public class SimpleValueFieldWrapperTest {
  private static final String PROPERTY_NAME = "propertyName";
  private static final String FIELD_NAME = "stringValue";
  private SimpleValueFieldWrapper instance;
  private PropertyNameCreator propertyNameCreatorMock;
  private Node nodeMock;

  @Before
  public void setUp() throws Exception {
    nodeMock = mock(Node.class);

    Field field = TestSystemEntityWrapper.class.getDeclaredField(FIELD_NAME);
    propertyNameCreatorMock = mock(PropertyNameCreator.class);
    instance = new SimpleValueFieldWrapper();
    instance.setField(field);
    instance.setPropertyNameCreator(propertyNameCreatorMock);

  }

  @Test
  public void addValueToNodeSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception {
    TestSystemEntityWrapper entity = new TestSystemEntityWrapper();
    String value = "value";
    entity.setStringValue(value);

    createPopertyNameForFieldName(PROPERTY_NAME, FIELD_NAME);

    instance.setContainingEntity(entity);

    // action
    instance.addValueToNode(nodeMock);

    // verify
    verify(nodeMock).setProperty(PROPERTY_NAME, value);
  }

  private void createPopertyNameForFieldName(String propertyName, String fieldName) {

    when(propertyNameCreatorMock.createName(TestSystemEntityWrapper.class, fieldName)).thenReturn(propertyName);

  }
}

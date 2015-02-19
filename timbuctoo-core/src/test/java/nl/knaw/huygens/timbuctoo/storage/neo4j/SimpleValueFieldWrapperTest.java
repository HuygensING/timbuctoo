package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import test.model.TestSystemEntityWrapper;

public class SimpleValueFieldWrapperTest implements FieldWrapperTest {
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private static final String FIELD_NAME = "stringValue";
  private SimpleValueFieldWrapper instance;
  private Node nodeMock;
  private Field field;
  private FieldType fieldType;

  @Before
  public void setUp() throws Exception {
    nodeMock = mock(Node.class);
    fieldType = FieldType.REGULAR;

    field = TYPE.getDeclaredField(FIELD_NAME);
    instance = new SimpleValueFieldWrapper();
    instance.setField(field);
    instance.setFieldType(fieldType);

  }

  @Override
  @Test
  public void addValueToNodeSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception {
    TestSystemEntityWrapper entity = new TestSystemEntityWrapper();
    String value = "value";
    entity.setStringValue(value);

    instance.setName(FIELD_NAME);
    String propertyName = fieldType.propertyName(TYPE, FIELD_NAME);

    instance.setContainingType(TYPE);

    // action
    instance.addValueToNode(entity, nodeMock);

    // verify
    verify(nodeMock).setProperty(propertyName, value);
  }

  @Override
  @Test
  public void addValueToNodeDoesNotSetIfTheValueIsNull() throws Exception {
    TestSystemEntityWrapper entity = new TestSystemEntityWrapper();
    String value = null;
    entity.setStringValue(value);

    instance.setName(FIELD_NAME);

    instance.setContainingType(TYPE);

    // action
    instance.addValueToNode(entity, nodeMock);

    // verify
    verify(nodeMock, never()).setProperty(anyString(), any());
  }
}

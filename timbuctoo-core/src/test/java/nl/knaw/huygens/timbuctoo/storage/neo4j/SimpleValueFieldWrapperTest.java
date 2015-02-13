package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import test.model.TestSystemEntityWrapper;

public class SimpleValueFieldWrapperTest {
  private static final String FIELD_NAME = "stringValue";
  private SimpleValueFieldWrapper instance;
  private Node nodeMock;
  private Field field;
  private FieldType fieldType;

  @Before
  public void setUp() throws Exception {
    nodeMock = mock(Node.class);
    fieldType = FieldType.REGULAR;

    field = TestSystemEntityWrapper.class.getDeclaredField(FIELD_NAME);
    instance = new SimpleValueFieldWrapper();
    instance.setField(field);
    instance.setFieldType(fieldType);

  }

  @Test
  public void addValueToNodeSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception {
    TestSystemEntityWrapper entity = new TestSystemEntityWrapper();
    String value = "value";
    entity.setStringValue(value);

    instance.setName(FIELD_NAME);
    String propertyName = fieldType.propertyName(TestSystemEntityWrapper.class, FIELD_NAME);

    instance.setContainingEntity(entity);

    // action
    instance.addValueToNode(nodeMock);

    // verify
    verify(nodeMock).setProperty(propertyName, value);
  }
}

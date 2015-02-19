package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.util.Change;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import test.model.TestSystemEntityWrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectValueFieldWrapperTest implements FieldWrapperTest {
  private static final FieldType FIELD_TYPE = FieldType.REGULAR;
  private static final String FIELD_NAME = "fieldName";
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private Node nodeMock;
  private TestSystemEntityWrapper containingEntity;
  private Field field;
  private String propertyName;
  private ObjectValueFieldWrapper instance;

  @Before
  public void setUp() throws Exception {
    nodeMock = mock(Node.class);
    containingEntity = new TestSystemEntityWrapper();
    field = TYPE.getDeclaredField("objectValue");
    propertyName = FIELD_TYPE.propertyName(TYPE, FIELD_NAME);

    instance = new ObjectValueFieldWrapper();
    instance.setContainingType(TYPE);
    instance.setField(field);
    instance.setFieldType(FIELD_TYPE);
    instance.setName(FIELD_NAME);
  }

  @Override
  @Test
  public void addValueToNodeSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception {
    // setup
    Change change = new Change(87l, "userId", "vreId");
    String serializedValue = serializeValue(change);

    containingEntity.setObjectValue(change);

    // action
    instance.addValueToNode(containingEntity, nodeMock);

    // verify
    verify(nodeMock).setProperty(propertyName, serializedValue);
  }

  private String serializeValue(Change change) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    String serializedValue = objectMapper.writeValueAsString(change);
    return serializedValue;
  }

  @Override
  @Test
  public void addValueToNodeDoesNotSetIfTheValueIsNull() throws Exception {
    // setup
    containingEntity.setObjectValue(null);

    // action
    instance.addValueToNode(containingEntity, nodeMock);

    // verify
    verify(nodeMock, never()).setProperty(anyString(), any());
  }

}

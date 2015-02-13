package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.util.Change;

import org.junit.Test;
import org.neo4j.graphdb.Node;

import test.model.TestSystemEntityWrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectValueFieldWrapperTest {
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;

  @Test
  public void addValueToNodeSetsThePropertyWithTheFieldNameToTheSerializedValueOfTheNode() throws Exception {
    Field field = TYPE.getDeclaredField("objectValue");
    String fieldName = "fieldName";
    FieldType fieldType = FieldType.REGULAR;
    String propertyName = fieldType.propertyName(TYPE, fieldName);
    Node nodeMock = mock(Node.class);

    Change change = new Change(87l, "userId", "vreId");
    String serializedValue = serializeValue(change);

    TestSystemEntityWrapper containingType = new TestSystemEntityWrapper();
    containingType.setObjectValue(change);

    ObjectValueFieldWrapper instance = new ObjectValueFieldWrapper();
    instance.setContainingEntity(containingType);
    instance.setField(field);
    instance.setFieldType(fieldType);
    instance.setName(fieldName);

    // action
    instance.addValueToNode(nodeMock);

    // verify
    verify(nodeMock).setProperty(propertyName, serializedValue);
  }

  private String serializeValue(Change change) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    String serializedValue = objectMapper.writeValueAsString(change);
    return serializedValue;
  }
}

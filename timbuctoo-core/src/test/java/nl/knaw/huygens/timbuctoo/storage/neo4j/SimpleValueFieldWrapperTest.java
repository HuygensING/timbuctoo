package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import test.model.TestSystemEntityWrapper;

public class SimpleValueFieldWrapperTest implements FieldWrapperTest {
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private static final String FIELD_NAME = "stringValue";
  private static final FieldType fieldType = FieldType.REGULAR;
  private SimpleValueFieldWrapper instance;
  private Node nodeMock;
  private Field field;
  private String propertyName;
  private TestSystemEntityWrapper entity;

  @Before
  public void setUp() throws Exception {
    entity = new TestSystemEntityWrapper();
    nodeMock = mock(Node.class);
    propertyName = fieldType.propertyName(TYPE, FIELD_NAME);

    field = TYPE.getDeclaredField(FIELD_NAME);
    instance = new SimpleValueFieldWrapper();
    instance.setField(field);
    instance.setFieldType(fieldType);
    instance.setName(FIELD_NAME);
    instance.setContainingType(TYPE);

  }

  @Override
  @Test
  public void addValueToNodeSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception {

    String value = "value";
    entity.setStringValue(value);

    // action
    instance.addValueToNode(nodeMock, entity);

    // verify
    verify(nodeMock).setProperty(propertyName, value);
  }

  @Override
  @Test
  public void addValueToNodeDoesNotSetIfTheValueIsNull() throws Exception {
    // setup
    String value = null;
    entity.setStringValue(value);

    // action
    instance.addValueToNode(nodeMock, entity);

    // verify
    verify(nodeMock, never()).setProperty(anyString(), any());
  }

  @Override
  @Test
  public void addValueToEntitySetTheFieldOfTheEntityWithTheValue() throws Exception {
    // setup 
    when(nodeMock.hasProperty(propertyName)).thenReturn(true);
    String value = "stringValue";
    when(nodeMock.getProperty(propertyName)).thenReturn(value);

    // action
    instance.addValueToEntity(entity, nodeMock);

    // verify
    assertThat(entity.getStringValue(), is(equalTo(value)));
    verify(nodeMock).hasProperty(propertyName);
    verify(nodeMock).getProperty(propertyName);
    verifyNoMoreInteractions(nodeMock);

  }

  @Override
  @Test
  public void addValueToEntityDoesNothingIfThePropertyDoesNotExist() throws Exception {
    // setup
    when(nodeMock.hasProperty(propertyName)).thenReturn(false);

    // action
    instance.addValueToEntity(entity, nodeMock);

    // verify
    assertThat(entity.getStringValue(), is(nullValue()));
    verify(nodeMock).hasProperty(propertyName);
    verifyNoMoreInteractions(nodeMock);
  }

}

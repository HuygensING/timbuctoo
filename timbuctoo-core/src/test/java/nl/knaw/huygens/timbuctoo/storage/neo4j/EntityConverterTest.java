package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.TypeNames;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import test.model.TestSystemEntityWrapper;

public class EntityConverterTest {
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private static final String TYPE_NAME = TypeNames.getInternalName(TYPE);
  private static final TestSystemEntityWrapper ENTITY = new TestSystemEntityWrapper();
  private Node nodeMock;
  private FieldConverter fieldConverterMock1;
  private FieldConverter fieldConverterMock2;
  private EntityConverter<TestSystemEntityWrapper> instance;

  @Before
  public void setUp() {
    fieldConverterMock1 = mock(FieldConverter.class);
    fieldConverterMock2 = mock(FieldConverter.class);
    nodeMock = mock(Node.class);

    instance = new EntityConverter<TestSystemEntityWrapper>(TYPE);
    instance.addFieldConverter(fieldConverterMock1);
    instance.addFieldConverter(fieldConverterMock2);
  }

  @Test
  public void addValuesToNodeLetsTheFieldConvertersAddTheirValuesToTheNode() throws Exception {
    // action
    instance.addValuesToNode(nodeMock, ENTITY);

    // verify
    verify(nodeMock).addLabel(DynamicLabel.label(TYPE_NAME));
    verify(fieldConverterMock1).setNodeProperty(nodeMock, ENTITY);
    verify(fieldConverterMock2).setNodeProperty(nodeMock, ENTITY);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToNodeFieldMapperThrowsException() throws Exception {
    // setup
    doThrow(ConversionException.class).when(fieldConverterMock1).setNodeProperty(nodeMock, ENTITY);

    // action
    instance.addValuesToNode(nodeMock, ENTITY);

    // verify
    verify(nodeMock).addLabel(DynamicLabel.label(TYPE_NAME));
    verify(fieldConverterMock1).setNodeProperty(nodeMock, ENTITY);
    verifyZeroInteractions(fieldConverterMock2);
  }

  @Test
  public void addValuesToEntityLetsAllTheFieldConvertersExtractTheValueOfTheNode() throws Exception {
    // action
    instance.addValuesToEntity(ENTITY, nodeMock);

    // verify
    verify(fieldConverterMock1).addValueToEntity(ENTITY, nodeMock);
    verify(fieldConverterMock2).addValueToEntity(ENTITY, nodeMock);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToEntityThrowsAConversionExceptionIfAFieldConverterAddValueToEntityThrowsOne() throws Exception {
    // setup
    doThrow(ConversionException.class).when(fieldConverterMock1).addValueToEntity(ENTITY, nodeMock);

    try {
      // action
      instance.addValuesToEntity(ENTITY, nodeMock);
    } finally {
      // verify
      verify(fieldConverterMock1).addValueToEntity(ENTITY, nodeMock);
      verifyZeroInteractions(fieldConverterMock2);
    }
  }

  @Test
  public void updateNodeSetsTheValuesOfTheNonAdministrativeFields() throws Exception {
    // setup
    when(fieldConverterMock1.getFieldType()).thenReturn(FieldType.ADMINISTRATIVE);
    when(fieldConverterMock2.getFieldType()).thenReturn(FieldType.REGULAR);

    // action
    instance.updateNode(nodeMock, ENTITY);

    // verify
    verify(fieldConverterMock1, never()).setNodeProperty(nodeMock, ENTITY);
    verify(fieldConverterMock2).setNodeProperty(nodeMock, ENTITY);
  }

  @Test(expected = ConversionException.class)
  public void updateNodeThrowsAnExceptionWhenAFieldConverterThrowsOne() throws Exception {
    // setup
    when(fieldConverterMock1.getFieldType()).thenReturn(FieldType.REGULAR);
    doThrow(ConversionException.class).when(fieldConverterMock1).setNodeProperty(nodeMock, ENTITY);

    // action
    instance.updateNode(nodeMock, ENTITY);

    // verify
    verify(fieldConverterMock1).setNodeProperty(nodeMock, ENTITY);
    verifyZeroInteractions(fieldConverterMock2);
  }

}

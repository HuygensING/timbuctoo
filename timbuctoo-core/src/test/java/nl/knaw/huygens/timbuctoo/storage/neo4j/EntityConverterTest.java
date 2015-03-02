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

public class EntityConverterTest {
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private static final String TYPE_NAME = TypeNames.getInternalName(TYPE);
  private static final TestSystemEntityWrapper ENTITY = new TestSystemEntityWrapper();
  private Node nodeMock;
  private FieldConverter fieldWrapperMock1;
  private FieldConverter fieldWrapperMock2;
  private EntityConverter<TestSystemEntityWrapper> instance;

  @Before
  public void setUp() {
    fieldWrapperMock1 = mock(FieldConverter.class);
    fieldWrapperMock2 = mock(FieldConverter.class);
    nodeMock = mock(Node.class);

    instance = new EntityConverter<TestSystemEntityWrapper>(TYPE);
    instance.addFieldWrapper(fieldWrapperMock1);
    instance.addFieldWrapper(fieldWrapperMock2);
  }

  @Test
  public void addValuesToNodeLetsTheFieldWrappersAddTheirValuesToTheNode() throws Exception {
    // action
    instance.addValuesToNode(nodeMock, ENTITY);

    // verify
    verify(nodeMock).addLabel(DynamicLabel.label(TYPE_NAME));
    verify(fieldWrapperMock1).addValueToNode(nodeMock, ENTITY);
    verify(fieldWrapperMock2).addValueToNode(nodeMock, ENTITY);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToNodeFieldMapperThrowsException() throws Exception {
    // setup
    doThrow(ConversionException.class).when(fieldWrapperMock1).addValueToNode(nodeMock, ENTITY);

    // action
    instance.addValuesToNode(nodeMock, ENTITY);

    // verify
    verify(nodeMock).addLabel(DynamicLabel.label(TYPE_NAME));
    verify(fieldWrapperMock1).addValueToNode(nodeMock, ENTITY);
    verifyZeroInteractions(fieldWrapperMock2);
  }

  @Test
  public void addValuesToEntityLetsAllTheFieldWrappersExtractTheValueOfTheNode() throws Exception {
    // action
    instance.addValuesToEntity(ENTITY, nodeMock);

    // verify
    verify(fieldWrapperMock1).addValueToEntity(ENTITY, nodeMock);
    verify(fieldWrapperMock2).addValueToEntity(ENTITY, nodeMock);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToEntityThrowsAConversionExceptionIfAFieldWrapperAddValueToEntityThrowsOne() throws Exception {
    // setup
    doThrow(ConversionException.class).when(fieldWrapperMock1).addValueToEntity(ENTITY, nodeMock);

    try {
      // action
      instance.addValuesToEntity(ENTITY, nodeMock);
    } finally {
      // verify
      verify(fieldWrapperMock1).addValueToEntity(ENTITY, nodeMock);
      verifyZeroInteractions(fieldWrapperMock2);
    }
  }

}

package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import test.model.TestSystemEntityWrapper;

public class EntityConverterTest {
  private static final String FIELD_CONVERTER2_NAME = "fieldConverter2";
  private static final String FIELD_CONVERTER1_NAME = "fieldConverter1";
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private static final String TYPE_NAME = TypeNames.getInternalName(TYPE);
  private static final TestSystemEntityWrapper ENTITY = new TestSystemEntityWrapper();
  private Node nodeMock;
  private FieldConverter administrativeFieldConverterMock;
  private FieldConverter regularFieldConverterMock;
  private EntityConverter<TestSystemEntityWrapper> instance;

  @Before
  public void setUp() {
    administrativeFieldConverterMock = createFieldConverterMock(FIELD_CONVERTER1_NAME, FieldType.ADMINISTRATIVE);
    regularFieldConverterMock = createFieldConverterMock(FIELD_CONVERTER2_NAME, FieldType.REGULAR);

    nodeMock = mock(Node.class);

    instance = new EntityConverter<TestSystemEntityWrapper>(TYPE);
    instance.addFieldConverter(administrativeFieldConverterMock);
    instance.addFieldConverter(regularFieldConverterMock);
  }

  private FieldConverter createFieldConverterMock(String name, FieldType fieldType) {
    FieldConverter fieldConverterMock = mock(FieldConverter.class);
    when(fieldConverterMock.getName()).thenReturn(name);
    when(fieldConverterMock.getFieldType()).thenReturn(fieldType);

    return fieldConverterMock;
  }

  @Test
  public void addValuesToNodeLetsTheFieldConvertersAddTheirValuesToTheNode() throws Exception {
    // action
    instance.addValuesToNode(nodeMock, ENTITY);

    // verify
    verify(nodeMock).addLabel(DynamicLabel.label(TYPE_NAME));
    verify(administrativeFieldConverterMock).setNodeProperty(nodeMock, ENTITY);
    verify(regularFieldConverterMock).setNodeProperty(nodeMock, ENTITY);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToNodeFieldMapperThrowsException() throws Exception {
    // setup
    doThrow(ConversionException.class).when(administrativeFieldConverterMock).setNodeProperty(nodeMock, ENTITY);

    // action
    instance.addValuesToNode(nodeMock, ENTITY);

    // verify
    verify(nodeMock).addLabel(DynamicLabel.label(TYPE_NAME));
    verify(administrativeFieldConverterMock).setNodeProperty(nodeMock, ENTITY);
    verifyZeroInteractions(regularFieldConverterMock);
  }

  @Test
  public void addValuesToEntityLetsAllTheFieldConvertersExtractTheValueOfTheNode() throws Exception {
    // action
    instance.addValuesToEntity(ENTITY, nodeMock);

    // verify
    verify(administrativeFieldConverterMock).addValueToEntity(ENTITY, nodeMock);
    verify(regularFieldConverterMock).addValueToEntity(ENTITY, nodeMock);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToEntityThrowsAConversionExceptionIfAFieldConverterAddValueToEntityThrowsOne() throws Exception {
    // setup
    doThrow(ConversionException.class).when(administrativeFieldConverterMock).addValueToEntity(ENTITY, nodeMock);

    try {
      // action
      instance.addValuesToEntity(ENTITY, nodeMock);
    } finally {
      // verify
      verify(administrativeFieldConverterMock).addValueToEntity(ENTITY, nodeMock);
    }
  }

  @Test
  public void updateNodeSetsTheValuesOfTheNonAdministrativeFields() throws Exception {
    // setup

    // action
    instance.updateNode(nodeMock, ENTITY);

    // verify
    verify(administrativeFieldConverterMock, never()).setNodeProperty(nodeMock, ENTITY);
    verify(regularFieldConverterMock).setNodeProperty(nodeMock, ENTITY);
  }

  @Test(expected = ConversionException.class)
  public void updateNodeThrowsAnExceptionWhenAFieldConverterThrowsOne() throws Exception {
    // setup
    doThrow(ConversionException.class).when(regularFieldConverterMock).setNodeProperty(nodeMock, ENTITY);

    // action
    instance.updateNode(nodeMock, ENTITY);

    // verify
    verify(administrativeFieldConverterMock).setNodeProperty(nodeMock, ENTITY);
  }

  @Test
  public void updateModifiedAndRevLetTheFieldConvertersSetTheValuesForRevisionAndModified() throws Exception {
    // setup
    FieldConverter modifiedConverterMock = createFieldConverterMock(Entity.MODIFIED_PROPERTY_NAME, FieldType.ADMINISTRATIVE);
    FieldConverter revConverterMock = createFieldConverterMock(Entity.REVISION_PROPERTY_NAME, FieldType.ADMINISTRATIVE);

    instance.addFieldConverter(modifiedConverterMock);
    instance.addFieldConverter(revConverterMock);

    // action
    instance.updateModifiedAndRev(nodeMock, ENTITY);

    // verify
    verify(modifiedConverterMock).setNodeProperty(nodeMock, ENTITY);
    verify(revConverterMock).setNodeProperty(nodeMock, ENTITY);
    verify(administrativeFieldConverterMock, never()).setNodeProperty(nodeMock, ENTITY);
    verify(regularFieldConverterMock, never()).setNodeProperty(nodeMock, ENTITY);

  }

}

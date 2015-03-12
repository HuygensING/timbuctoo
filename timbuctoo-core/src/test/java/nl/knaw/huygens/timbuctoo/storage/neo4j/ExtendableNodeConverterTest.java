package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.neo4j.PropertyConverterMockBuilder.newPropertyConverter;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import test.model.TestSystemEntityWrapper;

public class ExtendableNodeConverterTest {
  private static final String FIELD_CONVERTER2_NAME = "fieldConverter2";
  private static final String FIELD_CONVERTER1_NAME = "fieldConverter1";
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private static final String TYPE_NAME = TypeNames.getInternalName(TYPE);
  private static final TestSystemEntityWrapper ENTITY = new TestSystemEntityWrapper();
  private Node nodeMock;
  private PropertyConverter administrativePropertyConverterMock;
  private PropertyConverter regularPropertyConverterMock;
  private ExtendableNodeConverter<TestSystemEntityWrapper> instance;

  @Before
  public void setUp() {
    administrativePropertyConverterMock = createPropertyConverterMock(FIELD_CONVERTER1_NAME, FieldType.ADMINISTRATIVE);
    regularPropertyConverterMock = createPropertyConverterMock(FIELD_CONVERTER2_NAME, FieldType.REGULAR);

    nodeMock = mock(Node.class);

    instance = new ExtendableNodeConverter<TestSystemEntityWrapper>(TYPE);
    instance.addPropertyConverter(administrativePropertyConverterMock);
    instance.addPropertyConverter(regularPropertyConverterMock);
  }

  private PropertyConverter createPropertyConverterMock(String name, FieldType fieldType) {
    return newPropertyConverter().withName(name).withType(fieldType).build();
  }

  @Test
  public void addValuesToNodeLetsTheFieldConvertersAddTheirValuesToTheNode() throws Exception {
    // action
    instance.addValuesToPropertyContainer(nodeMock, ENTITY);

    // verify
    verify(nodeMock).addLabel(DynamicLabel.label(TYPE_NAME));
    verify(administrativePropertyConverterMock).setPropertyContainerProperty(nodeMock, ENTITY);
    verify(regularPropertyConverterMock).setPropertyContainerProperty(nodeMock, ENTITY);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToNodeFieldMapperThrowsException() throws Exception {
    // setup
    doThrow(ConversionException.class).when(administrativePropertyConverterMock).setPropertyContainerProperty(nodeMock, ENTITY);

    // action
    instance.addValuesToPropertyContainer(nodeMock, ENTITY);

    // verify
    verify(nodeMock).addLabel(DynamicLabel.label(TYPE_NAME));
    verify(administrativePropertyConverterMock).setPropertyContainerProperty(nodeMock, ENTITY);
    verifyZeroInteractions(regularPropertyConverterMock);
  }

  @Test
  public void addValuesToEntityLetsAllTheFieldConvertersExtractTheValueOfTheNode() throws Exception {
    // action
    instance.addValuesToEntity(ENTITY, nodeMock);

    // verify
    verify(administrativePropertyConverterMock).addValueToEntity(ENTITY, nodeMock);
    verify(regularPropertyConverterMock).addValueToEntity(ENTITY, nodeMock);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToEntityThrowsAConversionExceptionIfAFieldConverterAddValueToEntityThrowsOne() throws Exception {
    // setup
    doThrow(ConversionException.class).when(administrativePropertyConverterMock).addValueToEntity(ENTITY, nodeMock);

    try {
      // action
      instance.addValuesToEntity(ENTITY, nodeMock);
    } finally {
      // verify
      verify(administrativePropertyConverterMock).addValueToEntity(ENTITY, nodeMock);
    }
  }

  @Test
  public void updateNodeSetsTheValuesOfTheNonAdministrativeFields() throws Exception {
    // setup

    // action
    instance.updatePropertyContainer(nodeMock, ENTITY);

    // verify
    verify(administrativePropertyConverterMock, never()).setPropertyContainerProperty(nodeMock, ENTITY);
    verify(regularPropertyConverterMock).setPropertyContainerProperty(nodeMock, ENTITY);
  }

  @Test(expected = ConversionException.class)
  public void updateNodeThrowsAnExceptionWhenAFieldConverterThrowsOne() throws Exception {
    // setup
    doThrow(ConversionException.class).when(regularPropertyConverterMock).setPropertyContainerProperty(nodeMock, ENTITY);

    // action
    instance.updatePropertyContainer(nodeMock, ENTITY);

    // verify
    verify(administrativePropertyConverterMock).setPropertyContainerProperty(nodeMock, ENTITY);
  }

  @Test
  public void updateModifiedAndRevLetTheFieldConvertersSetTheValuesForRevisionAndModified() throws Exception {
    // setup
    PropertyConverter modifiedConverterMock = createPropertyConverterMock(Entity.MODIFIED_PROPERTY_NAME, FieldType.ADMINISTRATIVE);
    PropertyConverter revConverterMock = createPropertyConverterMock(Entity.REVISION_PROPERTY_NAME, FieldType.ADMINISTRATIVE);

    instance.addPropertyConverter(modifiedConverterMock);
    instance.addPropertyConverter(revConverterMock);

    // action
    instance.updateModifiedAndRev(nodeMock, ENTITY);

    // verify
    verify(modifiedConverterMock).setPropertyContainerProperty(nodeMock, ENTITY);
    verify(revConverterMock).setPropertyContainerProperty(nodeMock, ENTITY);
    verify(administrativePropertyConverterMock, never()).setPropertyContainerProperty(nodeMock, ENTITY);
    verify(regularPropertyConverterMock, never()).setPropertyContainerProperty(nodeMock, ENTITY);
  }

}

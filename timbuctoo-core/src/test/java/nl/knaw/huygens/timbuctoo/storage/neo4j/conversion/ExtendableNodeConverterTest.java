package nl.knaw.huygens.timbuctoo.storage.neo4j.conversion;

import static nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.PropertyConverterMockBuilder.newPropertyConverter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.neo4j.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.neo4j.EntityInstantiator;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import test.model.TestSystemEntityWrapper;

public class ExtendableNodeConverterTest {
  private static final String REGULAR_FIELD_NAME = "fieldConverter2";
  private static final String ADMINISTRATIVE_FIELD_NAME = "fieldConverter1";
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private static final String TYPE_NAME = TypeNames.getInternalName(TYPE);
  private TestSystemEntityWrapper entity;
  private Node nodeMock;
  private EntityInstantiator entityInstantiatorMock;
  private PropertyConverter administrativePropertyConverterMock;
  private PropertyConverter regularPropertyConverterMock;
  private ExtendableNodeConverter<TestSystemEntityWrapper> instance;

  @Before
  public void setUp() {
    entity = new TestSystemEntityWrapper();
    nodeMock = mock(Node.class);

    entityInstantiatorMock = mock(EntityInstantiator.class);
    administrativePropertyConverterMock = createPropertyConverterMock(ADMINISTRATIVE_FIELD_NAME, FieldType.ADMINISTRATIVE);
    regularPropertyConverterMock = createPropertyConverterMock(REGULAR_FIELD_NAME, FieldType.REGULAR);

    instance = new ExtendableNodeConverter<TestSystemEntityWrapper>(TYPE, entityInstantiatorMock);
    instance.addPropertyConverter(administrativePropertyConverterMock);
    instance.addPropertyConverter(regularPropertyConverterMock);
  }

  private PropertyConverter createPropertyConverterMock(String name, FieldType fieldType) {
    return newPropertyConverter().withName(name).withType(fieldType).build();
  }

  @Test
  public void addValuesToNodeLetsTheFieldConvertersAddTheirValuesToTheNode() throws Exception {
    // action
    instance.addValuesToPropertyContainer(nodeMock, entity);

    // verify
    verify(nodeMock).addLabel(DynamicLabel.label(TYPE_NAME));
    verify(administrativePropertyConverterMock).setPropertyContainerProperty(nodeMock, entity);
    verify(regularPropertyConverterMock).setPropertyContainerProperty(nodeMock, entity);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToNodeFieldMapperThrowsAConversionException() throws Exception {
    // setup
    doThrow(ConversionException.class).when(administrativePropertyConverterMock).setPropertyContainerProperty(nodeMock, entity);

    // action
    instance.addValuesToPropertyContainer(nodeMock, entity);

    // verify
    verify(nodeMock).addLabel(DynamicLabel.label(TYPE_NAME));
    verify(administrativePropertyConverterMock).setPropertyContainerProperty(nodeMock, entity);
    verifyZeroInteractions(regularPropertyConverterMock);
  }

  @Test
  public void addValuesToEntityLetsAllTheFieldConvertersExtractTheValueOfTheNode() throws Exception {
    // action
    instance.addValuesToEntity(entity, nodeMock);

    // verify
    verify(administrativePropertyConverterMock).addValueToEntity(entity, nodeMock);
    verify(regularPropertyConverterMock).addValueToEntity(entity, nodeMock);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToEntityThrowsAConversionExceptionIfAFieldConverterAddValueToEntityThrowsOne() throws Exception {
    // setup
    doThrow(ConversionException.class).when(administrativePropertyConverterMock).addValueToEntity(entity, nodeMock);

    try {
      // action
      instance.addValuesToEntity(entity, nodeMock);
    } finally {
      // verify
      verify(administrativePropertyConverterMock).addValueToEntity(entity, nodeMock);
    }
  }

  @Test
  public void updateNodeSetsTheValuesOfTheNonAdministrativeFields() throws Exception {
    // setup

    // action
    instance.updatePropertyContainer(nodeMock, entity);

    // verify
    verify(administrativePropertyConverterMock, never()).setPropertyContainerProperty(nodeMock, entity);
    verify(regularPropertyConverterMock).setPropertyContainerProperty(nodeMock, entity);
  }

  @Test(expected = ConversionException.class)
  public void updateNodeThrowsAnExceptionWhenAFieldConverterThrowsOne() throws Exception {
    // setup
    doThrow(ConversionException.class).when(regularPropertyConverterMock).setPropertyContainerProperty(nodeMock, entity);

    // action
    instance.updatePropertyContainer(nodeMock, entity);

    // verify
    verify(administrativePropertyConverterMock).setPropertyContainerProperty(nodeMock, entity);
  }

  @Test
  public void updateModifiedAndRevLetTheFieldConvertersSetTheValuesForRevisionAndModified() throws Exception {
    // setup
    PropertyConverter modifiedConverterMock = createPropertyConverterMock(Entity.MODIFIED_PROPERTY_NAME, FieldType.ADMINISTRATIVE);
    PropertyConverter revConverterMock = createPropertyConverterMock(Entity.REVISION_PROPERTY_NAME, FieldType.ADMINISTRATIVE);

    instance.addPropertyConverter(modifiedConverterMock);
    instance.addPropertyConverter(revConverterMock);

    // action
    instance.updateModifiedAndRev(nodeMock, entity);

    // verify
    verify(modifiedConverterMock).setPropertyContainerProperty(nodeMock, entity);
    verify(revConverterMock).setPropertyContainerProperty(nodeMock, entity);
    verify(administrativePropertyConverterMock, never()).setPropertyContainerProperty(nodeMock, entity);
    verify(regularPropertyConverterMock, never()).setPropertyContainerProperty(nodeMock, entity);
  }

  @Test
  public void convertToEntityCreatesANewInstanceOfTheTypeAndLetThePropertyConvertersAddTheirValuesToIt() throws Exception {
    // setup
    when(entityInstantiatorMock.createInstanceOf(TYPE)).thenReturn(entity);

    // action
    TestSystemEntityWrapper actualEntity = instance.convertToEntity(nodeMock);

    // verify
    assertThat(actualEntity, is(sameInstance(entity)));

    verify(administrativePropertyConverterMock).addValueToEntity(entity, nodeMock);
    verify(regularPropertyConverterMock).addValueToEntity(entity, nodeMock);
  }

  @Test(expected = InstantiationException.class)
  public void convertToEntityThrowsAnInstantionExceptionWhenTheEntityCannotBeInstatiated() throws Exception {
    // setup
    when(entityInstantiatorMock.createInstanceOf(TYPE)).thenThrow(new InstantiationException());

    // action
    instance.convertToEntity(nodeMock);

  }

  @Test(expected = ConversionException.class)
  public void convertToEntityThrowsAConversionExceptionWhenOneOfTheValuesCannotBeConverted() throws Exception {
    // setup
    doThrow(ConversionException.class).when(administrativePropertyConverterMock).addValueToEntity(entity, nodeMock);
    when(entityInstantiatorMock.createInstanceOf(TYPE)).thenReturn(entity);

    // action
    instance.convertToEntity(nodeMock);
  }

  @Test
  public void getPropertyNameReturnsTheNameOfTheFoundPropertyConverter() {
    // setup
    String propertyName = "test";
    when(regularPropertyConverterMock.getPropertyName()).thenReturn(propertyName);

    // action
    String actualPropertyName = instance.getPropertyName(REGULAR_FIELD_NAME);

    // verify
    assertThat(actualPropertyName, is(equalTo(propertyName)));
  }

  @Test(expected = FieldNonExistingException.class)
  public void getPropertyNameThrowsARuntimeExceptionWhenThePropertyIsNotFound() {
    // setup
    String nonExistingFieldName = "nonExistingPropertyName";

    // action
    instance.getPropertyName(nonExistingFieldName);

  }
}

package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;
import nl.knaw.huygens.timbuctoo.tools.util.metadata.TypeFacade.FieldType;

import org.junit.Before;
import org.junit.Test;

public class FieldMetaDataGeneratorFactoryTest {
  private TypeFacade containingTypeMock;
  private FieldMetaDataGeneratorFactory instance;

  @Before
  public void setUp() {
    TypeNameGenerator typeNameGeneratorMock = mock(TypeNameGenerator.class);
    FieldMapper fieldMapperMock = mock(FieldMapper.class);
    containingTypeMock = mock(TypeFacade.class);
    instance = new FieldMetaDataGeneratorFactory(typeNameGeneratorMock);
  }

  private void testCreateFieldTypeResultsInGenerator(FieldType fieldType, Class<? extends FieldMetaDataGenerator> expectedGeneratorType) {
    Field field = null;

    // when 
    when(containingTypeMock.getFieldType(any(Field.class))).thenReturn(fieldType);

    // action
    FieldMetaDataGenerator metaDataGenerator = instance.create(field, containingTypeMock);

    // verify
    verify(containingTypeMock).getFieldType(field);
    assertThat(metaDataGenerator, is(instanceOf(expectedGeneratorType)));
  }

  @Test
  public void testCreateForEnumField() {
    testCreateFieldTypeResultsInGenerator(FieldType.ENUM, EnumValueFieldMetaDataGenerator.class);
  }

  @Test
  public void testCreateForConstantField() {
    testCreateFieldTypeResultsInGenerator(FieldType.CONSTANT, ConstantFieldMetaDataGenerator.class);
  }

  @Test
  public void testCreateForPoorMansEnumField() {
    testCreateFieldTypeResultsInGenerator(FieldType.POOR_MANS_ENUM, PoorMansEnumFieldMetaDataGenerator.class);
  }

  @Test
  public void testCreateForDefaultField() {
    testCreateFieldTypeResultsInGenerator(FieldType.DEFAULT, DefaultFieldMetaDataGenerator.class);
  }

  @Test
  public void testCreateForUnknownTypeField() {
    testCreateFieldTypeResultsInGenerator(FieldType.UNKNOWN, NoOpFieldMetaDataGenerator.class);
  }
}

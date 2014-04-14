package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MetaDataGeneratorTest {
  @Mock
  private HashMap<String, Object> metaDataMapMock;
  private FieldMetaDataGeneratorFactory fieldMetaDataGeneratorFactoryMock;
  private MetaDataGenerator instance;
  private TypeFacade containingType;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    containingType = mock(TypeFacade.class);

    fieldMetaDataGeneratorFactoryMock = mock(FieldMetaDataGeneratorFactory.class);
    instance = new MetaDataGenerator(fieldMetaDataGeneratorFactoryMock) {
      protected java.util.Map<String, Object> createMetaDataMap() {
        return metaDataMapMock;
      }

      @Override
      protected TypeFacade createTypeFacade(Class<?> type) {
        return containingType;
      }
    };
  }

  @Test
  public void testGenerateWithConcreteClass() throws SecurityException, NoSuchFieldException {
    // setup
    FieldMetaDataGenerator fieldMetaDataGeneratorMock1 = mock(FieldMetaDataGenerator.class);
    FieldMetaDataGenerator fieldMetaDataGeneratorMock2 = mock(FieldMetaDataGenerator.class);

    Class<TestConcreteClass> type = TestConcreteClass.class;
    Field field1 = type.getDeclaredField("field1");
    Field field2 = type.getDeclaredField("field2");

    when(fieldMetaDataGeneratorFactoryMock.create(field1, containingType)).thenReturn(fieldMetaDataGeneratorMock1);
    when(fieldMetaDataGeneratorFactoryMock.create(field2, containingType)).thenReturn(fieldMetaDataGeneratorMock2);

    // action
    instance.generate(type);

    // verify
    verify(fieldMetaDataGeneratorFactoryMock).create(field1, containingType);
    verify(fieldMetaDataGeneratorMock1).addMetaDataToMap(metaDataMapMock, field1);
    verify(fieldMetaDataGeneratorFactoryMock).create(field2, containingType);
    verify(fieldMetaDataGeneratorMock2).addMetaDataToMap(metaDataMapMock, field2);

  }

  @Test
  public void testGenerateWithAbstractClass() {
    // action
    instance.generate(TestAbstractClass.class);

    // verify
    verifyZeroInteractions(fieldMetaDataGeneratorFactoryMock);
  }

  @Test
  public void testGenerateWithInterface() {
    // action
    instance.generate(TestInterface.class);

    // verify
    verifyZeroInteractions(fieldMetaDataGeneratorFactoryMock);
  }

  private static class TestConcreteClass {
    private Object field1;
    private Object field2;
  }

  private static abstract class TestAbstractClass {
    private Object field1;
    private Object field2;
  }

  private static interface TestInterface {
    String FIELD1 = "FIELD1";
    String FIELD2 = "FIELD2";
  }
}

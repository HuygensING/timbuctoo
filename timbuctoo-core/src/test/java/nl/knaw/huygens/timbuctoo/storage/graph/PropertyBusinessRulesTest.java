package nl.knaw.huygens.timbuctoo.storage.graph;

import nl.knaw.huygens.timbuctoo.model.Entity;
import org.junit.Before;
import org.junit.Test;
import test.model.TestSystemEntityWrapper;

import java.lang.reflect.Field;

import static nl.knaw.huygens.timbuctoo.storage.graph.FieldType.ADMINISTRATIVE;
import static nl.knaw.huygens.timbuctoo.storage.graph.FieldType.REGULAR;
import static nl.knaw.huygens.timbuctoo.storage.graph.FieldType.VIRTUAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static test.model.TestSystemEntityWrapper.ANNOTED_GETTER_NAME;
import static test.model.TestSystemEntityWrapper.ANOTATED_PROPERTY_NAME;
import static test.model.TestSystemEntityWrapper.DB_PROPERTY_ANNOTATED;

public class PropertyBusinessRulesTest {
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private PropertyBusinessRules instance;

  @Before
  public void setUp() {
    instance = new PropertyBusinessRules();
  }

  @Test
  public void getFieldTypeReturnsREGULARByDefault() throws Exception {
    Field field = TYPE.getDeclaredField("stringValue");

    verifyThatFieldIsOfType(TYPE, field, REGULAR);
  }

  @Test
  public void getFieldTypeReturnsADMINISTRATIVEIfTheFieldHasADBPropertyAnnotationWithATypeADMINISTRATIVE() throws Exception {
    Field fieldWithNameThatStartsWithUnderscore = Entity.class.getDeclaredField("id");

    verifyThatFieldIsOfType(Entity.class, fieldWithNameThatStartsWithUnderscore, ADMINISTRATIVE);
  }

  @Test
  public void getFieldTypeReturnsVIRTUALIfTheFieldIsStatic() throws Exception {
    Field staticField = TYPE.getDeclaredField("ID_PREFIX");

    verifyThatFieldIsOfType(TYPE, staticField, VIRTUAL);
  }

  @Test
  public void getFieldTypeReturnsVIRTUALIfTheFieldHasADBPropertyAnnotationWithTheTypeVirtual() throws Exception {
    Field staticField = TYPE.getDeclaredField("dbPropertyAnnotatedWithTypeVirtual");

    verifyThatFieldIsOfType(TYPE, staticField, VIRTUAL);
  }


  private void verifyThatFieldIsOfType(Class<? extends Entity> containingType, Field field, FieldType expectedFieldType) {
    FieldType fieldType = instance.getFieldType(containingType, field);

    assertThat(fieldType, is(equalTo(expectedFieldType)));
  }

  @Test
  public void getFieldNameReturnsNameOfTheFieldIfTheFieldHasNoAnnotations() throws Exception {
    verifyThatGetFieldNameReturnsName("stringValue", "stringValue");
  }

  @Test
  public void getFieldNameReturnsTheValueOfTheJsonPropertyAnnotationOfTheField() throws Exception {
    verifyThatGetFieldNameReturnsName("annotatedProperty", ANOTATED_PROPERTY_NAME);
  }

  @Test
  public void getFieldNameReturnsTheValueOfTheDBPorpertyAnnotationOfTheField() throws Exception {
    verifyThatGetFieldNameReturnsName("dbPropertyAnnotatedWithTypeVirtual", "dbPropertyAnnotatedWithTypeVirtual");
  }

  @Test
  public void getFieldNameReturnsTheValueOfTheJsonPropertyAnnotationOfTheGetterOfTheField() throws Exception {
    verifyThatGetFieldNameReturnsName("propertyWithAnnotatedGetter", ANNOTED_GETTER_NAME);
  }

  private void verifyThatGetFieldNameReturnsName(String fieldName, String expectedName) throws Exception {
    Field field = TYPE.getDeclaredField(fieldName);

    // action
    String actualFieldName = instance.getFieldName(TYPE, field);

    // verify
    assertThat(actualFieldName, is(equalTo(expectedName)));
  }

  @Test
  public void getPropertyNameReturnsNameOfTheFieldIfTheFieldHasNoAnnotations() throws Exception {
    verifyThatGetPropertyNameReturnsName("stringValue", "stringValue");
  }

  @Test
  public void getPropertyNameReturnsTheNameOfTheFieldIfItIsAnnotatedWithATheJsonPropertyAnnotation() throws Exception {
    verifyThatGetPropertyNameReturnsName("annotatedProperty", "annotatedProperty");
  }

  @Test
  public void getPropertyNameReturnsTheValueOfTheDBProeprtyAnnotationOfTheField() throws Exception {
    verifyThatGetPropertyNameReturnsName("dbPropertyAnnotatedWithTypeVirtual", DB_PROPERTY_ANNOTATED);
  }

  @Test
  public void getPropertyNameReturnsTheNameOfTheFieldIfTheGetterIsAnnotatedWithATheJsonPropertyAnnotation() throws Exception {
    verifyThatGetPropertyNameReturnsName("propertyWithAnnotatedGetter", "propertyWithAnnotatedGetter");
  }

  private void verifyThatGetPropertyNameReturnsName(String fieldName, String expectedName) throws Exception {
    Field field = TYPE.getDeclaredField(fieldName);

    // action
    String actualFieldName = instance.getPropertyName(TYPE, field);

    // verify
    assertThat(actualFieldName, is(equalTo(expectedName)));
  }
}

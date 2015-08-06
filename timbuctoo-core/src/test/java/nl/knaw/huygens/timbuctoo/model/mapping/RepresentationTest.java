package nl.knaw.huygens.timbuctoo.model.mapping;

import nl.knaw.huygens.timbuctoo.model.DerivedProperty;
import nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMapFactory.Representation;
import org.junit.Test;
import test.model.MappingExample;
import test.model.projecta.ProjectAMappingExample;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMapFactory.Representation.CLIENT;
import static nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMapFactory.Representation.INDEX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RepresentationTest {

  public static final Class<MappingExample> TYPE = MappingExample.class;
  public static final Class<ProjectAMappingExample> SUB_TYPE = ProjectAMappingExample.class;

  @Test
  public void getFieldNameOfINDEXGetsTheNameFromTheIndexAnnotationOfTheGetterOfTheField() throws Exception {
    String expectedName = MappingExample.INDEX_AND_CLIENT_INDEX_NAME;
    verifyFieldName(INDEX, "indexAndClient", expectedName);
  }

  @Test
  public void getFieldNameOfINDEXReturnsNullIfTheFieldHasNoGetter() throws Exception {
    String expectedName = null;
    verifyFieldName(INDEX, "fieldWithoutGetter", expectedName);
  }

  @Test
  public void getFieldNameOfINDEXReturnsNullIfTheFieldHasAGetterWithoutIndexAnnotation() throws Exception {
    String expectedName = null;
    verifyFieldName(INDEX, "fieldWithGetterWithoutIndexAnnotation", expectedName);
  }

  @Test
  public void getFieldNameOfINDEXReturnsNullIfTheFieldHasAGetterWithAnIndexAnnotationThatIsSortable() throws Exception {
    String expectedName = null;
    verifyFieldName(INDEX, "fieldWithSortableIndexAnnotation", expectedName);
  }

  @Test
  public void getFieldNameOfINDEXReturnsTheNameOfTheFirstNonSortableIndexAnnotationIfTheFieldHasGetterWithIndexAnnotations() throws Exception {
    String expectedName = MappingExample.FIRST_NON_SORTABLE;
    verifyFieldName(INDEX, "fieldWithIndexAnnotations", expectedName);
  }

  @Test
  public void getFieldNameForDerivedPropertyOfINDEXReturnsTheNameFromTheAnnotationOfTheGetter() {
    DerivedProperty derivedProperty = ProjectAMappingExample.DERIVED_PROPERTY_1;
    String expectedName = ProjectAMappingExample.DERIVED1_INDEX;

    verifyFieldName(INDEX, derivedProperty, expectedName);
  }

  @Test
  public void getFieldNameForVirtualPropertyOfINDEXReturnsTheFieldNameOfIndexAnnotationOfTheAccessor() throws Exception {
    String expectedName = ProjectAMappingExample.VIRTUAL_INDEX;
    verifyFieldNameForVirtualProperty(INDEX, "getVirtualProperty", expectedName);
  }

  @Test
  public void getFieldNameForVirtualPropertyOfINDEXReturnsTheFieldNameOfIndexAnnotationOfTheOverriddenAccessorInTheSuperclass() throws Exception {
    String expectedName = ProjectAMappingExample.VIRTUAL_SUPER_PROPERTY;
    verifyFieldNameForVirtualProperty(INDEX, "getVirtualSuperProperty", expectedName);
  }


  @Test
  public void getFieldNameOfCLIENTReturnsTheValueOfTheJsonPropertyAnnotationOfTheField() throws Exception {
    String expectedName = MappingExample.INDEX_AND_CLIENT_CLIENT_NAME;
    verifyFieldName(CLIENT, "indexAndClient", expectedName);
  }

  @Test
  public void getFieldNameOfCLIENTReturnsTheValueOfTheJsonPropertyAnnotationOnTheGetterOfTheField() throws Exception {
    String expectedName = MappingExample.FIELD_NAME_OF_GETTER_WITH_ANNOTATION;
    verifyFieldName(CLIENT, "fieldWithGetterWithJsonPropertyAnnotation", expectedName);
  }

  @Test
  public void getFieldNameOfCLIENTReturnsTheNameOfTheFieldWhenNoJsonPropertyAnnotationsArePresent() throws Exception {
    String expectedName = "fieldWithGetterWithoutAnnotations";
    verifyFieldName(CLIENT, "fieldWithGetterWithoutAnnotations", expectedName);
  }

  @Test
  public void getFieldNameForDerivedPropertyOfCLIENTReturnsThePropertyName() {
    DerivedProperty derivedProperty = ProjectAMappingExample.DERIVED_PROPERTY_1;
    String expectedName = derivedProperty.getPropertyName();

    verifyFieldName(CLIENT, derivedProperty, expectedName);
  }

  @Test
  public void getFieldNameForVirtualPropertyOfCLIENTReturnsThePropertyName() throws Exception {
    String expectedName = ProjectAMappingExample.VIRTUAL_CLIENT;
    verifyFieldNameForVirtualProperty(CLIENT, "getVirtualProperty", expectedName);
  }

  private void verifyFieldName(Representation representation, String nameOfTheField, String nameInPresentation) throws Exception {
    Field field = TYPE.getDeclaredField(nameOfTheField);

    // action
    String fieldName = representation.getFieldName(TYPE, field);

    // verify
    assertThat(fieldName, is(nameInPresentation));
  }

  private void verifyFieldName(Representation representation, DerivedProperty derivedProperty, String expectedName) {
    // action
    String fieldName = representation.getFieldName(ProjectAMappingExample.class, derivedProperty);

    // verify
    assertThat(fieldName, is(expectedName));
  }

  private void verifyFieldNameForVirtualProperty(Representation representation, String methodName, String expectedName) throws Exception {
    Method method = SUB_TYPE.getMethod(methodName);
    String fieldName = representation.getFieldName(SUB_TYPE, method);

    assertThat(fieldName, is(expectedName));
  }

}

package nl.knaw.huygens.timbuctoo.model.mapping;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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

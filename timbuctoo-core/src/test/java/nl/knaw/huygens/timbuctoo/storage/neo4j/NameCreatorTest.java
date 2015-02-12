package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.junit.Before;
import org.junit.Test;

import test.model.TestSystemEntityWrapper;

public class NameCreatorTest {
  private static final String PROPERTY_WITH_ANNOTATED_GETTER_NAME = "propertyWithAnnotatedGetter";
  private static final String ANNOTATED_FIELD_NAME = "annotatedProperty";
  private static final String DEFAULT_FIELD_NAME = "stringValue";
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private PropertyBusinessRules propertyBusinessRulesMock;
  private NameCreator instance;

  @Before
  public void setUp() {
    propertyBusinessRulesMock = mock(PropertyBusinessRules.class);
    instance = new NameCreator(propertyBusinessRulesMock);
  }

  @Test
  public void propertyNameReturnsTheInnerNameOfTheTypeAndTheFieldNameSeparatedByAColon() throws Exception {
    Field field = TYPE.getDeclaredField(DEFAULT_FIELD_NAME);

    fieldIsFieldType(TYPE, field, FieldType.REGULAR);

    // action
    String name = instance.propertyName(TYPE, field);

    // verify
    assertThat(name, is(equalTo(namePrefixedWithTypeName(DEFAULT_FIELD_NAME))));
  }

  @Test
  public void propertyNameReturnsTheFieldNameIfTheFieldIsAnAdministrative() throws Exception {
    Field field = TYPE.getDeclaredField(DEFAULT_FIELD_NAME);

    fieldIsFieldType(TYPE, field, FieldType.ADMINISTRATIVE);

    // action
    String name = instance.propertyName(TYPE, field);

    // verify
    assertThat(name, is(DEFAULT_FIELD_NAME));
  }

  private void fieldIsFieldType(Class<? extends Entity> containingType, Field field, FieldType fieldType) {
    when(propertyBusinessRulesMock.getFieldType(containingType, field)).thenReturn(fieldType);
  }

  @Test
  public void propertyNameReturnsTheFieldNameIfTheFieldIsVirtual() throws Exception {
    Field field = TYPE.getDeclaredField(DEFAULT_FIELD_NAME);

    fieldIsFieldType(TYPE, field, FieldType.VIRTUAL);

    // action
    String name = instance.propertyName(TYPE, field);

    // verify
    assertThat(name, is(DEFAULT_FIELD_NAME));
  }

  @Test
  public void propertyNameUsesThePropertyNameAnnotationWhenPresentOnTheProperty() throws Exception {
    Field field = TYPE.getDeclaredField(ANNOTATED_FIELD_NAME);

    fieldIsFieldType(TYPE, field, FieldType.REGULAR);

    // action
    String name = instance.propertyName(TYPE, field);

    // verify
    assertThat(name, is(equalTo(namePrefixedWithTypeName(TestSystemEntityWrapper.ANOTATED_PROPERTY_NAME))));
  }

  @Test
  public void propertyNameUsesThePropertyNameAnnotationWhenPresentOnGetterOfTheProperty() throws Exception {
    Field field = TYPE.getDeclaredField(PROPERTY_WITH_ANNOTATED_GETTER_NAME);

    fieldIsFieldType(TYPE, field, FieldType.REGULAR);

    // action
    String name = instance.propertyName(TYPE, field);

    // verify
    assertThat(name, is(equalTo(namePrefixedWithTypeName(TestSystemEntityWrapper.ANNOTED_GETTER_NAME))));
  }

  private String namePrefixedWithTypeName(String fieldName) {
    return String.format("%s:%s", TypeNames.getInternalName(TYPE), fieldName);
  }

  @Test
  public void internalTypeNameReturnsTheInnerNameOfTheType() {
    // action
    String internalTypeName = instance.internalTypeName(TYPE);

    // verify
    assertThat(internalTypeName, is(equalTo("testsystementitywrapper")));
  }
}

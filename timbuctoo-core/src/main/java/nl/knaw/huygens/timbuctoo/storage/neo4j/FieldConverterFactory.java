package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.apache.commons.lang3.ClassUtils;

public class FieldConverterFactory {

  private final PropertyBusinessRules propertyBusinessRules;

  public FieldConverterFactory(PropertyBusinessRules propertyBusinessRules) {
    this.propertyBusinessRules = propertyBusinessRules;
  }

  public <T extends Entity> FieldConverter wrap(Class<T> type, Field field) {
    FieldConverter fieldWrapper = createFieldWrapper(field);

    fieldWrapper.setField(field);
    fieldWrapper.setContainingType(type);
    fieldWrapper.setFieldType(propertyBusinessRules.getFieldType(type, field));
    fieldWrapper.setName(propertyBusinessRules.getFieldName(type, field));

    return fieldWrapper;
  }

  private FieldConverter createFieldWrapper(Field field) {
    if (Modifier.isStatic(field.getModifiers())) {
      return createNoOpFieldWrapper();
    } else if (isSimpleValue(field)) {
      return createSimpleValueFieldWrapper();
    } else if (isSimpleCollection(field)) {
      return createSimpleCollectionFieldWrapper(getComponentType(field));
    }

    return createObjectValueFieldWrapper();
  }

  private Class<?> getComponentType(Field field) {
    Type[] actualTypeArguments = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();

    if (actualTypeArguments.length <= 0) {
      return null;
    }

    Type firstTypeArgument = actualTypeArguments[0];
    return firstTypeArgument instanceof Class<?> ? (Class<?>) firstTypeArgument : null;

  }

  private boolean isSimpleCollection(Field field) {
    Class<?> type = field.getType();
    return Collection.class.isAssignableFrom(type) && hasSimpleTypeArgument(field);
  }

  private boolean hasSimpleTypeArgument(Field field) {
    Class<?> componentType = getComponentType(field);

    if (componentType == null) {
      return false;
    }

    return componentType instanceof Class ? isSimpleValueType(componentType) : false;
  }

  private boolean isSimpleValue(Field field) {
    Class<?> type = field.getType();

    return isSimpleValueType(type);
  }

  private boolean isSimpleValueType(Class<?> type) {
    return ClassUtils.isPrimitiveOrWrapper(type) || type == String.class;
  }

  protected FieldConverter createSimpleValueFieldWrapper() {
    return new SimpleValueFieldConverter();
  }

  protected FieldConverter createObjectValueFieldWrapper() {
    return new ObjectValueFieldConverter();
  }

  protected FieldConverter createNoOpFieldWrapper() {
    return new NoOpFieldConverter();
  }

  protected <T> FieldConverter createSimpleCollectionFieldWrapper(Class<T> componentType) {
    return new SimpleCollectionFieldConverter<T>(componentType);
  }

}

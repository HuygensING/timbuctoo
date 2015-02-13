package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import org.apache.commons.lang3.ClassUtils;

public class FieldWrapperFactory {

  private final PropertyBusinessRules propertyBusinessRules;

  public FieldWrapperFactory(PropertyBusinessRules propertyBusinessRules) {
    this.propertyBusinessRules = propertyBusinessRules;
  }

  public FieldWrapper wrap(Field field, SystemEntity entity) {
    FieldWrapper fieldWrapper = createFieldWrapper(field);

    fieldWrapper.setField(field);
    fieldWrapper.setContainingEntity(entity);
    Class<? extends SystemEntity> containingType = entity.getClass();
    fieldWrapper.setFieldType(propertyBusinessRules.getFieldType(containingType, field));
    fieldWrapper.setName(propertyBusinessRules.getFieldName(containingType, field));

    return fieldWrapper;
  }

  private FieldWrapper createFieldWrapper(Field field) {
    return isSimpleValue(field) || isSimpleCollection(field) ? createSimpleValueFieldWrapper() : createObjectValueFieldWrapper();
  }

  private boolean isSimpleCollection(Field field) {
    Class<?> type = field.getType();
    return Collection.class.isAssignableFrom(type) && hasSimpleTypeArgument(field);
  }

  private boolean hasSimpleTypeArgument(Field field) {
    Type[] actualTypeArguments = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();

    if (actualTypeArguments.length <= 0) {
      return false;
    }

    return actualTypeArguments[0] instanceof Class ? isSimpleValue((Class<?>) actualTypeArguments[0]) : false;
  }

  private boolean isSimpleValue(Field field) {
    Class<?> type = field.getType();

    return isSimpleValue(type);
  }

  private boolean isSimpleValue(Class<?> type) {
    return ClassUtils.isPrimitiveOrWrapper(type) || type == String.class;
  }

  protected FieldWrapper createSimpleValueFieldWrapper() {
    return new SimpleValueFieldWrapper();
  }

  protected FieldWrapper createObjectValueFieldWrapper() {
    return new ObjectValueFieldWrapper();
  }

}

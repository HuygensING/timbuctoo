package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.property;

import static nl.knaw.huygens.timbuctoo.storage.graph.FieldType.VIRTUAL;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;
import nl.knaw.huygens.timbuctoo.storage.graph.PropertyBusinessRules;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.PropertyConverter;

import org.apache.commons.lang3.ClassUtils;

public class PropertyConverterFactory {

  private final PropertyBusinessRules propertyBusinessRules;

  public PropertyConverterFactory() {
    this(new PropertyBusinessRules());
  }

  public PropertyConverterFactory(PropertyBusinessRules propertyBusinessRules) {
    this.propertyBusinessRules = propertyBusinessRules;
  }

  public <T extends Entity> PropertyConverter createPropertyConverter(Class<T> type, Field field) {
    FieldType fieldType = propertyBusinessRules.getFieldType(type, field);
    PropertyConverter propertyConverter = createPropertyConverter(field, fieldType);

    propertyConverter.setField(field);
    propertyConverter.setContainingType(type);
    propertyConverter.setFieldType(fieldType);
    propertyConverter.setFieldName(propertyBusinessRules.getFieldName(type, field));

    return propertyConverter;
  }

  private PropertyConverter createPropertyConverter(Field field, FieldType fieldType) {
    if (fieldType == VIRTUAL) {
      return createNoOpPropertyConverter();
    } else if (isSimpleValue(field)) {
      return createSimpleValuePropertyConverter();
    } else if (isSimpleCollection(field)) {
      return createSimpleCollectionPropertyConverter(getComponentType(field));
    }

    return createObjectValuePropertyConverter();
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

  protected PropertyConverter createSimpleValuePropertyConverter() {
    return new SimpleValuePropertyConverter();
  }

  protected PropertyConverter createObjectValuePropertyConverter() {
    return new ObjectValuePropertyConverter();
  }

  protected PropertyConverter createNoOpPropertyConverter() {
    return new NoOpPropertyConverter();
  }

  protected <T> PropertyConverter createSimpleCollectionPropertyConverter(Class<T> componentType) {
    return new SimpleCollectionPropertyConverter<T>(componentType);
  }

}

package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

import com.google.common.collect.Lists;

public class PoorMansEnumFieldMetaDataGenerator extends EnumValueFieldMetaDataGenerator {

  private final Class<?> enumType;

  public PoorMansEnumFieldMetaDataGenerator(Class<?> containingType, TypeNameGenerator typeNameGenerator, FieldMapper fieldMapper, Class<?> enumType) {
    super(containingType, typeNameGenerator, fieldMapper);
    this.enumType = enumType;
  }

  @Override
  protected void addValueToValueMap(Field field, Map<String, Object> metadataMap) {
    List<Object> values = Lists.newArrayList();

    for (Field enumField : enumType.getDeclaredFields()) {
      enumField.setAccessible(true);
      if (isConstant(enumField)) {
        try {
          values.add(enumField.get(null));
        } catch (IllegalArgumentException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IllegalAccessException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    metadataMap.put(VALUE_FIELD, values);
  }

  private boolean isConstant(Field enumField) {
    return Modifier.isStatic(enumField.getModifiers()) && Modifier.isFinal(enumField.getModifiers());
  }
}

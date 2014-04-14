package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PoorMansEnumFieldMetaDataGenerator extends FieldMetaDataGenerator {

  public PoorMansEnumFieldMetaDataGenerator(TypeFacade containingType, TypeNameGenerator typeNameGenerator) {
    super(containingType);
  }

  @Override
  protected Map<String, Object> constructValue(Field field) {
    Map<String, Object> metadataMap = Maps.newHashMap();
    metadataMap.put(TYPE_FIELD, containingType.getTypeNameOfField(field));

    addValueToValueMap(field, metadataMap);

    return metadataMap;
  }

  protected void addValueToValueMap(Field field, Map<String, Object> metadataMap) {
    List<Object> values = Lists.newArrayList();

    Class<?> enumType = containingType.getPoorMansEnumType(field);

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

package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class EnumValueFieldMetaDataGenerator extends FieldMetaDataGenerator {

  public EnumValueFieldMetaDataGenerator(TypeNameGenerator typeNameGenerator, FieldMapper fieldMapper) {
    super(typeNameGenerator, fieldMapper);
  }

  @Override
  protected Object constructValue(Field field) {
    Map<String, Object> metadataMap = Maps.newHashMap();
    metadataMap.put(TYPE_FIELD, typeNameGenerator.getTypeName(field));

    List<String> enumValues = Lists.newArrayList();
    Class<?> type = field.getType();

    for (Object value : type.getEnumConstants()) {
      enumValues.add(value.toString());
    }
    metadataMap.put(VALUE_FIELD, enumValues);

    return metadataMap;
  }
}

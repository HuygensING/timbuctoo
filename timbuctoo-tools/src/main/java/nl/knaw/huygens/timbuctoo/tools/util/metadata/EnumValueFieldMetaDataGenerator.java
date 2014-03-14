package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;
import java.util.List;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

import com.google.common.collect.Lists;

public class EnumValueFieldMetaDataGenerator extends FieldMetaDataGenerator {

  public EnumValueFieldMetaDataGenerator(TypeNameGenerator typeNameGenerator, FieldMapper fieldMapper) {
    super(typeNameGenerator, fieldMapper);
    // TODO Auto-generated constructor stub
  }

  @Override
  protected Object constructValue(Field field) {
    List<String> enumValues = Lists.newArrayList();
    Class<?> type = field.getType();

    for (Object value : type.getEnumConstants()) {
      enumValues.add(value.toString());
    }

    return enumValues;
  }

}

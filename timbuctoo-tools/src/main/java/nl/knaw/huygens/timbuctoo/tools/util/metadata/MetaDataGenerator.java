package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MetaDataGenerator {
  private final FieldMapper fieldMapper;
  private final TypeNameGenerator typeNameGenerator;

  public MetaDataGenerator(FieldMapper fieldMapper) {
    this.fieldMapper = fieldMapper;
    this.typeNameGenerator = new TypeNameGenerator();
  }

  public Map<String, Object> generate(Class<?> type) throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> metadataMap = Maps.newTreeMap();

    if (!isAbstract(type)) {
      for (Field field : getFields(type)) {
        FieldMetaDataGenerator fieldMetaDataGenerator = getFieldMetaDataGenerator(field);

        fieldMetaDataGenerator.addMetaDataToMap(metadataMap, field, type);

      }
    }

    return metadataMap;
  }

  private boolean isConstantField(Field field) {
    return isFinalField(field) && isStaticField(field);
  }

  private boolean isStaticField(Field field) {
    return Modifier.isStatic(field.getModifiers());
  }

  private boolean isAbstract(Class<?> type) {
    return Modifier.isAbstract(type.getModifiers()) || type.isInterface();
  }

  private boolean isFinalField(Field field) {
    return Modifier.isFinal(field.getModifiers());
  }

  private List<Field> getFields(Class<?> type) {

    List<Field> fields = Lists.newArrayList(type.getDeclaredFields());

    if (!Object.class.equals(type.getSuperclass())) {
      fields.addAll(getFields(type.getSuperclass()));
    }

    return fields;
  }

  private FieldMetaDataGenerator getFieldMetaDataGenerator(Field field) {
    if (field.getType().isEnum()) {
      return new EnumValueFieldMetaDataGenerator(typeNameGenerator, fieldMapper);
    } else if (isConstantField(field)) {
      return new ConstantFieldMetadataGenerator(typeNameGenerator, fieldMapper);
    } else if (!isStaticField(field)) {
      return new DefaultFieldMetadataGenerator(typeNameGenerator, fieldMapper);
    } else {
      return new NoOpFieldMetaDataGenerator(typeNameGenerator, fieldMapper);
    }
  }
}
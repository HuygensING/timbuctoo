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
      FieldMetaDataGeneratorFactory fieldMetaDataGeneratorFactory = new FieldMetaDataGeneratorFactory(typeNameGenerator, fieldMapper, getInnerClasses(type));
      for (Field field : getFields(type)) {
        FieldMetaDataGenerator fieldMetaDataGenerator = fieldMetaDataGeneratorFactory.createFieldMetaDataGenerator(field);

        fieldMetaDataGenerator.addMetaDataToMap(metadataMap, field, type);

      }
    }

    return metadataMap;
  }

  private List<Class<?>> getInnerClasses(Class<?> type) {
    List<Class<?>> classes = Lists.newArrayList(type.getDeclaredClasses());

    if (!Object.class.equals(type.getSuperclass())) {
      classes.addAll(getInnerClasses(type.getSuperclass()));
    }

    return classes;
  }

  private List<Field> getFields(Class<?> type) {

    List<Field> fields = Lists.newArrayList(type.getDeclaredFields());

    if (!Object.class.equals(type.getSuperclass())) {
      fields.addAll(getFields(type.getSuperclass()));
    }

    return fields;
  }

  private boolean isAbstract(Class<?> type) {
    return Modifier.isAbstract(type.getModifiers()) || type.isInterface();
  }

}
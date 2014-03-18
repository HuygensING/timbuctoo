package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MetaDataGenerator {
  private final FieldMetaDataGeneratorFactory fieldMetaDataGeneratorFactory;

  public MetaDataGenerator(FieldMetaDataGeneratorFactory fieldMetaDataGeneratorFactory) {
    this.fieldMetaDataGeneratorFactory = fieldMetaDataGeneratorFactory;
  }

  public Map<String, Object> generate(Class<?> type) throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> metadataMap = Maps.newTreeMap();

    if (!isAbstract(type)) {
      for (Field field : getFields(type)) {
        FieldMetaDataGenerator fieldMetaDataGenerator = fieldMetaDataGeneratorFactory.createFieldMetaDataGenerator(field, type);

        fieldMetaDataGenerator.addMetaDataToMap(metadataMap, field);

      }
    }

    return metadataMap;
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
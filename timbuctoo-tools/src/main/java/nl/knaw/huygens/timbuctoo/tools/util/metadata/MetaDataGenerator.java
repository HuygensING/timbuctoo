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

  public Map<String, Object> generate(Class<?> type) {
    Map<String, Object> metaDataMap = createMetaDataMap();

    TypeFacade typeFacade = createTypeFacade(type);

    if (!isAbstract(type)) {
      for (Field field : getFields(type)) {
        FieldMetaDataGenerator fieldMetaDataGenerator = fieldMetaDataGeneratorFactory.create(field, typeFacade);

        fieldMetaDataGenerator.addMetaDataToMap(metaDataMap, field);

      }
    }

    return metaDataMap;
  }

  protected TypeFacade createTypeFacade(Class<?> type) {
    return new TypeFacade(type);
  }

  protected Map<String, Object> createMetaDataMap() {
    return Maps.newHashMap();
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
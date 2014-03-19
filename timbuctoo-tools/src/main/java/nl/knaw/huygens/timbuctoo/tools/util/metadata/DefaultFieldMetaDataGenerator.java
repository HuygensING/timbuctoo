package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;
import java.util.Map;

import com.google.common.collect.Maps;

public class DefaultFieldMetaDataGenerator extends FieldMetaDataGenerator {

  public DefaultFieldMetaDataGenerator(TypeFacade containingType, TypeNameGenerator typeNameGenerator) {
    super(containingType, typeNameGenerator);
  }

  @Override
  protected Map<String, Object> constructValue(Field field) {
    Map<String, Object> valueMap = Maps.newHashMap();
    valueMap.put(TYPE_FIELD, typeNameGenerator.getTypeName(field));

    return valueMap;
  }

}

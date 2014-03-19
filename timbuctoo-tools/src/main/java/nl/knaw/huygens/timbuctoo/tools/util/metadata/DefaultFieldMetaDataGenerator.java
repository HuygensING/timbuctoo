package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;
import java.util.Map;

import com.google.common.collect.Maps;

public class DefaultFieldMetaDataGenerator extends FieldMetaDataGenerator {

  public DefaultFieldMetaDataGenerator(TypeFacade containingType) {
    super(containingType);
  }

  @Override
  protected Map<String, Object> constructValue(Field field) {
    Map<String, Object> valueMap = Maps.newHashMap();
    valueMap.put(TYPE_FIELD, containingType.getTypeNameOfField(field));

    return valueMap;
  }

}

package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;
import java.util.Map;

public abstract class FieldMetaDataGenerator {

  protected static final String TYPE_FIELD = "type";
  protected static final String VALUE_FIELD = "value";
  protected final TypeNameGenerator typeNameGenerator;
  private final TypeFacade containingType;

  public FieldMetaDataGenerator(TypeFacade containingType, TypeNameGenerator typeNameGenerator) {
    this.typeNameGenerator = typeNameGenerator;
    this.containingType = containingType;

  }

  /**
   * Add the metadata to the map of the {@code containingType}
   * @param mapToAddTo
   * @param field the field to get the metadata from.
   */
  public void addMetaDataToMap(Map<String, Object> mapToAddTo, Field field) {

    Map<String, Object> value = constructValue(field);

    mapToAddTo.put(containingType.getFieldName(field), value);
  }

  protected abstract Map<String, Object> constructValue(Field field);
}

package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

public class NoOpFieldMetaDataGenerator extends FieldMetaDataGenerator {

  public NoOpFieldMetaDataGenerator(TypeNameGenerator typeNameGenerator, FieldMapper fieldMapper) {
    super(typeNameGenerator, fieldMapper);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void addMetaDataToMap(Map<String, Object> mapToAddTo, Field field, Class<?> containingType) {
    // do nothing
  }

  @Override
  protected Object constructValue(Field field) {
    // TODO Auto-generated method stub
    return null;
  }

}

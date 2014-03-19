package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

public class NoOpFieldMetaDataGenerator extends FieldMetaDataGenerator {

  public NoOpFieldMetaDataGenerator(TypeFacade containingType, TypeNameGenerator typeNameGenerator, FieldMapper fieldMapper) {
    super(containingType, typeNameGenerator, fieldMapper);
  }

  @Override
  public void addMetaDataToMap(Map<String, Object> mapToAddTo, Field field) {
    // do nothing
  }

  @Override
  protected Map<String, Object> constructValue(Field field) {
    // TODO Auto-generated method stub
    return null;
  }

}

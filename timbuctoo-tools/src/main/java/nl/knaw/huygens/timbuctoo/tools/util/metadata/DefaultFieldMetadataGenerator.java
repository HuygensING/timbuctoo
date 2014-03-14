package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

public class DefaultFieldMetadataGenerator extends FieldMetaDataGenerator {

  public DefaultFieldMetadataGenerator(TypeNameGenerator typeNameGenerator, FieldMapper fieldMapper) {
    super(typeNameGenerator, fieldMapper);
  }

  @Override
  protected Object constructValue(Field field) {
    return typeNameGenerator.getTypeName(field);
  }

}

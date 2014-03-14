package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

public class ConstantFieldMetadataGenerator extends FieldMetaDataGenerator {

  public ConstantFieldMetadataGenerator(TypeNameGenerator typeNameGenerator, FieldMapper fieldMapper) {
    super(typeNameGenerator, fieldMapper);
  }

  @Override
  protected String constructValue(Field field) {
    // to get the values of private constants
    field.setAccessible(true);

    String value = null;
    try {
      return String.format("%s <%s>", typeNameGenerator.getTypeName(field), field.get(null));
    } catch (IllegalArgumentException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return value;
  }
}

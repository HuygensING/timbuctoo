package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

public class FieldMetaDataGeneratorFactory {
  private final TypeNameGenerator typeNameGenerator;
  private final FieldMapper fieldMapper;

  public FieldMetaDataGeneratorFactory(TypeNameGenerator typeNameGenerator, FieldMapper fieldMapper) {
    this.typeNameGenerator = typeNameGenerator;
    this.fieldMapper = fieldMapper;
  }

  /**
   * Creates a meta data generator for {@code field}.
   * @param field the field where the meta data generator has to be created for.
   * @param containingType the class containing the field.
   * @return the meta data generator of the field.
   */
  public FieldMetaDataGenerator create(Field field, TypeFacade containingType) {
    switch (containingType.getFieldType(field)) {
      case ENUM:
        return new EnumValueFieldMetaDataGenerator(containingType, typeNameGenerator, fieldMapper);
      case CONSTANT:
        return new ConstantFieldMetaDataGenerator(containingType, typeNameGenerator, fieldMapper);
      case POOR_MANS_ENUM:
        return new PoorMansEnumFieldMetaDataGenerator(containingType, typeNameGenerator, fieldMapper, containingType.getPoorMansEnumType(field));
      case DEFAULT:
        return new DefaultFieldMetaDataGenerator(containingType, typeNameGenerator, fieldMapper);
      default:
        return new NoOpFieldMetaDataGenerator(containingType, typeNameGenerator, fieldMapper);
    }
  }

}

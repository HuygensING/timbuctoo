package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;

public class FieldMetaDataGeneratorFactory {
  private final TypeNameGenerator typeNameGenerator;

  public FieldMetaDataGeneratorFactory(TypeNameGenerator typeNameGenerator) {
    this.typeNameGenerator = typeNameGenerator;
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
        return new EnumValueFieldMetaDataGenerator(containingType);
      case CONSTANT:
        return new ConstantFieldMetaDataGenerator(containingType);
      case POOR_MANS_ENUM:
        return new PoorMansEnumFieldMetaDataGenerator(containingType, typeNameGenerator);
      case DEFAULT:
        return new DefaultFieldMetaDataGenerator(containingType);
      default:
        return new NoOpFieldMetaDataGenerator(containingType);
    }
  }

}

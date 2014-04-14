package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

public class TypeFacadeBuilder {
  private FieldMapper fieldMapper = new FieldMapper();
  private TypeNameGenerator typeNameGenerator = new TypeNameGenerator();
  private final Class<?> type;

  private TypeFacadeBuilder(Class<?> type) {
    this.type = type;
  }

  public TypeFacadeBuilder withFieldMapper(FieldMapper fieldMapper) {
    this.fieldMapper = fieldMapper;
    return this;
  }

  public TypeFacadeBuilder withTypeNameGenerator(TypeNameGenerator typeNameGenerator) {
    this.typeNameGenerator = typeNameGenerator;
    return this;
  }

  public TypeFacade build() {
    return new TypeFacade(type, fieldMapper, typeNameGenerator);
  }

  public static TypeFacadeBuilder aTypeFacade(Class<?> type) {
    return new TypeFacadeBuilder(type);
  }
}

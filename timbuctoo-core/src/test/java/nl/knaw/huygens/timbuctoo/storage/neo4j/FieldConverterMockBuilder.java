package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FieldConverterMockBuilder {
  private String name;
  private FieldType type;

  private FieldConverterMockBuilder() {

  }

  public static FieldConverterMockBuilder newFieldConverter() {
    return new FieldConverterMockBuilder();
  }

  public FieldConverterMockBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public FieldConverterMockBuilder withType(FieldType type) {
    this.type = type;
    return this;
  }

  public FieldConverter build() {
    FieldConverter fieldConverter = mock(FieldConverter.class);

    when(fieldConverter.getName()).thenReturn(name);
    when(fieldConverter.getFieldType()).thenReturn(type);

    return fieldConverter;
  }

}

package nl.knaw.huygens.timbuctoo.graphql.derivedschema;

class DerivedValueTypeSchemaGenerator implements DerivedTypeSchemaGenerator {
  private static final String VALUE_INTERFACE_NAME = "Value";
  private static final String LANGUAGE_INTERFACE_NAME = "Language";

  private final String name;
  private final boolean hasLanguage;

  public DerivedValueTypeSchemaGenerator(String name, boolean hasLanguage) {
    this.name = name;
    this.hasLanguage = hasLanguage;
  }

  @Override
  public StringBuilder getSchema() {
    String interfaceName = VALUE_INTERFACE_NAME;
    if (hasLanguage) {
      interfaceName += " & " + LANGUAGE_INTERFACE_NAME;
    }

    StringBuilder builder = new StringBuilder();
    builder.append("type ").append(name).append(" implements ").append(interfaceName).append(" {\n")
           .append("  value: String!\n")
           .append("  type: String!\n");

    if (hasLanguage) {
      builder.append("  language: String!\n");
    }

    builder.append("}\n\n");

    return builder;
  }

}

package nl.knaw.huygens.repository.config;

/**
 * Validates the Configuration of the Repository Project.
 */
public class ConfigValidator {

  private final Configuration config;
  private final DocTypeRegistry registry;
  private boolean error;

  public ConfigValidator(Configuration config, DocTypeRegistry registry) {
    this.config = config;
    this.registry = registry;
  }

  public void validate() {
    error = false;

    validateDocTypes("doctypes");
    validateDocTypes("indexeddoctypes");
    validateDocTypes("variationdoctypes");
    validateDocTypes("versioneddoctypes");

    if (error) {
      throw new RuntimeException("Configuration error(s)");
    }
  }

  private void validateDocTypes(String configKey) {
    for (String type : config.getSettings(configKey)) {
      if (registry.getClassFromWebServiceTypeString(type) == null) {
        System.err.printf("Configuration key '%s': '%s' is not a document type%n", configKey, type);
        error = true;
      }
    }
  }

}

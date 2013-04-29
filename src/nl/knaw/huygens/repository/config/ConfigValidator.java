package nl.knaw.huygens.repository.config;

import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;

/**
 * Validates the Configuration of the Repository Project.
 */
public class ConfigValidator {

  private final Configuration config;
  private final DocumentTypeRegister registry;
  private boolean error;

  public ConfigValidator(Configuration config, DocumentTypeRegister registry) {
    this.config = config;
    this.registry = registry;
  }

  public void validate() {
    error = false;

    validateDocTypes("doctypes");
    validateDocTypes("indexeddoctypes");
    validateDocTypes("variationdoctypes");
    validateDocTypes("versioneddoctypes");
    validateDocTypes("defaultdoctype");

    if (error) {
      throw new RuntimeException("Configuration errors");
    }
  }

  private void validateDocTypes(String configKey) {
    for (String type : config.getSettings(configKey)) {
      if (registry.getClassFromTypeString(type) == null) {
        System.err.printf("Configuration key '%s': '%s' is not a document type%n", configKey, type);
        error = true;
      }
    }
  }

}

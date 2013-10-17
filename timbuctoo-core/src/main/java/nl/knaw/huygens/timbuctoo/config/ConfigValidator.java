package nl.knaw.huygens.timbuctoo.config;

import java.io.File;

/**
 * Validates the Configuration of the Repository Project.
 */
public class ConfigValidator {

  private final Configuration config;
  private final TypeRegistry registry;
  private boolean error;

  public ConfigValidator(Configuration config, TypeRegistry registry) {
    this.config = config;
    this.registry = registry;
  }

  public void validate() {
    error = false;

    validateDocTypes("doctypes");
    validateDocTypes("indexeddoctypes");
    validateDocTypes("versioneddoctypes");

    validateSolrDirectory();

    if (error) {
      throw new RuntimeException("Configuration error(s)");
    }
  }

  private void validateDocTypes(String configKey) {
    for (String type : config.getSettings(configKey)) {
      if (registry.getTypeForIName(type) == null) {
        System.err.printf("Configuration key '%s': '%s' is not an entity type%n", configKey, type);
        error = true;
      }
    }
  }

  private void validateSolrDirectory() {
    File dir = new File(config.getSolrDir());
    if (!dir.isDirectory()) {
      System.err.printf("Solr directory '%s' does not exist%n", dir.getAbsolutePath());
      error = true;
    }
  }

}

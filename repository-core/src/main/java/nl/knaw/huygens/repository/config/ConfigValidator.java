package nl.knaw.huygens.repository.config;

import java.io.File;

import com.google.common.base.Strings;

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
    validateDocTypes("versioneddoctypes");

    validateSolrDirectory();

    if (error) {
      throw new RuntimeException("Configuration error(s)");
    }
  }

  private void validateDocTypes(String configKey) {
    for (String type : config.getSettings(configKey)) {
      if (registry.getTypeForIName(type) == null) {
        System.err.printf("Configuration key '%s': '%s' is not a document type%n", configKey, type);
        error = true;
      }
    }
  }

  private void validateSolrDirectory() {
    File dir = new File(getSolrDir(config));
    if (!dir.isDirectory()) {
      System.err.printf("Solr directory '%s' does not exist%n", dir.getAbsolutePath());
      error = true;
    }
  }

  // TODO make this part of Configuration
  private String getSolrDir(Configuration config) {
    String path = config.getSetting("solr.directory");
    return Strings.isNullOrEmpty(path) ? config.pathInUserHome("repository/solr") : path;
  }

}

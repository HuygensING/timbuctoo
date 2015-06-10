package nl.knaw.huygens.timbuctoo.config;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates the configuration.
 */
public class ConfigValidator {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigValidator.class);

  protected final Configuration config;
  private boolean error;

  public ConfigValidator(Configuration config) {
    this.config = config;
  }

  /**
   * Validate the configuration. Override the {@code validateSettings} method to add more settings to validate.
   */
  public final void validate() {
    error = false;

    validateSettings();

    if (error) {
      throw new RuntimeException("Configuration error(s)");
    }
  }

  /**
   * A method that validates the settings needed to be validated.
   */
  protected void validateSettings() {
    checkSettingExists(Configuration.KEY_HOME_DIR);
    validateSolrDirectory();
    validateAdminDataDirectory();
    validateGraphDatabase();
  }

  /**
   * Returns {@code true} if the specified condition is satisfied, {@code false} otherwise.
   */
  protected boolean checkCondition(boolean condition, String errorMessage, Object... arguments) {
    if (condition) {
      return true;
    } else {
      LOG.error(errorMessage, arguments);
      error = true;
      return false;
    }
  }

  protected boolean checkSettingExists(String key) {
    return checkCondition(config.hasSetting(key), "Setting '{}' does not exist", key);
  }

  private void validateAdminDataDirectory() {
    String key = "admin_data.directory";
    if (checkSettingExists(key)) {
      checkDirectoryExists(key);
    }
  }

  private void validateGraphDatabase() {
    String key = "graph.type";
    if (checkSettingExists(key)) {
      String type = config.getSetting(key);
      if (GraphTypes.valueOf(type) == GraphTypes.NEO4J) {
        checkSettingExists("graph.path");
      } else if (GraphTypes.valueOf(type) == GraphTypes.REXSTER) {
        checkSettingExists("graph.url");
      }
    }
  }

  private void checkDirectoryExists(String key) {
    File dir = new File(config.getDirectory(key));
    checkCondition(dir.isDirectory(), "Directory '{}' of key '{}' does not exist", dir, key);
  }

  private void validateSolrDirectory() {
    File dir = new File(config.getSolrHomeDir());
    checkCondition(dir.isDirectory(), "Solr directory '{}' does not exist", dir);
  }

}

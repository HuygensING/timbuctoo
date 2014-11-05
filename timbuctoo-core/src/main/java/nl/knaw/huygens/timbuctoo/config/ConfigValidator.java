package nl.knaw.huygens.timbuctoo.config;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates the configuration.
 */
public class ConfigValidator {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigValidator.class);

  private final Configuration config;
  private boolean error;

  public ConfigValidator(Configuration config) {
    this.config = config;
  }

  public void validate() {
    error = false;

    checkSettingExists(Configuration.KEY_HOME_DIR);
    validateSolrDirectory();
    validateAdminDataDirectory();
    validateLoginSettings();

    if (error) {
      throw new RuntimeException("Configuration error(s)");
    }
  }

  /**
   * Returns {@code true} if the specified condition is satisfied, {@code false} otherwise.
   */
  private boolean checkCondition(boolean condition, String errorMessage, Object... arguments) {
    if (condition) {
      return true;
    } else {
      LOG.error(errorMessage, arguments);
      error = true;
      return false;
    }
  }

  private boolean checkSettingExists(String key) {
    return checkCondition(config.hasSetting(key), "Setting '{}' does not exist", key);
  }

  private void validateAdminDataDirectory() {
    String key = "admin_data.directory";
    if (checkSettingExists(key)) {
      checkDirectoryExists(key);
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

  private void validateLoginSettings() {
    validateDuration();
    validateTimeUnit();

  }

  private void validateTimeUnit() {
    if (checkSettingExists(Configuration.EXPIRATION_TIME_UNIT_KEY)) {
      String value = config.getSetting(Configuration.EXPIRATION_TIME_UNIT_KEY);

      checkCondition(isValidTimeUnit(value), "{} is not a valid value for {}. (One of {} is allowed)",//
          value, //
          Configuration.EXPIRATION_TIME_UNIT_KEY, //
          TimeUnit.values());
    }
  }

  private boolean isValidTimeUnit(String value) {
    for (TimeUnit timeUnit : TimeUnit.values()) {
      if (timeUnit.toString().equalsIgnoreCase(value)) {
        return true;
      }
    }
    return false;
  }

  private void validateDuration() {
    if (checkSettingExists(Configuration.EXPIRATION_DURATION_KEY)) {
      int value = config.getIntSetting(Configuration.EXPIRATION_DURATION_KEY);
      checkCondition(value > 0, "{} Has not a valid int value", Configuration.EXPIRATION_DURATION_KEY);
    }

  }
}

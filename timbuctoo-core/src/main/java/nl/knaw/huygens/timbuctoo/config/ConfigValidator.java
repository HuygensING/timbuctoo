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

/**
 * Validates the configuration.
 */
public class ConfigValidator {

  private final Configuration config;
  private boolean error;

  public ConfigValidator(Configuration config) {
    this.config = config;
  }

  public void validate() {
    error = false;

    validateSolrDirectory();

    if (error) {
      throw new RuntimeException("Configuration error(s)");
    }
  }

  private void validateSolrDirectory() {
    File dir = new File(config.getSolrHomeDir());
    if (!dir.isDirectory()) {
      System.err.printf("Solr directory '%s' does not exist%n", dir.getAbsolutePath());
      error = true;
    }
  }

}

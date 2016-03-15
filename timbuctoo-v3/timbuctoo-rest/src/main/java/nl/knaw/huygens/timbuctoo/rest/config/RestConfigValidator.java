package nl.knaw.huygens.timbuctoo.rest.config;

/*
 * #%L
 * Timbuctoo REST api
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

import java.util.concurrent.TimeUnit;

import nl.knaw.huygens.timbuctoo.config.ConfigValidator;
import nl.knaw.huygens.timbuctoo.config.Configuration;

public class RestConfigValidator extends ConfigValidator {

  public RestConfigValidator(Configuration config) {
    super(config);
  }

  @Override
  protected void validateSettings() {
    super.validateSettings();
    validateLoginSettings();
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

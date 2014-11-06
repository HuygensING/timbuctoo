package nl.knaw.huygens.timbuctoo.rest.config;

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

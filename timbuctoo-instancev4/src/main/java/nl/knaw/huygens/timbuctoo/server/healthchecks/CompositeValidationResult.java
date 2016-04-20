package nl.knaw.huygens.timbuctoo.server.healthchecks;

import java.util.List;

public class CompositeValidationResult implements ValidationResult {
  private List<ValidationResult> validationResults;

  public CompositeValidationResult(List<ValidationResult> validationResults) {
    this.validationResults = validationResults;
  }

  @Override
  public boolean isValid() {
    return validationResults.stream().allMatch(result -> result.isValid());
  }

  @Override
  public String getMessage() {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}

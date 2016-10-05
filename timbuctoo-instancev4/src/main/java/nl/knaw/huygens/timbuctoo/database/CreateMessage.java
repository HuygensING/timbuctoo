package nl.knaw.huygens.timbuctoo.database;

import java.util.Optional;

public class CreateMessage {
  private Optional<String> errorMessage;

  private CreateMessage(Optional<String> errorMessage) {
    this.errorMessage = errorMessage;
  }

  public static CreateMessage success() {
    return new CreateMessage(Optional.empty());
  }

  public static CreateMessage failure(String message) {
    return new CreateMessage(Optional.ofNullable(message));
  }

  public boolean succeeded() {
    return !errorMessage.isPresent();
  }

  public Optional<String> getErrorMessage() {
    return errorMessage;
  }
}

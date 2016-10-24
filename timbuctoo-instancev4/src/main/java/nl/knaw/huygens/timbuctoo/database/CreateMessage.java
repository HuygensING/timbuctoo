package nl.knaw.huygens.timbuctoo.database;

import java.util.Optional;
import java.util.UUID;

public class CreateMessage {
  private Optional<String> errorMessage;
  private Optional<UUID> id;

  private CreateMessage(Optional<String> errorMessage, Optional<UUID> id) {
    this.errorMessage = errorMessage;
    this.id = id;
  }

  public static CreateMessage success(UUID id) {
    return new CreateMessage(Optional.empty(), Optional.of(id));
  }

  public static CreateMessage failure(String message) {
    return new CreateMessage(Optional.ofNullable(message), Optional.empty());
  }

  public boolean succeeded() {
    return !errorMessage.isPresent();
  }

  public Optional<String> getErrorMessage() {
    return errorMessage;
  }

  public Optional<UUID> getId() {
    return id;
  }
}

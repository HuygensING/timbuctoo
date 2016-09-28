package nl.knaw.huygens.timbuctoo.database;

import java.util.Optional;

public class UpdateReturnMessage {
  private final Optional<Integer> newRev;
  private UpdateStatus status;

  private UpdateReturnMessage(UpdateStatus status) {
    this.status = status;
    this.newRev = Optional.empty();
  }

  public UpdateReturnMessage(UpdateStatus status, Optional<Integer> newRev) {
    this.status = status;
    this.newRev = newRev;
  }

  public static UpdateReturnMessage notFound() {
    return new UpdateReturnMessage(UpdateStatus.NOT_FOUND);
  }

  public static UpdateReturnMessage allreadyUpdated() {
    return new UpdateReturnMessage(UpdateStatus.ALREADY_UPDATED);
  }

  public static UpdateReturnMessage success(int newRev) {
    return new UpdateReturnMessage(UpdateStatus.SUCCESS, Optional.of(newRev));
  }

  /**
   * @return the new rev of the entity when status is success else it will be an empty Optional
   */
  public Optional<Integer> getNewRev() {
    return newRev;
  }

  public UpdateStatus getStatus() {
    return status;
  }

  public enum UpdateStatus {
    NOT_FOUND,
    ALREADY_UPDATED,
    SUCCESS
  }

}

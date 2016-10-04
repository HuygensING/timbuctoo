package nl.knaw.huygens.timbuctoo.database;

public class DeleteMessage {
  private final DeleteStatus status;

  private DeleteMessage(DeleteStatus status) {
    this.status = status;
  }

  public static DeleteMessage notFound() {
    return new DeleteMessage(DeleteStatus.NOT_FOUND);
  }

  public static DeleteMessage success() {
    return new DeleteMessage(DeleteStatus.SUCCESS);
  }

  public DeleteStatus getStatus() {
    return status;
  }

  public enum DeleteStatus {
    SUCCESS,
    NOT_FOUND
  }
}

package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;

import java.util.Optional;

public class GetMessage {
  private final Optional<ReadEntity> readEntity;
  private GetStatus status;

  private GetMessage(GetStatus status) {
    this.status = status;
    this.readEntity = Optional.empty();
  }

  public GetMessage(GetStatus status, Optional<ReadEntity> readEntity) {
    this.status = status;
    this.readEntity = readEntity;
  }

  public static GetMessage notFound() {
    return new GetMessage(GetStatus.NOT_FOUND);
  }


  public static GetMessage success(ReadEntity readEntity) {
    return new GetMessage(GetStatus.SUCCESS, Optional.of(readEntity));
  }

  /**
   * @return the new rev of the entity when status is success else it will be an empty Optional
   */
  public Optional<ReadEntity> getReadEntity() {
    return readEntity;
  }

  public GetStatus getStatus() {
    return status;
  }

  public enum GetStatus {
    NOT_FOUND,
    SUCCESS
  }
}

package nl.knaw.huygens.timbuctoo.v5.datastores.dto;

import java.util.Optional;

public interface StoreStatus {
  long getCurrentVersion();

  boolean isUpdating();

  long getPosition();

  long getFinish();

  Optional<String> getCurrentError();
}

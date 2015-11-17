package nl.knaw.huygens.timbuctoo.persistence;

import nl.knaw.huygens.timbuctoo.messages.Action;

public interface PersistenceRequest {
  Action toAction();
}

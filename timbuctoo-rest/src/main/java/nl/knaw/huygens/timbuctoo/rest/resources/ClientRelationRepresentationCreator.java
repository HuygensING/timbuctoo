package nl.knaw.huygens.timbuctoo.rest.resources;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.ClientRelationRepresentation;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class ClientRelationRepresentationCreator {
  public <T extends DomainEntity> List<ClientRelationRepresentation> createRefs(Class<T> type, List<T> result) {
    throw new UnsupportedOperationException();
  }
}

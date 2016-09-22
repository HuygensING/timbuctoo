package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.crud.HandleAdder;
import nl.knaw.huygens.timbuctoo.crud.HandleAdderParameters;
import nl.knaw.huygens.timbuctoo.database.dto.CreateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;

import java.io.IOException;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

/**
 * This class is performs all the steps needed to save entities relations, etc.
 */
public class TimbuctooDbAccess {

  private final Authorizer authorizer;
  private final DataAccess dataAccess;
  private final Clock clock;
  private final HandleAdder handleAdder;

  public TimbuctooDbAccess(Authorizer authorizer, DataAccess dataAccess, Clock clock, HandleAdder handleAdder) {
    this.authorizer = authorizer;
    this.dataAccess = dataAccess;
    this.clock = clock;
    this.handleAdder = handleAdder;
  }

  public UUID createEntity(Collection collection, Optional<Collection> baseCollection, CreateEntity createEntity,
                           String userId)
    throws AuthorizationUnavailableException, AuthorizationException, IOException {
    checkIfAllowedToWrite(userId, collection);
    UUID id = UUID.randomUUID();
    try (DataAccessMethods db = dataAccess.start()) {
      try {
        db.createEntity(collection, baseCollection, createEntity, userId, clock.instant(), id);
        db.success();
      } catch (IOException e) {
        db.rollback();
        throw e;
      }
    }

    handleAdder.add(new HandleAdderParameters(collection.getCollectionName(), id, 1));

    return id;
  }

  private void checkIfAllowedToWrite(String userId, Collection collection) throws
    AuthorizationException, AuthorizationUnavailableException {
    if (!authorizer.authorizationFor(collection, userId).isAllowedToWrite()) {
      throw AuthorizationException.notAllowedToCreate(collection.getCollectionName());
    }
  }
}


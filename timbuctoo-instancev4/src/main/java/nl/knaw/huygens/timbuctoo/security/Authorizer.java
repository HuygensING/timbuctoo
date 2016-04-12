package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.crud.Authorization;

public interface Authorizer {

  public Authorization authorizationFor(String collectionName, String userId);
}

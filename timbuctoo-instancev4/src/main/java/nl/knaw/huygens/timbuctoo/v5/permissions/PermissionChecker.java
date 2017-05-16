package nl.knaw.huygens.timbuctoo.v5.permissions;

import nl.knaw.huygens.security.client.UnauthorizedException;

/**
 * Permissionchecker approach inspired by macaroons (https://github.com/rescrv/libmacaroons)
 *
 * <p>The basic premise is that permission validation is embedded in an object that over the course of the request
 * receives information from the handling methods. e.g. the jersey method may add the user groups and the database
 * method may add the type of resource that is being requested.
 *
 * <p>It is then the object itself that validates whether the action is allowed. It is the action executor that checks
 * for permission at the right time.
 *
 * <p>This approach allows us to write the authentication logic separately from the business code. While writing the
 * business code we simply call satisfy and hasPermission at the places where this makes sense and then afterwards we
 * can configure PermissionCheckers that require a certain satisfy() call in order to grant permissions
 *
 * <p>Often a user has multiple roles depending on the context. This is modelled by using multiple permissionCheckers
 * that are satisfied by
 *
 * <p>you load a user
 * based on the metadata of that user (group membership, serialized permissionCheckers) you create a
 * CombinedPermissionChecker. You execute the actions and you verify that one of the permissioncheckers allows this
 *
 * <p>PermissionCheckers are hard to convince (everything must match) but you only need to convince one. I think that
 * strikes a good balance.
 */
public interface PermissionChecker {
  void satisfy(String key, String value);

  void hasPermission() throws UnauthorizedException;

  PermissionChecker split();
}

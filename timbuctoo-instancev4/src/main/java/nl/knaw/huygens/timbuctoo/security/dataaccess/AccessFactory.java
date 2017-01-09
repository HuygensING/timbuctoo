package nl.knaw.huygens.timbuctoo.security.dataaccess;

public interface AccessFactory {
  LoginAccess getLoginAccess() throws AccessNotPossibleException;

  UserAccess getUserAccess() throws AccessNotPossibleException;

  VreAuthorizationAccess getVreAuthorizationAccess() throws AccessNotPossibleException;
}

package nl.knaw.huygens.timbuctoo.security.dummy;

import nl.knaw.huygens.timbuctoo.security.BasicPermissionFetcher;
import nl.knaw.huygens.timbuctoo.security.JsonBasedAuthorizer;
import nl.knaw.huygens.timbuctoo.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.security.SecurityFactory;
import nl.knaw.huygens.timbuctoo.security.UserValidator;
import nl.knaw.huygens.timbuctoo.security.dataaccess.AccessFactory;
import nl.knaw.huygens.timbuctoo.security.exceptions.AccessNotPossibleException;

public class DummySecurityFactory implements SecurityFactory {
  private final AccessFactory accessFactory;

  public DummySecurityFactory(AccessFactory accessFactory) {
    this.accessFactory = accessFactory;
  }

  @Override
  public UserValidator getUserValidator() {
    return new DummyUserValidator();
  }

  @Override
  public PermissionFetcher getPermissionFetcher() throws AccessNotPossibleException {
    return new BasicPermissionFetcher(
        new JsonBasedAuthorizer(accessFactory.getVreAuthorizationAccess()),
        accessFactory.getPermissionConfig()
    );
  }
}

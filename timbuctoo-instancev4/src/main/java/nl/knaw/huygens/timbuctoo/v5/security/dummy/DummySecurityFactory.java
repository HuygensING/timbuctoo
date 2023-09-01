package nl.knaw.huygens.timbuctoo.v5.security.dummy;

import nl.knaw.huygens.timbuctoo.security.BasicPermissionFetcher;
import nl.knaw.huygens.timbuctoo.security.JsonBasedAuthorizer;
import nl.knaw.huygens.timbuctoo.security.dataaccess.AccessFactory;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.SecurityFactory;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AccessNotPossibleException;

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

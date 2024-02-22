package nl.knaw.huygens.timbuctoo.security.dummy;

import nl.knaw.huygens.timbuctoo.security.BasicPermissionFetcher;
import nl.knaw.huygens.timbuctoo.security.JsonBasedAuthorizer;
import nl.knaw.huygens.timbuctoo.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.security.SecurityFactory;
import nl.knaw.huygens.timbuctoo.security.UserValidator;
import nl.knaw.huygens.timbuctoo.security.dataaccess.AccessFactory;
import nl.knaw.huygens.timbuctoo.security.exceptions.AccessNotPossibleException;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;

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

  @Override
  public URI getLoginEndpoint() {
    return URI.create("/dummylogin");
  }

  @Override
  public void register(Consumer<Object> registerToJersey) throws NoSuchAlgorithmException, AccessNotPossibleException {
    registerToJersey.accept(new DummyLoginEndPoint());
  }
}

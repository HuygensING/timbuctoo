package nl.knaw.huygens.timbuctoo.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.security.client.AuthenticationHandler;
import nl.knaw.huygens.security.client.HttpCaller;
import nl.knaw.huygens.timbuctoo.security.dataaccess.AccessFactory;
import nl.knaw.huygens.timbuctoo.security.dataaccess.AccessNotPossibleException;
import nl.knaw.huygens.timbuctoo.security.dataaccess.LoginAccess;
import nl.knaw.huygens.timbuctoo.security.dataaccess.UserAccess;
import nl.knaw.huygens.timbuctoo.security.dataaccess.VreAuthorizationAccess;
import nl.knaw.huygens.timbuctoo.util.TimeoutFactory;
import nl.knaw.huygens.timbuctoo.util.Tuple;

import javax.validation.constraints.NotNull;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class SecurityFactory {
  @JsonProperty
  @NotNull
  private AccessFactory localAuthentication;
  @JsonProperty
  private String algorithm = "SHA-256";
  @JsonProperty
  @NotNull
  private TimeoutFactory autoLogoutTimeout;
  @JsonProperty()
  @NotNull
  private FederatedAuthConfiguration federatedAuthentication;

  @JsonIgnore
  private JsonBasedAuthenticator jsonBasedAuthenticator;
  @JsonIgnore
  private JsonBasedUserStore jsonBasedUserStore;
  @JsonIgnore
  private JsonBasedAuthorizer jsonBasedAuthorizer;
  @JsonIgnore
  private LoggedInUsers loggedInUsers;

  @JsonIgnore
  private LoginAccess loginAccess;
  @JsonIgnore
  private UserAccess userAccess;
  @JsonIgnore
  private VreAuthorizationAccess vreAuthorizationAccess;

  @JsonIgnore
  AuthenticationHandler authHandler;

  protected abstract HttpCaller getHttpCaller();

  private JsonBasedAuthenticator getJsonBasedAuthenticator() throws AccessNotPossibleException,
    NoSuchAlgorithmException {
    if (jsonBasedAuthenticator == null) {
      jsonBasedAuthenticator = new JsonBasedAuthenticator(getLoginAccess(), algorithm);
    }
    return jsonBasedAuthenticator;
  }

  private JsonBasedUserStore getJsonBasedUserStore() throws AccessNotPossibleException {
    if (jsonBasedUserStore == null) {
      jsonBasedUserStore = new JsonBasedUserStore(getUserAccess());
    }
    return jsonBasedUserStore;
  }

  private JsonBasedAuthorizer getJsonBasedAuthorizer() throws AccessNotPossibleException {
    if (jsonBasedAuthorizer == null) {
      jsonBasedAuthorizer = new JsonBasedAuthorizer(getVreAuthorizationAccess());
    }
    return jsonBasedAuthorizer;
  }

  private LoginAccess getLoginAccess() throws AccessNotPossibleException {
    if (loginAccess == null) {
      loginAccess = localAuthentication.getLoginAccess();
    }
    return loginAccess;
  }

  private UserAccess getUserAccess() throws AccessNotPossibleException {
    if (userAccess == null) {
      userAccess = localAuthentication.getUserAccess();
    }
    return userAccess;
  }

  private VreAuthorizationAccess getVreAuthorizationAccess() throws AccessNotPossibleException {
    if (vreAuthorizationAccess == null) {
      vreAuthorizationAccess = localAuthentication.getVreAuthorizationAccess();
    }
    return vreAuthorizationAccess;
  }

  private AuthenticationHandler getAuthHandler(HttpCaller httpCaller) {
    if (authHandler == null) {
      authHandler = federatedAuthentication.makeHandler(httpCaller);
    }
    return authHandler;
  }

  public VreAuthorizationCrud getVreAuthorizationCreator() throws AccessNotPossibleException {
    return getJsonBasedAuthorizer();
  }

  public UserCreator getUserCreator() throws AccessNotPossibleException {
    return getJsonBasedUserStore();
  }

  public LoginCreator getLoginCreator() throws NoSuchAlgorithmException, AccessNotPossibleException {
    return getJsonBasedAuthenticator();
  }

  public UserStore getUserStore() throws AccessNotPossibleException {
    return getJsonBasedUserStore();
  }

  public Authorizer getAuthorizer() throws AccessNotPossibleException {
    return getJsonBasedAuthorizer();
  }

  public Authenticator getAuthenticator() throws NoSuchAlgorithmException, AccessNotPossibleException {
    return getJsonBasedAuthenticator();
  }

  public LoggedInUsers getLoggedInUsers()
    throws AccessNotPossibleException, NoSuchAlgorithmException {
    if (loggedInUsers == null) {
      loggedInUsers = new LoggedInUsers(
        getAuthenticator(),
        new BasicUserValidator(getAuthHandler(getHttpCaller()), getUserStore()),
        autoLogoutTimeout.createTimeout()
      );
    }
    return loggedInUsers;
  }

  public Iterator<Tuple<String, Supplier<Optional<String>>>> getHealthChecks() {
    return localAuthentication.getHealthChecks();
  }
}

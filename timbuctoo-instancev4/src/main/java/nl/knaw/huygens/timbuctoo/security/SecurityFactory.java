package nl.knaw.huygens.timbuctoo.security;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.util.EmptyIterator;
import io.dropwizard.setup.Environment;
import nl.knaw.huygens.security.client.AuthenticationHandler;
import nl.knaw.huygens.timbuctoo.security.dataaccess.AccessNotPossibleException;
import nl.knaw.huygens.timbuctoo.security.dataaccess.LoginAccess;
import nl.knaw.huygens.timbuctoo.security.dataaccess.UserAccess;
import nl.knaw.huygens.timbuctoo.security.dataaccess.VreAuthorizationAccess;
import nl.knaw.huygens.timbuctoo.security.dataaccess.azure.AzureAccessFactory;
import nl.knaw.huygens.timbuctoo.security.dataaccess.localfile.LocalfileAccessFactory;
import nl.knaw.huygens.timbuctoo.server.FederatedAuthConfiguration;
import nl.knaw.huygens.timbuctoo.util.Timeout;
import nl.knaw.huygens.timbuctoo.util.TimeoutFactory;
import nl.knaw.huygens.timbuctoo.util.Tuple;

import javax.validation.constraints.NotNull;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

public class SecurityFactory {
  @JsonProperty
  private AzureAccessFactory azure;
  @JsonProperty
  private LocalfileAccessFactory localfile;
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
      if (azure != null) {
        loginAccess = azure.getLoginAccess();
      }
      if (localfile != null) {
        loginAccess = localfile.getLoginAccess();
      }
    }
    return loginAccess;
  }

  private UserAccess getUserAccess() throws AccessNotPossibleException {
    if (userAccess == null) {
      if (azure != null) {
        userAccess = azure.getUserAccess();
      }
      if (localfile != null) {
        userAccess = localfile.getUserAccess();
      }
    }
    return userAccess;
  }

  private VreAuthorizationAccess getVreAuthorizationAccess() throws AccessNotPossibleException {
    if (vreAuthorizationAccess == null) {
      if (azure != null) {
        vreAuthorizationAccess = azure.getVreAuthorizationAccess();
      }
      if (localfile != null) {
        vreAuthorizationAccess = localfile.getVreAuthorizationAccess();
      }
    }
    return vreAuthorizationAccess;
  }

  private AuthenticationHandler getAuthHandler(Environment environment) {
    if (authHandler == null) {
      authHandler = federatedAuthentication.makeHandler(environment);
    }
    return authHandler;
  }

  public VreAuthorizationCreator getVreAuthorizationCreator() throws AccessNotPossibleException {
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

  public LoggedInUsers getLoggedInUsers(Environment environment)
    throws AccessNotPossibleException, NoSuchAlgorithmException {
    if (loggedInUsers == null) {
      loggedInUsers = new LoggedInUsers(
        getAuthenticator(),
        getUserStore(),
        autoLogoutTimeout.createTimeout(),
        getAuthHandler(environment)
      );
    }
    return loggedInUsers;
  }

  @Deprecated
  @JsonIgnore
  public void setLocalfileAccessFactory(LocalfileAccessFactory localfile) {
    this.localfile = localfile;
  }

  @Deprecated
  @JsonIgnore
  public void setAutoLogoutTimeout(Timeout autoLogoutTimeout) {
    TimeoutFactory timeoutFactory = new TimeoutFactory();
    timeoutFactory.setDuration(autoLogoutTimeout.duration);
    timeoutFactory.setTimeUnit(autoLogoutTimeout.timeUnit);
    this.autoLogoutTimeout = timeoutFactory;
  }

  @Deprecated
  @JsonIgnore
  public void setAuthHandler(AuthenticationHandler authHandler) {
    this.authHandler = authHandler;
  }

  public Iterator<Tuple<String, HealthCheck>> getHealthChecks() {
    if (localfile != null) {
      return localfile.getHealthChecks();
    }
    if (azure != null) {
      return azure.getHealthChecks();
    }
    return new EmptyIterator<>();
  }
}

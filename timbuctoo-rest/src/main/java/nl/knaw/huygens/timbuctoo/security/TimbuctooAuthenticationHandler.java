package nl.knaw.huygens.timbuctoo.security;

import static nl.knaw.huygens.timbuctoo.security.LocalLoggedInUsers.LOCAL_SESSION_KEY_PREFIX;
import nl.knaw.huygens.security.client.AuthenticationHandler;
import nl.knaw.huygens.security.client.HuygensAuthenticationHandler;
import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.security.client.model.SecurityInformation;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

public class TimbuctooAuthenticationHandler implements AuthenticationHandler {

  private final LocalLoggedInUsers localLoggedInUsers;
  // The AuthenticationHandler for the security server.
  private final HuygensAuthenticationHandler huygensAuthenticationHandler;

  @Inject
  public TimbuctooAuthenticationHandler(LocalLoggedInUsers localLoggedInUsers, HuygensAuthenticationHandler huygensAuthenticationHandler) {
    this.localLoggedInUsers = localLoggedInUsers;
    this.huygensAuthenticationHandler = huygensAuthenticationHandler;
  }

  @Override
  public SecurityInformation getSecurityInformation(String sessionId) throws UnauthorizedException {
    if (isLocalSessionToken(sessionId)) {
      return localLoggedInUsers.getSecurityInformation(sessionId);
    }

    return huygensAuthenticationHandler.getSecurityInformation(sessionId);
  }

  private boolean isLocalSessionToken(String sessionId) {
    return StringUtils.startsWith(sessionId, LOCAL_SESSION_KEY_PREFIX);
  }

}

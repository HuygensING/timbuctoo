package nl.knaw.huygens.timbuctoo.security;

import java.security.Principal;
import java.util.EnumSet;

import nl.knaw.huygens.security.client.AuthorizationHandler;
import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.security.client.model.HuygensSecurityInformation;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.security.core.model.Affiliation;

public class ExampleAuthorizationHandler implements AuthorizationHandler {

  @Override
  public SecurityInformation getSecurityInformation(String sessionId) throws UnauthorizedException {

    if("admin".equals(sessionId)){
      return createAdmin();
    }
    else if("user".equals(sessionId)){
      return createUser();
    }
    
    
    throw new UnauthorizedException();
  }

  private SecurityInformation createUser() {
    HuygensSecurityInformation securityInformation = new HuygensSecurityInformation();
    securityInformation.setAffiliations(EnumSet.of(Affiliation.employee));
    securityInformation.setCommonName("U Ser");
    securityInformation.setDisplayName("U Ser");
    securityInformation.setEmailAddress("user@example.com");
    securityInformation.setGivenName("U");
    securityInformation.setSurname("Ser");
    securityInformation.setOrganization("example inc.");
    securityInformation.setPersistentID("User");
    securityInformation.setPrincipal(new Principal() {

      @Override
      public String getName() {
        return "U Ser";
      }
    });

    return securityInformation;
  }

  private SecurityInformation createAdmin() {
    HuygensSecurityInformation securityInformation = new HuygensSecurityInformation();
    securityInformation.setAffiliations(EnumSet.of(Affiliation.employee));
    securityInformation.setCommonName("Ad Min");
    securityInformation.setDisplayName("Ad Min");
    securityInformation.setEmailAddress("admin@example.com");
    securityInformation.setGivenName("Ad");
    securityInformation.setSurname("Min");
    securityInformation.setOrganization("example inc.");
    securityInformation.setPersistentID("Admin");
    securityInformation.setPrincipal(new Principal() {

      @Override
      public String getName() {
        return "Ad Min";
      }
    });

    return securityInformation;

  }

}

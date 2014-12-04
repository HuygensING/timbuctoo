package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.security.client.model.HuygensSecurityInformation;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.security.core.model.HuygensPrincipal;
import nl.knaw.huygens.timbuctoo.model.Login;

public class LoginConverter {

  public SecurityInformation toSecurityInformation(final Login login) {

    HuygensPrincipal principal = new HuygensPrincipal();
    principal.setCommonName(login.getCommonName());
    principal.setDisplayName(login.getIdentificationName());
    principal.setEmailAddress(login.getEmailAddress());
    principal.setGivenName(login.getGivenName());
    principal.setOrganization(login.getOrganization());
    principal.setPersistentID(login.getUserPid());
    principal.setSurname(login.getSurname());

    return new HuygensSecurityInformation(principal);
  }

}

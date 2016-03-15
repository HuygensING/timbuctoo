package nl.knaw.huygens.timbuctoo.security;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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

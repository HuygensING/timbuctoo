package nl.knaw.huygens.timbuctoo.security;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import javax.ws.rs.core.SecurityContext;

import nl.knaw.huygens.security.client.SecurityContextCreator;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.storage.Repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class UserSecurityContextCreator implements SecurityContextCreator {

  private static final Logger LOG = LoggerFactory.getLogger(UserSecurityContextCreator.class);

  private final Repository repository;

  @Inject
  public UserSecurityContextCreator(Repository repository) {
    this.repository = repository;
  }

  @Override
  public SecurityContext createSecurityContext(SecurityInformation securityInformation) {
    if (securityInformation == null) {
      return null;
    }

    User example = new User();
    example.setPersistentId(securityInformation.getPersistentID());

    User user = findUser(example);

    if (user == null) {
      example.setDisplayName(securityInformation.getDisplayName());
      user = createUser(example, securityInformation);
    }

    UserSecurityContext userSecurityContext = new UserSecurityContext(securityInformation.getPrincipal(), user);

    return userSecurityContext;
  }

  private User createUser(User user, SecurityInformation securityInformation) {
    LOG.debug("Create new user: " + user.getDisplayName());
    User returnValue = null;

    user.setCommonName(securityInformation.getCommonName());
    user.setDisplayName(securityInformation.getDisplayName());
    user.setEmail(securityInformation.getEmailAddress());
    user.setFirstName(securityInformation.getGivenName());
    user.setLastName(securityInformation.getSurname());
    user.setPersistentId(securityInformation.getPersistentID());
    user.setOrganisation(securityInformation.getOrganization());

    try {
      repository.addSystemEntity(User.class, user);
    } catch (Exception e) {
      LOG.error(e.getMessage());
    }
    // This is needed, to be less dependend on the StorageLayer to set the id.
    returnValue = findUser(user);

    return returnValue;
  }

  private User findUser(final User example) {

    return repository.findEntity(User.class, example);
  }

}

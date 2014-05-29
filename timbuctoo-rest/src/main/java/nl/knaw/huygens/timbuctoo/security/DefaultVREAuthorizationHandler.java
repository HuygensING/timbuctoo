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

import static nl.knaw.huygens.timbuctoo.security.UserRoles.ADMIN_ROLE;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.UNVERIFIED_USER_ROLE;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.mail.MailSender;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * A type that retrieves the roles the logged in user has on a VRE. 
 * It will send an email to the admin of the VRE when the user is not known in the VRE.
 */
public class DefaultVREAuthorizationHandler implements VREAuthorizationHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultVREAuthorizationHandler.class);

  private final Repository repository;
  private final MailSender mailSender;

  @Inject
  public DefaultVREAuthorizationHandler(Repository repository, MailSender mailSender) {
    this.repository = repository;
    this.mailSender = mailSender;
  }

  @Override
  public VREAuthorization getVREAuthorization(String vreId, User user) {
    String userId = user.getId();
    VREAuthorization authorization = findVreAuthorization(vreId, userId);

    if (authorization == null) {
      // VRE does not know about the user
      try {
        authorization = createVreAuthorization(vreId, userId);
      } catch (Exception e) {
        LOG.error("Creation of VREAuthorization for user {} and vre {} failed", userId, vreId);
      }
      sendEmail(vreId, user);
    }
    return authorization;
  }

  private VREAuthorization createVreAuthorization(String vreId, String userId) throws StorageException, ValidationException {
    VREAuthorization authorization = new VREAuthorization(vreId, userId, UNVERIFIED_USER_ROLE);
    authorization.setId(repository.addSystemEntity(VREAuthorization.class, authorization));
    return authorization;
  }

  private VREAuthorization findVreAuthorization(String vreId, String userId) {
    VREAuthorization example = new VREAuthorization(vreId, userId);
    return repository.findEntity(VREAuthorization.class, example);
  }

  /**
   * Sends an email to the admin of the VRE the new user is trying to use. 
   */
  private void sendEmail(String vreId, User user) {
    User admin = getFirstAdminOfVRE(vreId);
    if (admin != null && !StringUtils.isBlank(admin.getEmail())) {
      StringBuilder builder = new StringBuilder("Beste admin,\n");
      builder.append(user.getDisplayName());
      builder.append(" heeft interesse getoond voor je VRE.\n");
      builder.append("Met vriendelijke groet,\n");
      builder.append("Timbuctoo");
      mailSender.sendMail(admin.getEmail(), "Nieuwe gebruiker", builder.toString());
    }
  }

  private User getFirstAdminOfVRE(String vreId) {
    VREAuthorization example = new VREAuthorization(vreId, null, ADMIN_ROLE);
    VREAuthorization authorization = repository.findEntity(VREAuthorization.class, example);
    return authorization != null ? repository.getEntity(User.class, authorization.getUserId()) : null;
  }

}

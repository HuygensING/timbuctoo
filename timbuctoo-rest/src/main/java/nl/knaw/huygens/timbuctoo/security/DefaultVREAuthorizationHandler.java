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
import nl.knaw.huygens.timbuctoo.mail.MailSender;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * A type that retrieves the roles the logged in user has on a VRE. 
 * It will send an email to the admin of the VRE when the user is not known in the VRE.
 */
public class DefaultVREAuthorizationHandler implements VREAuthorizationHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultVREAuthorizationHandler.class);

  private final MailSender mailSender;
  private final StorageManager storageManager;

  @Inject
  public DefaultVREAuthorizationHandler(MailSender mailSender, StorageManager storageManager) {
    this.mailSender = mailSender;
    this.storageManager = storageManager;
  }

  @Override
  public VREAuthorization getVREAuthorization(User user, String vreId) {
    String userId = user.getId();
    VREAuthorization authorization = findVreAuthorization(userId, vreId);

    if (authorization == null) {
      // VRE does not know about the user
      try {
        authorization = createVreAuthorization(userId, vreId);
      } catch (Exception e) {
        LOG.error("Creation of VREAuthorization for user {} and vre {} failed", userId, vreId);
      }
      sendEmail(user, vreId);
    }
    return authorization;
  }

  private VREAuthorization createVreAuthorization(String userId, String vreId) throws StorageException, ValidationException {
    VREAuthorization authorization = new VREAuthorization();
    authorization.setUserId(userId);
    authorization.setVreId(vreId);
    authorization.setRoles(Lists.newArrayList(UNVERIFIED_USER_ROLE));

    authorization.setId(storageManager.addSystemEntity(VREAuthorization.class, authorization));

    return authorization;
  }

  private VREAuthorization findVreAuthorization(String userId, String vreId) {
    VREAuthorization example = new VREAuthorization();
    example.setVreId(vreId);
    example.setUserId(userId);

    return storageManager.findEntity(VREAuthorization.class, example);
  }

  /**
   * sends an email to the admin of the VRE the new user is trying to use. 
   * @param user
   * @param vreId 
   */
  private void sendEmail(User user, String vreId) {
    User admin = getFirstAdminOfVRE(vreId);

    StringBuilder contentbuilder = new StringBuilder("Beste admin,\n");
    contentbuilder.append(user.getDisplayName());
    contentbuilder.append(" heeft interesse getoond voor je VRE.\n");
    contentbuilder.append("Met vriendelijke groet,\n");
    contentbuilder.append("De datarepository");

    if (admin != null && !StringUtils.isBlank(admin.getEmail())) {
      mailSender.sendMail(admin.getEmail(), "Nieuwe gebruiker", contentbuilder.toString());
    }
  }

  private User getFirstAdminOfVRE(String vreId) {
    VREAuthorization example = new VREAuthorization();
    example.setRoles(Lists.newArrayList(ADMIN_ROLE));
    example.setVreId(vreId);

    VREAuthorization authorization = storageManager.findEntity(VREAuthorization.class, example);

    return authorization != null ? storageManager.getEntity(User.class, authorization.getUserId()) : null;
  }

}

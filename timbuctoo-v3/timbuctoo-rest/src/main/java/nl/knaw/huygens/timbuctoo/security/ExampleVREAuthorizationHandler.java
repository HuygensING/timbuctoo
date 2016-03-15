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

import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * @Deprecated Use local login instead.
 */
@Deprecated
public class ExampleVREAuthorizationHandler implements VREAuthorizationHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ExampleVREAuthorizationHandler.class);

  private final UserConfigurationHandler userConfigurationHandler;

  @Inject
  public ExampleVREAuthorizationHandler(UserConfigurationHandler userConfigurationHandler) {
    this.userConfigurationHandler = userConfigurationHandler;
  }

  @Override
  public VREAuthorization getVREAuthorization(String vreId, User user) {
    String userId = user.getId();
    String persistentId = user.getPersistentId();

    try {
      VREAuthorization vreAuthorization = findVreAuthorization(vreId, userId);
      if (vreAuthorization == null) {
        vreAuthorization = createVreAuthorization(vreId, userId, persistentId);
      }
      return vreAuthorization;
    } catch (Exception e) {
      LOG.error("Creating VREAuthorization for user {} and VRE {} throws exception {}", user.getCommonName(), vreId, e);
      return null;
    }
  }

  private VREAuthorization createVreAuthorization(String vreId, String userId, String persistentId) throws StorageException, ValidationException {
    VREAuthorization authorization = new VREAuthorization(vreId, userId);
    if ("Admin".equals(persistentId)) {
      authorization.setRoles(Lists.newArrayList(UserRoles.ADMIN_ROLE));
    } else if ("User".equals(persistentId)) {
      authorization.setRoles(Lists.newArrayList(UserRoles.USER_ROLE));
    }
    authorization.setId(userConfigurationHandler.addVREAuthorization(authorization));
    return authorization;
  }

  private VREAuthorization findVreAuthorization(String vreId, String userId) {
    VREAuthorization example = new VREAuthorization(vreId, userId);
    return userConfigurationHandler.findVREAuthorization(example);
  }

}

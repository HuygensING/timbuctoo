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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;

public class ExampleVREAuthorizationHandler implements VREAuthorizationHandler {
  private static final Logger LOG = LoggerFactory.getLogger(ExampleVREAuthorizationHandler.class);

  private final Repository repository;

  @Inject
  public ExampleVREAuthorizationHandler(Repository repository) {
    this.repository = repository;
  }

  @Override
  public VREAuthorization getVREAuthorization(String vreId, User user) {
    String persistentId = user.getPersistentId();

    VREAuthorization example = new VREAuthorization(vreId, user.getId());
    VREAuthorization vreAuthorization = repository.findEntity(VREAuthorization.class, example);

    if (vreAuthorization == null) {
      vreAuthorization = example;
      if ("Admin".equals(persistentId)) {
        vreAuthorization.setRoles(Lists.newArrayList(UserRoles.ADMIN_ROLE));
      } else if ("User".equals(persistentId)) {
        vreAuthorization.setRoles(Lists.newArrayList(UserRoles.USER_ROLE));
      }

      try {
        vreAuthorization.setId(repository.addSystemEntity(VREAuthorization.class, vreAuthorization));
      } catch (Exception e) {
        LOG.error("Creating VREAuthorization for user {} and VRE {} throws exception {}", user.getCommonName(), vreId, e);
      }
    }

    return vreAuthorization;
  }

}

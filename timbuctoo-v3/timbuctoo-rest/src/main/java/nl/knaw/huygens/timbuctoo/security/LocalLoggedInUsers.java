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

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import nl.knaw.huygens.security.client.AuthenticationHandler;
import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.model.Login;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class LocalLoggedInUsers implements AuthenticationHandler {
  static final String LOCAL_SESSION_KEY_PREFIX = "Timbuctoo";
  private final LoginConverter loginConverter;
  private final Cache<String, Login> sessionCache;

  @Inject
  public LocalLoggedInUsers(LoginConverter loginConverter, Configuration config) {
    this(loginConverter, createCache(config));
  }

  private static Cache<String, Login> createCache(Configuration config) {
    int duration = config.getIntSetting(Configuration.EXPIRATION_DURATION_KEY);
    TimeUnit timeUnit = TimeUnit.valueOf(config.getSetting(Configuration.EXPIRATION_TIME_UNIT_KEY));
    return CacheBuilder.newBuilder().expireAfterAccess(duration, timeUnit).build();
  }

  LocalLoggedInUsers(LoginConverter loginConverterMock, Cache<String, Login> sessionCache) {
    loginConverter = loginConverterMock;
    this.sessionCache = sessionCache;
  }

  public synchronized String add(Login login) {
    String sessionKey = createSessionKey();

    sessionCache.put(sessionKey, login);

    return sessionKey;
  }

  private String createSessionKey() {

    return String.format("%s%s", LOCAL_SESSION_KEY_PREFIX, UUID.randomUUID());
  }

  /**
   * Retrieves the persistent id.
   * @param sessionKey the key to retrieve the id for.
   * @return the id or {@code null} if none found.
   */
  public synchronized Login get(String sessionKey) {
    return sessionCache.getIfPresent(sessionKey);
  }

  @Override
  public SecurityInformation getSecurityInformation(String sessionKey) throws UnauthorizedException {
    Login login = get(sessionKey);

    if (login == null) {
      throw new UnauthorizedException("Session could not be retrieved.");
    }

    return loginConverter.toSecurityInformation(login);
  }
}

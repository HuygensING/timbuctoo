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

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;

import nl.knaw.huygens.security.client.model.HuygensSecurityInformation;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

public class UserSecurityContextCreatorTest {

  private static final String USER_ID = "test123";
  private static final String DISPLAY_NAME = "displayName";

  private UserSecurityContextCreator instance;
  private Repository repository;

  @Before
  public void setUp() {
    repository = mock(Repository.class);
    instance = new UserSecurityContextCreator(repository);
  }

  @After
  public void tearDown() {
    reset(repository);
  }

  @Test
  public void testCreateSecurityContextKnownUser() throws Exception {
    User user = createUser(DISPLAY_NAME, USER_ID);

    User example = new User();
    example.setPersistentId(USER_ID);

    SecurityInformation securityInformation = createSecurityInformation(DISPLAY_NAME, USER_ID);

    when(repository.findEntity(User.class, example)).thenReturn(user);

    instance.createSecurityContext(securityInformation);

    verify(repository, only()).findEntity(User.class, example);
    verify(repository, never()).addSystemEntity(Matchers.<Class<User>> any(), any(User.class));
  }

  protected User createUser(String displayName, String userId) {
    User user = new User();
    user.setDisplayName(displayName);
    user.setPersistentId(userId);
    return user;
  }

  protected SecurityInformation createSecurityInformation(String displayName, String userId) {
    Principal principal = mock(Principal.class);
    when(principal.getName()).thenReturn(userId);

    HuygensSecurityInformation securityInformation = new HuygensSecurityInformation();
    securityInformation.setDisplayName(displayName);
    securityInformation.setPrincipal(principal);
    return securityInformation;
  }

  @Test
  public void testCreateSecurityContextUnknownUser() throws Exception {
    SecurityInformation securityInformation = createSecurityInformation(DISPLAY_NAME, USER_ID);
    User user = createUser(DISPLAY_NAME, USER_ID);

    User example = new User();
    example.setPersistentId(USER_ID);

    when(repository.findEntity(Matchers.<Class<User>> any(), any(User.class))).thenReturn(null, user);

    instance.createSecurityContext(securityInformation);

    verify(repository, times(2)).findEntity(Matchers.<Class<User>> any(), any(User.class));
    verify(repository, times(1)).addSystemEntity(Matchers.<Class<User>> any(), any(User.class));
  }

  @Test
  public void testCreateSecurityContextParamNull() {
    assertNull(instance.createSecurityContext(null));
  }
}

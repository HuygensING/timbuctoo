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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import nl.knaw.huygens.security.client.model.HuygensSecurityInformation;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.storage.JsonFileHandler;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.verification.VerificationMode;

public class UserSecurityContextCreatorTest {

  private static final String USER_ID = "test123";
  private static final String DISPLAY_NAME = "displayName";

  private UserSecurityContextCreator instance;
  private JsonFileHandler jsonFileWriter;

  @Before
  public void setUp() {
    jsonFileWriter = mock(JsonFileHandler.class);
    instance = new UserSecurityContextCreator(jsonFileWriter);
  }

  @After
  public void tearDown() {
    reset(jsonFileWriter);
  }

  @Test
  public void testCreateSecurityContextKnownUser() throws Exception {
    User user = createUser(DISPLAY_NAME, USER_ID);

    User example = new User();
    example.setPersistentId(USER_ID);

    SecurityInformation securityInformation = createSecurityInformation(DISPLAY_NAME, USER_ID);

    userIsFoundTheFirstTime(user, example);

    // action
    SecurityContext context = instance.createSecurityContext(securityInformation);

    // verify
    assertThatContextContainsUser(context, user);
    verifyUserSearchedFor(only(), example);
  }

  @Test
  public void testCreateSecurityContextUnknownUser() throws Exception {
    SecurityInformation securityInformation = createSecurityInformation(DISPLAY_NAME, USER_ID);
    User user = createUser(DISPLAY_NAME, USER_ID);

    User example = new User();
    example.setPersistentId(USER_ID);

    userIsFoundTheSecondTime(user, example);

    // action
    SecurityContext context = instance.createSecurityContext(securityInformation);

    // verify
    assertThatContextContainsUser(context, user);
    verifyUserSearchedFor(times(2), example);
    verifyUserIsSaved(example);
  }

  @Test
  public void testCreateSecurityContextParamNull() {
    assertNull(instance.createSecurityContext(null));
  }

  private void userIsFoundTheFirstTime(User user, User example) {
    when(jsonFileWriter.findEntity(User.class, example)).thenReturn(user);
  }

  private void assertThatContextContainsUser(SecurityContext context, User user) {
    assertThat(context, is(instanceOf(UserSecurityContext.class)));
    assertThat(((UserSecurityContext) context).getUser(), is(equalTo(user)));
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

  private void userIsFoundTheSecondTime(User user, User example) {
    when(jsonFileWriter.findEntity(User.class, example)).thenReturn(null, user);
  }

  private void verifyUserIsSaved(User example) throws StorageException, ValidationException {
    verify(jsonFileWriter, times(1)).addSystemEntity(User.class, example);
  }

  private void verifyUserSearchedFor(VerificationMode verifictionMode, User example) {
    verify(jsonFileWriter, verifictionMode).findEntity(User.class, example);
  }

}

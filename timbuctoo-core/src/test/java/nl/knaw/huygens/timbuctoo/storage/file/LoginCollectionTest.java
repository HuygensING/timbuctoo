package nl.knaw.huygens.timbuctoo.storage.file;

/*
 * #%L
 * Timbuctoo core
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import nl.knaw.huygens.timbuctoo.model.Login;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class LoginCollectionTest extends FileCollectionTest<Login> {

  private static final String DIFFERENT_ID = "test2";
  private static final String ID = "test";
  private static final String DIFFERENT_USER_PID = "differentUserPid";
  private static final String USER_PID = "userPid";
  private static final String USER_NAME = ID;
  private static final String DIFFERENT_USER_NAME = "differentAuthString";

  private LoginCollection instance;

  @Before
  public void setup() {
    instance = new LoginCollection();
  }

  @Test
  public void addAddsTheEntityToItsCollection() {
    Login login = createLoginWithUserNameAndUserPid(USER_NAME, USER_PID);
    verifyAddAddsTheEntityToItsCollection(login);
  }

  @Test
  public void addReturnsAnIdAndAddsItToTheEntity() {
    Login login = createLoginWithUserNameAndUserPid(USER_NAME, USER_PID);
    String expectedId = "LOGI000000000001";

    verifyAddReturnsAnIdAndAddsItToTheEntity(login, expectedId);
  }

  @Test(expected = IllegalArgumentException.class)
  public void addThrowsAnIllegalArgumentExceptionWhenTheLoginDoesNotContainAnAuthString() {
    Login loginWithoutAuthString = new Login();
    instance.add(loginWithoutAuthString);
  }

  @Test
  public void addDoesNotAddASecondItemWithTheSameAuthStringItReturnsTheExistingId() {
    // setup
    Login firstLogin = createLoginWithUserNameAndUserPid(USER_NAME, USER_PID);
    Login secondLogin = createLoginWithUserNameAndUserPid(USER_NAME, DIFFERENT_USER_PID);

    // action
    String firstId = instance.add(firstLogin);
    String secondId = instance.add(secondLogin);

    // verify
    assertThat(secondId, is(firstId));
  }

  @Test
  public void addIncrementsTheId() {
    Login entity1 = createLoginWithUserNameAndUserPid(USER_NAME, USER_PID);
    Login entity2 = createLoginWithUserNameAndUserPid(DIFFERENT_USER_NAME, DIFFERENT_USER_PID);
    Login entity3 = createLoginWithUserNameAndUserPid("anotherAuthString", USER_PID);
    String expectedId = "LOGI000000000003";

    verifyAddIncrementsTheId(entity1, entity2, entity3, expectedId);
  }

  @Test
  public void findReturnsTheLoginFoundByTheAuthString() {
    // setup
    Login login = createLoginWithUserNameAndUserPid(USER_NAME, USER_PID);
    Login otherLogin = createLoginWithUserNameAndUserPid(DIFFERENT_USER_NAME, DIFFERENT_USER_PID);

    instance.add(login);
    instance.add(otherLogin);

    Login example = createLoginWithUserNameAndUserPid(USER_NAME, USER_PID);

    // action
    Login actualLogin = instance.findItem(example);

    // verify
    assertThat(actualLogin, is(login));
  }

  @Test
  public void findSearchesByUserPidIfTheAuthStringIsEmpty() {
    // setup
    Login login = createLoginWithUserNameAndUserPid(USER_NAME, USER_PID);

    instance.add(login);

    Login example = new Login();
    example.setUserPid(USER_PID);

    // action
    Login foundLogin = instance.findItem(example);

    // verify
    assertThat(foundLogin, is(login));
  }

  @Test
  public void findReturnsNullIfBothTheUAndUserPidAreNull() {
    Login login = new Login();
    login.setUserPid(USER_PID);
    login.setUserName(USER_NAME);

    instance.add(login);

    Login example = new Login();

    // action
    Login foundLogin = instance.findItem(example);

    // verify
    assertThat(foundLogin, is(nullValue(Login.class)));
  }

  private Login createLoginWithUserNameAndUserPid(String userName, String userPid) {
    Login login = new Login();
    login.setUserName(userName);
    login.setUserPid(userPid);
    return login;
  }

  @Test
  public void findReturnsNullIfNoLoginInTheCollectionExistsWhithTheAuthString() {
    // setup
    Login login = createLoginWithUserNameAndUserPid(DIFFERENT_USER_NAME, DIFFERENT_USER_PID);
    instance.add(login);

    Login example = new Login();
    example.setUserName(USER_NAME);

    // action
    Login actualLogin = instance.findItem(example);

    // verify
    assertThat(actualLogin, is(nullValue(Login.class)));
  }

  @Test
  public void asArrayReturnsAllTheValuesRepresentedInAnArray() {
    // setup
    Login login = createLoginWithUserNameAndUserPid(USER_NAME, USER_PID);
    Login otherLogin = createLoginWithUserNameAndUserPid(DIFFERENT_USER_NAME, DIFFERENT_USER_PID);

    instance.add(login);
    instance.add(otherLogin);

    // action
    Login[] logins = instance.asArray();

    // verify
    assertThat(logins, hasItemInArray(login));
    assertThat(logins, hasItemInArray(otherLogin));
  }

  @Override
  protected FileCollection<Login> getInstance() {
    return instance;
  }

  @Test
  public void intializeWithLoginsAddsTheLoginsToTheCollection() {
    // setup
    Login login = createLoginWithUserNameAndUserPid(USER_NAME, USER_PID);
    login.setId(ID);

    Login otherLogin = createLoginWithUserNameAndUserPid(DIFFERENT_USER_NAME, DIFFERENT_USER_PID);
    otherLogin.setId(DIFFERENT_ID);

    // action
    LoginCollection loginCollection = new LoginCollection(Lists.newArrayList(login, otherLogin));

    // verify
    assertThat(loginCollection.getAll().size(), is(equalTo(2)));
  }

}

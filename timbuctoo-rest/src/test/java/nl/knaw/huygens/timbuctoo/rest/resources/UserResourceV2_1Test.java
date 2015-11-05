package nl.knaw.huygens.timbuctoo.rest.resources;

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

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.mail.MailSender;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import org.junit.Ignore;

import javax.ws.rs.core.MediaType;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.ADMIN_ROLE;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.USER_ROLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Ignore
public class UserResourceV2_1Test extends UserResourceTest {
  @Override
  protected String getAPIVersion() {
    return Paths.V2_1_PATH;
  }

  @Override
  public void testPutUser() {
    setVREExist(VRE_ID, true);
    setupUserWithRoles(VRE_ID, "otherUserId", ADMIN_ROLE);
    MailSender sender = injector.getInstance(MailSender.class);

    User user = createUser(USER_ID, "firstName", "lastName");
    user.setEmail("test@test.com");

    User original = createUser(USER_ID, "test", "test");
    original.setEmail("test@test.com");

    when(userConfigurationHandler.getUser(USER_ID)).thenReturn(original);

    ClientResponse response = createResource(USER_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .put(ClientResponse.class, user);
    verifyResponseStatus(response, Status.OK);

    Class<User> type = User.class;
    verifyReturnedEntity(type, response.getEntity(type), USER_ID);

    verifySendEmailToTheUser(sender);
  }

  private <T extends SystemEntity> void verifyReturnedEntity(Class<T> type, T entity, String expectedId) {
    assertThat(entity, is(notNullValue(type)));
    assertThat(entity.getId(), is(equalTo(expectedId)));
  }

  @Override
  public void testPutVREAuthorizationAsAdmin() throws Exception {
    setVREExist(VRE_ID, true);
    setupUserWithRoles(VRE_ID, OTHER_USER_ID, ADMIN_ROLE);

    VREAuthorization example = new VREAuthorization(VRE_ID, USER_ID);
    VREAuthorization authorization = new VREAuthorization(VRE_ID, USER_ID, USER_ROLE);
    when(userConfigurationHandler.findVREAuthorization(example)).thenReturn(authorization);

    ClientResponse response = createResource(USER_ID, VREAUTHORIZATIONS_PATH, VRE_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header("VRE_ID", VRE_ID) //
        .put(ClientResponse.class, authorization);

    verifyResponseStatus(response, Status.OK);

    Class<VREAuthorization> type = VREAuthorization.class;
    VREAuthorization returnedAuthorization = response.getEntity(type);

    assertThat(returnedAuthorization, is(notNullValue(type)));
    assertThat(returnedAuthorization.getUserId(), is(equalTo(USER_ID)));

    verify(userConfigurationHandler).updateVREAuthorization(authorization);
  }

  protected void verifySendEmailToTheUser(MailSender sender) {
    verify(sender).sendMail(anyString(), anyString(), anyString());
  }
}

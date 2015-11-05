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

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import nl.knaw.huygens.security.client.AuthenticationHandler;
import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.security.client.filters.SecurityResourceFilterFactory;
import nl.knaw.huygens.security.client.model.HuygensSecurityInformation;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.rest.config.ServletInjectionModelHelper;
import nl.knaw.huygens.timbuctoo.rest.filters.UserResourceFilterFactory;
import nl.knaw.huygens.timbuctoo.rest.filters.VREAuthorizationFilterFactory;
import nl.knaw.huygens.timbuctoo.security.UserConfigurationHandler;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.security.Principal;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*
 * Abstract class names should not end with Test, 
 * because the asIterable script tries to execute all the classes with a Test suffix.
 */
public abstract class WebServiceTestSetup extends JerseyTest {

  protected static final String USER_ID = "USER000000001";
  protected static final String VRE_ID = "vreID";
  protected static final String CREDENTIALS = "bearer 12333322abef";

  protected static Injector injector;

  protected VRECollection vreCollection;
  protected Repository repository;
  protected UserConfigurationHandler userConfigurationHandler;

  private static ResourceTestModule resourceTestModule;

  public WebServiceTestSetup() {
    super(new GuiceTestContainerFactory(injector));
  }

  @BeforeClass
  public static void setupClass() {
    initLogger();
    resourceTestModule = new ResourceTestModule();
    injector = Guice.createInjector(resourceTestModule);
  }

  @Before
  public void setupJsonFileWriter() {
    userConfigurationHandler = injector.getInstance(UserConfigurationHandler.class);
  }

  @Before
  public void setupRepository() {
    repository = injector.getInstance(Repository.class);
  }

  @Before
  public void setUpVRECollection() {
    vreCollection = injector.getInstance(VRECollection.class);
  }

  /**
   * Return the version of the api to test.
   */
  protected String getAPIVersion() {
    return "";
  }

  /**
   * Bridges java.util.logging to SLF4J.
   * See: http://blog.cn-consult.dk/2009/03/bridging-javautillogging-to-slf4j.html
   */
  public static void initLogger() {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  @Before
  public void setUpAuthorizationHandler() throws UnauthorizedException {
    HuygensSecurityInformation securityInformation = new HuygensSecurityInformation();
    securityInformation.setDisplayName(USER_ID);
    securityInformation.setDisplayName("test");
    securityInformation.setPrincipal(mock(Principal.class));

    AuthenticationHandler authorizationHandler = injector.getInstance(AuthenticationHandler.class);
    when(authorizationHandler.getSecurityInformation(anyString())).thenReturn(securityInformation);
  }

  protected void setupUserWithRoles(String vreId, String userId, String... roles) {
    User user = new User();
    user.setId(userId);
    when(userConfigurationHandler.getUser(USER_ID)).thenReturn(user);
    when(userConfigurationHandler.findUser(user)).thenReturn(user);

    VREAuthorization example = new VREAuthorization(vreId, userId);
    VREAuthorization authorization = new VREAuthorization(vreId, userId, roles);
    when(userConfigurationHandler.findVREAuthorization(example)).thenReturn(authorization);
  }

  @After
  public void tearDownMocks() {
    // Reset the mocked object so they influence no tests after the current.
    resourceTestModule.cleanUpMocks();
  }

  @Override
  protected AppDescriptor configure() {
    WebAppDescriptor webAppDescriptor = new WebAppDescriptor.Builder().build();
    webAppDescriptor.getInitParams().put(PackagesResourceConfig.PROPERTY_PACKAGES,
        "nl.knaw.huygens.timbuctoo.rest.resources;com.fasterxml.jackson.jaxrs.json;nl.knaw.huygens.timbuctoo.rest.providers;nl.knaw.huygens.facetedsearch.serialization.providers;");
    webAppDescriptor.getInitParams().put(
        ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
        ServletInjectionModelHelper.getClassNamesString(SecurityResourceFilterFactory.class, VREAuthorizationFilterFactory.class, UserResourceFilterFactory.class,
            RolesAllowedResourceFilterFactory.class));
    webAppDescriptor.getInitParams().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, LoggingFilter.class.getName());
    webAppDescriptor.getInitParams().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, LoggingFilter.class.getName());

    return webAppDescriptor;
  }

  protected JacksonJsonProvider getJsonProvider() {
    return injector.getInstance(JacksonJsonProvider.class);
  }

  protected void makeVREAvailable(VRE vre, String vreId) {
    setVREExist(vreId, true);
    when(vreCollection.getVREById(vreId)).thenReturn(vre);
  }

  /**
   * A method to get past the {@code VREAuthorizationFilter}.
   * @param vreId the id to match the VRE.
   * @param vreExists {@code true} when the VRE has to exists {@code false} if not.
   */
  protected void setVREExist(String vreId, boolean vreExists) {
    when(vreCollection.doesVREExist(vreId)).thenReturn(vreExists);
  }

  @SuppressWarnings("unchecked")
  protected void setUserNotLoggedIn() {
    try {
      AuthenticationHandler authorizationHandler = injector.getInstance(AuthenticationHandler.class);
      when(authorizationHandler.getSecurityInformation(anyString())).thenThrow(UnauthorizedException.class);
    } catch (UnauthorizedException ex) {
      //Do nothing
    }
  }

  protected WebResource addPathToWebResource(WebResource resource, String... pathElements) {
    for (String pathElement : pathElements) {
      resource = resource.path(pathElement);
    }
    return resource;
  }

  /**
   * Verifies that the reponse has the expected status.
   */
  protected void verifyResponseStatus(ClientResponse response, Status expectedStatus) {
    Assert.assertEquals(expectedStatus, response.getStatusInfo());
  }

}

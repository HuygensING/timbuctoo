package nl.knaw.huygens.timbuctoo.rest.resources;

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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;

import nl.knaw.huygens.security.client.AuthorizationHandler;
import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.security.client.filters.SecurityResourceFilterFactory;
import nl.knaw.huygens.security.client.model.HuygensSecurityInformation;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.rest.config.ServletInjectionModelHelper;
import nl.knaw.huygens.timbuctoo.rest.filters.UserResourceFilterFactory;
import nl.knaw.huygens.timbuctoo.rest.filters.VREAuthorizationFilterFactory;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

/* abstract class names should not end with Test, 
 * because the build script tries to execute all the classes with a Test postfix. 
 */
public abstract class WebServiceTestSetup extends JerseyTest {

  protected static final String USER_ID = "USER000000001";
  protected static final String VRE_ID = "vreID";
  protected static Injector injector;

  // Needed practically always
  protected StorageManager storageManager;

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
  public void setupRepository() {
    storageManager = injector.getInstance(StorageManager.class);
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

    AuthorizationHandler authorizationHandler = injector.getInstance(AuthorizationHandler.class);
    when(authorizationHandler.getSecurityInformation(anyString())).thenReturn(securityInformation);
  }

  protected void setupUserWithRoles(String vreId, String userId, String... roles) {
    User user = new User();
    user.setId(userId);
    when(storageManager.findEntity(User.class, user)).thenReturn(user);

    VREAuthorization example = new VREAuthorization(vreId, userId);
    VREAuthorization authorization = new VREAuthorization(vreId, userId, roles);
    when(storageManager.findEntity(VREAuthorization.class, example)).thenReturn(authorization);
  }

  @After
  public void tearDownMocks() {
    // Reset the mocked object so they influence no tests after the current.
    resourceTestModule.cleanUpMocks();
  }

  @Override
  protected AppDescriptor configure() {
    WebAppDescriptor webAppDescriptor = new WebAppDescriptor.Builder().build();
    webAppDescriptor.getInitParams()
        .put(PackagesResourceConfig.PROPERTY_PACKAGES, "nl.knaw.huygens.timbuctoo.rest.resources;com.fasterxml.jackson.jaxrs.json;nl.knaw.huygens.timbuctoo.rest.providers");
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

  protected WebResource domainResource(String... pathElements) {
    WebResource resource = resource().path(Paths.DOMAIN_PREFIX);
    for (String pathElement : pathElements) {
      resource = resource.path(pathElement);
    }
    return resource;
  }

  protected void makeVREAvailable(VRE vre, String vreId) {
    VREManager vreManager = injector.getInstance(VREManager.class);
    when(vreManager.doesVREExist(vreId)).thenReturn(true);
    when(vreManager.getVREById(vreId)).thenReturn(vre);
  }

  /**
   * A method to get past the {@code VREAuthorizationFilter}.
   * @param vreId the id to match the VRE.
   * @param vreExists {@code true} when the VRE has to exists {@code false} if not.
   */
  protected void setUpVREManager(String vreId, boolean vreExists) {
    VREManager vreManager = injector.getInstance(VREManager.class);
    when(vreManager.doesVREExist(vreId)).thenReturn(vreExists);
  }

  @SuppressWarnings("unchecked")
  protected void setUserNotLoggedIn() {
    try {
      AuthorizationHandler authorizationHandler = injector.getInstance(AuthorizationHandler.class);
      when(authorizationHandler.getSecurityInformation(anyString())).thenThrow(UnauthorizedException.class);
    } catch (UnauthorizedException ex) {
      //Do nothing
    }
  }

}

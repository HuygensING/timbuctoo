package nl.knaw.huygens.timbuctoo.rest.filters;

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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.rest.filters.VREAuthorizationFilterFactory.VREAuthorizationResourceFilter;
import nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.spi.container.ContainerRequest;

public class VREAuthorizationResourceFilterTest {

  private static final String VRE_ID = "testVRE";
  private VREAuthorizationResourceFilter instance;
  private VREManager vreManager;

  @Before
  public void setUp() {
    vreManager = mock(VREManager.class);
    instance = new VREAuthorizationResourceFilter(vreManager);
  }

  @After
  public void tearDown() {
    vreManager = null;
    instance = null;
  }

  @Test
  public void testFilterValidVREId() {
    ContainerRequest request = setupRequestForDomainEntities(VRE_ID);

    setUpVREManager(VRE_ID, true);

    instance.filter(request);

    verify(vreManager, only()).doesVREExist(VRE_ID);
  }

  @Test(expected = TimbuctooException.class)
  public void testFilterNoVREIdSent() {
    ContainerRequest request = setupRequestForDomainEntities(null);

    try {
      instance.filter(request);
    } catch (TimbuctooException e) {
      assertEquals(Status.UNAUTHORIZED.getStatusCode(), e.getResponse().getStatus());
      verifyZeroInteractions(vreManager);
      throw e;
    }
  }

  @Test(expected = TimbuctooException.class)
  public void testFilterUnknownVRE() {
    ContainerRequest request = setupRequestForDomainEntities(VRE_ID);
    setUpVREManager(VRE_ID, false);

    try {
      instance.filter(request);
    } catch (TimbuctooException e) {
      assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
      verify(vreManager, only()).doesVREExist(VRE_ID);
      throw e;
    }
  }

  private ContainerRequest setupRequestForDomainEntities(String vreId) {
    ContainerRequest request = mock(ContainerRequest.class);
    when(request.getHeaderValue(CustomHeaders.VRE_ID_KEY)).thenReturn(vreId);
    return request;
  }

  private void setUpVREManager(String vreId, boolean vreExists) {
    when(vreManager.doesVREExist(vreId)).thenReturn(vreExists);
  }

}

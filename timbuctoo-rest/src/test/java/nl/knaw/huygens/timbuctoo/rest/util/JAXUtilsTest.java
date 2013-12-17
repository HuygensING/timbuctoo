package nl.knaw.huygens.timbuctoo.rest.util;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.timbuctoo.annotations.APIDesc;
import nl.knaw.huygens.timbuctoo.rest.util.JAXUtils.API;

import org.junit.Test;

public class JAXUtilsTest {

  private static final String PATH = "element/sub";

  private static class ClassWithoutPath {

  }

  @Path(PATH)
  private static class ClassWithPath1 {
    @GET
    @Path("/all")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
    public String getMethod() {
      return "result";
    }
  }

  @Path("/" + PATH)
  private static class ClassWithPath2 {
    @PUT
    @Path("/{xx: [a-zA-Z][a-zA-Z][a-zA-Z]\\d+}")
    @APIDesc("description")
    public void putMethod(String json) throws IOException {}
  }

  @Test
  public void testPathValue() {
    assertEquals("", JAXUtils.pathValueOf(ClassWithoutPath.class));
    assertEquals(PATH, JAXUtils.pathValueOf(ClassWithPath1.class));
    assertEquals(PATH, JAXUtils.pathValueOf(ClassWithPath2.class));
  }

  @Test
  public void testGenerateAPIs1() {
    List<API> list = JAXUtils.generateAPIs(ClassWithPath1.class);
    assertEquals(1, list.size());
    API api = list.get(0);
    assertEquals(PATH + "/all", api.path);
    assertEquals("", api.desc);
    assertNotNull(api.mediaTypes);
    assertEquals(2, api.mediaTypes.size());
    assertTrue(api.mediaTypes.contains(MediaType.APPLICATION_JSON));
    assertTrue(api.mediaTypes.contains(MediaType.TEXT_HTML));
  }

  @Test
  public void testGenerateAPIs2() {
    List<API> list = JAXUtils.generateAPIs(ClassWithPath2.class);
    assertEquals(1, list.size());
    API api = list.get(0);
    assertEquals(PATH + "/{xx}", api.path);
    assertEquals("description", api.desc);
    assertNotNull(api.mediaTypes);
    assertEquals(0, api.mediaTypes.size());
  }

}

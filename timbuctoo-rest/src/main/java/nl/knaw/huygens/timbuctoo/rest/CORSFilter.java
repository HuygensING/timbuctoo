package nl.knaw.huygens.timbuctoo.rest;

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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.StringUtils;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

public class CORSFilter implements ContainerResponseFilter {

  @Override
  public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
    String origin = request.getHeaderValue("Origin");
    if (origin != null) {
      ResponseBuilder resp = Response.fromResponse(response.getResponse());
      // Use the * to allow every Origin.
      resp.header("Access-Control-Allow-Origin", "*");

      //Done by a lot of examples. I am not sure this is the right way.
      resp.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");

      String reqHead = request.getHeaderValue("Access-Control-Request-Headers");
      if (!StringUtils.isBlank(reqHead)) {
        resp.header("Access-Control-Allow-Headers", reqHead);
      }

      //Needed so the VRE can access the Location when an object  is created.
      resp.header("Access-Control-Expose-Headers", "Location, Link");

      response.setResponse(resp.build());
    }
    return response;
  }

}

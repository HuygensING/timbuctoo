package nl.knaw.huygens.repository;

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

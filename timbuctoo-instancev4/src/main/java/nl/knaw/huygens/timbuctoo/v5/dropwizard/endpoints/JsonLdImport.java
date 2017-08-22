package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;

@Path("/v5/{user}/{dataset}/upload/jsonld")
public class JsonLdImport {

  @PUT
  public void submitChanges(nl.knaw.huygens.timbuctoo.v5.jsonldimport.JsonLdImport jsonLdImport) {

  }
}

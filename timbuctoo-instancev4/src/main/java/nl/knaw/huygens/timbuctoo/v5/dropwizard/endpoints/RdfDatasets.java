package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.v5.datastores.DataStoreFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Path("/v4/dataSets/all")
public class RdfDatasets {

  private final DataStoreFactory dataStoreFactory;

  public RdfDatasets(DataStoreFactory dataStoreFactory) {
    this.dataStoreFactory = dataStoreFactory;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Set<String> getDataSets() {
    return dataStoreFactory.getDataSets();
  }
}

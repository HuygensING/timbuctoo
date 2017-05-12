package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.v5.datastores.DataSetManager;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.io.IOException;
import java.net.URI;

@Path("/v4/{dataSet}/prefixes")
public class Prefixes {

  private final DataSetManager dataSetManager;

  private Prefixes(DataSetManager dataSetManager) {
    this.dataSetManager = dataSetManager;
  }

  @POST
  public void setPrefix(
    @FormParam("prefix") String prefix,
    @FormParam("uri") URI uri,
    @PathParam("dataSet") String dataSet) throws IOException {
    dataSetManager.getDataStores(dataSet).getTypeNameStore().addPrefix(prefix, uri.toString());
  }
}

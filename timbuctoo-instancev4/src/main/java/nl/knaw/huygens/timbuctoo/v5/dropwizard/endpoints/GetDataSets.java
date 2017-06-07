package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Path("/v5/dataSets/{userId}")
public class GetDataSets {
  private final DataSetFactory dataSetFactory;
  private final GraphQl graphQlEndpoint;

  public GetDataSets(DataSetFactory dataSetFactory, GraphQl graphQlEndpoint) {
    this.dataSetFactory = dataSetFactory;
    this.graphQlEndpoint = graphQlEndpoint;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, URI> getDataSets(@PathParam("userId") String userId) {
    return dataSetFactory
      .getDataSets()
      .getOrDefault(userId, new HashSet<>())
      .stream()
      .map(dataSetId -> Tuple.tuple(dataSetId, graphQlEndpoint.makeUrl(userId, dataSetId)))
      .collect(toMap(Tuple::getLeft, Tuple::getRight));
  }
}

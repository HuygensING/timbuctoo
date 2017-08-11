package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetFactory;
import nl.knaw.huygens.timbuctoo.v5.dataset.PromotedDataSet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

@Path("/v5/dataSets/")
public class GetDataSets {
  private final DataSetFactory dataSetFactory;
  private final GraphQl graphQlEndpoint;

  public GetDataSets(DataSetFactory dataSetFactory, GraphQl graphQlEndpoint) {
    this.dataSetFactory = dataSetFactory;
    this.graphQlEndpoint = graphQlEndpoint;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Map<String, URI>> getDataSets() {
    Map<String, Set<PromotedDataSet>> dataSets = dataSetFactory.getDataSets();
    Map<String, Map<String, URI>> dataSetUris = new HashMap<>();

    for (Map.Entry<String, Set<PromotedDataSet>> userDataSets : dataSets.entrySet()) {
      Map<String, URI> mappedUserSets = userDataSets.getValue()
                                                    .stream()
                                                    .map(dataSetEntry -> Tuple.tuple(
                                                      dataSetEntry.getName(),
                                                      graphQlEndpoint.makeUrl(userDataSets.getKey(),
                                                      dataSetEntry.getName())
                                                    ))
                                                    .collect(toMap(Tuple::getLeft, Tuple::getRight));
      dataSetUris.put(userDataSets.getKey(), mappedUserSets);
    }


    return dataSetUris;
  }

  @GET
  @Path("promoted")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Map<String, URI>> getPromotedDataSets() {
    Map<String, Set<PromotedDataSet>> dataSets = dataSetFactory.getPromotedDataSets();
    Map<String, Map<String, URI>> dataSetUris = new HashMap<>();

    for (Map.Entry<String, Set<PromotedDataSet>> userDataSets : dataSets.entrySet()) {
      Map<String, URI> mappedUserSets = userDataSets.getValue()
                                                    .stream()
                                                    .map(dataSetEntry -> Tuple.tuple(
                                                      dataSetEntry.getName(),
                                                      graphQlEndpoint.makeUrl(userDataSets.getKey(),
                                                        dataSetEntry.getName())
                                                    ))
                                                    .collect(toMap(Tuple::getLeft, Tuple::getRight));
      dataSetUris.put(userDataSets.getKey(), mappedUserSets);
    }


    return dataSetUris;
  }

  @GET
  @Path("{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, URI> getUserDataSets(@PathParam("userId") String userId) {
    return dataSetFactory
      .getDataSets()
      .getOrDefault(userId, new HashSet<>())
      .stream()
      .map(dataSetEntry -> Tuple.tuple(dataSetEntry.getName(), graphQlEndpoint.makeUrl(userId, dataSetEntry.getName())))
      .collect(toMap(Tuple::getLeft, Tuple::getRight));
  }
}
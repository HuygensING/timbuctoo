package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.security.LoggedInUsers;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetWithRoles;
import nl.knaw.huygens.timbuctoo.v5.dataset.PromotedDataSet;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

@Path("/v5/dataSets/")
public class GetDataSets {
  private final DataSetRepository dataSetRepository;
  private final GraphQl graphQlEndpoint;
  private final LoggedInUsers loggedInUsers;

  public GetDataSets(DataSetRepository dataSetRepository, GraphQl graphQlEndpoint, LoggedInUsers loggedInUsers) {
    this.dataSetRepository = dataSetRepository;
    this.graphQlEndpoint = graphQlEndpoint;
    this.loggedInUsers = loggedInUsers;
  }


  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Map<String, URI>> getDataSets() {
    Map<String, Set<PromotedDataSet>> dataSets = dataSetRepository.getDataSets();
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
    Map<String, Set<PromotedDataSet>> dataSets = dataSetRepository.getPromotedDataSets();
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
    return dataSetRepository
      .getDataSets()
      .getOrDefault(userId, new HashSet<>())
      .stream()
      .map(dataSetEntry -> Tuple.tuple(dataSetEntry.getName(), graphQlEndpoint.makeUrl(userId, dataSetEntry.getName())))
      .collect(toMap(Tuple::getLeft, Tuple::getRight));
  }

  @GET
  @Path("me/writeAccess")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getDataSetsWithWriteAccess(@HeaderParam("Authorization") String
                                               authHeader) {

    Map<String, Set<DataSetWithRoles>> dataSetsDisplay = new HashMap<>();

    Optional<User> user = loggedInUsers.userFor(authHeader);

    if (user.isPresent()) {
      String userId = user.get().getPersistentId();
      Map<String, Set<DataSetWithRoles>> dataSets = dataSetRepository.getDataSetsWithWriteAccess(userId);

      for (Map.Entry<String, Set<DataSetWithRoles>> userDataSets : dataSets.entrySet()) {
        Set<DataSetWithRoles> dataSetInfo = new HashSet<>();
        userDataSets.getValue().forEach(dataSet -> {
          dataSet.setUri(graphQlEndpoint.makeUrl(userDataSets.getKey(),
            dataSet.getName()));
          dataSetInfo.add(dataSet);
        });
        dataSetsDisplay.put(userDataSets.getKey(), dataSetInfo);
      }

      return Response.ok(dataSetsDisplay).build();
    } else {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

  }
}

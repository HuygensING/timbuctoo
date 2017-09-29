package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleepycat.je.DatabaseException;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import nl.knaw.huygens.timbuctoo.security.LoggedInUsers;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.QueryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static graphql.ExecutionInput.newExecutionInput;

@Path("/v5/graphql")
public class RootGraphQl {

  private static final Logger LOG = LoggerFactory.getLogger(GraphQl.class);


  private final Supplier<GraphQLSchema> graphqlGetter;
  private final LoggedInUsers loggedInUsers;
  private final ObjectMapper objectMapper;
  private GraphQL graphQl;
  private GraphQLSchema prevGraphQlSchema;

  public RootGraphQl(Supplier<GraphQLSchema> graphqlGetter, LoggedInUsers loggedInUsers)
    throws DatabaseException, RdfProcessingFailedException {
    this.graphqlGetter = graphqlGetter;
    this.loggedInUsers = loggedInUsers;
    objectMapper = new ObjectMapper();
  }

  @POST
  @Consumes("application/json")
  public Response postJson(JsonNode body, @QueryParam("query") String query,
                           @HeaderParam("accept") String acceptHeader,
                           @HeaderParam("Authorization") String authHeader) {
    final String queryFromBody;
    if (body.has("query")) {
      queryFromBody = body.get("query").asText();
    } else {
      queryFromBody = null;
    }
    Map variables = null;
    if (body.has("variables")) {
      try {
        variables = objectMapper.treeToValue(body.get("variables"), HashMap.class);
      } catch (JsonProcessingException e) {
        return Response
          .status(400)
          .entity("'variables' should be an object node")
          .build();
      }
    }
    final String operationName = body.has("operationName") ? body.get("operationName").asText() : null;

    return executeGraphql(query, acceptHeader, queryFromBody, variables, operationName, authHeader);
  }

  @POST
  @Consumes("application/graphql")
  public Response postGraphql(String query, @QueryParam("query") String queryParam,
                              @HeaderParam("accept") String acceptHeader,
                              @HeaderParam("Authorization") String authHeader) {
    return executeGraphql(queryParam, acceptHeader, query, null, null, authHeader);
  }

  @GET
  public Response get(@QueryParam("query") String query, @HeaderParam("accept") String acceptHeader,
                      @HeaderParam("Authorization") String authHeader) {
    return executeGraphql(null, acceptHeader, query, null, null, authHeader);
  }


  public Response executeGraphql(String query, String acceptHeader, String queryFromBody, Map variables,
                                 String operationName, String authHeader) {

    if (unSpecifiedAcceptHeader(acceptHeader)) {
      return Response
        .status(400)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .entity("{\"errors\": [\"Please specify a mimetype in the accept header. For example: application/ld+json\"]}")
        .build();
    }
    if (query != null && queryFromBody != null) {
      return Response
        .status(400)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .entity("{\"errors\": [\"There's both a query as url paramatere and a query in the body. Please pick one.\"]}")
        .build();
    }
    if (query == null && queryFromBody == null) {
      return Response
        .status(400)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .entity("{\"errors\": [\"Please provide the graphql query as the query property of a JSON encoded object. " +
          "E.g. {query: \\\"{\\n  persons {\\n ... \\\"}\"]}")
        .build();
    }
    GraphQLSchema graphQlSchema = graphqlGetter.get();
    if (graphQlSchema != prevGraphQlSchema) {
      prevGraphQlSchema = graphQlSchema;
      graphQl = GraphQL
        .newGraphQL(graphQlSchema)
        .build();
    }
    final ExecutionResult result = graphQl
      .execute(newExecutionInput()
        .root(new QueryType.RootData(loggedInUsers.userFor(authHeader)))
        .query(queryFromBody)
        .operationName(operationName)
        .variables(variables == null ? Collections.emptyMap() : variables)
        .build());
    return Response
      .ok()
      .type(MediaType.APPLICATION_JSON_TYPE)
      .entity(result.toSpecification())
      .build();
  }


  public boolean unSpecifiedAcceptHeader(@HeaderParam("accept") String acceptHeader) {
    return acceptHeader == null || acceptHeader.isEmpty() || "*/*".equals(acceptHeader);
  }

}

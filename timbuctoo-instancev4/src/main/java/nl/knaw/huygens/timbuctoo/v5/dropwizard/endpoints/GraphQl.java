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
import nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes.SerializerWriter;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes.SerializerWriterRegistry;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.RootData;
import nl.knaw.huygens.timbuctoo.v5.graphql.serializable.SerializerExecutionStrategy;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableResult;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static graphql.ExecutionInput.newExecutionInput;

@Path("/v5/graphql")
public class GraphQl {
  private final Supplier<GraphQLSchema> graphqlGetter;
  private final SerializerWriterRegistry serializerWriterRegistry;
  private final LoggedInUsers loggedInUsers;
  private final ObjectMapper objectMapper;
  private GraphQL graphQl;
  private GraphQLSchema prevGraphQlSchema;

  public GraphQl(Supplier<GraphQLSchema> graphqlGetter, SerializerWriterRegistry serializerWriterRegistry,
                 LoggedInUsers loggedInUsers)
    throws DatabaseException, RdfProcessingFailedException {
    this.graphqlGetter = graphqlGetter;
    this.serializerWriterRegistry = serializerWriterRegistry;
    this.loggedInUsers = loggedInUsers;
    objectMapper = new ObjectMapper();
  }

  @POST
  @Consumes("application/json")
  public Response postJson(JsonNode body, @QueryParam("query") String query,
                           @HeaderParam("accept") String acceptHeader,
                           @QueryParam("accept") String acceptParam,
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
    final String operationName = body.has("operationName") && !body.get("operationName").isNull() ?
      body.get("operationName").asText() :
      null;

    return executeGraphql(query, acceptHeader, acceptParam, queryFromBody, variables, operationName, authHeader);
  }

  @POST
  @Consumes("application/graphql")
  public Response postGraphql(String query, @QueryParam("query") String queryParam,
                              @HeaderParam("accept") String acceptHeader,
                              @QueryParam("accept") String acceptParam,
                              @HeaderParam("Authorization") String authHeader) {
    return executeGraphql(queryParam, acceptHeader, acceptParam, query, null, null, authHeader);
  }

  @GET
  public Response get(@QueryParam("query") String query, @HeaderParam("accept") String acceptHeader,
                      @QueryParam("accept") String acceptParam,
                      @HeaderParam("Authorization") String authHeader) {
    return executeGraphql(null, acceptHeader, acceptParam, query, null, null, authHeader);
  }


  public Response executeGraphql(String query, String acceptHeader, String acceptParam, String queryFromBody,
                                 Map variables, String operationName, String authHeader) {

    final SerializerWriter serializerWriter;
    if (acceptParam != null && !acceptParam.isEmpty()) {
      acceptHeader = acceptParam; //Accept param overrules header because it's more under the user's control
    }
    if (unSpecifiedAcceptHeader(acceptHeader) || acceptHeader.equals(MediaType.APPLICATION_JSON)) {
      serializerWriter = null;
    } else {
      Optional<SerializerWriter> bestMatch = serializerWriterRegistry.getBestMatch(acceptHeader);
      if (bestMatch.isPresent()) {
        serializerWriter = bestMatch.get();
      } else {
        return Response
          .status(415)
          .type(MediaType.APPLICATION_JSON_TYPE)
          .entity("{\"errors\": [\"The available mediatypes are: " +
            String.join(", ", serializerWriterRegistry.getSupportedMimeTypes()) + "\"]}")
          .build();
      }
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
      final GraphQL.Builder builder = GraphQL
        .newGraphQL(graphQlSchema);
      if (!acceptHeader.equals(MediaType.APPLICATION_JSON)) {
        builder.queryExecutionStrategy(new SerializerExecutionStrategy());
      }
      graphQl = builder
        .build();
    }
    final ExecutionResult result = graphQl
      .execute(newExecutionInput()
        .root(new RootData(loggedInUsers.userFor(authHeader)))
        .query(queryFromBody)
        .operationName(operationName)
        .variables(variables == null ? Collections.emptyMap() : variables)
        .build());
    if (serializerWriter == null) {
      return Response
        .ok()
        .type(MediaType.APPLICATION_JSON_TYPE)
        .entity(result.toSpecification())
        .build();
    } else {
      return Response
        .ok()
        .type(serializerWriter.getMimeType())
        .entity((StreamingOutput) os -> {
          serializerWriter.getSerializationFactory().create(os).serialize(new SerializableResult(result.getData()));
        })
        .build();
    }
  }


  public boolean unSpecifiedAcceptHeader(@HeaderParam("accept") String acceptHeader) {
    return acceptHeader == null || acceptHeader.isEmpty() || "*/*".equals(acceptHeader);
  }

}

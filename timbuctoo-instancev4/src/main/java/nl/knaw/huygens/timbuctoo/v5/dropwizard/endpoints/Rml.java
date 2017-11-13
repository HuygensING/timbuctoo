package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import io.dropwizard.jersey.params.UUIDParam;
import nl.knaw.huygens.timbuctoo.rml.LoggingErrorHandler;
import nl.knaw.huygens.timbuctoo.rml.dto.Quad;
import nl.knaw.huygens.timbuctoo.rml.jena.JenaBasedReader;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportStatus;
import nl.knaw.huygens.timbuctoo.v5.dataset.PlainRdfCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;
import nl.knaw.huygens.timbuctoo.v5.rml.RdfDataSourceFactory;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.UserValidationException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.ErrorResponseHelper.handleImportManagerResult;

@Path("/v5/{userId}/{dataSetId}/rml")
public class Rml {
  private final DataSetRepository dataSetRepository;
  private final ErrorResponseHelper errorResponseHelper;
  private final JenaBasedReader rmlBuilder = new JenaBasedReader();
  private final UserValidator userValidator;

  public Rml(DataSetRepository dataSetRepository, ErrorResponseHelper errorResponseHelper,
             UserValidator userValidator) {
    this.dataSetRepository = dataSetRepository;
    this.errorResponseHelper = errorResponseHelper;
    this.userValidator = userValidator;
  }

  @POST
  public Response upload(final String rdfData,
                         @PathParam("userId") final String ownerId,
                         @PathParam("dataSetId") final String dataSetId,
                         @HeaderParam("authorization") String authHeader)
    throws DataStoreCreationException, LogStorageFailedException, ExecutionException, InterruptedException {
    Optional<User> user;
    try {
      user = userValidator.getUserFromAccessToken(authHeader);
    } catch (UserValidationException e) {
      user = Optional.empty();
    }
    final Optional<DataSet> dataSet = dataSetRepository.getDataSet(user.get().getPersistentId(),ownerId, dataSetId);
    if (dataSet.isPresent()) {
      ImportManager importManager = dataSet.get().getImportManager();
      RdfDataSourceFactory dataSourceFactory = dataSet.get().getDataSource();

      final Model model = ModelFactory.createDefaultModel();
      try {
        model.read(new ByteArrayInputStream(rdfData.getBytes(StandardCharsets.UTF_8)), null, "JSON-LD");
      } catch (Exception e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
      }

      final RmlMappingDocument rmlMappingDocument = rmlBuilder.fromRdf(
        model,
        rdfResource -> dataSourceFactory.apply(rdfResource, dataSetId + "_" + ownerId)//fixme remove vreName from here
      );
      if (rmlMappingDocument.getErrors().size() > 0) {
        return Response.status(Response.Status.BAD_REQUEST)
          .entity("failure: " + String.join("\nfailure: ", rmlMappingDocument.getErrors()) + "\n")
          .build();
      }
      //FIXME: trigger onprefix for all rml prefixes
      //FIXME: store rml and retrieve it from tripleStore when mapping
      final String baseUri = dataSet.get().getMetadata().getBaseUri();
      Future<ImportStatus> promise = importManager.generateLog(
        baseUri,
        baseUri,
        new RmlRdfCreator(rmlMappingDocument, baseUri)
      );
      return handleImportManagerResult(promise);
    } else {
      return errorResponseHelper.dataSetNotFound(ownerId, dataSetId);
    }
  }

  @GET
  @Path("{importId}")
  public Response getStatus(@PathParam("importId") final UUIDParam importId) {
    Optional<String> status = dataSetRepository.getStatus(importId.get());

    if (status.isPresent()) {
      return Response.ok(status).build();
    }

    return Response.status(Response.Status.NOT_FOUND).build();
  }

  private class RmlRdfCreator implements PlainRdfCreator {
    private final RmlMappingDocument rmlMappingDocument;
    private final String baseUri;

    public RmlRdfCreator(RmlMappingDocument rmlMappingDocument, String baseUri) {
      this.rmlMappingDocument = rmlMappingDocument;
      this.baseUri = baseUri;
    }

    @Override
    public void sendQuads(RdfSerializer saver) throws LogStorageFailedException {
      Stream<Quad> triples = rmlMappingDocument.execute(new LoggingErrorHandler());
      Iterator<Quad> iterator = triples.iterator();
      while (iterator.hasNext()) {
        Quad triple = iterator.next();
        saver.onQuad(
          triple.getSubject().getUri().get(),
          triple.getPredicate().getUri().get(),
          triple.getObject().getContent(),
          triple.getObject().getLiteralType().orElse(null),
          triple.getObject().getLiteralLanguage().orElse(null),
          baseUri
        );
      }
    }
  }
}

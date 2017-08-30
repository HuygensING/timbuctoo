package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import io.dropwizard.jersey.params.UUIDParam;
import nl.knaw.huygens.timbuctoo.rml.jena.JenaBasedReader;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.LoggingErrorHandler;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetFactory;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfCreator;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;
import nl.knaw.huygens.timbuctoo.v5.rml.RdfDataSourceFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

@Path("/v5/{userId}/{dataSetId}/rml")
public class Rml {
  private final DataSetFactory dataSetFactory;
  private final JenaBasedReader rmlBuilder = new JenaBasedReader();

  public Rml(DataSetFactory dataSetFactory) {
    this.dataSetFactory = dataSetFactory;
  }

  @POST
  public Response upload(final String rdfData,
                         @PathParam("userId") final String ownerId,
                         @PathParam("dataSetId") final String dataSetId)
    throws DataStoreCreationException, LogStorageFailedException, ExecutionException, InterruptedException {
    ImportManager importManager = dataSetFactory.createImportManager(ownerId, dataSetId);
    RdfDataSourceFactory dataSourceFactory = dataSetFactory.createDataSource(ownerId, dataSetId);

    final Model model = ModelFactory.createDefaultModel();
    model.read(new ByteArrayInputStream(rdfData.getBytes(StandardCharsets.UTF_8)), null, "JSON-LD");
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
    String graph = "http://aasad" + UUID.randomUUID();  //FIXME:
    Future<?> future = importManager.generateLog(
      URI.create(graph),
      new RdfCreator() {
        @Override
        public void sendQuads(RdfSerializer saver) throws LogStorageFailedException {
          Stream<Triple> triples = rmlMappingDocument.execute(new LoggingErrorHandler());
          Iterator<Triple> iterator = triples.iterator();
          while (iterator.hasNext()) {
            Triple triple = iterator.next();
            boolean isLiteral = triple.getObject().isLiteral();
            saver.onQuad(
              triple.getSubject().toString(),
              triple.getPredicate().toString(),
              isLiteral ? triple.getObject().getLiteral().getLexicalForm() : triple.getObject().toString(),
              isLiteral ? triple.getObject().getLiteralDatatypeURI() : null,
              isLiteral ? triple.getObject().getLiteralLanguage() : null,
              graph
            );
          }
        }
      }
    );
    future.get();
    return Response.noContent().build();
  }

  @GET
  @Path("{importId}")
  public Response getStatus(@PathParam("importId") final UUIDParam importId) {
    Optional<String> status = dataSetFactory.getStatus(importId.get());

    if (status.isPresent()) {
      return Response.ok(status).build();
    }

    return Response.status(Response.Status.NOT_FOUND).build();
  }

}

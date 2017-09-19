package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import io.dropwizard.jersey.params.UUIDParam;
import nl.knaw.huygens.timbuctoo.rml.dto.Quad;
import nl.knaw.huygens.timbuctoo.rml.jena.JenaBasedReader;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument;
import nl.knaw.huygens.timbuctoo.rml.LoggingErrorHandler;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.PlainRdfCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;
import nl.knaw.huygens.timbuctoo.v5.rml.RdfDataSourceFactory;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import javax.ws.rs.GET;
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

@Path("/v5/{userId}/{dataSetId}/rml")
public class Rml {
  private final DataSetRepository dataSetRepository;
  private final TimbuctooRdfIdHelper rdfIdHelper;
  private final ErrorResponseHelper errorResponseHelper;
  private final JenaBasedReader rmlBuilder = new JenaBasedReader();

  public Rml(DataSetRepository dataSetRepository, TimbuctooRdfIdHelper rdfIdHelper,
             ErrorResponseHelper errorResponseHelper) {
    this.dataSetRepository = dataSetRepository;
    this.rdfIdHelper = rdfIdHelper;
    this.errorResponseHelper = errorResponseHelper;
  }

  @POST
  public Response upload(final String rdfData,
                         @PathParam("userId") final String ownerId,
                         @PathParam("dataSetId") final String dataSetId)
    throws DataStoreCreationException, LogStorageFailedException, ExecutionException, InterruptedException {
    final Optional<DataSet> dataSet = dataSetRepository.getDataSet(ownerId, dataSetId);
    if (dataSet.isPresent()) {
      ImportManager importManager = dataSet.get().getImportManager();
      RdfDataSourceFactory dataSourceFactory = dataSet.get().getDataSource();

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
      Future<?> future = importManager.generateLog(
        rdfIdHelper.dataSet(ownerId, dataSetId),
        rdfIdHelper.dataSet(ownerId, dataSetId),
        new PlainRdfCreator() {
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
                rdfIdHelper.dataSet(ownerId, dataSetId)
              );
            }
          }
        }
      );
      future.get();
      return Response.noContent().build();
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

}

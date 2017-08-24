package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.jsonldimport.Entity;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/v5/{user}/{dataset}/upload/jsonld")
public class JsonLdImport {

  private DataSetRepository dataSetRepository;

  public JsonLdImport(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;

  }

  @PUT
  public Response submitChanges(nl.knaw.huygens.timbuctoo.v5.jsonldimport.JsonLdImport jsonLdImport,
                                @PathParam("user") String userId,
                                @PathParam("dataset") String dataSetId)
    throws DataStoreCreationException, LogStorageFailedException {

    Optional<DataSet> dataSetOpt = dataSetRepository.getDataSet(userId, dataSetId);

    if (!dataSetOpt.isPresent()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    DataSet dataSet = dataSetOpt.get();

    QuadStore quadStore = dataSet.getQuadStore();
    ImportManager importManager = dataSet.getImportManager();


    for (Entity entity : jsonLdImport.getGenerates()) {
      //importManager.generateLog(URI.create(""), new GenerateRDFPatchFromJsonLdEntity(entity, "FIXME:", quadStore));
      // new GenerateRDFPatchFromJsonLdEntity(entity);
    }

    return Response.noContent().build();
  }
}

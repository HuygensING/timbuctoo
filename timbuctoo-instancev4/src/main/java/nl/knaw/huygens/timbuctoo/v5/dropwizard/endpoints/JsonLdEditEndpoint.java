package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.jsonldimport.Entity;
import nl.knaw.huygens.timbuctoo.v5.jsonldimport.GenerateRdfPatchFromJsonLdEntity;
import nl.knaw.huygens.timbuctoo.v5.jsonldimport.JsonLdImport;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Optional;

@Path("/v5/{user}/{dataset}/upload/jsonld")
public class JsonLdEditEndpoint {

  private DataSetRepository dataSetRepository;
  private ObjectMapper objectMapper;
  private TimbuctooRdfIdHelper rdfIdHelper;


  public JsonLdEditEndpoint(DataSetRepository dataSetRepository, ObjectMapper objectMapper,
                            TimbuctooRdfIdHelper rdfIdHelper) {
    this.dataSetRepository = dataSetRepository;
    this.objectMapper = objectMapper;

    this.rdfIdHelper = rdfIdHelper;
  }

  @PUT
  public Response submitChanges(String jsonLdImport,
                                @PathParam("user") String userId,
                                @PathParam("dataset") String dataSetId)
    throws DataStoreCreationException, LogStorageFailedException, IOException, JSONException, JsonLdError {

    if (!integrityCheck(jsonLdImport)) {
      return Response.status(400).entity("JSON-LD failed integrity check").build();
    }

    nl.knaw.huygens.timbuctoo.v5.jsonldimport.JsonLdImport parsed =
      objectMapper.readValue(jsonLdImport, nl.knaw.huygens.timbuctoo.v5.jsonldimport.JsonLdImport.class);

    Optional<DataSet> dataSetOpt = dataSetRepository.getDataSet(userId, dataSetId);

    if (!dataSetOpt.isPresent()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    DataSet dataSet = dataSetOpt.get();
    QuadStore quadStore = dataSet.getQuadStore();
    ImportManager importManager = dataSet.getImportManager();

    GenerateRdfPatchFromJsonLdEntity generateRdfPatchFromJsonLdEntity =
      new GenerateRdfPatchFromJsonLdEntity(parsed.getGenerates(), quadStore);

    if (lastRevisionCheck(parsed.getGenerates(), quadStore)) {
      importManager.generateLog(rdfIdHelper.dataSet(userId, dataSetId), rdfIdHelper.dataSet(userId, dataSetId),
        generateRdfPatchFromJsonLdEntity);
    } else {
      return Response.status(400).entity("Last revision check failed").build();
    }

    return Response.noContent().build();

  }


  private Boolean integrityCheck(String jsonLdString) throws JSONException, JsonLdError {
    JSONObject jsonld = new JSONObject(jsonLdString);

    if (!jsonld.has("prov:qualifiedAssociation")) {
      return false;
    }

    if (!jsonld.has("prov:used")) {
      return false;
    }

    HashMap context = new HashMap();

    JsonLdOptions options = new JsonLdOptions();

    JsonLdProcessor.compact(jsonld, context, options);

    return true;
  }

  Boolean lastRevisionCheck(Entity[] entities, QuadStore quadStore) {
    for (Entity entity : entities) {
      URI revision = entity.getWasRevisionOf().get("@id");

      Optional<CursorQuad> previous =
        quadStore.getQuads(revision.toString(), RdfConstants.TIM_LATEST_REVISION_OF,
          Direction.OUT, "").findFirst();

      if (!previous.isPresent()) {
        return false;
      }
    }

    return true;
  }
}


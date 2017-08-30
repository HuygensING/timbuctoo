package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jsonldjava.core.JsonLdError;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.jsonldimport.GenerateRdfPatchFromJsonLdEntity;
import org.json.JSONException;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;
import org.json.JSONObject;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
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

    this.rdfIdHelper = this.rdfIdHelper;
  }

  @PUT
  public Response submitChanges(String jsonLdImport,
                                @PathParam("user") String userId,
                                @PathParam("dataset") String dataSetId)
    throws DataStoreCreationException, LogStorageFailedException, IOException, JSONException, JsonLdError {

    if (!integrityCheck(jsonLdImport)) {
      throw new JSONException("JSON-LD failed integrity check");
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

    importManager.generateLog(rdfIdHelper.dataSet(userId, dataSetId), rdfIdHelper.dataSet(userId, dataSetId),
      generateRdfPatchFromJsonLdEntity);


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

    Object compact = JsonLdProcessor.compact(jsonld, context, options);

    return true;
  }
}


package nl.knaw.huygens.timbuctoo.server.rest;


import com.google.common.collect.Lists;
import nl.knaw.huygens.concordion.extensions.HttpRequest;
import org.concordion.integration.junit4.ConcordionRunner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;
import java.util.AbstractMap;
import java.util.List;

@RunWith(ConcordionRunner.class)
public class WwRelationV2_1EndpointFixture extends BaseDomainV2_1EndpointFixture {

  private String personPath;
  private String personId;

  public String getPersonPath() {
    return personPath;
  }

  public String getDocumentPath() {
    return documentPath;
  }

  private String documentId;
  private String documentPath;

  public String makeDocumentRecord() throws JSONException {
    List<AbstractMap.SimpleEntry<String, String>> headers = makeAuthHeaders();

    HttpRequest postRequest =
        new HttpRequest("POST", "/v2.1/domain/wwdocuments", headers, makeDocumentJson(), null, Lists.newArrayList());

    Response response = executeRequestUsingJaxRs(postRequest);
    documentPath = response.getHeaderString("Location").replaceAll("http://[^/]+/", "");;
    documentId = documentPath.replaceAll(".*\\/", "");
    retrievePid(documentPath);
    return documentId;
  }

  public String makePersonRecord() throws JSONException {
    List<AbstractMap.SimpleEntry<String, String>> headers = makeAuthHeaders();
    HttpRequest postRequest =
        new HttpRequest("POST", "/v2.1/domain/wwpersons", headers, makePersonJson(), null, Lists.newArrayList());

    Response response = executeRequestUsingJaxRs(postRequest);
    personPath = response.getHeaderString("Location").replaceAll("http://[^/]+/", "");;
    personId = personPath.replaceAll(".*\\/", "");
    retrievePid(personPath);
    return personId;
  }

  public void deleteEntities() {
    List<AbstractMap.SimpleEntry<String, String>> headers = makeAuthHeaders();
    HttpRequest deletePersonRequest = new HttpRequest("DELETE", personPath, headers, null, null, Lists.newArrayList());
    HttpRequest deleteDocumentRequest =
        new HttpRequest("DELETE", documentPath, headers, null, null, Lists.newArrayList());
    executeRequestUsingJaxRs(deletePersonRequest);
    executeRequestUsingJaxRs(deleteDocumentRequest);
  }

  private String makeDocumentJson() throws JSONException {
    JSONObject documentObject = new JSONObject();
    documentObject.put("@type", "wwdocument");
    documentObject.put("title", "A title");
    return documentObject.toString();
  }

  private String makePersonJson() throws JSONException {
    JSONObject personObject = new JSONObject();
    JSONArray types = new JSONArray("[\"AUTHOR\"]");
    JSONArray names = new JSONArray("[{\"components\": [{\"type\": \"FORENAME\", \"value\": \"name\"}]}]");
    personObject.put("@type", "wwperson");
    personObject.put("gender", "MALE");
    personObject.put("birthDate", "1589");
    personObject.put("deathDate", "1653");
    personObject.put("types", types);
    personObject.put("names", names);
    return personObject.toString();
  }


  private List<AbstractMap.SimpleEntry<String, String>> makeAuthHeaders() {
    List<AbstractMap.SimpleEntry<String, String>> headers = Lists.newArrayList();
    headers.add(new AbstractMap.SimpleEntry<>("Authorization",  getAuthenticationToken()));
    headers.add(new AbstractMap.SimpleEntry<>("Content-type",  "application/json"));
    headers.add(new AbstractMap.SimpleEntry<>("VRE_ID",  "WomenWriters"));
    return headers;
  }


}

package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.LoggableObject;
import nl.knaw.huygens.oaipmh.MyOAISet;
import nl.knaw.huygens.oaipmh.OAIRecord;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class OaiPmhRestClient extends LoggableObject {
  private final Client client;
  private final WebResource recordsTarget;
  private final WebResource setsTarget;

  public OaiPmhRestClient(String baseURL) {
    client = Client.create();
    WebResource oaipmh = client.resource(baseURL);
    recordsTarget = oaipmh.path("records");
    setsTarget = oaipmh.path("sets");
  };

  public List<OAIRecord> getRecords() {
    return getRecords(0);
  }

  public List<OAIRecord> getRecords(int start) {
    List<OAIRecord> oAIRecords = recordsTarget.queryParam("start", "" + start).accept(MediaType.APPLICATION_JSON).get(new GenericType<List<OAIRecord>>() {});
    return oAIRecords;
  }

  public void postRecord(OAIRecord oAIRecord) {
    ClientResponse response = recordsTarget.type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, oAIRecord);
    LOG.info("Response: {}", response);
    if (response.getStatus() != Status.CREATED.getStatusCode()) {
      LOG.error(response.getEntity(String.class));
    }

  }

  public OAIRecord getRecord(String identifier) {
    WebResource path = recordsTarget.path(urlEncode(identifier));
    LOG.info("{}", path.getURI());
    OAIRecord oAIRecord = path.accept(MediaType.APPLICATION_JSON).get(OAIRecord.class);
    return oAIRecord;
  }

  private String urlEncode(String identifier) {
    String encodedIdentifier = identifier;
    try {
      encodedIdentifier = URLEncoder.encode(identifier, "UTF-8");
      //      LOG.info("encodedIdentifier={}", encodedIdentifier);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return encodedIdentifier;
  }

  public void deleteRecord(String identifier) {
    ClientResponse response = recordsTarget.path(urlEncode(identifier)).delete(ClientResponse.class);
    LOG.info("Response: {}", response);
  }

  public List<MyOAISet> getSets() {
    List<MyOAISet> myOAISets = setsTarget.accept(MediaType.APPLICATION_JSON).get(new GenericType<List<MyOAISet>>() {});
    return myOAISets;
  }

  public void postSet(MyOAISet myOAISet) {
    ClientResponse response = setsTarget.type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, myOAISet);
    LOG.info("Response: {}", response);
    if (response.getStatus() != Status.CREATED.getStatusCode()) {
      LOG.error(response.getEntity(String.class));
    }
  }

  public MyOAISet getSet(String setSpec) {
    Builder request = setsTarget.path(setSpec).accept(MediaType.APPLICATION_JSON);
    ClientResponse r = request.get(ClientResponse.class);
    if (r.getStatus() == Status.OK.getStatusCode()) {
      return request.get(MyOAISet.class);
    }
    return null;
  }

  public void deleteSet(String setSpec) {
    ClientResponse response = setsTarget.path(setSpec).delete(ClientResponse.class);
    LOG.info("Response: {}", response);
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      LOG.info("closing client");
      client.destroy();
    } finally {
      super.finalize();
    }
  }

}

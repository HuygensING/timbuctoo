package nl.knaw.huygens.repository.importer.database;

import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.repository.model.Document;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RemoteDataPoster implements DataPoster {

  private static final String SERVICE_PATH = "repository/resources";
  private static final String URL = "http://demo17.huygens.knaw.nl";
  /*
   * replace before running this file.
   * An access token can be created by the next url: http://demo17.huygens.knaw.nl/apis-example-client-app/test
   */
  private static final String ACCESS_TOKEN = "1586e952-bd2c-4b6f-a190-f42975d8c5b5";

  private Client client;

  public RemoteDataPoster() {
    client = Client.create();
  }

  @Override
  public <T extends Document> T getDocument(Class<T> type, String id) {
    return null;
  }

  @Override
  public <T extends Document> String addDocument(Class<T> type, T document, boolean isComplete) {
    String path = type.getSimpleName().toLowerCase();
    WebResource resource = client.resource(URL).path(SERVICE_PATH).path(path);
    ClientResponse response = resource.type(MediaType.APPLICATION_JSON).header("Authorization", "bearer " + ACCESS_TOKEN).post(ClientResponse.class, document);
    if (!ClientResponse.Status.CREATED.equals(response.getClientResponseStatus())) {
      System.out.println("uri: " + resource.getURI());
      System.out.println("response: " + response.getClientResponseStatus());
      System.out.println("doc: " + document.getDisplayName());
      return null;
    }
    String location = response.getHeaders().getFirst("Location");
    int pos = location.lastIndexOf("/");
    return location.substring(pos + 1);
  }

  @Override
  public <T extends Document> T modDocument(Class<T> type, T document) {
    return null;
  }

}

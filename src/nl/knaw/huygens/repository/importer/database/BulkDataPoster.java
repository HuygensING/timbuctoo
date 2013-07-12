package nl.knaw.huygens.repository.importer.database;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.dwcbia.DWCPlace;
import nl.knaw.huygens.repository.model.dwcbia.DWCScientist;
import nl.knaw.huygens.repository.model.raa.RAACivilServant;
import nl.knaw.huygens.repository.util.Progress;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class BulkDataPoster {
  private static final String SERVICE_PATH = "resources";
  private static final String URL = "http://repository.huygens.knaw.nl";
  /*
   * replace before running this file.
   * An access token can be created by the next url: http://demo17.huygens.knaw.nl/apis-example-client-app/test
   */
  private static final String ACCESS_TOKEN = "1586e952-bd2c-4b6f-a190-f42975d8c5b5";

  //private static final String URL = "http://localhost:8080";

  /**
   * @param args
   * @throws IOException 
   * @throws JsonProcessingException 
   * @throws ClassNotFoundException 
   */
  public static void main(String[] args) throws JsonProcessingException, IOException, ClassNotFoundException {
    long start = System.currentTimeMillis();
    sendData(DWCScientist.class, DWCScientist[].class, "dwcscientist/all");
    sendData(DWCPlace.class, DWCPlace[].class, "dwcplace/all");
    sendData(RAACivilServant.class, RAACivilServant[].class, "raacivilservant/all");
    long end = System.currentTimeMillis();

    System.out.printf("post took %d seconds", (end - start) / 1000);

  }

  protected static <T extends Document> void sendData(Class<T> type, Class<T[]> typeArray, String path) throws IOException, JsonParseException, JsonMappingException {
    System.out.printf("%n=== Post documents of type '%s'%n", type.getSimpleName());
    T[] docs = readFile(type, typeArray, new File("testdata" + File.separator + type.getSimpleName() + ".json"));
    postData(type, docs, path);
  }

  protected static <T extends Document> T[] readFile(Class<T> type, Class<T[]> typeArray, File fileToRead) throws IOException, JsonParseException, JsonMappingException {
    ObjectMapper mapper = new ObjectMapper();

    mapper.enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE, As.PROPERTY);

    T[] documents = mapper.readValue(fileToRead, typeArray);
    return documents;
  }

  protected static <T extends Document> void postData(Class<T> type, T[] documents, String path) {
    Client client = Client.create();
    WebResource resource = client.resource(URL).path(SERVICE_PATH).path(path);
    Progress progress = new Progress();
    for (T document : documents) {
      progress.step();
      ClientResponse response = resource.type(MediaType.APPLICATION_JSON).header("Authorization", "bearer " + ACCESS_TOKEN).post(ClientResponse.class, document);
      if (!ClientResponse.Status.CREATED.equals(response.getClientResponseStatus())) {
        System.out.println("uri: " + resource.getURI());
        System.out.println("response: " + response.getClientResponseStatus());
        System.out.println("doc: " + document.getDisplayName());
        break;
      }
    }
    progress.done();
  }
}

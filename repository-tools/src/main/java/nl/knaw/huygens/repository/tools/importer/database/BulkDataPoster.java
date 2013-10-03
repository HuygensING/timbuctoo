package nl.knaw.huygens.repository.tools.importer.database;

import java.io.File;

import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Entity;
import nl.knaw.huygens.repository.model.dwcbia.DWCPlace;
import nl.knaw.huygens.repository.model.dwcbia.DWCScientist;
import nl.knaw.huygens.repository.model.raa.RAACivilServant;
import nl.knaw.huygens.repository.tools.util.Progress;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
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

  public static void main(String[] args) throws Exception {
    long start = System.currentTimeMillis();

    Configuration config = new Configuration("config.xml");
    DocTypeRegistry registry = new DocTypeRegistry(config.getSetting("model-packages"));

    sendData(registry, DWCScientist.class, DWCScientist[].class);
    sendData(registry, DWCPlace.class, DWCPlace[].class);
    sendData(registry, RAACivilServant.class, RAACivilServant[].class);

    long end = System.currentTimeMillis();
    System.out.printf("post took %d seconds", (end - start) / 1000);
  }

  protected static <T extends Entity> void sendData(DocTypeRegistry registry, Class<T> type, Class<T[]> typeArray) throws Exception {
    System.out.printf("%n=== Post documents of type '%s'%n", type.getSimpleName());
    T[] docs = readFile(type, typeArray, new File("testdata" + File.separator + type.getSimpleName() + ".json"));
    postData(type, docs, registry.getXNameForType(type));
  }

  protected static <T extends Entity> T[] readFile(Class<T> type, Class<T[]> typeArray, File fileToRead) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    mapper.enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE, As.PROPERTY);
    return mapper.readValue(fileToRead, typeArray);
  }

  protected static <T extends Entity> void postData(Class<T> type, T[] entities, String collection) {
    Client client = Client.create();
    WebResource resource = client.resource(URL).path(SERVICE_PATH).path(collection).path("all");
    Progress progress = new Progress();
    for (T entity : entities) {
      progress.step();
      ClientResponse response = resource.type(MediaType.APPLICATION_JSON).header("Authorization", "bearer " + ACCESS_TOKEN).post(ClientResponse.class, entity);
      if (!ClientResponse.Status.CREATED.equals(response.getClientResponseStatus())) {
        System.out.println("uri: " + resource.getURI());
        System.out.println("response: " + response.getClientResponseStatus());
        System.out.println("doc: " + entity.getDisplayName());
        break;
      }
    }
    progress.done();
  }

}

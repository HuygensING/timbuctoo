package nl.knaw.huygens.timbuctoo.rml;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.knaw.huygens.timbuctoo.server.TimbuctooConfiguration;
import nl.knaw.huygens.timbuctoo.server.TimbuctooV4;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;

import static com.google.common.io.Resources.asCharSource;
import static com.google.common.io.Resources.getResource;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.JsonContractMatcher.matchesContract;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RmlIntegrationTest {

  public static final String DATABASE_PATH = resourceFilePath("integrationtest/database");
  public static final String AUTH_PATH = resourceFilePath("integrationtest/authorizations");

  public static final DropwizardAppRule<TimbuctooConfiguration> APP = new DropwizardAppRule<>(
    TimbuctooV4.class,
    resourceFilePath("integrationtest/config.yaml"),
    config("databaseConfiguration.databasePath", DATABASE_PATH),
    config("securityConfiguration.localfile.authorizationsPath", AUTH_PATH),
    config("securityConfiguration.localfile.usersFilePath", resourceFilePath("integrationtest/users.json")),
    config("securityConfiguration.localfile.loginsFilePath", resourceFilePath("integrationtest/logins.json"))
  );

  @ClassRule
  public static final RuleChain chain = RuleChain
    .outerRule(new CleaningRule())
      .around(APP);

  private static final String AUTH = "FAKE_AUTH_TOKEN";
  private static Client client;

  @BeforeClass
  public static void beforeClass() throws IOException {
    // client = new JerseyClientBuilder(APP.getEnvironment()).build("test client");
    ClientConfig configuration = new ClientConfig();
    client = ClientBuilder.newClient(configuration);
  }

  @Test
  public void succeedingSmallUpload() throws Exception {
    String vreName = "demo-upload";
    String prefixedVreName = "DUMMY_" + vreName;
    Response uploadResponse = rawUpload(
      vreName,
      new File(getResource(RmlIntegrationTest.class, "demo-upload.xlsx").toURI())
    );

    assertThat(uploadResponse.getStatus(), is(200));
    uploadResponse.readEntity(String.class); //needed for blocking side-effect

    Response rmlResponse = map(
      prefixedVreName,
      asCharSource(getResource(RmlIntegrationTest.class, "demo-upload-rml.json"), Charset.forName("UTF8")).read()
        .replace("{VRE_NAME}", prefixedVreName)
    );

    String output = rmlResponse.readEntity(String.class); //also needed for blocking side-effect
    String rmlResult = output.substring(output.lastIndexOf('\n') + 1);
    assertThat(rmlResult, is("success"));

    JsonNode metadata = call("/v2.1/metadata/" + prefixedVreName + "?withCollectionInfo=true").get(JsonNode.class);
    String personsType = prefixedVreName + "Persons";
    String placesType = prefixedVreName + "Places";
    String unknownType = prefixedVreName + "unknown";
    String personsCollection = personsType + "s";
    String placesCollection = placesType + "s";

    assertThat(metadata, matchesContract(jsnO(
      personsCollection, jsnO(),
      placesCollection, jsnO()
    )));

    JsonNode persons = call("/v2.1/domain/" + personsCollection + "/autocomplete?query=helmhol*").get(JsonNode.class);
    JsonNode personData = call(persons.get(0).get("key").asText()).get(JsonNode.class);
    assertThat(personData, matchesContract(jsnO(
      "@displayName", jsn("H.L.F. Helmholtz"),
      "@relations", jsnO(
        "hasBirthPlace", jsnA(
            jsnO(
            "displayName", jsn("Potsdam")
          )
        )
      ),
      "@variationRefs", jsnO(
        "custom-matcher", jsn("/*ALL_MATCH_ONE_OF*/"),
        "keyProp", jsn("type"),
        "possibilities", jsnO(
          personsType, jsnO(
            "type", jsn(personsType)
          ),
          "person", jsnO(
            "type", jsn("person")
          ),
          "concept", jsnO(
            "type", jsn("concept")
          ),
          unknownType, jsnO(
            "type", jsn(unknownType)
          )
        )
      )
    )));
  }

  @Test
  public void failingBiaUpload() throws Exception {
  }

  @Test
  public void succeedingBiaUpload() throws Exception {
  }

  private Response rawUpload(String vrename, File resource) throws ParseException {
    MultiPart formdata = new MultiPart();

    formdata.bodyPart(new FormDataBodyPart("vreName", vrename));

    formdata.bodyPart(new FormDataBodyPart(
      new FormDataContentDisposition(
        "form-data; name=\"file\"; filename=\"" + resource.getName().replace("\"", "") + "\""
      ),
      resource,
      new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ));

    Response result = call("/v2.1/bulk-upload")
      .post(Entity.entity(formdata, "multipart/form-data; boundary=Boundary_1_498293219_1483974344746"));
    return result;
  }

  private Response map(String vrename, String rmlMapping) {
    Response response = call("/v2.1/bulk-upload/" + vrename + "/rml/execute")
      .post(Entity.entity(rmlMapping, new MediaType("application", "ld+json")));
    return response;
  }

  private Invocation.Builder call(String path) {
    String server;
    if (path.startsWith("http://") || path.startsWith("https://")) {
      server = path;
    } else {
      server = String.format("http://localhost:%d" + path, APP.getLocalPort());
    }
    return client
      .target(server)
      .register(MultiPartFeature.class)
      // .register(new LoggingFilter(Logger.getLogger(LoggingFilter.class.getName()), true))
      .request()
      .header("Authorization", AUTH);
  }

  private static class CleaningRule extends ExternalResource {
    @Override
    protected void before() throws Throwable {
      super.before();
      FileUtils.cleanDirectory(new File(DATABASE_PATH));
      FileUtils.cleanDirectory(new File(AUTH_PATH));
    }
  }
}

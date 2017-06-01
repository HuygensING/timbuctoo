package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.knaw.huygens.timbuctoo.server.TimbuctooConfiguration;
import nl.knaw.huygens.timbuctoo.server.TimbuctooV4;
import nl.knaw.huygens.timbuctoo.util.EvilEnvironmentVariableHacker;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;

import static com.google.common.io.Resources.getResource;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class TabularUploadTest {

  private static final String TESTRUNSTATE = resourceFilePath("testrunstate");

  static {
    EvilEnvironmentVariableHacker.setEnv(ImmutableMap.of(
      "timbuctoo_dataPath", TESTRUNSTATE,
      "timbuctoo_port", "0",
      "timbuctoo_adminPort", "0"
    ));
  }

  @ClassRule
  public static final DropwizardAppRule<TimbuctooConfiguration> APP = new DropwizardAppRule<>(
    TimbuctooV4.class,
    "example_config.yaml"
  );

  @AfterClass
  public static void cleanUp() throws IOException {
    FileUtils.deleteDirectory(new File(TESTRUNSTATE, "datasets"));
    FileUtils.deleteDirectory(new File(TESTRUNSTATE, "files"));
  }

  @Test
  public void uploadReturnsAUriOfForTheUploadProgress() {
    Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
    WebTarget target =
      client.target(String.format("http://localhost:%d/v5/DUMMY/dataset/upload/table", APP.getLocalPort()));

    FormDataMultiPart multiPart = new FormDataMultiPart();
    multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart(
      "file",
      new File(getResource(TabularUpload.class, "2017_04_17_BIA_Clusius.xlsx").getFile())
    );
    multiPart.bodyPart(fileDataBodyPart);
    multiPart.field("type", "xlsx");


    Response response = target.request()
                              .header(HttpHeaders.AUTHORIZATION, "fake")
                              .post(Entity.entity(multiPart, multiPart.getMediaType()));

    assertThat(response.getStatus(), is(201));
    assertThat(response.getHeaderString(HttpHeaders.LOCATION), is(notNullValue()));
  }
}

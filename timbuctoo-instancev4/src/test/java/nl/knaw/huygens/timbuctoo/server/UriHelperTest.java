package nl.knaw.huygens.timbuctoo.server;

import org.junit.Test;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UriHelperTest {
  @Test
  public void fromResourceUriPrefixesTheResourceUriWithTheBaseUri() {
    TimbuctooConfiguration configuration = mock(TimbuctooConfiguration.class);
    String baseUri = "http://localhost:8080/path1";
    when(configuration.getBaseUri()).thenReturn(baseUri);
    UriHelper instance = new UriHelper(configuration);
    URI resourceUri = UriBuilder.fromUri("/path2/path3").queryParam("q1", "v1").queryParam("q2", "v2").build();

    String uri = instance.fromResourceUri(resourceUri).toString();

    assertThat(uri, startsWith(baseUri));
    assertThat(uri, endsWith("/path2/path3?q1=v1&q2=v2"));

  }

  @Path("/test/{var}")
  private static class TestResource {

  }

}

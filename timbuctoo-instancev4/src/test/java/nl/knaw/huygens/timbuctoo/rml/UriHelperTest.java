package nl.knaw.huygens.timbuctoo.rml;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.server.TimbuctooConfiguration;
import org.junit.Test;

import javax.ws.rs.Path;
import java.net.URI;
import java.util.TreeMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UriHelperTest {
  @Test
  public void makeUriUsesTheMapTheFillInTheVariables() {
    TimbuctooConfiguration configuration = mock(TimbuctooConfiguration.class);
    when(configuration.getBaseUri()).thenReturn("http://localhost:8080");
    UriHelper instance = new UriHelper(configuration);

    URI uri = instance.makeUri(TestResource.class, ImmutableMap.of("var", "value"));

    assertThat(uri.getPath(), is("/test/value"));
  }

  @Test
  public void makeUriUsesAddsTheBaseUriBeforeTheResourceUri() {
    TimbuctooConfiguration configuration = mock(TimbuctooConfiguration.class);
    String baseUri = "http://localhost:8080/path";
    when(configuration.getBaseUri()).thenReturn(baseUri);
    UriHelper instance = new UriHelper(configuration);

    URI uri = instance.makeUri(TestResource.class, ImmutableMap.of("var", "value"));

    assertThat(uri.toString(), startsWith(baseUri));
  }

  @Test
  public void makeUriUsesTheQueryMapToAddQueryParameters() {
    TimbuctooConfiguration configuration = mock(TimbuctooConfiguration.class);
    when(configuration.getBaseUri()).thenReturn("http://localhost:8080");
    UriHelper instance = new UriHelper(configuration);
    // use tree map for this test to make sure the query params are ordered to make the query easier to test.
    TreeMap<String, String> query = Maps.newTreeMap();

    URI uri = instance.makeUri(
      TestResource.class,
      ImmutableMap.of(
        "var", "value"
      ),
      ImmutableMap.of(
        "q1", "v1",
        "q2", "v2"
      ));

    assertThat(uri.getQuery(), is("q1=v1&q2=v2"));
  }

  @Path("/test/{var}")
  private static class TestResource {

  }

}

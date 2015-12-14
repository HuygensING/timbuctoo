package nl.knaw.huygens.concordion.extensions;

import com.google.common.collect.Maps;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class HttpResult {
  private final int status;
  private final Map<String, String> headers = Maps.newHashMap();
  private final String body;

  public HttpResult(Response callResult) {
    this.status = callResult.getStatus();
    callResult.getHeaders().forEach(new BiConsumer<String, List<Object>>() {
      @Override
      public void accept(String key, List<Object> values) {
        HttpResult.this.headers.put(key.toLowerCase(), "" + values.get(0));
      }
    });
    this.body = callResult.readEntity(String.class);
  }

  public int getStatus() {
    return status;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public String getBody() {
    return body;
  }
}

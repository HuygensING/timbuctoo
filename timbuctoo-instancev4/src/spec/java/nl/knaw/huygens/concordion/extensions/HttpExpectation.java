package nl.knaw.huygens.concordion.extensions;

import com.google.common.base.Strings;

import java.util.AbstractMap;
import java.util.List;

public class HttpExpectation {
  public final int status;
  public final String body;
  public final List<AbstractMap.SimpleEntry<String, String>> headers;

  public HttpExpectation(int status, String body, List<AbstractMap.SimpleEntry<String, String>> headers) {
    this.status = status;
    this.body = body;
    this.headers = headers;
  }

  public boolean hasBody() {
    return !Strings.isNullOrEmpty(body);
  }
}

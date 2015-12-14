package nl.knaw.huygens.concordion.extensions;

import javax.ws.rs.core.Response;

public interface HttpCaller {
  Response call(HttpRequest value);
}

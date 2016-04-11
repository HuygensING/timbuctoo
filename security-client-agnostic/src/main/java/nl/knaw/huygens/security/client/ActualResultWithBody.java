package nl.knaw.huygens.security.client;

import com.google.common.collect.LinkedListMultimap;

public class ActualResultWithBody<T> extends ActualResult {
  private final T body;

  public ActualResultWithBody(int status, String statusInfo, LinkedListMultimap<String, String> headers, T body) {
    super(status, statusInfo, headers);
    this.body = body;
  }

  public T getBody() {
    return body;
  }
}

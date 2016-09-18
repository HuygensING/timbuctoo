package nl.knaw.huygens.timbuctoo.remote.rs.discover;


import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class Result<T> implements Consumer<T> {

  private final URI uri;
  private int statusCode;
  private T content;
  private Throwable error;

  private Result parent;
  private Map<URI, Result> children = new HashMap<>();

  public Result(URI uri) {
    this.uri = uri;
  }

  public URI getUri() {
    return uri;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public Optional<T> getContent() {
    return Optional.ofNullable(content);
  }

  public Optional<Throwable> getError() {
    return Optional.ofNullable(error);
  }

  public void setError(Throwable error) {
    this.error = error;
  }

  @Override
  public void accept(T content) {
    this.content = content;
  }

  void setParent(Result parent) {
    this.parent = parent;
  }

  public Result getParent() {
    return parent;
  }

  void addChild(Result child) {
    children.put(child.getUri(), child);
  }

  public <R> Result<R> shallowCopy(Result<R> copy) {
    copy.statusCode = this.statusCode;
    copy.error = this.error;
    return copy;
  }

}

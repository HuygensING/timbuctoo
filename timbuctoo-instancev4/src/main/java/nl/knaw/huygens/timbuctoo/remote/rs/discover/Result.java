package nl.knaw.huygens.timbuctoo.remote.rs.discover;


import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class Result<T> implements Consumer<T> {

  private final URI uri;
  private int statusCode;
  private T content;
  private Throwable error;

  private Map<URI, Result<?>> parents = new HashMap<>();
  private Map<URI, Result<?>> children = new HashMap<>();

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

  public Map<URI, Result> getParents() {
    return Collections.unmodifiableMap(parents);
  }

  public Map<URI, Result> getChildren() {
    return Collections.unmodifiableMap(children);
  }

  public <R> Result<R> shallowCopyTo(Result<R> copy) {
    copy.statusCode = this.statusCode;
    copy.error = this.error;
    return copy;
  }

  void addParent(Result<?> parent) {
    if (!parents.containsKey(parent.getUri())) {
      parents.put(parent.getUri(), parent);
      parent.addChild(this);
    }
  }

  void addChild(Result<?> child) {
    if (!children.containsKey(child.getUri())) {
      children.put(child.getUri(), child);
      child.addParent(this);
    }
  }



}

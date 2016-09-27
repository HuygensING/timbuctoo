package nl.knaw.huygens.timbuctoo.remote.rs.discover;


import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class Result<T> implements Consumer<T> {

  private URI uri;
  private int statusCode;
  private T content;
  private List<Throwable> errors = new ArrayList<>();

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

  public List<Throwable> getErrors() {
    return Collections.unmodifiableList(errors);
  }

  public void addError(Throwable error) {
    errors.add(error);
  }

  @Override
  public void accept(T content) {
    this.content = content;
  }

  public Map<URI, Result<?>> getParents() {
    return Collections.unmodifiableMap(parents);
  }

  public Map<URI, Result<?>> getChildren() {
    return Collections.unmodifiableMap(children);
  }

  public <R> Result<R> map(Function<T, R> func) {
    Result<R> copy = new Result<R>(uri);
    copy.statusCode = statusCode;

    List<Throwable> copyErrors = new ArrayList<>();
    copyErrors.addAll(errors);
    copy.errors = copyErrors;

    Map<URI, Result<?>> copyParents = new HashMap<>();
    copyParents.putAll(parents);
    copy.parents = copyParents;

    Map<URI, Result<?>> copyChildren = new HashMap<>();
    copyChildren.putAll(children);
    copy.children = copyChildren;

    if (content != null) {
      copy.accept(func.apply(content));
    }

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

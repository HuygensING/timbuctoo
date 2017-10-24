package nl.knaw.huygens.timbuctoo.remote.rs.discover;


import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;

public class Result<T> implements Consumer<T> {

  private URI uri;
  private int ordinal;
  private int statusCode;
  private T content;
  private List<Throwable> errors = new ArrayList<>();
  private Map<URI, Result<?>> parents = new TreeMap<>();
  private Map<URI, Result<?>> children = new TreeMap<>();
  private Result<Description> descriptionResult;
  private Set<String> invalidUris = new TreeSet<>();

  public Result(URI uri) {
    this.uri = uri;
  }

  public URI getUri() {
    return uri;
  }

  public int getOrdinal() {
    return ordinal;
  }

  public void setOrdinal(int ordinal) {
    this.ordinal = ordinal;
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

  public Set<String> getInvalidUris() {
    return invalidUris;
  }

  public void addInvalidUri(String invalidUri) {
    invalidUris.add(invalidUri);
  }

  @Override
  public void accept(T content) {
    this.content = content;
  }

  public Map<URI, Result<?>> getParents() {
    return parents;
  }

  public Map<URI, Result<?>> getChildren() {
    return children;
  }

  public Optional<Result<Description>> getDescriptionResult() {
    return Optional.ofNullable(descriptionResult);
  }

  public <R> Result<R> map(Function<T, R> func) {
    Result<R> copy = new Result<R>(uri);

    copy.statusCode = statusCode;
    copy.ordinal = ordinal;
    copy.errors.addAll(errors);
    copy.invalidUris.addAll(invalidUris);
    copy.parents.putAll(parents);
    copy.children.putAll(children);
    copy.descriptionResult = descriptionResult;

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

  void setDescriptionResult(Result<Description> descriptionResult) {
    if (descriptionResult.getContent().isPresent()) {
      descriptionResult.getContent().get().setDescribes(uri);
    }
    this.descriptionResult = descriptionResult;
  }

}

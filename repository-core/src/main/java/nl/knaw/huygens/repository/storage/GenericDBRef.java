package nl.knaw.huygens.repository.storage;

import nl.knaw.huygens.repository.model.Entity;

import com.fasterxml.jackson.annotation.JsonView;

public class GenericDBRef<T extends Entity> {

  private T item = null;
  public String type;
  public String id;

  public void setItem(T obj) {
    item = obj;
  }

  @JsonView(JsonViews.WebView.class)
  public T getItem() {
    return item;
  }

}

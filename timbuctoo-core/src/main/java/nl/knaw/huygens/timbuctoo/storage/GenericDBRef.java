package nl.knaw.huygens.timbuctoo.storage;

import nl.knaw.huygens.timbuctoo.model.Entity;

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

package nl.knaw.huygens.repository.storage.mongo;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.storage.GenericDBRef;
import nl.knaw.huygens.repository.model.storage.JsonViews;

import com.fasterxml.jackson.annotation.JsonView;


public class MongoDBRef<T extends Document> implements GenericDBRef<T> {
  private T item = null;
  private String collectionName;
  private String id;

  @Override
  public void setItem(T obj) {
    item = obj;
  }

  @Override
  @JsonView(JsonViews.WebView.class)
  public T getItem() {
    return item;
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getType() {
    return collectionName;
  }

  public void setType(String type) {
    collectionName = type;
  }
}

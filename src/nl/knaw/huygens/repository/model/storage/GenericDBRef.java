package nl.knaw.huygens.repository.model.storage;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.mongo.MongoDBRef;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = MongoDBRef.class)
public interface GenericDBRef<T extends Document> {
  public T getItem();

  public void setItem(T obj);

  public String getId();

  public String getType();
}

package nl.knaw.huygens.repository.index;

import com.fasterxml.jackson.annotation.JsonIgnore;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.Storage;

public class ExplicitlyAnnotatedModel extends Document {

  @IndexAnnotation
  public String getString() {
    return "";
  }

  @Override
  @IndexAnnotation(fieldName = "id")
  public String getId() {
    return "";
  }

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDescription() {
    // TODO Auto-generated method stub
    return "";
  }

  @Override
  public void fetchAll(Storage storage) {
    // TODO Auto-generated method stub

  }
}

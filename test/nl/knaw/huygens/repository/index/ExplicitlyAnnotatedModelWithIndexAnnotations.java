package nl.knaw.huygens.repository.index;

import com.fasterxml.jackson.annotation.JsonIgnore;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.indexdata.IndexAnnotations;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.Storage;

public class ExplicitlyAnnotatedModelWithIndexAnnotations extends Document {

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
    return null;
  }

  @Override
  public void fetchAll(Storage storage) {
    // TODO Auto-generated method stub

  }

  @IndexAnnotations(value = { @IndexAnnotation(fieldName = "test"), @IndexAnnotation(fieldName = "test2") })
  public String getString() {
    return "";
  }

}

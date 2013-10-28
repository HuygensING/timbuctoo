package nl.knaw.huygens.timbuctoo.model;

import java.util.List;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

public class ExplicitlyAnnotatedModelWithIndexAnnotations extends Entity {

  protected List<Reference> variations = Lists.newArrayList();
  protected String currentVariation;

  @Override
  @IndexAnnotation(fieldName = "id")
  public String getId() {
    return "";
  }

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    return null;
  }

  @IndexAnnotations(value = { @IndexAnnotation(fieldName = "test"), @IndexAnnotation(fieldName = "test2") })
  public String getString() {
    return "";
  }

}

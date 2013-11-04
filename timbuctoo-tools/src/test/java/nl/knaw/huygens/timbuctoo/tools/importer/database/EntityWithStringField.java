package nl.knaw.huygens.timbuctoo.tools.importer.database;

import java.util.List;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Reference;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

public class EntityWithStringField extends Entity {

  private String test;
  protected List<Reference> variations = Lists.newArrayList();
  protected String currentVariation;

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    return null;
  }

  public void setTest(String test) {
    this.test = test;
  }

  public String getTest() {
    return this.test;
  }

}

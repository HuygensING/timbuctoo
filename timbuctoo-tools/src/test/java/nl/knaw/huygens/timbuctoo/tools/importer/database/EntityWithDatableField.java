package nl.knaw.huygens.timbuctoo.tools.importer.database;

import java.util.List;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.util.Datable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

public class EntityWithDatableField extends Entity {

  private Datable datable;
  protected List<Reference> variations = Lists.newArrayList();

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    return null;
  }

  public void setDatable(Datable datable) {
    this.datable = datable;
  }

  public Datable getDatable() {
    return datable;
  }

}

package nl.knaw.huygens.timbuctoo.tools.importer.database;

import java.util.List;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.util.Datable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

public class DocumentExtensionWithDatableField extends Entity {

  public DocumentExtensionWithDatableField() {}

  private Datable datable;
  protected List<Reference> variations = Lists.newArrayList();
  protected String currentVariation;

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setDatable(Datable datable) {
    this.datable = datable;
  }

  public Datable getDatable() {
    return this.datable;
  }

}
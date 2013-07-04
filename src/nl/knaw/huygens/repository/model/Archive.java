package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.annotations.IDPrefix;

@IDPrefix("ARC")
@DocumentTypeName("archive")
public class Archive extends DomainDocument {

  @Override
  public String getDisplayName() {
    return String.format("Archive - %s", getId());
  }

}

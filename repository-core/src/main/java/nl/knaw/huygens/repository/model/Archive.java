package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.annotations.IDPrefix;

@IDPrefix("ARCH")
public class Archive extends DomainDocument {

  @Override
  public String getDisplayName() {
    return String.format("Archive - %s", getId());
  }

}

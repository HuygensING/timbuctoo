package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.annotations.IDPrefix;

@IDPrefix("LEGL")
public class Legislation extends DomainDocument {

  @Override
  public String getDisplayName() {
    return String.format("Legislation - %s", getId());
  }

}
